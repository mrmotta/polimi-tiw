package it.polimi.tiw.controllers.frontend;

import it.polimi.tiw.beans.File;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FileDAO;
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

public class MoveFile extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        templateEngine = getTemplateEngine(getServletContext());
        connection = null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

        User user = (User) request.getSession().getAttribute("user");
        String pathInfo = request.getPathInfo();
        FileDAO fileDao = new FileDAO(connection);
        FolderDAO fdao = new FolderDAO(connection);

        if (pathInfo == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/home/");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 0) {
            response.sendRedirect(getServletContext().getContextPath() + "/home/");
            return;
        }

        int fileId;
        try {
            fileId = Integer.parseInt(pathParts[pathParts.length - 1]);
        } catch (NumberFormatException e) {
            fileId = -1;
        }

        try {
            File file = fileDao.getFile(fileId);
            if (file == null || user.id() != file.owner()) {
                context.setVariable("errorMessage", "Error: file not owned or not found");
                templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            } else {
                List<Folder> res = fdao.getFoldersFromUser(user);
                context.setVariable("folders", res);
                context.setVariable("source", fdao.getFolder(file.parent()));
                context.setVariable("moving_file", file);
                templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
            }
        } catch (SQLException e) {
            context.setVariable("errorMessage", "There was an error while processing the request.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
