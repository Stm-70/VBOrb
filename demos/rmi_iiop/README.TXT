Example of using RMI (of Java) via IIOP (of CORBA)
--------------------------------------------------

EJBs (Enterprise Java Beans) are using the same technique
for example. IIOP is better designed for very large distributed
systems than RMI or DCOM. One reason why CORBA and IIOP are
better for large systems is that object lifetimes are independent
of IIOP connection lifetimes. CORBA applications must therefore using
the evictor pattern instead of a global garbage collection.

In native CORBA you first write the IDL file. After that you
can generate your Java, C++ or VB files. This is the best way
of using CORBA.

Using RMI via IIOP you first write your Java interface. After
that you generate the IDL file(s). Finally you can generate
your C++ or VB files. The mapping of Java interfaces to
IDL interfaces results in very complex IDL types. Therefore
this is not the best way of using CORBA. This example shows
that this way is even possible to go with VBOrb.

Steps to test this example
--------------------------

Edit and start file /jdk1.4/StartNamingService.bat
Edit and start file /jdk1.4/StartHelloServer.bat

Edit and start file /jdk1.4/StartHelloClient.bat
to test the Java client.

Start file /HelloClient.vbp to test the VB client.


Important steps to rewrite this example again
---------------------------------------------
1.
Write the file /jdk1.4/HelloApp/HelloInterface.java
Write the file /jdk1.4/HelloApp/HelloImpl.java

2.
Edit and start file /jdk1.4/CompileHelloApp.bat

3.
Edit the file /jdk1.4/idl/HelloApp/HelloInterface.idl
to correct the forward declaration of the HashMap
custom valuetypes. (It is a bug in the JDK1.4 rmic).
Start file /CompileIDL2VB.bat to generate the VB files.

Correct #pragma prefix in org/omg/boxedRMI types.
Here is currently a problem in VBOrb!

4.
Write the file /c_javautilHashMap.cls to support a
custom value.

