package com.zur.remoting;

/**
 * This interface must implemented by anything that wants to make a call to the server
 * @author Matt Roth
 *
 */
public interface JacksonRemotingCallListener {

	public void onRemotingCallFinished(String theClass, String method, Object result);
	public abstract Object getSystemService (String name);
	public String urlForGateway();
}
