package it.polimi.tiw.controllers.backend;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import java.util.List;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;

public class SubFolder extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        connection = null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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
            response.getWriter().println("Wrong data");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        if (pathParts.length == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Wrong data");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        int folderIndex;
        try {
            folderIndex = Integer.parseInt(pathParts[pathParts.length - 1]);
        } catch (NumberFormatException e) {
            folderIndex = -1;
        }
        FileDAO fileDao = new FileDAO(connection);
        FolderDAO folderDao = new FolderDAO(connection);

        try {
            Folder folderObj = folderDao.getFolder(folderIndex);

            if (folderObj == null || folderObj.owner() != user.id()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Folder not found or not owned");
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
            } else if (!folderObj.isSubfolder()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Folder is not a subfolder");
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
            } else {
                JsonObject json = new JsonObject();
                json.addProperty("name", folderObj.name());
                json.add("items", parseFiles(folderObj, fileDao));

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println(json);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("An unexpected error occurred.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        }
    }

    private JsonArray parseFiles(Folder folder, FileDAO fileDao) throws SQLException {
        JsonArray ret = new JsonArray();
        List<File> list = fileDao.getFilesFromFolder(folder.id());
        for (File file : list) {
            JsonObject fileJson = new JsonObject();
            fileJson.addProperty("name", file.name());
            fileJson.addProperty("ext", file.extension());
            fileJson.addProperty("id", file.id());
            ret.add(fileJson);
        }
        return ret;
    }


    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
