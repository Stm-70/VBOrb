package DB;

/**
 *	Generated from IDL definition of interface "Session"
 *	@author JacORB IDL compiler 
 */

public final class SessionHelper
{
	public static void insert (final org.omg.CORBA.Any any, final DB.Session s)
	{
		any.insert_Object (s);
	}
	public static DB.Session extract (final org.omg.CORBA.Any any)
	{
		return narrow (any.extract_Object ());
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:DB/Session:1.0", "Session");
	}
	public static String id()
	{
		return "IDL:DB/Session:1.0";
	}
	public static Session read (final org.omg.CORBA.portable.InputStream in)
	{
		return narrow (in.read_Object ());
	}
	public static void write (final org.omg.CORBA.portable.OutputStream _out, final DB.Session s)
	{
		_out.write_Object(s);
	}
	public static DB.Session narrow (final org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (DB.Session)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:DB/Session:1.0"))
			{
				DB._SessionStub stub;
				stub = new DB._SessionStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
	}
}
