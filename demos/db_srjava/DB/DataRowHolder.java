package DB;

/**
 *	Generated from IDL definition of alias "DataRow"
 *	@author JacORB IDL compiler 
 */

public final class DataRowHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.ColumnData[] value;

	public DataRowHolder ()
	{
	}
	public DataRowHolder (final DB.ColumnData[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return DataRowHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = DataRowHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		DataRowHelper.write (out,value);
	}
}
