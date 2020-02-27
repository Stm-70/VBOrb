Example of using CORBA Naming Service
-------------------------------------

This is the simple `Hello' example of the Java JDK ORB ported to
Visual Basic.

The Hello example consists of 3 applications:

1. Any running COBRA Naming Service
   (Start `tnameserv -ORBInitialPort 2809' of JDK 1.3 or 1.4
   or start the naming\NameServ.exe of VBOrb for example)

2. A simple server written in Java or Visual Basic
   (Start HelloServer.bat or HelloServer.exe)

3. A simple client written in Java or Visual Basic
   (Start HelloClient.bat or HelloClient.exe)


Directory structure
-------------------

vb/demos/hello          This directory

  Hello.idl				File containing the interface description
                        between client and server

  HelloClient           Client example using Visual Basic and VBOrb
  HelloServer           Server example using Visual Basic and VBOrb
  
  jdk1.3/HelloClient    Client example using JDK 1.3.1 and his Java ORB
  jdk1.3/HelloServer    Server example using JDK 1.3.1 and his Java ORB

  jdk1.4/HelloClient    Client example using JDK 1.4 and his Java ORB
  jdk1.4/HelloServer    Server example using JDK 1.4 and his Java ORB
