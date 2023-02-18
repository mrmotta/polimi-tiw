package it.polimi.tiw.controllers.backend;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;

public class Controller extends HttpServlet {

    @Serial
    private static final long serialVersionUID = 1L;

    public Controller() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (request.getSession().getAttribute("user") == null)
            response.sendRedirect(getServletContext().getContextPath() + "/login/");
        else
            response.sendRedirect(getServletContext().getContextPath() + "/home/");
    }
}