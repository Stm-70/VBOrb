package DB;

/**
 *	Generated from IDL definition of interface "Cursor"
 *	@author JacORB IDL compiler 
 */

public final class CursorHolder	implements org.omg.CORBA.portable.Streamable{
	 public Cursor value;
	public CursorHolder ()
	{
	}
	public CursorHolder (final Cursor initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return CursorHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = CursorHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		CursorHelper.write (_out,value);
	}
}
