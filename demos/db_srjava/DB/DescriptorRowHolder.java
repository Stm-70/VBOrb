package DB;

/**
 *	Generated from IDL definition of alias "DescriptorRow"
 *	@author JacORB IDL compiler 
 */

public final class DescriptorRowHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.ColumnDescriptor[] value;

	public DescriptorRowHolder ()
	{
	}
	public DescriptorRowHolder (final DB.ColumnDescriptor[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return DescriptorRowHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = DescriptorRowHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		DescriptorRowHelper.write (out,value);
	}
}
