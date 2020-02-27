// Callback server example (first compile client.idl and server.idl).
//
// If you start tnameserv on port 7809 for example you can use
//     oOrb.stringToObject("corbaname:::7809#CBServer")
// to get the Server object reference of this server in your Visual Basic application.
//
// JDK 1.4.0
//    - Compile by c:\j2sdk1.4.0\bin\javac -classpath . CBServer.java
//      (it also compiles CounterImpl.java CoordinatorImpl.java
//                        Server\*.java Client\*java)
//    - Starting tnameserv -ORBInitialPort 7809
//    - Calling c:\jsdk1.4.0\bin\java -classpath . CBServer -ORBInitialPort 7809

// All CORBA applications need these classes.
import org.omg.CORBA.*;
import org.omg.PortableServer.*;


import Client.*;				// The package containing our stubs.
import Server.*;				// The package containing our stubs.
import org.omg.CosNaming.*;		// CBServer will use the naming service.
import org.omg.CosNaming.NamingContextPackage.*;	// The package containing
								// special exceptions thrown by the name service.
import org.omg.CORBA.*;			// All CORBA applications need these classes.

public class CBServer
{
	public static ORB orb;
	public static POA rootPoa;
	
	public static void main(String args[])
	{
		try
		{	// Init ORB
			orb= ORB.init(args, null);
			
			// Get root POA
			rootPoa= POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

			// Create Servant
			CoordinatorImpl coordImpl= new CoordinatorImpl();

			// Get object reference from the servant
			org.omg.CORBA.Object objRef;
			objRef= rootPoa.servant_to_reference(coordImpl);
			Coordinator coordRef= CoordinatorHelper.narrow(objRef);
		
			// Get object reference from the root naming context
			objRef= orb.resolve_initial_references("NameService");
			NamingContext ncRef= NamingContextHelper.narrow(objRef);

			NameComponent nc= new NameComponent("CBServer", "");
			NameComponent path[]= { nc };
			ncRef.rebind(path, coordRef);
			
			// Activate root POA
			rootPoa.the_POAManager().activate();
			
			// Wait for requests
			// The object is activated by POA policy IMPLICIT_ACTIVATION
			orb.run();
		}catch(Exception e)
		{
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}
}
