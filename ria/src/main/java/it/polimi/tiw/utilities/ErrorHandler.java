package it.polimi.tiw.utilities;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;

public class ErrorHandler extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -921708848667143034L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int code = response.getStatus();

        RequestDispatcher view = request.getRequestDispatcher(this.getInitParameter("html_" + switch (code) {
            case 400 -> "400";
            case 404 -> "404";
            case 405 -> "405";
            default -> "500";
        }));

        if (code < 400) {
            response.sendRedirect(getServletContext().getContextPath() + "/");
            return;
        }

        response.setStatus(switch (code) {
            case 400 -> 400;
            case 404 -> 404;
            case 405 -> 405;
            default -> 500;
        });

        view.forward(request, response);
    }
}
