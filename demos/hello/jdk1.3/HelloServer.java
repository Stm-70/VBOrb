// Hello server example (first compile Hello.idl).
//
// If you start tnameserv on port 7809 for example you can use
//     oOrb.stringToObject("corbaname:::7809#Hello")
// to get the Hello object reference of this server in your Visual Basic application.
//
// JDK 1.3.1
//    - Compile by c:\jdk1.3.1\bin\javac -classpath . HelloServer.java
//      (it also compiles HelloServant.java HelloApp\*.java)
//    - Starting tnameserv -ORBInitialPort 7809
//    - Calling c:\jdk1.3.1\bin\java -classpath . HelloServer -ORBInitialPort 7809

// All CORBA applications need these classes.
import org.omg.CORBA.*;


import HelloApp.*;				// The package containing our stubs.
import org.omg.CosNaming.*;		// HelloServer will use the naming service.
import org.omg.CosNaming.NamingContextPackage.*;	// The package containing
								// special exceptions thrown by the name service.
import org.omg.CORBA.*;			// All CORBA applications need these classes.

public class HelloServer
{
	public static void main(String args[])
	{
		try
		{	ORB orb= ORB.init(args, null);
			
			HelloServant helloRef= new HelloServant();
			orb.connect(helloRef);
		
			org.omg.CORBA.Object objRef=
				orb.resolve_initial_references("NameService");

			NamingContext ncRef= NamingContextHelper.narrow(objRef);

			NameComponent nc= new NameComponent("Hello", "");

			NameComponent path[]= { nc };

			ncRef.rebind(path, helloRef);
			
			java.lang.Object sync= new java.lang.Object();
			synchronized(sync)
			{	sync.wait();
			}
		}catch(Exception e)
		{
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}
}
