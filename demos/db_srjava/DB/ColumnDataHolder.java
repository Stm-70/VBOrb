package DB;
/**
 *	Generated from IDL definition of union "ColumnData"
 *	@author JacORB IDL compiler 
 */

public final class ColumnDataHolder
	implements org.omg.CORBA.portable.Streamable
{
	public ColumnData value;

	public ColumnDataHolder ()
	{
	}
	public ColumnDataHolder (final ColumnData initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return ColumnDataHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = ColumnDataHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		ColumnDataHelper.write (out, value);
	}
}
