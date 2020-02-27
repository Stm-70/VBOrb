package DB;

/**
 *	Generated from IDL definition of interface "Manager"
 *	@author JacORB IDL compiler 
 */

public final class ManagerHelper
{
	public static void insert (final org.omg.CORBA.Any any, final DB.Manager s)
	{
		any.insert_Object (s);
	}
	public static DB.Manager extract (final org.omg.CORBA.Any any)
	{
		return narrow (any.extract_Object ());
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:DB/Manager:1.0", "Manager");
	}
	public static String id()
	{
		return "IDL:DB/Manager:1.0";
	}
	public static Manager read (final org.omg.CORBA.portable.InputStream in)
	{
		return narrow (in.read_Object ());
	}
	public static void write (final org.omg.CORBA.portable.OutputStream _out, final DB.Manager s)
	{
		_out.write_Object(s);
	}
	public static DB.Manager narrow (final org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (DB.Manager)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:DB/Manager:1.0"))
			{
				DB._ManagerStub stub;
				stub = new DB._ManagerStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
	}
}
