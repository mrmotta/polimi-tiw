package it.polimi.tiw.utilities;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serial;

import static it.polimi.tiw.utilities.Utilities.getTemplateEngine;

public class ErrorHandler extends HttpServlet {

    @Serial
    private static final long serialVersionUID = -921708848667143034L;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        templateEngine = getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        int code = response.getStatus();
        String message;
        String description;

        if (code < 400) {
            response.sendRedirect(getServletContext().getContextPath() + "/");
            return;
        }

        switch (code) {
            case 400 -> {
                message = "Bad Request";
                description = "The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).";
            }
            case 404 -> {
                message = "Not Found";
                description = "The server can not find the requested resource.";
            }
            case 405 -> {
                message = "Method Not Allowed";
                description = "The request method is known by the server but is not supported by the target resource.";
            }
            default -> {
                code = 500;
                message = "Internal Server Error";
                description = "The server has encountered a situation it does not know how to handle.";
            }
        }

        final WebContext context = new WebContext(request, response, getServletContext(), request.getLocale());
        context.setVariable("code", String.valueOf(code));
        context.setVariable("message", message);
        context.setVariable("description", description);
        templateEngine.process(this.getInitParameter("html"), context, response.getWriter());
        response.setStatus(code);
    }
}
