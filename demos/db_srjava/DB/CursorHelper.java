package DB;

/**
 *	Generated from IDL definition of interface "Cursor"
 *	@author JacORB IDL compiler 
 */

public final class CursorHelper
{
	public static void insert (final org.omg.CORBA.Any any, final DB.Cursor s)
	{
		any.insert_Object (s);
	}
	public static DB.Cursor extract (final org.omg.CORBA.Any any)
	{
		return narrow (any.extract_Object ());
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:DB/Cursor:1.0", "Cursor");
	}
	public static String id()
	{
		return "IDL:DB/Cursor:1.0";
	}
	public static Cursor read (final org.omg.CORBA.portable.InputStream in)
	{
		return narrow (in.read_Object ());
	}
	public static void write (final org.omg.CORBA.portable.OutputStream _out, final DB.Cursor s)
	{
		_out.write_Object(s);
	}
	public static DB.Cursor narrow (final org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (DB.Cursor)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:DB/Cursor:1.0"))
			{
				DB._CursorStub stub;
				stub = new DB._CursorStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
	}
}
