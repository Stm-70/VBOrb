package DB;

/**
 *	Generated from IDL definition of union "ColumnData"
 *	@author JacORB IDL compiler 
 */

public final class ColumnData
	implements org.omg.CORBA.portable.IDLEntity
{
	private DB.DataType discriminator;
	private byte[] binaryValue;
	private java.lang.String textValue;
	private double doubleValue;
	private int integerValue;
	private short shortValue;
	private java.lang.String stringValue;

	public ColumnData ()
	{
	}

	public DB.DataType discriminator ()
	{
		return discriminator;
	}

	public byte[] binaryValue ()
	{
		if( discriminator != DB.DataType.TypeBinary)
			throw new org.omg.CORBA.BAD_OPERATION();
		return binaryValue;
	}

	public void binaryValue (byte[] _x)
	{
		discriminator = DB.DataType.TypeBinary;
		binaryValue = _x;
	}

	public java.lang.String textValue ()
	{
		if( discriminator != DB.DataType.TypeText)
			throw new org.omg.CORBA.BAD_OPERATION();
		return textValue;
	}

	public void textValue (java.lang.String _x)
	{
		discriminator = DB.DataType.TypeText;
		textValue = _x;
	}

	public double doubleValue ()
	{
		if( discriminator != DB.DataType.TypeDouble)
			throw new org.omg.CORBA.BAD_OPERATION();
		return doubleValue;
	}

	public void doubleValue (double _x)
	{
		discriminator = DB.DataType.TypeDouble;
		doubleValue = _x;
	}

	public int integerValue ()
	{
		if( discriminator != DB.DataType.TypeInteger)
			throw new org.omg.CORBA.BAD_OPERATION();
		return integerValue;
	}

	public void integerValue (int _x)
	{
		discriminator = DB.DataType.TypeInteger;
		integerValue = _x;
	}

	public short shortValue ()
	{
		if( discriminator != DB.DataType.TypeShort)
			throw new org.omg.CORBA.BAD_OPERATION();
		return shortValue;
	}

	public void shortValue (short _x)
	{
		discriminator = DB.DataType.TypeShort;
		shortValue = _x;
	}

	public java.lang.String stringValue ()
	{
		if( discriminator != DB.DataType.TypeString)
			throw new org.omg.CORBA.BAD_OPERATION();
		return stringValue;
	}

	public void stringValue (java.lang.String _x)
	{
		discriminator = DB.DataType.TypeString;
		stringValue = _x;
	}

	public void __default ()
	{
		discriminator = DB.DataType.TypeNull;
	}
	public void __default (DB.DataType _discriminator)
	{
		discriminator = _discriminator;
	}
}
