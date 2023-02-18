package it.polimi.tiw.controllers.backend;

import it.polimi.tiw.beans.File;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FileDAO;
import it.polimi.tiw.dao.FolderDAO;

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

public class MoveFile extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        connection = null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            if (connection == null || connection.isClosed())
                connection = getConnection(getServletContext());
        } catch (SQLException e) {
            connection = getConnection(getServletContext());
        }

        if (connection == null) {
            processError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "There was an error while connecting to the DataBase.", response);
            return;
        }

        FileDAO fileDao = new FileDAO(connection);
        FolderDAO folderDao = new FolderDAO(connection);

        String pathInfo = request.getPathInfo();
        User user = (User) request.getSession().getAttribute("user");

        if (pathInfo == null) {
            processError(HttpServletResponse.SC_BAD_REQUEST, "Wrong request.", response);
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length < 2) {
            processError(HttpServletResponse.SC_BAD_REQUEST, "Wrong request.", response);
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
                processError(HttpServletResponse.SC_BAD_REQUEST, "File not found or not owned.", response);
                return;
            }

            Folder destFolder = folderDao.getFolder(destFolderId);

            if (destFolder == null || destFolder.owner() != user.id())
                processError(HttpServletResponse.SC_BAD_REQUEST, "The destination folder does not exist or it's not owned.", response);
            else if (destFolder.parent() == 0)
                processError(HttpServletResponse.SC_BAD_REQUEST, "The destination folder is not a subfolder.", response);
            else if (movingFile.parent() == destFolderId)
                processError(HttpServletResponse.SC_FORBIDDEN, "Can't move the file to the same folder.", response);
            else if (fileDao.exists(movingFile.name(), movingFile.extension(), destFolderId))
                processError(HttpServletResponse.SC_FORBIDDEN, "A file with the same name and extension is already present in the destination folder.", response);
            else {
                fileDao.moveFile(movingFile, folderDao.getFolder(destFolderId));
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (SQLException e) {
            processError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "There was an error while processing the request.", response);
        }
    }

    private void processError(int errorCode, String message, HttpServletResponse response) throws IOException {
        response.setStatus(errorCode);
        response.getWriter().println(message);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
