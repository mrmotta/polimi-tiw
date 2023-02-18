package it.polimi.tiw.controllers.backend;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

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

        FileDAO fileDao = new FileDAO(connection);
        FolderDAO folderDao = new FolderDAO(connection);

        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        if (pathInfo == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/home/");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length < 2) {
            response.sendRedirect(getServletContext().getContextPath() + "/home/");
            return;
        }

        int fileId;
        try {
            fileId = Integer.parseInt(pathParts[pathParts.length - 1]);
        } catch (NumberFormatException e) {
            fileId = -1;
        }
        int destFolderId;
        try {
            destFolderId = Integer.parseInt(pathParts[pathParts.length - 2]);
        } catch (NumberFormatException e) {
            destFolderId = -1;
        }

        try {

            File movingFile = fileDao.getFile(fileId);

            if (movingFile == null || movingFile.owner() != user.id()) {
                processError("File not found or not owned.", context, response);
                return;
            }

            Folder destFolder = folderDao.getFolder(destFolderId);

            if (destFolder == null || destFolder.owner() != user.id())
                processError("The destination folder does not exist or it's not owned.", context, response);
            else if (destFolder.parent() == -1)
                processError("The destination folder is not a subfolder.", context, response);
            else if (movingFile.parent() == destFolderId)
                processError("Can't move the file to the same folder.", context, response);
            else if (fileDao.exists(movingFile.name(), movingFile.extension(), destFolderId))
                processError("A file with the same name and extension is already present in the destination folder.", context, response);
            else {
                fileDao.moveFile(movingFile, destFolder);
                response.sendRedirect(request.getServletContext().getContextPath() + "/home/folder/" + destFolderId);
            }
        } catch (SQLException e) {
            processError("There was an error while processing the request.", context, response);
        }
    }

    private void processError(String message, WebContext context, HttpServletResponse response) throws IOException {
        context.setVariable("errorMessage", message);
        templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
