package it.polimi.tiw.controllers.frontend;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.excetions.EmptyCredentialsException;
import it.polimi.tiw.excetions.MissingCredentialsException;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;
import static it.polimi.tiw.utilities.Utilities.getTemplateEngine;

public class Login extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 8427912208761525837L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public Login() {
        super();
    }

    @Override
    public void init() throws ServletException {
        templateEngine = getTemplateEngine(getServletContext());
        connection = null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
        templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.getSession().invalidate();
        final WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());

        try {
            if (connection == null || connection.isClosed())
                connection = getConnection(getServletContext());
        } catch (SQLException e) {
            connection = getConnection(getServletContext());
        }

        if (connection == null) {
            context.setVariable("errorMessage", "There was an error while connecting to the DataBase.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            return;
        }

        String username;
        String password;
        User user;
        UserDAO userDao;

        try {
            username = StringEscapeUtils.escapeJava(request.getParameter("username"));
            password = StringEscapeUtils.escapeJava(request.getParameter("password"));
            if (username == null || password == null)
                throw new EmptyCredentialsException();
            if (username.isEmpty() || password.isEmpty())
                throw new MissingCredentialsException();
        } catch (Exception e) {
            context.setVariable("errorMessage", "Wrong credentials, please, try again.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            return;
        }

        userDao = new UserDAO(connection);
        try {
            user = userDao.checkCredentials(username, password);
        } catch (SQLException e) {
            context.setVariable("errorMessage", "There was an error while checking credentials.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            return;
        }

        if (user == null) {
            context.setVariable("errorMessage", "Try again, wrong credentials.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
        } else {
            request.getSession().setAttribute("user", user);
            response.sendRedirect(getServletContext().getContextPath() + "/home/");
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
