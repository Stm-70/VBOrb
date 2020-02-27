package DB;

/**
 *	Generated from IDL definition of alias "Errors"
 *	@author JacORB IDL compiler 
 */

public final class ErrorsHelper
{
	private static org.omg.CORBA.TypeCode _type = null;

	public static void insert (org.omg.CORBA.Any any, DB.Message[] s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}

	public static DB.Message[] extract (final org.omg.CORBA.Any any)
	{
		return read (any.create_input_stream ());
	}

	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_alias_tc(DB.ErrorsHelper.id(), "Errors",org.omg.CORBA.ORB.init().create_sequence_tc(0, DB.ErrorHelper.type() ) );
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:DB/Errors:1.0";
	}
	public static DB.Message[] read (final org.omg.CORBA.portable.InputStream _in)
	{
		DB.Message[] _result;
		int _l_result1 = _in.read_long();
		_result = new DB.Message[_l_result1];
		for(int i=0;i<_result.length;i++)
		{
			_result[i] = DB.ErrorHelper.read(_in);
		}

		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, DB.Message[] _s)
	{
		
		_out.write_long(_s.length);
		for( int i=0; i<_s.length;i++)
		{
			DB.ErrorHelper.write(_out,_s[i]);
		}

	}
}
