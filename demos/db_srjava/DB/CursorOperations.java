package DB;

/**
 *	Generated from IDL definition of interface "Cursor"
 *	@author JacORB IDL compiler 
 */


public interface CursorOperations
{
	/* constants */
	/* operations  */
	void open(DB.ColumnData[] using, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	int fetch(int func, DB.DataRowHolder row, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	int fetchSet(int rowcnt, DB.DataRowsHolder rows, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void close(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void free(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
}
