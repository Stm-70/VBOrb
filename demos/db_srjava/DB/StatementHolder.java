package DB;

/**
 *	Generated from IDL definition of interface "Statement"
 *	@author JacORB IDL compiler 
 */

public final class StatementHolder	implements org.omg.CORBA.portable.Streamable{
	 public Statement value;
	public StatementHolder ()
	{
	}
	public StatementHolder (final Statement initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return StatementHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream in)
	{
		value = StatementHelper.read (in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		StatementHelper.write (_out,value);
	}
}
