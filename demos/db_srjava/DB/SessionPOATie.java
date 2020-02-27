package DB;

import org.omg.PortableServer.POA;

/**
 *	Generated from IDL definition of interface "Session"
 *	@author JacORB IDL compiler 
 */

public class SessionPOATie
	extends SessionPOA
{
	private SessionOperations _delegate;

	private POA _poa;
	public SessionPOATie(SessionOperations delegate)
	{
		_delegate = delegate;
	}
	public SessionPOATie(SessionOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public DB.Session _this()
	{
		return DB.SessionHelper.narrow(_this_object());
	}
	public DB.Session _this(org.omg.CORBA.ORB orb)
	{
		return DB.SessionHelper.narrow(_this_object(orb));
	}
	public SessionOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(SessionOperations delegate)
	{
		_delegate = delegate;
	}
	public void destroy(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.destroy(warns);
	}

	public int executeStatement(java.lang.String stmstr, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.executeStatement(stmstr,warns);
	}

	public void rollback(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.rollback(warns);
	}

	public void close(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.close(warns);
	}

	public void commit(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.commit(warns);
	}

	public DB.Statement prepareStatement(java.lang.String stmstr, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.prepareStatement(stmstr,warns);
	}

	public void open(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
_delegate.open(warns);
	}

}
