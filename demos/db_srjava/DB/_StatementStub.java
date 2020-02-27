package DB;


/**
 *	Generated from IDL definition of interface "Statement"
 *	@author JacORB IDL compiler 
 */

public class _StatementStub
	extends org.omg.CORBA.portable.ObjectImpl
	implements DB.Statement
{
	private String[] ids = {"IDL:DB/Statement:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public String[] _ids()
	{
		return ids;
	}

	public final static java.lang.Class _opsClass = DB.StatementOperations.class;
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
			StatementOperations _localServant = (StatementOperations)_so.servant;
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

	public int execute(DB.ColumnData[] using, DB.DataRowHolder row, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "execute", true);
				DB.DataRowHelper.write(_os,using);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "execute", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			StatementOperations _localServant = (StatementOperations)_so.servant;
			int _result;			try
			{
			_result = _localServant.execute(using,row,warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return _result;
		}

		}

	}

	public int describe(DB.DescriptorRowHolder cols, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "describe", true);
				_is = _invoke(_os);
				int _result = _is.read_long();
				cols.value = DB.DescriptorRowHelper.read(_is);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "describe", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			StatementOperations _localServant = (StatementOperations)_so.servant;
			int _result;			try
			{
			_result = _localServant.describe(cols,warns);
			}
			finally
			{
				_servant_postinvoke(_so);
			}
			return _result;
		}

		}

	}

	public DB.Cursor declareCursor(boolean hold, DB.WarningsHolder warns) throws DB.ErrMsgsEx
	{
		while(true)
		{
		if(! this._is_local())
		{
			org.omg.CORBA.portable.InputStream _is = null;
			try
			{
				org.omg.CORBA.portable.OutputStream _os = _request( "declareCursor", true);
				_os.write_boolean(hold);
				_is = _invoke(_os);
				DB.Cursor _result = DB.CursorHelper.read(_is);
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
			org.omg.CORBA.portable.ServantObject _so = _servant_preinvoke( "declareCursor", _opsClass );
			if( _so == null )
				throw new org.omg.CORBA.UNKNOWN("local invocations not supported!");
			StatementOperations _localServant = (StatementOperations)_so.servant;
			DB.Cursor _result;			try
			{
			_result = _localServant.declareCursor(hold,warns);
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
