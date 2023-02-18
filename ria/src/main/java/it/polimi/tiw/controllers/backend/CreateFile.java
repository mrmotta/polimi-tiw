package it.polimi.tiw.controllers.backend;

import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FileDAO;
import it.polimi.tiw.dao.FolderDAO;
import org.apache.commons.text.StringEscapeUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;

public class CreateFile extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public CreateFile() {
        super();
    }

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
            response.getWriter().println("There was an error while connecting to the DataBase.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        String name = StringEscapeUtils.escapeJava(request.getParameter("name"));
        String ext = StringEscapeUtils.escapeJava(request.getParameter("ext"));
        int parent;
        try {
            parent = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("parent")));
        } catch (NumberFormatException e) {
            parent = -1;
        }
        Date creation;
        try {
            creation = new SimpleDateFormat("yyyy-MM-dd").parse(StringEscapeUtils.escapeJava(request.getParameter("date")));
        } catch (ParseException e) {
            creation = new Date();
        }
        String summary = StringEscapeUtils.escapeJava(request.getParameter("summary"));
        String type = StringEscapeUtils.escapeJava(request.getParameter("type"));
        FileDAO fileDao = new FileDAO(connection);
        FolderDAO folderDao = new FolderDAO(connection);
        Folder parentObj;

        try {
            if (!fileDao.exists(name, ext, parent)) {
                parentObj = folderDao.getFolder(parent);
                if (parentObj == null || !parentObj.isSubfolder() || parentObj.owner() != user.id()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("There was an error while creating the file.");
                    response.setContentType("text/plain");
                    response.setCharacterEncoding("UTF-8");
                } else {
                    fileDao.createFile(user, name, ext, parent, creation, summary, type);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().println("File already exists in selected scope.");
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("There was an error while creating the file.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
