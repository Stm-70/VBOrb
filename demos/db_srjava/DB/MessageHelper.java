package DB;


/**
 *	Generated from IDL definition of struct "Message"
 *	@author JacORB IDL compiler 
 */

public final class MessageHelper
{
	private static org.omg.CORBA.TypeCode _type = null;
	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_struct_tc( DB.MessageHelper.id(),"Message",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("sqlcode", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(3)), null),new org.omg.CORBA.StructMember("sqlstate", org.omg.CORBA.ORB.init().create_string_tc(5), null),new org.omg.CORBA.StructMember("sqlmessage", org.omg.CORBA.ORB.init().create_string_tc(255), null)});
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final DB.Message s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static DB.Message extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}

	public static String id()
	{
		return "IDL:DB/Message:1.0";
	}
	public static DB.Message read (final org.omg.CORBA.portable.InputStream in)
	{
		DB.Message result = new DB.Message();
		result.sqlcode=in.read_long();
		result.sqlstate=in.read_string();
		result.sqlmessage=in.read_string();
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final DB.Message s)
	{
		out.write_long(s.sqlcode);
		out.write_string(s.sqlstate);
		out.write_string(s.sqlmessage);
	}
}
