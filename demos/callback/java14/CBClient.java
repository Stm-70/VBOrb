// Callback client example (first compile client.idl and server.idl).
//
// If you start tnameserv on port 7809 for example you can use
//     oOrb.stringToObject("corbaname:::7809#CBServer")
// to get the Server object reference
//
// JDK 1.4.0
//    - Compile by c:\j2sdk1.4.0\bin\javac -classpath . CBClient.java
//      (it also compiles ControlImpl.java Server\*.java Client\*java)
//    - Starting tnameserv -ORBInitialPort 7809
//    - Calling c:\jsdk1.4.0\bin\java -classpath . CBClient -ORBInitialPort 7809

// All CORBA applications need these classes.
import org.omg.CORBA.*;
import org.omg.PortableServer.*;


import Client.*;				// The package containing our stubs.
import Server.*;				// The package containing our stubs.
import org.omg.CosNaming.*;		// CBServer will use the naming service.
import org.omg.CosNaming.NamingContextPackage.*;	// The package containing
								// special exceptions thrown by the name service.
import org.omg.CORBA.*;			// All CORBA applications need these classes.

public class CBClient
{
	public static void main(String args[])
	{
		try
		{	// Init ORB
			ORB orb= ORB.init(args, null);
			
			// Get root POA
			POA rootPoa;
			rootPoa= POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

			org.omg.CORBA.Object objRef;

			// Get reference to server object
			Coordinator coordRef;
			if(args.length < 0 && args[0] != null && args[0].length() > 0)
			{	System.out.println("ServerURL= " + args[0]);
				objRef= orb.string_to_object(args[0]);
			}else
			{	// Get object reference from the root naming context
				objRef= orb.resolve_initial_references("NameService");
				NamingContext ncRef= NamingContextHelper.narrow(objRef);
	
				NameComponent nc= new NameComponent("CBServer", "");
				NameComponent path[]= { nc };
				objRef= ncRef.resolve(path);
			}
			coordRef= CoordinatorHelper.narrow(objRef);
			
			// Create Servant
			ControlImpl ctrlImpl= new ControlImpl();

			// Get object reference from the servant
			objRef= rootPoa.servant_to_reference(ctrlImpl);
			Control ctrlRef= ControlHelper.narrow(objRef);
		
			// Activate root POA
			rootPoa.the_POAManager().activate();
			
			// Wait for requests
			// The object is activated by POA policy IMPLICIT_ACTIVATION
			//orb.run();
			
			ctrlImpl.cntRef= coordRef.register(ctrlRef);
			ctrlImpl.cntRef.sum(4);
			System.out.println("Sum= " + ctrlImpl.cntRef.sum());
			System.out.println("Inc= " + ctrlImpl.cntRef.increment());
			coordRef.unregister(ctrlRef);
		}catch(Exception e)
		{
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}
}
