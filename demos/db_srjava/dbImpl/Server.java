package dbImpl;

import java.io.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;


public class Server 
{
	/**
	 */
	public static ORB orb;

	/**
	 */
	public static POA rootPoa;

	/**
	 */
	public static void main(String[] args) 
	{
		if(args.length != 1)
		{
			System.out.println("Usage: java dbImpl.Server IOR-Datei");
			System.exit(1);
		}
		System.out.println("I: Start of Server");
		String fileName= args[0];
		// Loading JDBC-Driver
		Class informixDriver;
		Class sybaseDriver;
		try
		{	informixDriver= Class.forName("com.informix.jdbc.IfxDriver");
			System.out.println("I: jdbc:informix-sqli");
		}catch(Exception e)
		{	informixDriver= null;
			System.out.println("ERROR: failed to load Informix JDBC driver.");
			e.printStackTrace();
		}
		try
		{	sybaseDriver= Class.forName("connect.sybase.SybaseDriver");
			System.out.println("I: jdbc:ff-sybase");
		}catch(Exception e)
		{	sybaseDriver= null;
			System.out.println("ERROR: failed to load Sybase JDBC driver.");
			e.printStackTrace();
		}
		try
		{
			/* turn off any output, otherwise it will garble up our
			   IOR file
			*/
			java.util.Properties props= new java.util.Properties();
			props.put("jacorb.implname","DB");
			props.put("OAPort","9999");
			//props.put("jacorb.verbosity","0");

			// Init ORB
			orb= ORB.init(args, props);

			// Get root POA
			rootPoa= POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

			//init new POA
			Policy[] policies= new Policy[2];
			policies[0]= rootPoa.create_id_assignment_policy(
				IdAssignmentPolicyValue.USER_ID);
			//property jacorb.implname must be set
			policies[1]= rootPoa.create_lifespan_policy(
				LifespanPolicyValue.PERSISTENT);
			POA managerPoa= rootPoa.create_POA("POA", // A no name POA
				rootPoa.the_POAManager(), policies);

			// Activate root POA
			rootPoa.the_POAManager().activate();
		
			// create a db manager object
			ManagerImpl managerImpl = new ManagerImpl();

			// Activate object
			byte[] objId= "Manager".getBytes();
			managerPoa.activate_object_with_id(objId, managerImpl);
			//org.omg.CORBA.Object obj= managerPoa.id_to_reference(objId);
			// create the object reference
			//org.omg.CORBA.Object obj= managerPoa.servant_to_reference(managerImpl);

			// print stringified object reference
			//System.out.println(orb.object_to_string(obj));
			//System.out.println("I: Writing IOR to '" + fileName + "'");
			//BufferedWriter iorWriter;
			//iorWriter= new BufferedWriter(new FileWriter(new File(fileName)));
			//iorWriter.write(orb.object_to_string(obj));
			//iorWriter.close();

			// Wait for requests
			orb.run();
		}catch(SystemException e)
		{
			System.out.println("ERROR: CORBA SystemException");
			e.printStackTrace();
		}catch(java.lang.Exception ie)
		{
			System.out.println("ERROR: Java Exception");
			ie.printStackTrace();
		}
		System.out.println("I: End of Server");
	}
}
