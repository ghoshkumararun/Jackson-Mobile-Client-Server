package com.zur.remoting;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


/**
 * Credit for this class goes to Jason Hudgins 
 * http://foo.jasonhudgins.com/2009/08/http-connection-reuse-in-android.html
 * 
 * Keeps same session id by only creating a httpclient once
 * @author Matt Roth
 *
 */
public class HttpClientFactory {

    private static DefaultHttpClient client;

    public synchronized static DefaultHttpClient getThreadSafeClient() {
  
        if (client != null)
            return client;

        HttpParams httpParams = new BasicHttpParams();
        
        int timeout = 5000; // in milliseconds
        
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        
        client = new DefaultHttpClient(httpParams);
        
        ClientConnectionManager mgr = client.getConnectionManager();

        HttpParams params = client.getParams();
        client = new DefaultHttpClient(
        new ThreadSafeClientConnManager(params,
            mgr.getSchemeRegistry()), params);
  
        return client;
    } 
}
