package DB;

/**
 *	Generated from IDL definition of alias "Errors"
 *	@author JacORB IDL compiler 
 */

public final class ErrorsHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.Message[] value;

	public ErrorsHolder ()
	{
	}
	public ErrorsHolder (final DB.Message[] initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return ErrorsHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = ErrorsHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream out)
	{
		ErrorsHelper.write (out,value);
	}
}
