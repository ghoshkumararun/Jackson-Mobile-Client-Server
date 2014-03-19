/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static final String SERVER_JAVABEAN_POJO_PACKAGE = "com.tsr.remoting.valueobjects";
    private static final String CLIENT_JAVABEAN_POJO_PACKAGE = "com.tsr.remoting.valueobjects";
    private static final String JAVABEAN_POJO_CLASS = "javabean_pojo_class";
    private static final String JACKSON_CONFIG = "JACKSON_CONFIG";

    @Override
    public void init() throws ServletException {
        try {
            String configLocation = getServletConfig().getInitParameter(JACKSON_CONFIG);
            System.out.println(configLocation);
            InputStream inputStream = getServletContext().getResourceAsStream(configLocation);
            ConfigDigester configDigester = new ConfigDigester();
            configDigester.clear();
            configDigester.push(this);
            configDigester.parse(inputStream);
        } catch (IOException | SAXException ex) {
            ex.printStackTrace();
        }
    }
    
    public void addCustomPackageMappingConfig(CustomPackageMappingConfig customPackageMappingConfig)
    {
        
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

                Object obj = unparseObjectFromJSON(params.get(x));

                argTypes[x] = obj.getClass();
                args[x] = obj;

            }
            Method main = c.getDeclaredMethod(method, argTypes);
            Object obj = main.invoke(t, args);
            String json = mapper.writeValueAsString(parseObjectForJSON(obj));

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

    /**
     * Converts all JAVABEAN_POJOs in the obj down every level from
     * LinkedHashMaps to JAVABEAN_POJOs
     *
     * @param obj
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public Object unparseObjectFromJSON(Object obj) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        if (obj != null) {
            if (obj instanceof LinkedHashMap && ((LinkedHashMap) obj).get(JAVABEAN_POJO_CLASS) != null) {

                return convertLinkedHashMapsToJAVABEAN_POJO((LinkedHashMap) obj);

            } else if (obj instanceof List) {

                List list = (List) obj;
                for (int x = 0; x < list.size(); x++) {
                    list.set(x, unparseObjectFromJSON(list.get(x)));
                }
                return list;

            } else if (obj instanceof Map) {

                Map map = (Map) obj;
                Iterator entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Entry e = (Entry) entries.next();
                    map.put(e.getKey(), unparseObjectFromJSON(e.getValue()));
                }
                return map;
            }
        }
        return obj;
    }

    /**
     * Converts LinkedHashMaps to JAVABEAN_POJOs in the package
     * SERVER_JAVABEAN_POJO_PACKAGE when they come for the client and have the
     * key/value pair for JAVABEAN_POJO_CLASS
     *
     * Checks the values to see if they might be of type Map,List or a
     * JAVABEAN_POJO and then converts
     *
     * @param lhm
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object convertLinkedHashMapsToJAVABEAN_POJO(LinkedHashMap lhm) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (lhm != null) {
            if (lhm.get(JAVABEAN_POJO_CLASS) != null) {
                Class<?> javaBeanPojo = Class.forName(lhm.get(JAVABEAN_POJO_CLASS).toString().replaceAll(CLIENT_JAVABEAN_POJO_PACKAGE + ".", SERVER_JAVABEAN_POJO_PACKAGE + "."));
                Object theJavaBeanPojo = javaBeanPojo.newInstance();
                Field[] theFields = javaBeanPojo.getDeclaredFields();
                for (Field theField : theFields) {
                    String fieldKey = theField.getName();
                    Object fieldValue = lhm.get(fieldKey);
                    String setter = "set" + fieldKey.substring(0, 1).toUpperCase() + fieldKey.substring(1);
                    Class[] setterArgTypes = new Class[1];
                    Object[] setterArgs = new Object[1];
                    setterArgs[0] = fieldValue;
                    try {

                        if (fieldValue != null) {

                            fieldValue = unparseObjectFromJSON(fieldValue);

                            setterArgTypes[0] = fieldValue.getClass();

                            Method m = javaBeanPojo.getDeclaredMethod(setter, setterArgTypes);
                            m.invoke(theJavaBeanPojo, setterArgs);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setterArgTypes[0] = Object.class;
                        try {
                            Method m = javaBeanPojo.getDeclaredMethod(setter, setterArgTypes);
                            m.invoke(theJavaBeanPojo, setterArgs);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                return theJavaBeanPojo;
            } else {
                return lhm;
            }
        } else {
            return lhm;
        }
    }

    /**
     * Convert all JAVABEAN_POJOs all the way down every level to LinkedHashMaps
     * with custom field to determine what JAVABEAN_POJO it is
     *
     * @param obj
     * @return
     */
    private Object parseObjectForJSON(Object obj) {
        if (obj != null) {
            boolean isJavaBeanPojo = Package.getPackage(SERVER_JAVABEAN_POJO_PACKAGE).equals(obj.getClass().getPackage());

            if (isJavaBeanPojo) {

                return convertJAVABEAN_POJOtoLinkedHashMap(obj);

            } else if (obj instanceof List) {

                List list = (List) obj;
                for (int x = 0; x < list.size(); x++) {
                    list.set(x, parseObjectForJSON(list.get(x)));
                }
                return list;

            } else if (obj instanceof Map) {

                Map map = (Map) obj;
                Iterator entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Entry e = (Entry) entries.next();
                    map.put(e.getKey(), parseObjectForJSON(e.getValue()));
                }
                return map;

            } else {
                return obj;
            }
        } else {
            return obj;
        }
    }

    /**
     * Converts JAVABEAN_POJOs to LinkedHashMaps before they go to the Android
     * app and checks JAVABEAN_POJO values to see if they need to be converted
     *
     * @param obj
     * @return
     */
    private Object convertJAVABEAN_POJOtoLinkedHashMap(Object obj) {
        if (obj != null) {
            boolean isJavaBeanPojo = Package.getPackage(SERVER_JAVABEAN_POJO_PACKAGE).equals(obj.getClass().getPackage());

            if (isJavaBeanPojo) {

                LinkedHashMap lhm = new LinkedHashMap();
                Field[] theFields = obj.getClass().getDeclaredFields();
                for (Field theField : theFields) {
                    try {
                        String getter = "get" + theField.getName().substring(0, 1).toUpperCase() + theField.getName().substring(1);

                        Class c = obj.getClass();
                        Method m = c.getDeclaredMethod(getter, (Class[]) null);
                        Object o = m.invoke(obj, (Object[]) null);
                        lhm.put(theField.getName(), parseObjectForJSON(o));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                lhm.put(JAVABEAN_POJO_CLASS, obj.getClass());
                return lhm;

            } else {
                return obj;
            }
        } else {
            return obj;
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
