package DB;

/**
 *	Generated from IDL definition of interface "Manager"
 *	@author JacORB IDL compiler 
 */


public abstract class ManagerPOA
	extends org.omg.PortableServer.Servant
	implements org.omg.CORBA.portable.InvokeHandler, DB.ManagerOperations
{
	static private final java.util.Hashtable m_opsHash = new java.util.Hashtable();
	static
	{
		m_opsHash.put ( "getSession", new java.lang.Integer(0));
		m_opsHash.put ( "shutdownServer", new java.lang.Integer(1));
	}
	private String[] ids = {"IDL:DB/Manager:1.0","IDL:omg.org/CORBA/Object:1.0"};
	public DB.Manager _this()
	{
		return DB.ManagerHelper.narrow(_this_object());
	}
	public DB.Manager _this(org.omg.CORBA.ORB orb)
	{
		return DB.ManagerHelper.narrow(_this_object(orb));
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
			case 0: // getSession
			{
			try
			{
				java.lang.String _arg0=_input.read_string();
				java.lang.String _arg1=_input.read_string();
				java.lang.String _arg2=_input.read_string();
				DB.WarningsHolder _arg3= new DB.WarningsHolder();
				_out = handler.createReply();
				DB.SessionHelper.write(_out,getSession(_arg0,_arg1,_arg2,_arg3));
				DB.WarningsHelper.write(_out,_arg3.value);
			}
			catch(DB.ErrMsgsEx _ex0)
			{
				_out = handler.createExceptionReply();
				DB.ErrMsgsExHelper.write(_out, _ex0);
			}
				break;
			}
			case 1: // shutdownServer
			{
			try
			{
				java.lang.String _arg0=_input.read_string();
				java.lang.String _arg1=_input.read_string();
				_out = handler.createReply();
				shutdownServer(_arg0,_arg1);
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
