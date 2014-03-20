package com.zur.remoting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * To use this class call the static method
 * JacksonRemotingCall.remotingCall(JacksonRemotingCallListener activity, String
 * theClass, String method, ArrayList params)
 * 
 * JacksonRemotingCallListener activity is the activity that is calling the
 * method, the activity must implement JacksonRemotingCallListener String theClass
 * is the Java class on the server side you wish to call, ex.
 * com.example.RemotingService, String method is the method in theClass you wish
 * to use ArrayList params is a list of params to be sent over to the method
 * 
 * If the method that is called returns a JavaBean POJO you will need to cast the object
 * returned from onRemotingCallFinished to the JavaBean POJO
 * 
 * WARNING: This does not support parameter types of primitives
 * 
 * NOTE: JacksonRemotingCallListener.onRemotingCallFinished(String theClass,String
 * method,Object result) will have remotingCallException or remotingCallError as
 * theClass and the method if an error occurs while doing work in this method
 * 
 * NOTE: If an error occurs on the Java server side
 * JacksonRemotingCallListener.onRemotingCallFinished(String theClass,String
 * method,Object result) will return the same theClass and method that was
 * originally used but the result will be the exception.toString() caused in
 * the servlet
 * 
 * @author Matt Roth
 * 
 */
public class JacksonRemotingCall extends AsyncTask {
	/**
	 * 
	 * Use this to pass the JacksonRemotingCallListener, the class,
	 * the method, and an Array of the parameters 
	 * 
	 * The call is asynchronous
	 * 
	 * Example:
	 * You would like to call com.example.RemotingService.printInputXNumOfTimes(String input, int x) on the Java server
	 * 
	 * theClass = "com.example.RemotingService"
	 * method = "printInputXNumOfTimes"
	 * params = {"hello",2}
	 * 
	 * If there is networkInfo is null or network is not connected will do
	 * 	caller.onRemotingCallFinished("REMOTINGCALLEXCEPTION","REMOTINGCALLEXCEPTION","NetworkInfo is either null or connectivity doesn't exist.")
	 * 
	 * If there is an exception during communication with server will do
	 * 	caller.onRemotingCallFinished("REMOTINGCALLEXCEPTION","REMOTINGCALLEXCEPTION",exception.toString())
	 * 
	 * If there is an error during communication with server will do
	 * 	caller.onRemotingCallFinished("REMOTINGCALLERROR","REMOTINGCALLERROR",error.toString())
	 * 
	 * @param activity
	 * @param theClass
	 * @param method
	 * @param params
	 * @throws NullPointerException 
	 */
	public static void remotingCall(JacksonRemotingCallListener caller, String theClass, String method, ArrayList params) throws NullPointerException {

		if(caller != null) {
			ConnectivityManager connMgr = (ConnectivityManager) caller.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				new JacksonRemotingCall(caller, theClass, method, params).execute(caller.urlForGateway(), method, params);
			} else {
				caller.onRemotingCallFinished(remotingCallException, remotingCallException, NETWORK_INFO_ERROR);
			} 
		} else {
			throw new NullPointerException(remotingCallerNull + ":" + CALLER_NULL_ERROR);
		}
	}

	// Error messages
	protected static final String DEBUG_TAG = "JacksonClient";
	protected static final String NETWORK_INFO_ERROR = "NetworkInfo is either null or connectivity doesn't exist.";
	protected static final String INVALID_URL = "Unable to connect to server.";
	protected static final String NETWORK_ERROR = "Network error Response value is ";
	public static String remotingCallException = "REMOTINGCALLEXCEPTION";
	public static String remotingCallError = "REMOTINGCALLERROR";
	public static String remotingCallerNull = "REMOTINGCALLERNULL";
	public static String CALLER_NULL_ERROR = "JacksonRemotingCallListener is null";

	protected String method;
	protected String theClass;
	protected ArrayList params;
	protected JacksonRemotingCallListener caller;

	/**
	 * Constructor for which takes the current activity which must implement
	 * JacksonRemotingCallListener, the class,
	 * the method, and an ArrayList of parameters in the same order as in method
	 * declaration
	 * 
	 * @param activity
	 * @param method
	 * @param params
	 */
	public JacksonRemotingCall(JacksonRemotingCallListener caller, String theClass, String method, ArrayList params) {
		this.caller = caller;
		this.theClass = theClass;
		this.method = method;
		this.params = params;
	}

	/**
	 * Called when the remoting call is finished
	 */
	@Override
	protected void onPostExecute(Object result) {
		caller.onRemotingCallFinished(theClass, method, result);
	}

	/**
	 * Calls the method that will translate JSON
	 */
	@Override
	protected Object doInBackground(Object... urls) {

		// params comes from the execute() call: params[0] is the url.
		try {
			return downloadUrl((String) urls[0]);
		} catch (IOException e) {
			method = remotingCallException;
			return INVALID_URL;
		}
	}

	/**
	 * method to translate json Given a URL, establishes an HttpUrlConnection
	 * using a HttpClient so that the session stays the same
	 * 
	 * @param myurl
	 * @return
	 * @throws IOException
	 */
	protected Object downloadUrl(String myurl) throws IOException {
		InputStream is = null;
		OutputStream out = null;
		ArrayList<NameValuePair> list = new ArrayList<NameValuePair>(3);
		ObjectMapper mapper = new ObjectMapper();

		try {
			HttpClient httpclient = HttpClientFactory.getThreadSafeClient();
			HttpPost httpost = new HttpPost(myurl);

			list.add(new BasicNameValuePair("theClass", theClass));
			list.add(new BasicNameValuePair("method", method));
			list.add(new BasicNameValuePair("params", mapper.writeValueAsString(JacksonRemotingCallObjectConversion.convertJAVABEAN_POJOsInListToLinkedHashMaps(params))));
			httpost.setEntity(new UrlEncodedFormEntity(list));

			HttpResponse response = httpclient.execute(httpost);
			Log.d(DEBUG_TAG, "The response is: " + response.getStatusLine());
			HttpEntity entity = response.getEntity();

			if (entity != null && response.getStatusLine().getStatusCode() == 200) {
				Log.d(DEBUG_TAG, "Success");
				is = entity.getContent();
				Reader reader = new InputStreamReader(is);

				Object result = mapper.readValue(reader, Object.class);
				return JacksonRemotingCallObjectConversion.unparseObjectFromJSON(result);

			} else {
				method = remotingCallException;
				return NETWORK_ERROR + response.getStatusLine();
			}

		} catch (Exception e) {
			theClass = remotingCallException;
			method = remotingCallException;
			e.printStackTrace();
			return e;
		} catch (Error e) {
			theClass = remotingCallError;
			method = remotingCallError;
			e.printStackTrace();
			return e;
		} finally {
			if (is != null) {
				is.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
}
