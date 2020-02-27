package DB;


/**
 *	Generated from IDL definition of struct "ColumnDescriptor"
 *	@author JacORB IDL compiler 
 */

public final class ColumnDescriptorHelper
{
	private static org.omg.CORBA.TypeCode _type = null;
	public static org.omg.CORBA.TypeCode type ()
	{
		if( _type == null )
		{
			_type = org.omg.CORBA.ORB.init().create_struct_tc( DB.ColumnDescriptorHelper.id(),"ColumnDescriptor",new org.omg.CORBA.StructMember[]{new org.omg.CORBA.StructMember("name", org.omg.CORBA.ORB.init().create_string_tc(0), null),new org.omg.CORBA.StructMember("type", DB.DataTypeHelper.type(), null),new org.omg.CORBA.StructMember("sqltype", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)), null),new org.omg.CORBA.StructMember("length", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)), null),new org.omg.CORBA.StructMember("scale", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)), null),new org.omg.CORBA.StructMember("precision", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)), null),new org.omg.CORBA.StructMember("nullable", org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(8)), null)});
		}
		return _type;
	}

	public static void insert (final org.omg.CORBA.Any any, final DB.ColumnDescriptor s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static DB.ColumnDescriptor extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}

	public static String id()
	{
		return "IDL:DB/ColumnDescriptor:1.0";
	}
	public static DB.ColumnDescriptor read (final org.omg.CORBA.portable.InputStream in)
	{
		DB.ColumnDescriptor result = new DB.ColumnDescriptor();
		result.name=in.read_string();
		result.type=DB.DataTypeHelper.read(in);
		result.sqltype=in.read_short();
		result.length=in.read_short();
		result.scale=in.read_short();
		result.precision=in.read_short();
		result.nullable=in.read_boolean();
		return result;
	}
	public static void write (final org.omg.CORBA.portable.OutputStream out, final DB.ColumnDescriptor s)
	{
		out.write_string(s.name);
		DB.DataTypeHelper.write(out,s.type);
		out.write_short(s.sqltype);
		out.write_short(s.length);
		out.write_short(s.scale);
		out.write_short(s.precision);
		out.write_boolean(s.nullable);
	}
}
