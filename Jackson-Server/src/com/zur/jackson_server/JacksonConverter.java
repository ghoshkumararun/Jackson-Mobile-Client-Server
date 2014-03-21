/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zur.jackson_server;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Matt Roth
 */
public class JacksonConverter {

    private String SERVER_JAVABEAN_POJO_PACKAGE = null;
    private String CLIENT_JAVABEAN_POJO_PACKAGE = null;
    private static final String JAVABEAN_POJO_CLASS = "javabean_pojo_class";

    public JacksonConverter() {
        CustomPackageMappingConfig customPackageMappingConfig = JacksonConfig.getInstance().getCustomPackageMappingConfig();
        SERVER_JAVABEAN_POJO_PACKAGE = customPackageMappingConfig.getServerPackageName();
        CLIENT_JAVABEAN_POJO_PACKAGE = customPackageMappingConfig.getClientPackageName();
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
    public Object unparseObjectFromJackson(Object obj) throws ClassNotFoundException, InstantiationException, IllegalAccessException {

        if (obj != null) {
            if (obj instanceof LinkedHashMap && ((LinkedHashMap) obj).get(JAVABEAN_POJO_CLASS) != null) {

                return convertLinkedHashMapsToJAVABEAN_POJO((LinkedHashMap) obj);

            } else if (obj instanceof List) {

                List list = (List) obj;
                for (int x = 0; x < list.size(); x++) {
                    list.set(x, unparseObjectFromJackson(list.get(x)));
                }
                return list;

            } else if (obj instanceof Map) {

                Map map = (Map) obj;
                Iterator entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry e = (Map.Entry) entries.next();
                    map.put(e.getKey(), unparseObjectFromJackson(e.getValue()));
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

                            fieldValue = unparseObjectFromJackson(fieldValue);

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
    public Object parseObjectForJackson(Object obj) {
        if (obj != null) {
            boolean isJavaBeanPojo = Package.getPackage(SERVER_JAVABEAN_POJO_PACKAGE).equals(obj.getClass().getPackage());

            if (isJavaBeanPojo) {

                return convertJAVABEAN_POJOtoLinkedHashMap(obj);

            } else if (obj instanceof List) {

                List list = (List) obj;
                for (int x = 0; x < list.size(); x++) {
                    list.set(x, parseObjectForJackson(list.get(x)));
                }
                return list;

            } else if (obj instanceof Map) {

                Map map = (Map) obj;
                Iterator entries = map.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry e = (Map.Entry) entries.next();
                    map.put(e.getKey(), parseObjectForJackson(e.getValue()));
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
                        lhm.put(theField.getName(), parseObjectForJackson(o));

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
}
