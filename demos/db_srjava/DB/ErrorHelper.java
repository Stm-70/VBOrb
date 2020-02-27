package DB;

/**
 *	Generated from IDL definition of alias "Error"
 *	@author JacORB IDL compiler 
 */

public final class ErrorHelper
{
	private static org.omg.CORBA.TypeCode _type = null;

	public static void insert (org.omg.CORBA.Any any, DB.Message s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}

	public static DB.Message extract (final org.omg.CORBA.Any any)
	{
		return read (any.create_input_stream ());
	}

	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_alias_tc(DB.ErrorHelper.id(), "Error",DB.MessageHelper.type() );
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:DB/Error:1.0";
	}
	public static DB.Message read (final org.omg.CORBA.portable.InputStream _in)
	{
		DB.Message _result;
		_result=DB.MessageHelper.read(_in);
		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, DB.Message _s)
	{
		DB.MessageHelper.write(_out,_s);
	}
}
