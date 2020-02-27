package DB;
/**
 *	Generated from IDL definition of enum "DataType"
 *	@author JacORB IDL compiler 
 */

public final class DataTypeHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DataType value;

	public DataTypeHolder ()
	{
	}
	public DataTypeHolder (final DataType initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return DataTypeHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = DataTypeHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		DataTypeHelper.write (out,value);
	}
}
