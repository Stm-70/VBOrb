package DB;

/**
 *	Generated from IDL definition of union "ColumnData"
 *	@author JacORB IDL compiler 
 */

public final class ColumnDataHelper
{
	private static org.omg.CORBA.TypeCode _type;
	public static void insert (final org.omg.CORBA.Any any, final DB.ColumnData s)
	{
		any.type(type());
		write( any.create_output_stream(),s);
	}

	public static DB.ColumnData extract (final org.omg.CORBA.Any any)
	{
		return read(any.create_input_stream());
	}

	public static String id()
	{
		return "IDL:DB/ColumnData:1.0";
	}
	public static ColumnData read (org.omg.CORBA.portable.InputStream in)
	{
		ColumnData result = new ColumnData ();
		DB.DataType disc = DB.DataType.from_int(in.read_long());
		switch (disc.value ())
		{
			case DB.DataType._TypeBinary:
			{
				byte[] _var;
				int _l_var2 = in.read_long();
		_var = new byte[_l_var2];
	in.read_octet_array(_var,0,_l_var2);
				result.binaryValue (_var);
				break;
			}
			case DB.DataType._TypeText:
			{
				java.lang.String _var;
				_var=in.read_string();
				result.textValue (_var);
				break;
			}
			case DB.DataType._TypeDouble:
			{
				double _var;
				_var=in.read_double();
				result.doubleValue (_var);
				break;
			}
			case DB.DataType._TypeInteger:
			{
				int _var;
				_var=in.read_long();
				result.integerValue (_var);
				break;
			}
			case DB.DataType._TypeShort:
			{
				short _var;
				_var=in.read_short();
				result.shortValue (_var);
				break;
			}
			case DB.DataType._TypeString:
			{
				java.lang.String _var;
				_var=in.read_string();
				result.stringValue (_var);
				break;
			}
			default: result.__default (disc);
		}
		return result;
	}
	public static void write (org.omg.CORBA.portable.OutputStream out, ColumnData s)
	{
		out.write_long(s.discriminator().value());
		switch(s.discriminator().value())
		{
			case DB.DataType._TypeBinary:
			{
				
		out.write_long(s.binaryValue ().length);
		out.write_octet_array(s.binaryValue (),0,s.binaryValue ().length);
				break;
			}
			case DB.DataType._TypeText:
			{
				out.write_string(s.textValue ());
				break;
			}
			case DB.DataType._TypeDouble:
			{
				out.write_double(s.doubleValue ());
				break;
			}
			case DB.DataType._TypeInteger:
			{
				out.write_long(s.integerValue ());
				break;
			}
			case DB.DataType._TypeShort:
			{
				out.write_short(s.shortValue ());
				break;
			}
			case DB.DataType._TypeString:
			{
				out.write_string(s.stringValue ());
				break;
			}
		}
	}
	public static org.omg.CORBA.TypeCode type ()
	{
		if (_type == null)
		{
			org.omg.CORBA.UnionMember[] members = new org.omg.CORBA.UnionMember[6];
			org.omg.CORBA.Any label_any;
			label_any = org.omg.CORBA.ORB.init().create_any ();
			DB.DataTypeHelper.insert( label_any, DB.DataType.TypeBinary );
			members[0] = new org.omg.CORBA.UnionMember ("binaryValue", label_any, org.omg.CORBA.ORB.init().create_sequence_tc(0, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(10)) ),null);
			label_any = org.omg.CORBA.ORB.init().create_any ();
			DB.DataTypeHelper.insert( label_any, DB.DataType.TypeText );
			members[1] = new org.omg.CORBA.UnionMember ("textValue", label_any, org.omg.CORBA.ORB.init().create_string_tc(0),null);
			label_any = org.omg.CORBA.ORB.init().create_any ();
			DB.DataTypeHelper.insert( label_any, DB.DataType.TypeDouble );
			members[2] = new org.omg.CORBA.UnionMember ("doubleValue", label_any, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(7)),null);
			label_any = org.omg.CORBA.ORB.init().create_any ();
			DB.DataTypeHelper.insert( label_any, DB.DataType.TypeInteger );
			members[3] = new org.omg.CORBA.UnionMember ("integerValue", label_any, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(3)),null);
			label_any = org.omg.CORBA.ORB.init().create_any ();
			DB.DataTypeHelper.insert( label_any, DB.DataType.TypeShort );
			members[4] = new org.omg.CORBA.UnionMember ("shortValue", label_any, org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.from_int(2)),null);
			label_any = org.omg.CORBA.ORB.init().create_any ();
			DB.DataTypeHelper.insert( label_any, DB.DataType.TypeString );
			members[5] = new org.omg.CORBA.UnionMember ("stringValue", label_any, org.omg.CORBA.ORB.init().create_string_tc(0),null);
			 _type = org.omg.CORBA.ORB.init().create_union_tc(id(),"ColumnData",DB.DataTypeHelper.type(), members);
		}
		return _type;
	}
}
