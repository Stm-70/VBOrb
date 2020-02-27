Valuetype examples of OpenORB
=============================

These examples show you that valuetypes are interoperable with
OpenORB valuetypes.

A Visual Basic client using VBOrb can talk to a Java server
using OpenORB.
A Visual Basic client using VBOrb can talk to a Visual Basic
server.
A Java client using OpenORB can talk to a Visual Basic server
using VBOrb.

All client and servers are using then same IDL and the same
valuetype definitions.


Configuration to use OpenORB 1.2
--------------------------------
Remember, you have to customize your Java platform in order to use
OpenORB instead of JDK 1.2's ORB. One way to do that is to create a
Java properties file named orb.properties in your JAVA_HOME/lib
(e.g. /opt/java1.2/jre/lib) directory:
org.omg.CORBA.ORBClass=org.openorb.CORBA.ORB
org.omg.CORBA.ORBSingletonClass=org.openorb.CORBA.ORBSingleton

To set the classpath in a UNIX environment edit kshenv and type
. kshenv

Steps to use OpenORB as server
------------------------------
1. Start the OpenORB server
2. Take the IOR of the ObjectId file and put it into the VB application
3. Start the VBOrb client application
4. Call desired operation by button click

Steps to use OpenORB as client
------------------------------
1. Start the VBOrb server application
2. Write the IOR into the ObjectId file
3. Start the OpenORB client

Steps to use VBOrb as client and server
---------------------------------------
1. Start the VBOrb client/server application
2. Copy and paste the right IOR
3. Call desired operation by button click

Following OpenORB examples are tested:

Notice: You cannot build the VB executable of some examples
because I did not use #pragma vbname in IDL files to shorten
long IDL names. (You will get an error during building.)

- java obv.valuebox.Client
- java obv.valuebox.Server

- java obv.custom.Client
- java obv.custom.Server

- java obv.valuetype.Client
- java obv.valuetype.Server

- java obv.support_abstract.Client
- java obv.support_abstract.Server

- java obv.support_interface.Client
- java obv.support_interface.Server
