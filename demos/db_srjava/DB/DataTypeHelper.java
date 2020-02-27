package DB;
/**
 *	Generated from IDL definition of enum "DataType"
 *	@author JacORB IDL compiler 
 */

public final class DataTypeHelper
{
	private static org.omg.CORBA.TypeCode _type = null;
	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_enum_tc(DB.DataTypeHelper.id(),"DataType",new String[]{"TypeNull","TypeString","TypeShort","TypeInteger","TypeDouble","TypeDecimal","TypeTime","TypeDate","TypeDateTime","TypeText","TypeBinary"});
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final DB.DataType s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static DB.DataType extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}

	public static String id()
	{
		return "IDL:DB/DataType:1.0";
	}
	public static DataType read (final org.omg.CORBA.portable.InputStream in)
	{
		return DataType.from_int( in.read_long());
	}

	public static void write (final org.omg.CORBA.portable.OutputStream out, final DataType s)
	{
		out.write_long(s.value());
	}
}
