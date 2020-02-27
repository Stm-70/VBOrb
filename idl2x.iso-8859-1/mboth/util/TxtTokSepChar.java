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
public class TxtTokSepChar extends TxtToken
{
	/**
	 */
	private char value;

	/** 
	 *  @param	filepos	Filename and the start position
	 *  @param	flags	Token attributes
	 *  @param	sepchar	Token value
	 */
	public TxtTokSepChar(TxtFilePos filepos, int flags, char sepchar)
	{
		super(filepos, flags);
		this.value= sepchar;
	}
	
	/** Die Position wird nicht verglichen 
	 */
	public boolean equals(Object obj)
	{
		if(!super.equals(obj) || !(obj instanceof TxtTokSepChar))
			return false;
		TxtTokSepChar token= (TxtTokSepChar)obj;
		return token.value == value;
	}

	/** 
	 */
	public String toString()
	{
		char str[]= new char[1];
		str[0]= value;
		return super.toString() + new String(str);
	}

	/**
	 */
	public char getChar()
	{
		return value;
	}
}
