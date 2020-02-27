package DB;

/**
 *	Generated from IDL definition of alias "DataRows"
 *	@author JacORB IDL compiler 
 */

public final class DataRowsHelper
{
	private static org.omg.CORBA.TypeCode _type = null;

	public static void insert (org.omg.CORBA.Any any, DB.ColumnData[][] s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}

	public static DB.ColumnData[][] extract (final org.omg.CORBA.Any any)
	{
		return read (any.create_input_stream ());
	}

	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_alias_tc(DB.DataRowsHelper.id(), "DataRows",org.omg.CORBA.ORB.init().create_sequence_tc(0, DB.DataRowHelper.type() ) );
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:DB/DataRows:1.0";
	}
	public static DB.ColumnData[][] read (final org.omg.CORBA.portable.InputStream _in)
	{
		DB.ColumnData[][] _result;
		int _l_result4 = _in.read_long();
		_result = new DB.ColumnData[_l_result4][];
		for(int i=0;i<_result.length;i++)
		{
			_result[i] = DB.DataRowHelper.read(_in);
		}

		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, DB.ColumnData[][] _s)
	{
		
		_out.write_long(_s.length);
		for( int i=0; i<_s.length;i++)
		{
			DB.DataRowHelper.write(_out,_s[i]);
		}

	}
}
