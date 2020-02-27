package DB;
/**
 *	Generated from IDL definition of enum "DataType"
 *	@author JacORB IDL compiler 
 */

public final class DataType
	implements org.omg.CORBA.portable.IDLEntity
{
	private int value = -1;
	public static final int _TypeNull = 0;
	public static final DataType TypeNull = new DataType(_TypeNull);
	public static final int _TypeString = 1;
	public static final DataType TypeString = new DataType(_TypeString);
	public static final int _TypeShort = 2;
	public static final DataType TypeShort = new DataType(_TypeShort);
	public static final int _TypeInteger = 3;
	public static final DataType TypeInteger = new DataType(_TypeInteger);
	public static final int _TypeDouble = 4;
	public static final DataType TypeDouble = new DataType(_TypeDouble);
	public static final int _TypeDecimal = 5;
	public static final DataType TypeDecimal = new DataType(_TypeDecimal);
	public static final int _TypeTime = 6;
	public static final DataType TypeTime = new DataType(_TypeTime);
	public static final int _TypeDate = 7;
	public static final DataType TypeDate = new DataType(_TypeDate);
	public static final int _TypeDateTime = 8;
	public static final DataType TypeDateTime = new DataType(_TypeDateTime);
	public static final int _TypeText = 9;
	public static final DataType TypeText = new DataType(_TypeText);
	public static final int _TypeBinary = 10;
	public static final DataType TypeBinary = new DataType(_TypeBinary);
	public int value()
	{
		return value;
	}
	public static DataType from_int(int value)
	{
		switch (value) {
			case _TypeNull: return TypeNull;
			case _TypeString: return TypeString;
			case _TypeShort: return TypeShort;
			case _TypeInteger: return TypeInteger;
			case _TypeDouble: return TypeDouble;
			case _TypeDecimal: return TypeDecimal;
			case _TypeTime: return TypeTime;
			case _TypeDate: return TypeDate;
			case _TypeDateTime: return TypeDateTime;
			case _TypeText: return TypeText;
			case _TypeBinary: return TypeBinary;
			default: throw new org.omg.CORBA.BAD_PARAM();
		}
	}
	protected DataType(int i)
	{
		value = i;
	}
	java.lang.Object readResolve()
	throws java.io.ObjectStreamException
	{
		return from_int( value() );
	}
}
