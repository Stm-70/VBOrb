package DB;

/**
 *	Generated from IDL definition of interface "Statement"
 *	@author JacORB IDL compiler 
 */

public final class StatementHelper
{
	public static void insert (final org.omg.CORBA.Any any, final DB.Statement s)
	{
		any.insert_Object (s);
	}
	public static DB.Statement extract (final org.omg.CORBA.Any any)
	{
		return narrow (any.extract_Object ());
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		return org.omg.CORBA.ORB.init().create_interface_tc( "IDL:DB/Statement:1.0", "Statement");
	}
	public static String id()
	{
		return "IDL:DB/Statement:1.0";
	}
	public static Statement read (final org.omg.CORBA.portable.InputStream in)
	{
		return narrow (in.read_Object ());
	}
	public static void write (final org.omg.CORBA.portable.OutputStream _out, final DB.Statement s)
	{
		_out.write_Object(s);
	}
	public static DB.Statement narrow (final org.omg.CORBA.Object obj)
	{
		if( obj == null )
			return null;
		try
		{
			return (DB.Statement)obj;
		}
		catch( ClassCastException c )
		{
			if( obj._is_a("IDL:DB/Statement:1.0"))
			{
				DB._StatementStub stub;
				stub = new DB._StatementStub();
				stub._set_delegate(((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate());
				return stub;
			}
		}
		throw new org.omg.CORBA.BAD_PARAM("Narrow failed");
	}
}
