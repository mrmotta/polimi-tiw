package it.polimi.tiw.controllers.frontend;

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
import java.util.regex.Pattern;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;
import static it.polimi.tiw.utilities.Utilities.getTemplateEngine;

public class Signup extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1478983507509357025L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public Signup() {
        super();
    }

    @Override
    public void init() throws ServletException {
        templateEngine = getTemplateEngine(getServletContext());
        connection = null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

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
        String email;
        String password;
        String repeat;
        boolean check;
        UserDAO userDao;

        try {
            username = StringEscapeUtils.escapeJava(request.getParameter("username"));
            email = StringEscapeUtils.escapeJava(request.getParameter("email"));
            password = StringEscapeUtils.escapeJava(request.getParameter("password"));
            repeat = StringEscapeUtils.escapeJava(request.getParameter("repeat_password"));
            if (username == null || password == null || repeat == null)
                throw new EmptyCredentialsException();
            if (username.isEmpty() || password.isEmpty() || repeat.isEmpty())
                throw new MissingCredentialsException();
            if (!Pattern.compile("^\\w+([\\.-]?\\w+)*@\\w+([\\.-]?\\w+)*(\\.\\w{2,3})+$", Pattern.CASE_INSENSITIVE).matcher(email).find())
                throw new MissingCredentialsException();
            email = email.toLowerCase();
        } catch (Exception e) {
            context.setVariable("errorMessage", "Wrong credentials, please, try again.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            return;
        }

        userDao = new UserDAO(connection);
        try {
            check = userDao.exists(username);
        } catch (SQLException e) {
            context.setVariable("errorMessage", "There was an error while checking username.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            return;
        }

        if (check) {
            context.setVariable("errorMessage", "Try again, username already in use.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
        } else if (!password.equals(repeat)) {
            context.setVariable("errorMessage", "Try again, passwords don't coincide.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
        } else {
            try {
                request.getSession().setAttribute("user", userDao.createUser(username, email, password));
            } catch (SQLException e) {
                context.setVariable("errorMessage", "There was an error while creating the account.");
                templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
                return;
            }
            response.sendRedirect(getServletContext().getContextPath() + "/home/");
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
