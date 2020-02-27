package DB;

/**
 *	Generated from IDL definition of interface "Cursor"
 *	@author JacORB IDL compiler 
 */


public abstract class CursorPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, DB.CursorOperations
{
	static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "fetchSet", new java.lang.Integer(0));
		m_opsHash.put ( "open", new java.lang.Integer(1));
		m_opsHash.put ( "free", new java.lang.Integer(2));
		m_opsHash.put ( "close", new java.lang.Integer(3));
		m_opsHash.put ( "fetch", new java.lang.Integer(4));
	}
	private String[] ids = {"IDL:DB/Cursor:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public DB.Cursor _this()
	{
		return DB.CursorHelper.narrow(_this_object());
	}
	public DB.Cursor _this(org.omg.CORBA.ORB orb)
	{
		return DB.CursorHelper.narrow(_this_object(orb));
	}
	public org.omg.CORBA.portable.OutputStream _invoke(String method, org.omg.CORBA.portable.InputStream _input, org.omg.CORBA.portable.ResponseHandler handler)
		throws org.omg.CORBA.SystemException
	{
		org.omg.CORBA.portable.OutputStream _out = null;
		// do something
		// quick lookup of operation
		java.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );
		if ( null == opsIndex )
			throw new org.omg.CORBA.BAD_OPERATION(method + " not found");
		switch ( opsIndex.intValue() )
		{
			case 0: // fetchSet
			{
			try
			{
				int _arg0=_input.read_long();
				DB.DataRowsHolder _arg1= new DB.DataRowsHolder();
				DB.WarningsHolder _arg2= new DB.WarningsHolder();
				_out = handler.createReply();
				_out.write_long(fetchSet(_arg0,_arg1,_arg2));
				DB.DataRowsHelper.write(_out,_arg1.value);
				DB.WarningsHelper.write(_out,_arg2.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 1: // open
			{
			try
			{
				DB.ColumnData[] _arg0=DB.DataRowHelper.read(_input);
				DB.WarningsHolder _arg1= new DB.WarningsHolder();
				_out = handler.createReply();
				open(_arg0,_arg1);
				DB.WarningsHelper.write(_out,_arg1.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 2: // free
			{
			try
			{
				DB.WarningsHolder _arg0= new DB.WarningsHolder();
				_out = handler.createReply();
				free(_arg0);
				DB.WarningsHelper.write(_out,_arg0.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 3: // close
			{
			try
			{
				DB.WarningsHolder _arg0= new DB.WarningsHolder();
				_out = handler.createReply();
				close(_arg0);
				DB.WarningsHelper.write(_out,_arg0.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 4: // fetch
			{
			try
			{
				int _arg0=_input.read_long();
				DB.DataRowHolder _arg1= new DB.DataRowHolder();
				DB.WarningsHolder _arg2= new DB.WarningsHolder();
				_out = handler.createReply();
				_out.write_long(fetch(_arg0,_arg1,_arg2));
				DB.DataRowHelper.write(_out,_arg1.value);
				DB.WarningsHelper.write(_out,_arg2.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
		}
		return _out;
	}

	public String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] obj_id)
	{
		return ids;
	}
}
