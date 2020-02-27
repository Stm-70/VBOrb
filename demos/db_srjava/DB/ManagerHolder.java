package DB;

/**
 *	Generated from IDL definition of interface "Manager"
 *	@author JacORB IDL compiler 
 */

public final class ManagerHolder	implements org.omg.CORBA.portable.Streamable{
	 public Manager value;
	public ManagerHolder ()
	{
	}
	public ManagerHolder (final Manager initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return ManagerHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = ManagerHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		ManagerHelper.write (_out,value);
	}
}
