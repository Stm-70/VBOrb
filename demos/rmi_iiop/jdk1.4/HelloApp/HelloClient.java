/* HelloClient.java
*/

package HelloApp;

import java.rmi.RemoteException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import javax.rmi.*;
import java.util.Vector;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;

public class HelloClient
{
	public static void main(String args[])
	{
		Context ic;
		Object objref;
		HelloInterface hi;

		try
		{	ic= new InitialContext();
		}catch(NamingException e)
		{
			System.out.println("failed to obtain context" + e);
			e.printStackTrace();
			return;
		}

		// STEP 1: Get the Object reference from the Name Service
		// using JNDI call.
		try
		{	objref= ic.lookup("HelloService");
			System.out.println("Client: Obtained a ref. to Hello server.");
		}catch(NamingException e)
		{
			System.out.println("failed to lookup object reference");
			e.printStackTrace();
			return;
		}

		// STEP 2: Narrow the object reference to the concrete type and
		// invoke the method.
		try
		{	hi= (HelloInterface) PortableRemoteObject.narrow(
				objref, HelloInterface.class);
				hi.sayHello();
		}catch(ClassCastException e)
		{	System.out.println("narrow failed");
			e.printStackTrace();
			return;
		}catch(Exception e)
		{
			System.err.println("Exception " + e + "Caught");
			e.printStackTrace();
			return;
		}
	}
}
