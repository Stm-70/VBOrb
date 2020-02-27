package DB;

/**
 *	Generated from IDL definition of alias "DataRows"
 *	@author JacORB IDL compiler 
 */

public final class DataRowsHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.ColumnData[][] value;

	public DataRowsHolder ()
	{
	}
	public DataRowsHolder (final DB.ColumnData[][] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return DataRowsHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = DataRowsHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		DataRowsHelper.write (out,value);
	}
}
