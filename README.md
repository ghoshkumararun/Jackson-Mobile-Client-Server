Jackson-Mobile-Client-Server
============================

Provides a client for a Jackson Android client that can communicate with a Jackson Java servlet.

The client maintains a session with Java Servlet.  This requires that the class being called through the Java servlet has a parameter of HttpServletRequest

It handles JavaBean POJOs and will go into Maps and Lists and convert JavaBean POJOs in those as well.

You will need for Java Servlet and Android Client
* jackson-core-2.1.4.jar
* jackson-databind-2.1.4.jar
* jackson-annotations-2.1.4.jar

Download from http://wiki.fasterxml.com/JacksonDownload

You will need for Android Client
* commons-httpclient-3.1.jar
* commons-beanutils-1.8.3.jar
* commons-codec-1.8.jar
  
Download from https://hc.apache.org/downloads.cgi
