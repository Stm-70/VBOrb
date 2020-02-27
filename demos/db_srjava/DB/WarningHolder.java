package DB;

/**
 *	Generated from IDL definition of alias "Warning"
 *	@author JacORB IDL compiler 
 */

public final class WarningHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.Message value;

	public WarningHolder ()
	{
	}
	public WarningHolder (final DB.Message initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return WarningHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = WarningHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		WarningHelper.write (out,value);
	}
}
