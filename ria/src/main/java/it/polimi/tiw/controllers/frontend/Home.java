package it.polimi.tiw.controllers.frontend;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FolderDAO;

import javax.servlet.RequestDispatcher;
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

public class Home extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public Home() {
        super();
    }

    @Override
    public void init() throws ServletException {
        connection = null;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestDispatcher view = request.getRequestDispatcher(this.getInitParameter("html"));
        view.forward(request, response);
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
            response.getWriter().println("Cannot connect to the DataBase.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        try {
            JsonObject ret = new JsonObject();
            FolderDAO folderDAO = new FolderDAO(connection);
            ret.add("items", parseFolders(folderDAO.getFoldersFromUser((User) request.getSession().getAttribute("user")), folderDAO));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(ret);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("An unexpected error occurred.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }

    private JsonArray parseFolders(List<Folder> folders, FolderDAO folderDAO) throws SQLException {
        JsonArray response = new JsonArray();
        for (Folder folder : folders)
            response.add(parseFolder(folder, folderDAO));
        return response;
    }

    private JsonObject parseFolder(Folder folder, FolderDAO folderDAO) throws SQLException {
        JsonObject response = new JsonObject();
        response.addProperty("name", folder.name());
        response.addProperty("id", folder.id());
        response.add("subfolders", parseSubfolders(folder.subfolders()));
        return response;
    }

    private JsonArray parseSubfolders(List<Folder> subfolders) {
        JsonArray response = new JsonArray();
        JsonObject obj;
        for (Folder subfolder : subfolders) {
            obj = new JsonObject();
            obj.addProperty("name", subfolder.name());
            obj.addProperty("id", subfolder.id());

            response.add(obj);
        }
        return response;
    }
}
