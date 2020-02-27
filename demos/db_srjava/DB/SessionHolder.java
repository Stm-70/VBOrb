package DB;

/**
 *	Generated from IDL definition of interface "Session"
 *	@author JacORB IDL compiler 
 */

public final class SessionHolder	implements org.omg.CORBA.portable.Streamable{
	 public Session value;
	public SessionHolder ()
	{
	}
	public SessionHolder (final Session initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return SessionHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = SessionHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		SessionHelper.write (_out,value);
	}
}
