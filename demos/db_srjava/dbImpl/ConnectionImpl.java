package dbImpl;

import java.sql.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class ConnectionImpl extends DB.ConnectionPOA
{
	/**
	 */
	public static DB.Warning[] transferWarnings(SQLWarning sqlWarns)
	{
		int warnCnt;
		SQLWarning sqlWarn;
		DB.Warning[] result;
		
		for(warnCnt= 0, sqlWarn= sqlWarns; sqlWarn != null;
			warnCnt++, sqlWarn= sqlWarn.getNextWarning())
			;
		result= new DB.Warning[warnCnt];
		for(warnCnt= 0, sqlWarn= sqlWarns; sqlWarn != null;
			warnCnt++, sqlWarn= sqlWarn.getNextWarning())
		{
			String sqlState= sqlWarn.getSQLState();
			if(sqlState == null)
				sqlState= "";
			String sqlMessage= sqlWarn.getMessage();
			if(sqlMessage == null)
				sqlMessage= "";
			result[warnCnt]= new DB.Warning(sqlWarn.getErrorCode(), sqlState,
				sqlMessage);
		}
		return result;
	}

	/**
	 */
	public static void printWarnings(DB.Warning[] dbWarns)
	{
		for(int warnCnt= 0; warnCnt < dbWarns.length; warnCnt++)
		{	DB.Warning dbWarn= dbWarns[warnCnt];
			System.out.println("I: DB-Warning: sqlcode= " + dbWarn.sqlcode
				+ ", sqlstate= " + dbWarn.sqlstate
				+ ", sqlmessage= " + dbWarn.sqlmessage);
		}
	}

	/**
	 */
	public static DB.Exception transferExceptions(SQLException sqlEx)
	{
		DB.Exception result;
		
		// Only the first SQL-Exception???
		String sqlState= sqlEx.getSQLState();
		if(sqlState == null)
			sqlState= "";
		String sqlMessage= sqlEx.getMessage();
		if(sqlMessage == null)
			sqlMessage= "";
		result= new DB.Exception(sqlEx.getErrorCode(), sqlState, sqlMessage);
		return result;
	}
	
	/**
	 */
	public static void printExceptions(DB.Exception dbEx)
	{
		System.out.println("I: DB-Exception: sqlcode= " + dbEx.sqlcode
			+ ", sqlstate= " + dbEx.sqlstate
			+ ", sqlmessage= " + dbEx.sqlmessage);
	}

	/**
	 */
	private String url, user, password;
	private Connection sqlConn;
	private POA poa;	// Connection POA
	
	/** constructor
	 */
	public ConnectionImpl(String url, String user, String password)
		throws DB.Exception
	{
		this.url= url;
		this.user= user;
		this.password= password;
		
		try
		{	sqlConn= DriverManager.getConnection(url, user, password);
		}catch(SQLException sqlEx)
		{
			throw transferExceptions(sqlEx);
		}
		System.out.println("I: New Connection");
	}

	/**
	 */
	protected void finalize()
	{	System.out.println("I: Del Connection");
	}
	
	/**
	 */
	protected DB.Warning[] getAndClearWarnings() throws DB.Exception
	{
		SQLWarning sqlWarns;
		try
		{	sqlWarns= sqlConn.getWarnings();
			sqlConn.clearWarnings();
		}catch(SQLException sqlEx)
		{
			throw transferExceptions(sqlEx);
		}
		return transferWarnings(sqlWarns);
	}

	/**
	 */
	public DB.Connection activate(POA poa) throws DB.Exception
	{
		this.poa= poa;
		try
		{	return DB.ConnectionHelper.narrow(
				poa.servant_to_reference(this));
		}catch(UserException userEx)
		{	throw new DB.Exception(-999, "ST900", "Unknown UserException");
			//org.omg.PortableServer.POAPackage.ServantNotActive
			//org.omg.PortableServer.POAPackage.WrongPolicy
		}
	}

	/**
	 */
	public void close(DB.WarningsHolder warns) throws DB.Exception
	{
		try
		{	sqlConn.close();
		}catch(SQLException sqlEx)
		{	throw transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
		sqlConn= null;
		
		try
		{	poa.deactivate_object(poa.servant_to_id(this));
		}catch(UserException userEx)
		{	throw new DB.Exception(-999, "ST900", "Unknown UserException");
			//org.omg.PortableServer.POAPackage.ServantNotActive
			//org.omg.PortableServer.POAPackage.WrongPolicy
		}
		poa= null;
	}
	
	/**
	 */
	public int execute(java.lang.String stmstr, DB.WarningsHolder warns)
		throws DB.Exception
	{
		Statement sqlStm;
		try
		{	sqlStm= sqlConn.createStatement();
		}catch(SQLException sqlEx)
		{	throw transferExceptions(sqlEx);
		}
		int result;
		try
		{	result= sqlStm.executeUpdate(stmstr);
		}catch(SQLException sqlEx)
		{	try
			{	sqlStm.close();
			}catch(SQLException sqlEx2)
			{
			}
			throw transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
		return result;
	}
	
	/**
	 */
	public DB.Statement prepare(java.lang.String stmstr,
		DB.WarningsHolder warns) throws DB.Exception
	{
		StatementImpl stmImpl= new StatementImpl(sqlConn, stmstr);
		warns.value= stmImpl.getAndClearWarnings();
		return stmImpl.activate(Server.rootPoa);
	}
	
	/**
	 */
	public void commit(DB.WarningsHolder warns) throws DB.Exception
	{
		try
		{	sqlConn.commit();
		}catch(SQLException sqlEx)
		{	throw transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
	}
	
	/**
	 */
	public void rollback(DB.WarningsHolder warns) throws DB.Exception
	{
		try
		{	sqlConn.rollback();
		}catch(SQLException sqlEx)
		{	throw transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
	}
}
