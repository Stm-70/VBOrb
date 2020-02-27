package DB;

/**
 *	Generated from IDL definition of interface "Manager"
 *	@author JacORB IDL compiler 
 */


public interface ManagerOperations
{
	/* constants */
	/* operations  */
	DB.Session getSession(java.lang.String url, java.lang.String user, java.lang.String password, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void shutdownServer(java.lang.String user, java.lang.String password) throws DB.ErrMsgsEx;
}
