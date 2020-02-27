// Hello client example (first compile Hello.idl).
//
// JDK 1.3.1
//    - Compile by c:\jdk1.3.1\bin\javac -classpath . HelloClient.java
//      (it also compiles HelloApp\*.java)
//    - Calling c:\jdk1.3.1\bin\java -classpath . HelloClient -ORBInitialPort 7809

import HelloApp.*;				// The package containing our stubs.
import org.omg.CosNaming.*;		// HelloClient will use the naming service.
import org.omg.CORBA.*;			// All CORBA applications need these classes.

public class HelloClient
{
	public static void main(String args[])
	{
		try
		{	ORB orb= ORB.init(args, null);
			org.omg.CORBA.Object objRef=
				orb.resolve_initial_references("NameService");

			NamingContext ncRef= NamingContextHelper.narrow(objRef);

			NameComponent nc= new NameComponent("Hello", "");

			NameComponent path[]= { nc };
			Hello helloRef= HelloHelper.narrow(ncRef.resolve(path));

			String hello= helloRef.sayHello();

			System.out.println(hello);
		}catch(Exception e)
		{
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}
}
