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
public class TxtTokEndOfFile extends TxtToken
{
	/** Reader included from Reader or null
	 */
	private TxtTokenReader fromTokReader;
	
	/** 
	 *  @param	filepos	Filename and the start position
	 *  @param	flags	Token attributes
	 */
	public TxtTokEndOfFile(TxtFilePos filepos, int flags)
	{
		super(filepos, flags | TxtToken.EndOfFile);
	}

	/** 
	 *  @param	filepos	Filename and the start position
	 *  @param	flags	Token attributes
	 *  @param	from	Included from?
	 */
	public TxtTokEndOfFile(TxtFilePos filepos, int flags,
		TxtTokenReader from)
	{
		super(filepos, flags | TxtToken.EndOfFile);
		this.fromTokReader= from;
	}

	/** Die Position wird nicht verglichen 
	 */
	public boolean equals(Object obj)
	{
		if(!super.equals(obj) || !(obj instanceof TxtTokEndOfFile))
			return false;
		// TxtTokEndOfFile token= (TxtTokEndOfFile)obj;
		return true;
	}

	/** 
	 */
	public String toString()
	{
		return super.toString() + "<EOF>";
	}
	
	/** 
	 */
	public TxtTokenReader getFromTokenReader()
	{
		return fromTokReader;
	}
}
