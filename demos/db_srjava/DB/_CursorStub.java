package DB;


/**
 *	Generated from IDL definition of interface "Cursor"
 *	@author JacORB IDL compiler 
 */

public class _CursorStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements DB.Cursor
{
	private String[] ids = {"IDL:DB/Cursor:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	public final static java.lang.Class _opsClass = DB.CursorOperations.class;
	public int fetchSet(int rowcnt, DB.DataRowsHolder rows, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "fetchSet", true);
				_os.write_long(rowcnt);
				_is = _invoke(_os);
				int _result = _is.read_long();
				rows.value = DB.DataRowsHelper.read(_is);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "fetchSet", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			CursorOperations _localServant = (CursorOperations)_so.servant;
			int _result;			try
			{
			_result = _localServant.fetchSet(rowcnt,rows,warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return _result;
		}

		}

	}

	public void open(DB.ColumnData[] using, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "open", true);
				DB.DataRowHelper.write(_os,using);
				_is = _invoke(_os);
				warns.value = DB.WarningsHelper.read(_is);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "open", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			CursorOperations _localServant = (CursorOperations)_so.servant;
			try
			{
			_localServant.open(using,warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

		}

	}

	public void free(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "free", true);
				_is = _invoke(_os);
				warns.value = DB.WarningsHelper.read(_is);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "free", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			CursorOperations _localServant = (CursorOperations)_so.servant;
			try
			{
			_localServant.free(warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

		}

	}

	public void close(DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "close", true);
				_is = _invoke(_os);
				warns.value = DB.WarningsHelper.read(_is);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "close", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			CursorOperations _localServant = (CursorOperations)_so.servant;
			try
			{
			_localServant.close(warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return;
		}

		}

	}

	public int fetch(int func, DB.DataRowHolder row, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "fetch", true);
				_os.write_long(func);
				_is = _invoke(_os);
				int _result = _is.read_long();
				row.value = DB.DataRowHelper.read(_is);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "fetch", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			CursorOperations _localServant = (CursorOperations)_so.servant;
			int _result;			try
			{
			_result = _localServant.fetch(func,row,warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return _result;
		}

		}

	}

}
