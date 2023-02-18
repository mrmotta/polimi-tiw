package it.polimi.tiw.controllers.frontend;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.excetions.EmptyCredentialsException;
import it.polimi.tiw.excetions.MissingCredentialsException;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;

public class Login extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 8427912208761525837L;
    private Connection connection;

    public Login() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher view = request.getRequestDispatcher(this.getInitParameter("html"));
        view.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.getSession().invalidate();

        try {
            if (connection == null || connection.isClosed())
                connection = getConnection(getServletContext());
        } catch (SQLException e) {
            connection = getConnection(getServletContext());
        }

        if (connection == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("There was an error while connecting to the DataBase.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        String username;
        String password;
        User user;
        UserDAO userDao;

        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append(System.lineSeparator());
            }
            String data = buffer.toString();
            JsonObject ret = new JsonObject();
            ret = new Gson().fromJson(data, JsonObject.class);

            username = StringEscapeUtils.escapeJava(ret.get("username").getAsString());
            password = StringEscapeUtils.escapeJava(ret.get("password").getAsString());
            if (username == null || password == null)
                throw new EmptyCredentialsException();
            if (username.isEmpty() || password.isEmpty())
                throw new MissingCredentialsException();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong credentials, please, try again.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        userDao = new UserDAO(connection);
        try {
            user = userDao.checkCredentials(username, password);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("There was an error while checking credentials.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("Try again, wrong credentials.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");

        } else {
            request.getSession().setAttribute("user", user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(username);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
