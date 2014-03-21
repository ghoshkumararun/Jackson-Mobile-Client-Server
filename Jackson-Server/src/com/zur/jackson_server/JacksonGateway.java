package com.zur.jackson_server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.xml.sax.SAXException;

/**
 * Does not support passing of primitives as parameters to the methods that are
 * called
 *
 * NOTE: classes called through this servlet should have a constructor that
 * takes a HttpServletRequest as a parameter
 *
 * @author Matt Roth
 */
public class JacksonGateway extends HttpServlet {

    private static final String JACKSON_CONFIG = "JACKSON_CONFIG";
    private JacksonConverter converter;
    
    @Override
    public void init() throws ServletException {
//        loadConfig();
//        converter = new JacksonConverter();
        System.out.println("TEST");
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ObjectMapper mapper = new ObjectMapper();

        try {

            // get class and method of class from the request
            String theClass = request.getParameter("theClass");
            String method = request.getParameter("method");
            System.out.println("JacksonGateway request | " + theClass + " method " + method);
            // get the parameters for the method which have converted into Jackson json
            ArrayList params = mapper.readValue(request.getParameter("params"), ArrayList.class);

            // use reflection to create the class, call the method and convert result into json to be sent back in response
            Class<?> c = Class.forName(theClass);
            Object t = c.getConstructor(HttpServletRequest.class).newInstance(request);
            Class[] argTypes = new Class[params.size()];
            Object[] args = new Object[params.size()];

            for (int x = 0; x < params.size(); x++) {

                Object obj = converter.unparseObjectFromJackson(params.get(x));

                argTypes[x] = obj.getClass();
                args[x] = obj;

            }
            Method main = c.getDeclaredMethod(method, argTypes);
            Object obj = main.invoke(t, args);
            String json = mapper.writeValueAsString(converter.parseObjectForJackson(obj));

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

        } catch (Exception e) {

            String json = mapper.writeValueAsString(e.toString());

            response.setStatus(500);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            e.printStackTrace();

        }
    }

    private void loadConfig() {
        try {
            String configLocation = getServletConfig().getInitParameter(JACKSON_CONFIG);

            JacksonConfig config = JacksonConfig.getInstance();

            InputStream inputStream = getServletContext().getResourceAsStream(configLocation);

            ConfigDigester configDigester = new ConfigDigester();
            configDigester.clear();
            configDigester.push(config);
            configDigester.parse(inputStream);

        } catch (IOException | SAXException ex) {
            ex.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
