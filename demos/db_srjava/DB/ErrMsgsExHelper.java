package DB;


/**
 *	Generated from IDL definition of exception "ErrMsgsEx"
 *	@author JacORB IDL compiler 
 */

public final class ErrMsgsExHelper
{
	private static org.omg.CORBA.TypeCode _type = null;
	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_exception_tc( DB.ErrMsgsExHelper.id(),"ErrMsgsEx",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("errs", DB.ErrorsHelper.type(), null)});
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final DB.ErrMsgsEx s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static DB.ErrMsgsEx extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}

	public static String id()
	{
		return "IDL:DB/ErrMsgsEx:1.0";
	}
	public static DB.ErrMsgsEx read (final org.omg.CORBA.portable.InputStream in)
	{
		DB.ErrMsgsEx result = new DB.ErrMsgsEx();
		if(!in.read_string().equals(id())) throw new org.omg.CORBA.MARSHAL("wrong id");
		result.errs = DB.ErrorsHelper.read(in);
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final DB.ErrMsgsEx s)
	{
		out.write_string(id());
		DB.ErrorsHelper.write(out,s.errs);
	}
}
