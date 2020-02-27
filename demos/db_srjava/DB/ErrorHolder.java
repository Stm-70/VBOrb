package DB;

/**
 *	Generated from IDL definition of alias "Error"
 *	@author JacORB IDL compiler 
 */

public final class ErrorHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.Message value;

	public ErrorHolder ()
	{
	}
	public ErrorHolder (final DB.Message initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return ErrorHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = ErrorHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		ErrorHelper.write (out,value);
	}
}
