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

/** Read the rest of the line
 *
 *  @author  Martin Both
 */
public class TxtTokLineReader implements TxtTokenReader
{
	/**
	 */
	private boolean eol;
	
	/** 
	 */
	private TxtTokenReader ttrd;

	/** 
	 *  @param	ttrd	Token reader; start token has been read
	 */
	public TxtTokLineReader(TxtTokenReader ttrd)
	{
		this.ttrd= ttrd;
	}

	/** Token lesen
	 */
	public TxtToken readToken() throws TxtReadException
	{
		TxtToken token= ttrd.readToken();
		if(!token.isEndOfFile() && !token.isAfterNewLine())
			return token;
		ttrd.unreadToken();
		if(eol)
		{	throw new TxtReadException(token.getFilePos(),
				"Cannot read after end of line");
		}
		eol= true;
		// It would be nice to set the file pos here to the end of line
		// instead of the next line token pos. ???
		return new TxtTokEndOfFile(token.getFilePos(), token.getFlags());
		// token.isAfterBlanks()? TxtToken.PreBlanks: 0);
	}

	/** Token zur"uckstellen
	 */
	public void unreadToken() throws TxtReadException
	{
		ttrd.unreadToken();
		eol= false;
	}
	
	/** Get the last FilePos (including fromFilePos)
	 */
	public TxtFilePos getFilePos()
	{
		return ttrd.getFilePos();
	}
}
