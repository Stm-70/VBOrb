package dbImpl;

import java.sql.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class ManagerImpl extends DB.ManagerPOA
{
	/**
	 */
	public DB.Connection getConnection(String url, String user, String password,
		DB.WarningsHolder warns) throws DB.Exception
	{
		ConnectionImpl conn= new ConnectionImpl(url, user, password);
		warns.value= conn.getAndClearWarnings();
		return conn.activate(Server.rootPoa);
	}
	
	/** Shutdown the server
	 */
	public void shutdownServer()
	{
		Server.orb.shutdown(false);
	}
}
