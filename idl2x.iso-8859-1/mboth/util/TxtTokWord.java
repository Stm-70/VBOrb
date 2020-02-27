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
public class TxtTokWord extends TxtToken
{
	/**
	 */
	private String value;

	/** 
	 *  @param	filepos	Filename and the start position or null
	 *  @param	flags	Token attributes
	 *  @param	word	Token value; _abcdef...
	 */
	public TxtTokWord(TxtFilePos filepos, int flags, String word)
	{
		super(filepos, flags);
		this.value= word;
	}
	
	/** Die Position wird nicht verglichen 
	 */
	public boolean equals(Object obj)
	{
		if(!super.equals(obj) || !(obj instanceof TxtTokWord))
			return false;
		TxtTokWord token= (TxtTokWord)obj;
		return value.equals(token.value);
	}

	/** 
	 */
	public String toString()
	{
		return super.toString() + value;
	}

	/**
	 */
	public String getWord()
	{
		return value;
	}
}
