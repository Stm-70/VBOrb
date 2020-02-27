package DB;

/**
 *	Generated from IDL definition of alias "DescriptorRow"
 *	@author JacORB IDL compiler 
 */

public final class DescriptorRowHelper
{
	private static org.omg.CORBA.TypeCode _type = null;

	public static void insert (org.omg.CORBA.Any any, DB.ColumnDescriptor[] s)
	{
		any.type (type ());
		write (any.create_output_stream (), s);
	}

	public static DB.ColumnDescriptor[] extract (final org.omg.CORBA.Any any)
	{
		return read (any.create_input_stream ());
	}

	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_alias_tc(DB.DescriptorRowHelper.id(), "DescriptorRow",org.omg.CORBA.ORB.init().create_sequence_tc(0, DB.ColumnDescriptorHelper.type() ) );
		}
		return _type;
	}

	public static String id()
	{
		return "IDL:DB/DescriptorRow:1.0";
	}
	public static DB.ColumnDescriptor[] read (final org.omg.CORBA.portable.InputStream _in)
	{
		DB.ColumnDescriptor[] _result;
		int _l_result5 = _in.read_long();
		_result = new DB.ColumnDescriptor[_l_result5];
		for(int i=0;i<_result.length;i++)
		{
			_result[i]=DB.ColumnDescriptorHelper.read(_in);
		}

		return _result;
	}

	public static void write (final org.omg.CORBA.portable.OutputStream _out, DB.ColumnDescriptor[] _s)
	{
		
		_out.write_long(_s.length);
		for( int i=0; i<_s.length;i++)
		{
			DB.ColumnDescriptorHelper.write(_out,_s[i]);
		}

	}
}
