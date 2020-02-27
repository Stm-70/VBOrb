/*
   Copyright (c) 1999 Martin.Both

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public
   License as published by the Free Software Foundation; either
   version 2 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   Library General Public License for more details.
*/

package mboth.util;

/**
 * @author  Martin Both
 */
public class TxtTokString extends TxtToken
{
	/**
	 */
	public static TxtTokString readTxtTokString(TxtTokenReader tr,
		String name) throws TxtReadException
	{	TxtToken tt= tr.readToken();
		if(!(tt instanceof TxtTokString))
		{	StringBuffer sBuf= new StringBuffer();
			if(name == null)
				sBuf.append("String");
			else
				sBuf.append(name);
			sBuf.append(" expected: ");
			sBuf.append(tt.toString());
			throw new TxtReadException(tt.getFilePos(), sBuf.toString());
		}
		return (TxtTokString)tt;
	}
	
	/**
	 */
	private char boundary;
	private String value;

	/** 
	 *  @param	filepos		Filename and the start position
	 *  @param	flags		Token attributes
	 *  @param	boundary	Token boundary; "...", '...'
	 *  @param	str			Token value
	 */
	public TxtTokString(TxtFilePos filepos, int flags, char boundary,
		String str)
	{
		super(filepos, flags);
		this.boundary= boundary;
		this.value= str;
	}
		
	/** Die Position wird nicht verglichen 
	 */
	public boolean equals(Object obj)
	{
		if(!super.equals(obj) || !(obj instanceof TxtTokString))
			return false;
		TxtTokString token= (TxtTokString)obj;
		return value.equals(token.value) && token.boundary == boundary;
	}

	/** 
	 */
	public String toString()
	{
		return super.toString() + boundary + value + boundary;
	}

	/**
	 */
	public char getBoundary()
	{
		return boundary;
	}

	/**
	 */
	public String getOriginalStr()
	{
		return value;
	}

	/** Der normale Wert.
	 *  (Auch aufzurufen bevor benachbarte Zeichenketten zusammengesetzt werden.)
	 */
	public String getUnEscString() throws TxtReadException
	{
		int escStart= value.indexOf('\\');
		if(escStart < 0)
			return value;
		int strLen= value.length();
		StringBuffer sb= new StringBuffer(strLen);
		int strStart= 0;
		do
		{	if(escStart - strStart > 0)
				sb.append(value.substring(strStart, escStart));
			strStart= escStart + 1;
			if(strStart >= strLen)
			{	throw new TxtReadException(getFilePos(),
					"Escape sequence `\\?' character missing");
			}
			char ch= value.charAt(strStart);
			if(ch == 'n')			// newline
				ch= '\n';
			else if(ch == 'r')		// carriage return
				ch= '\r';
			else if(ch == 't')		// horizontal tab
				ch= '\t';
			else if(ch == 'v')		// vertical tab
				ch= '\013';
			else if(ch == 'b')		// backspace
				ch= '\b';
			else if(ch == 'f')		// form feed
				ch= '\f';
			else if(ch == 'a')		// alert
				ch= '\007';
			else if(ch >= '0' && ch <= '7')		// octal number '\07', '\077'
			{	int digit= Character.digit(value.charAt(strStart), 8);
				if(digit < 0)
				{	throw new TxtReadException(getFilePos(),
						"Escape sequence `\\x?' octal character expected");
				}
				int escval= digit;
				for(int digs= 2; digs > 0 && strStart + 1 < strLen; digs--)
				{	digit= Character.digit(value.charAt(strStart + 1), 8);
					if(digit < 0)
						break;
					strStart++;
					escval= escval * 8 + digit;
				}
				ch= (char)escval;
			}else if(ch == 'x')		// hexadecimal number '\xF', '\xFF'
			{	strStart++;
				if(strStart >= strLen)
				{	throw new TxtReadException(getFilePos(),
						"Escape sequence `\\x?' hex character missing");
				}
				int digit= Character.digit(value.charAt(strStart), 16);
				if(digit < 0)
				{	throw new TxtReadException(getFilePos(),
						"Escape sequence `\\x?' hex character expected");
				}
				int escval= digit;
				if(strStart + 1 < strLen)
				{	digit= Character.digit(value.charAt(strStart + 1), 16);
					if(digit >= 0)
					{	strStart++;
						escval= escval * 16 + digit;
					}
				}
				ch= (char)escval;
			}else if(ch == 'u')		// unicode character '\u000F', '\u00FF', ...
			{		strStart++;
				if(strStart >= strLen)
				{	throw new TxtReadException(getFilePos(),
						"Escape sequence `\\x?' octal character missing");
				}
				int digit= Character.digit(value.charAt(strStart), 16);
				if(digit < 0)
				{	throw new TxtReadException(getFilePos(),
						"Escape sequence `\\x?' octal character expected");
				}
				int escval= digit;
				for(int digs= 3; digs > 0 && strStart + 1 < strLen; digs--)
				{	digit= Character.digit(value.charAt(strStart + 1), 16);
					if(digit < 0)
						break;
					strStart++;
					escval= escval * 16 + digit;
				}
				ch= (char)escval;
			}else if(ch != '\"' && ch != '\'' && ch != '\\' && ch != '?')
			{						// not double quote, single quote,
									//     backslash, question mark
				throw new TxtReadException(getFilePos(),
					"Unknown escape sequence `\\" + ch + "'");
			}
			sb.append(ch);
			strStart++;
			escStart= value.indexOf('\\', strStart);
		}while(escStart >= 0);
		if(strLen - strStart > 0)
			sb.append(value.substring(strStart, strLen));
		// System.out.println("String= " + sb.toString());
		return sb.toString();
	}
}
