/* HelloServer.java
*/

package HelloApp;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject ;
import com.sun.corba.se.internal.POA.POAORB;
import org.omg.PortableServer.*;
import java.util.*;
import org.omg.CORBA.*;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

public class HelloServer
{
	public static void main(String[] args)
	{
		try
		{	Properties p= System.getProperties();
			// add runtime properties here
			p.put("org.omg.CORBA.ORBClass", 
			"com.sun.corba.se.internal.POA.POAORB");
			p.put("org.omg.CORBA.ORBSingletonClass", 
			"com.sun.corba.se.internal.corba.ORBSingleton");

			ORB orb= ORB.init(args, p);

			POA rootPOA= (POA)orb.resolve_initial_references("RootPOA");

			// STEP 1: Create a POA with the appropriate policies
			Policy[] tpolicy= new Policy[3];
			tpolicy[0]= rootPOA.create_lifespan_policy(
				LifespanPolicyValue.TRANSIENT );
			tpolicy[1]= rootPOA.create_request_processing_policy(
				RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY );
			tpolicy[2]= rootPOA.create_servant_retention_policy(
				ServantRetentionPolicyValue.RETAIN);
			POA tPOA= rootPOA.create_POA("MyTransientPOA", null, tpolicy);

			// STEP 2: Activate the POA Manager, otherwise all calls to the
			// servant hang because, by default, POAManager will be in the 
			// HOLD state.
			tPOA.the_POAManager().activate();

			// STEP 3: Instantiate the Servant and activate the Tie, If the
			// POA policy is USE_ACTIVE_OBJECT_MAP_ONLY
			HelloImpl helloImpl= new HelloImpl();
			_HelloImpl_Tie tie= (_HelloImpl_Tie)Util.getTie(helloImpl);
			String helloId= "hello";
			byte[] id= helloId.getBytes();
			tPOA.activate_object_with_id(id, tie);


			// STEP 4: Publish the object reference using the same object id
			// used to activate the Tie object.
			Context initialNamingContext= new InitialContext();
			initialNamingContext.rebind("HelloService", 
				tPOA.create_reference_with_id(id, 
				tie._all_interfaces(tPOA,id)[0]));
			System.out.println("Hello Server: Ready...");

			// STEP 5: Get ready to accept requests from the client
			orb.run();
		}catch (Exception e)
		{
			System.out.println("Problem running HelloServer: " + e);
			e.printStackTrace();
		}
	}
}
