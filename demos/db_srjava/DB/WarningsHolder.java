package DB;

/**
 *	Generated from IDL definition of alias "Warnings"
 *	@author JacORB IDL compiler 
 */

public final class WarningsHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.Message[] value;

	public WarningsHolder ()
	{
	}
	public WarningsHolder (final DB.Message[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return WarningsHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = WarningsHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		WarningsHelper.write (out,value);
	}
}
