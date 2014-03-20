Jackson-Mobile-Client-Server
============================

Provides a client for a Jackson Android client that can communicate with a Jackson Java servlet.

The client maintains a session with Java Servlet.  This requires that the class being called through the Java servlet has a parameter of HttpServletRequest

It handles JavaBean POJOs and will go into Maps and Lists and convert JavaBean POJOs in those as well.

###### To do
* Put Java servlet in a library with a xml config file
* Test Java servlet in working webapp
