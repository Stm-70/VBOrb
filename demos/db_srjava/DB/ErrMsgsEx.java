package DB;

/**
 *	Generated from IDL definition of exception "ErrMsgsEx"
 *	@author JacORB IDL compiler 
 */

public final class ErrMsgsEx
	extends org.omg.CORBA.UserException
{
	public ErrMsgsEx()
	{
		super(DB.ErrMsgsExHelper.id());
	}

	public DB.Message[] errs;
	public ErrMsgsEx(java.lang.String _reason,DB.Message[] errs)
	{
		super(DB.ErrMsgsExHelper.id()+""+_reason );
		this.errs = errs;
	}
	public ErrMsgsEx(DB.Message[] errs)
	{
		this.errs = errs;
	}
}
