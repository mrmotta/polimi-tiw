package it.polimi.tiw.controllers.backend;

import it.polimi.tiw.beans.Folder;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.FolderDAO;
import it.polimi.tiw.excetions.WrongValuesException;
import org.apache.commons.text.StringEscapeUtils;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.tiw.utilities.DBConnectionHandler.closeConnection;
import static it.polimi.tiw.utilities.DBConnectionHandler.getConnection;
import static it.polimi.tiw.utilities.Utilities.getTemplateEngine;

public class CreateFolder extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public CreateFolder() {
        super();
    }

    @Override
    public void init() throws ServletException {
        templateEngine = getTemplateEngine(getServletContext());
        connection = null;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        final WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());

        try {
            if (connection == null || connection.isClosed())
                connection = getConnection(getServletContext());
        } catch (SQLException e) {
            connection = getConnection(getServletContext());
        }

        if (connection == null) {
            context.setVariable("errorMessageFolder", "There was an error while connecting to the DataBase.");
            templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
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
            creation = new SimpleDateFormat("yyyy-MM-dd")
                    .parse(StringEscapeUtils.escapeJava(request.getParameter("date")));
        } catch (Exception e) {
            creation = new Date();
        }

        User user = ((User) request.getSession().getAttribute("user"));
        FolderDAO fdao = new FolderDAO(connection);

        try {
            if (!fdao.exists(name, parent)) {
                fdao.createFolder(user.id(), name, parent, creation);
                response.sendRedirect(getServletContext().getContextPath() + "/home/");
            } else
                processError("Folder already existing in selected scope.", user, context, response);
        } catch (SQLException | WrongValuesException e) {
            processError("There was an error while creating the folder.", user, context, response);
        }
    }

    private void processError(String message, User user, WebContext context, HttpServletResponse response) throws IOException {

        try {
            FolderDAO fdao = new FolderDAO(connection);
            List<Folder> res = fdao.getFoldersFromUser(user);
            Map<Integer, String> subfTree = new HashMap<>();
            for (Folder folder : res) {
                if (!folder.isSubfolder()) {
                    for (Folder sub : folder.subfolders()) {
                        subfTree.put(sub.id(), folder.name() + '/' + sub.name());
                    }
                }
            }
            context.setVariable("errorMessageFolder", message);
            context.setVariable("folders", res);
            context.setVariable("subfolders", subfTree);
        } catch (SQLException ex) {
            context.setVariable("errorMessageFolder", "There was an error while connecting to the DataBase.");
        }

        templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
    }

    @Override
    public void destroy() {
        closeConnection(connection);
    }
}
