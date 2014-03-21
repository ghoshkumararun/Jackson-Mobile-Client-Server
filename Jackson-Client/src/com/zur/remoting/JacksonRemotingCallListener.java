package com.zur.remoting;

/**
 * This interface must implemented by anything that wants to make a call to the server
 * @author Matt Roth
 *
 */
public interface JacksonRemotingCallListener {

	/**
	 * This method is called when the async request to the server is done
	 * @param theClass
	 * @param method
	 * @param result
	 */
	public void onRemotingCallFinished(String theClass, String method, Object result);
	public abstract Object getSystemService (String name);
	/**
	 * Must provided fully qualified url
	 * Ex. http://example.com/servlet/JacksonGateway
	 * @return
	 */
	public String urlForGateway();
}
