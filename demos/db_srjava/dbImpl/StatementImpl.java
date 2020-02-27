package dbImpl;

import java.sql.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class StatementImpl extends DB.StatementPOA
{
	/**
	 */
	private String stmStr;
	private PreparedStatement sqlStm;
	private POA poa;	// Statement POA
	public ResultSetMetaData sqlRsMd;
	
	/** constructor
	 */
	public StatementImpl(Connection sqlConn, String stmStr)
		throws DB.Exception
	{
		this.stmStr= stmStr;
		
		try
		{	sqlStm= sqlConn.prepareStatement(stmStr);
		}catch(SQLException sqlEx)
		{
			throw ConnectionImpl.transferExceptions(sqlEx);
		}
		System.out.println("I: New Statement");
	}

	/**
	 */
	protected void finalize()
	{	System.out.println("I: Del Statement");
	}

	/**
	 */
	protected DB.Warning[] getAndClearWarnings() throws DB.Exception
	{
		SQLWarning sqlWarns;
		try
		{	sqlWarns= sqlStm.getWarnings();
			sqlStm.clearWarnings();
		}catch(SQLException sqlEx)
		{
			throw ConnectionImpl.transferExceptions(sqlEx);
		}
		return ConnectionImpl.transferWarnings(sqlWarns);
	}

	/**
	 */
	public DB.Statement activate(POA poa) throws DB.Exception
	{
		this.poa= poa;
		try
		{	return DB.StatementHelper.narrow(
				poa.servant_to_reference(this));
		}catch(UserException userEx)
		{	throw new DB.Exception(-999, "ST900", "Unknown UserException");
			//org.omg.PortableServer.POAPackage.ServantNotActive
			//org.omg.PortableServer.POAPackage.WrongPolicy
		}
	}

	/**
	 */
	public void free(DB.WarningsHolder warns) throws DB.Exception
	{
		try
		{	sqlStm.close();
		}catch(SQLException sqlEx)
		{	throw ConnectionImpl.transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
		sqlStm= null;
		sqlRsMd= null;

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
	public int describe(DB.DescriptorRowHolder cols, DB.WarningsHolder warns)
		throws DB.Exception
	{
		if(sqlRsMd == null)
			throw new DB.Exception(-999, "ST900", "Use JDBC2.0 or execute first");
		try
		{	int numCols= sqlRsMd.getColumnCount();
			cols.value= new DB.ColumnDescriptor[numCols];
			for(int i= 0; i < numCols; i++)
			{	cols.value[i]= new DB.ColumnDescriptor(
					sqlRsMd.getColumnLabel(i+1), DB.DataType.TypeString,
					(short)0, (short)10, (short)0, (short)0, true);
			}
		}catch(SQLException sqlEx)
		{	throw ConnectionImpl.transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
		return 0;
	}
	
	/**
	 */
	public int execute(DB.ColumnData[] using, DB.DataRowHolder row,
		DB.WarningsHolder warns) throws DB.Exception
	{
		throw new DB.Exception(-999, "ST900", "Stm.execute not implemented");
	}
	
	/**
	 */
	public DB.Cursor declareCursor(boolean hold, DB.WarningsHolder warns)
		throws DB.Exception
	{
		CursorImpl curImpl= new CursorImpl(this, hold);
		warns.value= getAndClearWarnings();
		return curImpl.activate(Server.rootPoa);
	}

	/**
	 */
	public PreparedStatement getStatement()
	{	return sqlStm;
	}
}
