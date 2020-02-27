package DB;


/**
 *	Generated from IDL definition of interface "Manager"
 *	@author JacORB IDL compiler 
 */

public class _ManagerStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements DB.Manager
{
	private String[] ids = {"IDL:DB/Manager:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	public final static java.lang.Class _opsClass = DB.ManagerOperations.class;
	public DB.Session getSession(java.lang.String url, java.lang.String user, java.lang.String password, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "getSession", true);
				_os.write_string(url);
				_os.write_string(user);
				_os.write_string(password);
				_is = _invoke(_os);
				DB.Session _result = DB.SessionHelper.read(_is);
				warns.value = DB.WarningsHelper.read(_is);
				return _result;
			}
			catch( org.omg.CORBA.portable.RemarshalException _rx ){}
			catch( org.omg.CORBA.portable.ApplicationException _ax )
			{
				String _id = _ax.getId();
				if( _id.equals("IDL:DB/ErrMsgsEx:1.0"))
				{
					throw DB.ErrMsgsExHelper.read(_ax.getInputStream());
				}
				else 
					throw new RuntimeException("Unexpected exception " + _id );
			}
			finally
			{
				this._releaseReply(_is);
			}
		}
		else
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "getSession", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			ManagerOperations _localServant = (ManagerOperations)_so.servant;
			DB.Session _result;			try
			{
			_result = _localServant.getSession(url,user,password,warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return _result;
		}

		}

	}

	public void shutdownServer(java.lang.String user, java.lang.String password) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "shutdownServer", true);
				_os.write_string(user);
				_os.write_string(password);
				_is = _invoke(_os);
				return;
			}
			catch( org.omg.CORBA.portable.RemarshalException _rx ){}
			catch( org.omg.CORBA.portable.ApplicationException _ax )
			{
				String _id = _ax.getId();
				if( _id.equals("IDL:DB/ErrMsgsEx:1.0"))
				{
					throw DB.ErrMsgsExHelper.read(_ax.getInputStream());
				}
				else 
					throw new RuntimeException("Unexpected exception " + _id );
			}
			finally
			{
				this._releaseReply(_is);
			}
		}
		else
		{
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "shutdownServer", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			ManagerOperations _localServant = (ManagerOperations)_so.servant;
			try
			{
			_localServant.shutdownServer(user,password);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

		}

	}

}
