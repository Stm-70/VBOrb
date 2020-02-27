package DB;

/**
 *	Generated from IDL definition of struct "ColumnDescriptor"
 *	@author JacORB IDL compiler 
 */

public final class ColumnDescriptor
	implements org.omg.CORBA.portable.IDLEntity
{
	public ColumnDescriptor(){}
	public java.lang.String name = "";
	public DB.DataType type;
	public short sqltype;
	public short length;
	public short scale;
	public short precision;
	public boolean nullable;
	public ColumnDescriptor(java.lang.String name, DB.DataType type, short sqltype, short length, short scale, short precision, boolean nullable)
	{
		this.name = name;
		this.type = type;
		this.sqltype = sqltype;
		this.length = length;
		this.scale = scale;
		this.precision = precision;
		this.nullable = nullable;
	}
}
