package org.example.servlet;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Example HTTP servlet.
 * 
 * @author Oscar Stigter
 */
public class ExampleServlet extends HttpServlet {

    private static final long serialVersionUID = 6381828167428989365L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        final Writer writer = response.getWriter();
        writer.write("<h1>Example servlet</h1>");
        writer.write("<p>Hello World from a simple test servlet!</p>");
        writer.write("<p><a href=\"/\">Back</a></p>");
    }
}
