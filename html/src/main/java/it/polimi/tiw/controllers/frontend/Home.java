package it.polimi.tiw.controllers.frontend;

import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FolderDAO;
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
import java.util.List;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;
import static it.polimi.tiw.utilities.Utilities.getTemplateEngine;

public class Home extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public Home() {
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

        FolderDAO folderDao = new FolderDAO(connection);
        List<Folder> res;

        try {
            User user = (User) request.getSession().getAttribute("user");
            res = folderDao.getFoldersFromUser(user);
        } catch (SQLException e) {
            context.setVariable("errorMessage", "There was an error fetching folders.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            return;
        }

        context.setVariable("folders", res);
        templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
