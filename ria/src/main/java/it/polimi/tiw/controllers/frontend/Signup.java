package it.polimi.tiw.controllers.frontend;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import java.util.regex.Pattern;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;

public class Signup extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1478983507509357025L;
    private Connection connection;

    public Signup() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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
        String email;
        String password;
        String repeat;
        boolean check;
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
            email = StringEscapeUtils.escapeJava(ret.get("email").getAsString());
            password = StringEscapeUtils.escapeJava(ret.get("password").getAsString());
            repeat = StringEscapeUtils.escapeJava(ret.get("repeat_password").getAsString());
            if (username == null || password == null || repeat == null)
                throw new EmptyCredentialsException();
            if (username.isEmpty() || password.isEmpty() || repeat.isEmpty())
                throw new MissingCredentialsException();
            if (!Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$", Pattern.CASE_INSENSITIVE).matcher(email).find())
                throw new MissingCredentialsException();
            email = email.toLowerCase();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong credentials, try again");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        userDao = new UserDAO(connection);
        try {
            check = userDao.exists(username);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("There was an error while checking the username.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        if (check) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Try again, username already in use.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        } else if (!password.equals(repeat)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Try again, passwords don't coincide.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        } else {
            try {
                request.getSession().setAttribute("user", userDao.createUser(username, email, password));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("There was an error while creating the account.");
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
                return;
            }
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
