Naming Service
==============

This is a full implementation of the CosNaming Service
in Visual Basic.

This naming service is not persistent. After restarting
the naming service all IOR entries are lost.

The naming service is running as an external process because
normally you will use a persistant naming service of an other
ORB also running as an external process.


To get a reference to the naming service in your client or server
you can use the following code:

Dim oOrb as cOrbImpl
Set oOrb = VBOrb.init(_
    ORBInitRef:="NameService=corbaloc::localhost:2809/NameService", _
    )

Dim oObject As cOrbObject
Set oObject = oOrb.resolveInitialReferences("NameService")
Dim oNmRootContext As c_NmContextExt
Set oNmRootContext = m_NmContextExt.narrow(oObject)


To compile the CosNaming.idl file you can type:

java -jar ../bin/IDL2VB.jar -S -Bgenerated CosNaming.idl

