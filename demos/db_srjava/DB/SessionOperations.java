package DB;

/**
 *	Generated from IDL definition of interface "Session"
 *	@author JacORB IDL compiler 
 */


public interface SessionOperations
{
	/* constants */
	/* operations  */
	void open(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	int executeStatement(java.lang.String stmstr, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	DB.Statement prepareStatement(java.lang.String stmstr, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void commit(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void rollback(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void close(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void destroy(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
}
