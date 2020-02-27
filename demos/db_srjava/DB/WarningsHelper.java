package DB;

/**
 *	Generated from IDL definition of alias "Warnings"
 *	@author JacORB IDL compiler 
 */

public final class WarningsHelper
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
			_type = org.omg.CORBA.ORB.init().create_alias_tc(DB.WarningsHelper.id(), "Warnings",org.omg.CORBA.ORB.init().create_sequence_tc(0, DB.WarningHelper.type() ) );
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:DB/Warnings:1.0";
	}
	public static DB.Message[] read (final org.omg.CORBA.portable.InputStream _in)
	{
		DB.Message[] _result;
		int _l_result0 = _in.read_long();
		_result = new DB.Message[_l_result0];
		for(int i=0;i<_result.length;i++)
		{
			_result[i] = DB.WarningHelper.read(_in);
		}

		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, DB.Message[] _s)
	{
		
		_out.write_long(_s.length);
		for( int i=0; i<_s.length;i++)
		{
			DB.WarningHelper.write(_out,_s[i]);
		}

	}
}
