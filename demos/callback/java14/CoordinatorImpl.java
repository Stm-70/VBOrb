// Part of server example (compile together with CBServer.java).

import java.util.*;

import Server.*;				// The package containing our skeleton.

public class CoordinatorImpl extends CoordinatorPOA
{
	public int iClientId;
	public HashMap hmClients= new HashMap();

	public CounterImpl counterImpl;
	public Counter counterRef;

	public CoordinatorImpl()
	{
		try
		{	counterImpl= new CounterImpl();
			org.omg.CORBA.Object objRef;
			objRef= CBServer.rootPoa.servant_to_reference(counterImpl);
			counterRef= CounterHelper.narrow(objRef);
			//CBServer.rootPoa.activate_object_with_id(
			//	"Counter".getBytes(), counterImpl);
			// counterRef= counterImpl._this();
		}catch(Exception e)
		{	System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}
	}
	
	/** (CoordinatorOperations)
	 */
	public Server.Counter register(Client.Control clientObjRef)
	{
		//Big callback to client
		System.out.println("register, send wstring, send big array");
		clientObjRef.sendBigArrayToClient(new byte[4096]);

		iClientId++;
		hmClients.put(new Integer(iClientId), clientObjRef);
		System.out.println("Client " + iClientId + " registered");
		//Callback to client
		clientObjRef.setId(iClientId);
		System.out.println("clientObjRef= "
			+ CBServer.orb.object_to_string(clientObjRef));
		clientObjRef.sendWStringToClient("Hallo Client");
		return counterRef;
	}

	/** (CoordinatorOperations)
	 */
	public void unregister(Client.Control clientObjRef)
	{
		int iClId= clientObjRef.getId();
		hmClients.remove(new Integer(iClId));
		System.out.println("Client " + iClId + " unregistered");
	}
}
