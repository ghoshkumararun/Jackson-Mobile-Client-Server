package com.zur.remoting;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JacksonRemotingCallObjectConversion {
	public static String SERVER_JAVABEAN_POJO_PACKAGE = "com.tsr.remoting.valueobjects";
	public static String CLIENT_JAVABEAN_POJO_PACKAGE = "com.tsr.remoting.valueobjects";
	public static String JAVABEAN_POJO_CLASS = "javabean_pojo_class";

	// JAVABEAN_POJO to LinkedHashMap

	public static ArrayList convertJAVABEAN_POJOsInListToLinkedHashMaps(ArrayList list) {
		for (int x = 0; x < list.size(); x++) {

			list.set(x,parseObjectForJSON(list.get(x)));

		}

		return list;
	}

	/**
	 * Convert all JAVABEAN_POJOs all the way down every level to LinkedHashMaps with custom field to determine what JAVABEAN_POJO it is
	 * @param obj
	 * @return 
	 */
	public static Object parseObjectForJSON(Object obj) {
		boolean isJavaBeanPojo = Package.getPackage(CLIENT_JAVABEAN_POJO_PACKAGE).equals(obj.getClass().getPackage());

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

		}

		return obj;
	}

	/**
	 * Converts JAVABEAN_POJOs to LinkedHashMaps before they go to the Java Server
	 * and checks JAVABEAN_POJO values to see if they need to be converted
	 * 
	 * @param obj
	 * @return
	 */
	public static Object convertJAVABEAN_POJOtoLinkedHashMap(Object tobj)
	{
		boolean isJavaBeanPojo = Package.getPackage(CLIENT_JAVABEAN_POJO_PACKAGE).equals(tobj.getClass().getPackage());

		if (isJavaBeanPojo) {
			Field[] theFields = tobj.getClass().getDeclaredFields();
			LinkedHashMap lhm = new LinkedHashMap();

			for (int y = 0; y < theFields.length; y++) {
				Field theField = theFields[y];
				try {
					String getter = "get" + theField.getName().substring(0, 1).toUpperCase() + theField.getName().substring(1);

					Class c = tobj.getClass();
					Method m = c.getDeclaredMethod(getter, (Class[]) null);
					Object obj = m.invoke(tobj, (Object[]) null);
					lhm.put(theField.getName(), parseObjectForJSON(obj));

				} catch (Exception e) {
					if (e instanceof NoSuchMethodException) {
						// In case the getter doesn't have an uppercase letter after get in method name
						try {
							String getter = "get" + theField.getName();

							Class c = tobj.getClass();
							Method m = c.getDeclaredMethod(getter, (Class[]) null);
							Object o = m.invoke(tobj, (Object[]) null);
							lhm.put(theField.getName(), parseObjectForJSON(o));

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						e.printStackTrace();
					}				
				}
			}

			lhm.put(JAVABEAN_POJO_CLASS, tobj.getClass());
			return lhm;
		}

		return tobj;
	}

	//*************************************************************

	// LinkedHashMap to JAVABEAN_POJO

	/**
	 * Converts all JAVABEAN_POJOs in the obj down every level from LinkedHashMaps to JAVABEAN_POJOs
	 * 
	 * @param obj
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException 
	 */
	public static Object unparseObjectFromJSON(Object obj) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if(obj != null){

			if(obj instanceof LinkedHashMap && ((LinkedHashMap)obj).get(JAVABEAN_POJO_CLASS) != null) {

				return convertLinkedHashMapsToVO((LinkedHashMap)obj);

			} else if(obj instanceof List) {

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
	 * Converts LinkedHashMaps to JAVABEAN_POJOs in the package SERVER_JAVABEAN_POJO_PACKAGE when they
	 * come from the Java Server and have the key/value pair for JAVABEAN_POJO_CLASS
	 * 
	 * Checks the values to see if they might be of type Map,List or a JAVABEAN_POJO and then converts
	 *
	 * @param lhm
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Object convertLinkedHashMapsToVO(LinkedHashMap lhm) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (lhm != null){
			if (lhm.get(JAVABEAN_POJO_CLASS) != null) {
				Class<?> javaBeanPojo = Class.forName(lhm.get(JAVABEAN_POJO_CLASS).toString().replaceAll(SERVER_JAVABEAN_POJO_PACKAGE + ".", CLIENT_JAVABEAN_POJO_PACKAGE + "."));
				Object theJavaBeanPojo = javaBeanPojo.newInstance();
				Field[] theFields = javaBeanPojo.getDeclaredFields();

				for (int y = 0; y < theFields.length; y++) {
					String fieldKey = theFields[y].getName();
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
						setterArgTypes[0] = Object.class;
						try{
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
}
