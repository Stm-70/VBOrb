VB examples and VB test applications
====================================


Directory structure
-------------------

vb/demos/               This directory

  LIESMICH.TXT          This file in my native german language

  README.TXT            This file

  grid/                 Very simple example, client part

  grid_server/          Very simple example, server part

  hello/                An example where client and server are using
                        a CORBA Naming Service.

  db_query/             Example of a SQL client application accessing
                        a SQL database via IIOP interface.

  db_server/            Simulation of a SQL database written in VB
                        supporting an IIOP interface suitable for
                        the SQL client application.

  db_srjava/            Example of a server written in Java using JacORB
                        and JDBC to access a real SQL database by the
                        SQL client application.

  callback/             Example of an application where one or more clients
                        register at a server. The server is calling functions
                        of the clients which are calling functions of the
                        server.
                        This examples shows what is possible with CORBA
                        (even without real threads).

  obv/                  Object by value examples of OpenORB

  rmi_iiop/             Shows how a VB application can communicate with
                        a Java JDK 1.4 RMI/IIOP server. EJBs (Enterprise
                        Java Beans) are using the same technique.

  ejb/                  Hints for an application accessing EJBs
                        (Enterprise Java Beans).
