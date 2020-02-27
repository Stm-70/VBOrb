Callback example
----------------

This example shows many features of VBOrb. Although the applications
are called client and server the client is also server and the server
is also client in this example.

The server offers a counter interface and a coordinator interface.
Methods of the counter interface are called by clients and server.
Methods of the coordinator interface are called by the clients
to register/unregister the client so that the server knows the
clients and can call methods of the client control interface.


VBOrb
   - Create directory "generated"
   - Compile IDL "..\..\bin\idl2vb -S -Bgenerated client.idl"
   - Compile IDL "..\..\bin\idl2vb -S -Bgenerated server.idl"
   - Compile CBClient.exe "CBClient.vbp"
   - Compile CBServer.exe "CBServer.vbp"

JDK 1.4 ORB
   - Change current directory to java14
   - Compile IDL "c:\j2sdk1.4.0\bin\idlj -fall ..\client.idl"
   - Compile IDL "c:\j2sdk1.4.0\bin\idlj -fall -i ..\. ..\server.idl"
   - Compile client "javac -classpath . CBClient.java"
   - Compile server "javac -classpath . CBServer.java"


Directory structure
-------------------

vb/demos/callback       This directory

  README.TXT            This file

  client.idl            The control interface of the client application.
                        Operations of this interface are called by
                        the server to control all registered clients.

  server.idl            The interfaces of the server application.
                        The server is also using the counter interface of
                        itself to demonstrate collocation.

  CBClient.vbp          The client project to build CBClient.exe executable.
                        CBClient is using the VBOrb.DLL.

  CBClientGrp.vbp       The client project including VBOrb ActiveX-DLL so you
                        can debug the application including DLL. Normally you
                        don't need this.

  CBServer.vbp          The server project to build CBServer.exe executable.
                        CBServer is also using the VBOrb.DLL. The server is
                        using OrbRunLoopOutsideOfDLL() to avoid hanging
                        applications if you stop the application inside your
                        development environment (See vb/demos/callback/fServer.frm).

  CBServerGrp.vbp       The server project including VBOrb ActiveX-DLL so you
                        can debug the application including DLL. Normally you
                        don't need this.

  CBServer.log          The logging file of the running server to see what
                        the ORB is doing.

  fClient.frm           Source code of the client
  fServer.frm           Source code of the server

  generated/
    c_....cls           Generated .cls files by IDL2VB compiler
    m_....bas           Generated .bas files by IDL2VB compiler
