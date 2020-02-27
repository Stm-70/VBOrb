package DB;

/**
 *	Generated from IDL definition of interface "Session"
 *	@author JacORB IDL compiler 
 */


public abstract class SessionPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, DB.SessionOperations
{
	static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "destroy", new java.lang.Integer(0));
		m_opsHash.put ( "executeStatement", new java.lang.Integer(1));
		m_opsHash.put ( "rollback", new java.lang.Integer(2));
		m_opsHash.put ( "close", new java.lang.Integer(3));
		m_opsHash.put ( "commit", new java.lang.Integer(4));
		m_opsHash.put ( "prepareStatement", new java.lang.Integer(5));
		m_opsHash.put ( "open", new java.lang.Integer(6));
	}
	private String[] ids = {"IDL:DB/Session:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public DB.Session _this()
	{
		return DB.SessionHelper.narrow(_this_object());
	}
	public DB.Session _this(org.omg.CORBA.ORB orb)
	{
		return DB.SessionHelper.narrow(_this_object(orb));
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
			case 0: // destroy
			{
			try
			{
				DB.WarningsHolder _arg0= new DB.WarningsHolder();
				_out = handler.createReply();
				destroy(_arg0);
				DB.WarningsHelper.write(_out,_arg0.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 1: // executeStatement
			{
			try
			{
				java.lang.String _arg0=_input.read_string();
				DB.WarningsHolder _arg1= new DB.WarningsHolder();
				_out = handler.createReply();
				_out.write_long(executeStatement(_arg0,_arg1));
				DB.WarningsHelper.write(_out,_arg1.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 2: // rollback
			{
			try
			{
				DB.WarningsHolder _arg0= new DB.WarningsHolder();
				_out = handler.createReply();
				rollback(_arg0);
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
			case 4: // commit
			{
			try
			{
				DB.WarningsHolder _arg0= new DB.WarningsHolder();
				_out = handler.createReply();
				commit(_arg0);
				DB.WarningsHelper.write(_out,_arg0.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 5: // prepareStatement
			{
			try
			{
				java.lang.String _arg0=_input.read_string();
				DB.WarningsHolder _arg1= new DB.WarningsHolder();
				_out = handler.createReply();
				DB.StatementHelper.write(_out,prepareStatement(_arg0,_arg1));
				DB.WarningsHelper.write(_out,_arg1.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 6: // open
			{
			try
			{
				DB.WarningsHolder _arg0= new DB.WarningsHolder();
				_out = handler.createReply();
				open(_arg0);
				DB.WarningsHelper.write(_out,_arg0.value);
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
