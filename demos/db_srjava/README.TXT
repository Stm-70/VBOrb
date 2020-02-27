This example is out of date!!!

Database server with IIOP interface written in Java
===================================================

To compile and execute this example you do need
- a JDBC driver
- a SQL database
- and JacORB or any other Java ORB.
To get JacORB see http://www.inf.fu-berlin.de/~brose/jacorb or
http://jacorb.inf.fu-berlin.de

To set the classpath in a UNIX environment edit kshenv and type
	. kshenv

To compile the IDL file you can type
	java org.jacorb.idl.parser database.idl

To compile the server you can type
	javac dbImpl/Server.java

Remember, you have to customize your Java platform in order to use
JacORB instead of JDK 1.2's ORB. To do that, you have to create a
Java properties file named orb.properties in your JAVA_HOME/lib
directory:
org.omg.CORBA.ORBClass=jacorb.orb.ORB
org.omg.CORBA.ORBSingletonClass=jacorb.orb.ORBSingleton

To start the server you can type
java dbImpl.Server ior.txt

WARNING: This Server is not perfect. Don't use it in a production
environment.

Example of connect IOR used by the client
corbaloc::remotehost:9999/DB/POA/Manager

Example of connect URL for the JDBC driver of Informix
jdbc:informix-sqli:remotehost:1525/stores7:informixserver=infserv


Directory structure
-------------------

vb/demo/db_srjava       This directory

  LIESMICH.TXT          This file in my native german language

  README.TXT            This file

  kshenv                UNIX Korn shell environment

  Makefile              UNIX makefile for the Java application

  dbImpl/               Written Java classes for the Java application

    Server.java         The class with the main method

  DB/                   Generated classes by the IDL2JAVA compiler
