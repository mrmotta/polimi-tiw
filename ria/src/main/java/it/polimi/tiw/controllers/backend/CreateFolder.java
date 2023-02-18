package it.polimi.tiw.controllers.backend;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FolderDAO;
import it.polimi.tiw.excetions.WrongValuesException;
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

public class CreateFolder extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;

    public CreateFolder() {
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

        String name = StringEscapeUtils.escapeJava(request.getParameter("name"));
        int parent;
        try {
            parent = Integer.parseInt(request.getParameter("parent"));
        } catch (NumberFormatException e) {
            parent = -1;
        }
        Date creation;
        try {
            creation = new SimpleDateFormat("yyyy-MM-dd").parse(StringEscapeUtils.escapeJava(request.getParameter("date")));
        } catch (Exception e) {
            creation = new Date();
        }
        User user = ((User) request.getSession().getAttribute("user"));
        FolderDAO fdao = new FolderDAO(connection);

        try {
            if (!fdao.exists(name, parent)) {
                fdao.createFolder(user.id(), name, parent, creation);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().println("Folder already existing in selected scope.");
                response.setContentType("text/plain");
                response.setCharacterEncoding("UTF-8");
            }
        } catch (SQLException | WrongValuesException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("There was an error while checking the folder.");
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
        }
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
