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

public class Deletion extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public Deletion() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = null;
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            if (connection == null || connection.isClosed())
                connection = getConnection(getServletContext());
        } catch (SQLException e) {
            connection = getConnection(getServletContext());
        }

        if (connection == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Cannot connect to database");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong request.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length < 2 || (!pathParts[1].equals("folder") && !pathParts[1].equals("file"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong request.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        int itemId;
        try {
            itemId = Integer.parseInt(pathParts[2]);
        } catch (NumberFormatException e) {
            itemId = -1;
        }

        boolean success = pathParts[1].equals("folder") ? deleteFolder(itemId, user) : deleteFile(itemId, user);
        response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        if (!success) {
            response.getWriter().println("Item not found or not owned.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        }
    }

    private boolean deleteFile(int id, User user) {

        try {

            FileDAO fileDao = new FileDAO(connection);

            File file = fileDao.getFile(id);

            if (file == null || file.owner() != user.id())
                return false;

            fileDao.delete(id);

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private boolean deleteFolder(int id, User user) {

        try {

            FolderDAO folderDao = new FolderDAO(connection);
            Folder folder = folderDao.getFolder(id);

            if (folder == null || folder.owner() != user.id())
                return false;

            folderDao.delete(id);

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
