Jackson-Mobile-Client-Server
============================

Provides a client for a Jackson Android client that can communicate with a Jackson Java servlet.

The client maintains a session with Java Servlet.  This requires that the class being called through the Java servlet has a parameter of HttpServletRequest

It handles JavaBean POJOs and will go into Maps and Lists and convert JavaBean POJOs in those as well.

###### To do
* Put Java servlet in a library with a xml config file
* Test Java servlet in working webapp

### How to use

#### Client
* Add the android-jackson-client.jar to your project.
* Have the activity implement JacksonRemotingCallListener and create the following methods
  * 	public void onRemotingCallFinished(String theClass, String method, Object result)
  * 	public String urlForGateway()
* Call JacksonRemotingCall.remotingCall(JacksonRemotingCallListener caller, String theClass, String method, ArrayList params)
  * theClass = "com.example.RemotingService"
  * theMethod = "reverseAndAppendStrings"
  * params = {"first","second"}
* When the call is finished onRemotingCallFinished will be called on the activity
  * theClass = "com.example.RemotingService"
  * method = "reverseStrings"
  * result = "tsrif_dnoces"
