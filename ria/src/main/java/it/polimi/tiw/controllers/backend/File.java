package it.polimi.tiw.controllers.backend;

import com.google.gson.JsonObject;
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

public class File extends HttpServlet {

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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("There was an error while connecting to the DataBase.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        String pathInfo = request.getPathInfo();
        FileDAO fd = new FileDAO(connection);
        FolderDAO folderDao = new FolderDAO(connection);

        if (pathInfo == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong request.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong request.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        int fileIndex;
        try {
            fileIndex = Integer.parseInt(pathParts[pathParts.length - 1]);
        } catch (NumberFormatException e) {
            fileIndex = -1;
        }
        it.polimi.tiw.beans.File doc;
        try {
            doc = fd.getFile(fileIndex);

            if (doc != null && doc.owner() == user.id()) {
                Folder subf = folderDao.getFolder(doc.parent());
                Folder root = folderDao.getFolder(subf.parent());

                JsonObject json = new JsonObject();
                json.addProperty("folder", "/" + root.name() + "/" + subf.name());
                json.addProperty("name", doc.name());
                json.addProperty("ext", doc.extension());
                json.addProperty("summary", doc.summary());
                json.addProperty("id", doc.id());
                json.addProperty("type", doc.type());
                json.addProperty("date", doc.creation().toString());
                json.addProperty("parent", doc.parent());

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(json);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println("This file doesn't exist or doesn't belong to you.");
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("There was an error while reading this file.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
