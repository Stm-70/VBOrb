package DB;

/**
 *	Generated from IDL definition of struct "Message"
 *	@author JacORB IDL compiler 
 */

public final class MessageHolder
	implements org.omg.CORBA.portable.Streamable
{
	public DB.Message value;

	public MessageHolder ()
	{
	}
	public MessageHolder (final DB.Message initial)
	{
		value = initial;
	}
	public org.omg.CORBA.TypeCode _type ()
	{
		return DB.MessageHelper.type ();
	}
	public void _read (final org.omg.CORBA.portable.InputStream _in)
	{
		value = DB.MessageHelper.read (_in);
	}
	public void _write (final org.omg.CORBA.portable.OutputStream _out)
	{
		DB.MessageHelper.write (_out,value);
	}
}
