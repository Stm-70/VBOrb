package DB;

import org.omg.PortableServer.POA;

/**
 *	Generated from IDL definition of interface "Manager"
 *	@author JacORB IDL compiler 
 */

public class ManagerPOATie
	extends ManagerPOA
{
	private ManagerOperations _delegate;

	private POA _poa;
	public ManagerPOATie(ManagerOperations delegate)
	{
		_delegate = delegate;
	}
	public ManagerPOATie(ManagerOperations delegate, POA poa)
	{
		_delegate = delegate;
		_poa = poa;
	}
	public DB.Manager _this()
	{
		return DB.ManagerHelper.narrow(_this_object());
	}
	public DB.Manager _this(org.omg.CORBA.ORB orb)
	{
		return DB.ManagerHelper.narrow(_this_object(orb));
	}
	public ManagerOperations _delegate()
	{
		return _delegate;
	}
	public void _delegate(ManagerOperations delegate)
	{
		_delegate = delegate;
	}
	public DB.Session getSession(java.lang.String url, java.lang.String user, java.lang.String password, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		return _delegate.getSession(url,user,password,warns);
	}

	public void shutdownServer(java.lang.String user, java.lang.String password) throws DB.ErrMsgsEx
	{
_delegate.shutdownServer(user,password);
	}

}
