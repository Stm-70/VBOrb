package DB;

import org.omg.PortableServer.POA;

/**
 *	Generated from IDL definition of interface "Statement"
 *	@author JacORB IDL compiler 
 */

public class StatementPOATie
	extends StatementPOA
{
	private StatementOperations _delegate;

	private POA _poa;
	public StatementPOATie(StatementOperations delegate)
	{
		_delegate = delegate;
	}
	public StatementPOATie(StatementOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public DB.Statement _this()
	{
		return DB.StatementHelper.narrow(_this_object());
	}
	public DB.Statement _this(org.omg.CORBA.ORB orb)
	{
		return DB.StatementHelper.narrow(_this_object(orb));
	}
	public StatementOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(StatementOperations delegate)
	{
		_delegate = delegate;
	}
	public void free(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.free(warns);
	}

	public int execute(DB.ColumnData[] using, DB.DataRowHolder row, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.execute(using,row,warns);
	}

	public int describe(DB.DescriptorRowHolder cols, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.describe(cols,warns);
	}

	public DB.Cursor declareCursor(boolean hold, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.declareCursor(hold,warns);
	}

}
