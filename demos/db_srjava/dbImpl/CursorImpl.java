package dbImpl;

import java.sql.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;

public class CursorImpl extends DB.CursorPOA
{
	/**
	 */
	private StatementImpl stmImpl;
	private boolean hold;
	private ResultSet sqlRSet;
	//private Cursor sqlConn;
	private POA poa;	// Cursor POA
	
	/** constructor
	 */
	public CursorImpl(StatementImpl stmImpl, boolean hold) throws DB.Exception
	{
		this.stmImpl= stmImpl;
		this.hold= hold;
		sqlRSet= null;
/*		try
		{	sqlConn= stmImpl.getStatement().getCursor(url, user, password);
		}catch(SQLException sqlEx)
		{
			throw ConnectionImpl.transferExceptions(sqlEx);
		}
**/		System.out.println("I: New Cursor");
	}

	/**
	 */
	protected void finalize()
	{	System.out.println("I: Del Cursor");
	}

	/**
	 */
	protected DB.Warning[] getAndClearWarnings() throws DB.Exception
	{
		SQLWarning sqlWarns;
		try
		{	sqlWarns= stmImpl.getStatement().getWarnings();
			stmImpl.getStatement().clearWarnings();
			if(sqlWarns == null && sqlRSet != null)
			{	sqlWarns= sqlRSet.getWarnings();
				sqlRSet.clearWarnings();
			}
		}catch(SQLException sqlEx)
		{
			throw ConnectionImpl.transferExceptions(sqlEx);
		}
		return ConnectionImpl.transferWarnings(sqlWarns);
	}

	/**
	 */
	public DB.Cursor activate(POA poa) throws DB.Exception
	{
		this.poa= poa;
		try
		{	return DB.CursorHelper.narrow(
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
		warns.value= getAndClearWarnings();

		try
		{	poa.deactivate_object(poa.servant_to_id(this));
		}catch(UserException userEx)
		{	throw new DB.Exception(-999, "ST900", "Unknown UserException");
			//org.omg.PortableServer.POAPackage.ServantNotActive
			//org.omg.PortableServer.POAPackage.WrongPolicy
		}
		stmImpl= null;
		poa= null;
	}
	
	/**
	 */
	public void open(DB.ColumnData[] using, DB.WarningsHolder warns)
		throws DB.Exception
	{
		// using mit den stmImpl.getStatement().setMethoden schreiben
		//??? using
System.out.println("I: Open Cursor using " + using.length);
		try
		{	sqlRSet= stmImpl.getStatement().executeQuery();
			if(stmImpl.sqlRsMd == null)
				stmImpl.sqlRsMd= sqlRSet.getMetaData();
		}catch(SQLException sqlEx)
		{	throw ConnectionImpl.transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
	}

	/**
	 */
	public void close(DB.WarningsHolder warns) throws DB.Exception
	{
System.out.println("I: Close Cursor");
		try
		{	sqlRSet.close();
		}catch(SQLException sqlEx)
		{	throw ConnectionImpl.transferExceptions(sqlEx);
		}
		warns.value= getAndClearWarnings();
		sqlRSet= null;
	}

	/**
	 */
	public int fetch(int func, DB.DataRowHolder row, DB.WarningsHolder warns)
		throws DB.Exception
	{
		int curState= 0;
		try
		{	if(sqlRSet.next())
			{	int numCols= stmImpl.sqlRsMd.getColumnCount();
				row.value= new DB.ColumnData[numCols];
				for(int i= 0; i < numCols; i++)
				{	row.value[i]= new DB.ColumnData();
					String rowValue= sqlRSet.getString(i+1);
					if(rowValue != null)
						row.value[i].stringValue(rowValue);
					else
						row.value[i].__default(DB.DataType.TypeNull);
				}
			}else
			{	row.value= new DB.ColumnData[0];
				curState= 100;
			}
		}catch(SQLException sqlEx)
		{	throw ConnectionImpl.transferExceptions(sqlEx);
		}catch(java.lang.Exception ex)
		{	new DB.Exception(-99, "ZZZZZ", ex.getMessage());
		}
		warns.value= getAndClearWarnings();
		return curState;
	}

	/**
	 */
	public int fetchSet(int rowcnt, DB.DataRowsHolder rows,
		DB.WarningsHolder warns) throws DB.Exception
	{
		throw new DB.Exception(-999, "ST900", "Cur.fetchSet not implemented");
	}
}
