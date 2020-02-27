package DB;

/**
 *	Generated from IDL definition of interface "Statement"
 *	@author JacORB IDL compiler 
 */


public interface StatementOperations
{
	/* constants */
	/* operations  */
	int describe(DB.DescriptorRowHolder cols, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	int execute(DB.ColumnData[] using, DB.DataRowHolder row, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	DB.Cursor declareCursor(boolean hold, DB.WarningsHolder warns) throws DB.ErrMsgsEx;
	void free(DB.WarningsHolder warns) throws DB.ErrMsgsEx;
}
