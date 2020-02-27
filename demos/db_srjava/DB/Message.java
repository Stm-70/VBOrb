package DB;

/**
 *	Generated from IDL definition of struct "Message"
 *	@author JacORB IDL compiler 
 */

public final class Message
	implements org.omg.CORBA.portable.IDLEntity
{
	public Message(){}
	public int sqlcode;
	public java.lang.String sqlstate = "";
	public java.lang.String sqlmessage = "";
	public Message(int sqlcode, java.lang.String sqlstate, java.lang.String sqlmessage)
	{
		this.sqlcode = sqlcode;
		this.sqlstate = sqlstate;
		this.sqlmessage = sqlmessage;
	}
}
