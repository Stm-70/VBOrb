package DB;

import org.omg.PortableServer.POA;

/**
 *	Generated from IDL definition of interface "Cursor"
 *	@author JacORB IDL compiler 
 */

public class CursorPOATie
	extends CursorPOA
{
	private CursorOperations _delegate;

	private POA _poa;
	public CursorPOATie(CursorOperations delegate)
	{
		_delegate = delegate;
	}
	public CursorPOATie(CursorOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public DB.Cursor _this()
	{
		return DB.CursorHelper.narrow(_this_object());
	}
	public DB.Cursor _this(org.omg.CORBA.ORB orb)
	{
		return DB.CursorHelper.narrow(_this_object(orb));
	}
	public CursorOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(CursorOperations delegate)
	{
		_delegate = delegate;
	}
	public int fetchSet(int rowcnt, DB.DataRowsHolder rows, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.fetchSet(rowcnt,rows,warns);
	}

	public void open(DB.ColumnData[] using, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.open(using,warns);
	}

	public void free(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.free(warns);
	}

	public void close(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.close(warns);
	}

	public int fetch(int func, DB.DataRowHolder row, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.fetch(func,row,warns);
	}

}
