package DB;

/**
 *	Generated from IDL definition of exception "ErrMsgsEx"
 *	@author JacORB IDL compiler 
 */

public final class ErrMsgsExHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.ErrMsgsEx value;

	public ErrMsgsExHolder ()
	{
	}
	public ErrMsgsExHolder (final DB.ErrMsgsEx initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return DB.ErrMsgsExHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream _in)
	{
		value = DB.ErrMsgsExHelper.read (_in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		DB.ErrMsgsExHelper.write (_out,value);
	}
}
