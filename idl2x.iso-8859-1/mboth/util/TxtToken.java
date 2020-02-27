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
public abstract class TxtToken implements Cloneable
{
	/** Token attributes
	 */
	public static final int PreBlanks= 1;			// Comments included

	/** First token in a new logical line?
	 *  Not the first token in a file.
	 */
	public static final int PreLineBreak= 2;		// CR, CRLF, LF

	/** White spaces before token
	 */
	public static final int PreWhiteSpaces= 1+2;

	/** First token in a file
	 */
	public static final int PreFileStart= 4;

	/** Is `end of file' token
	 */
	public static final int EndOfFile= 8;
	
	/** Optional Infos or null
	 */
	private TxtFilePos filepos;

	/** Token attributes
	 */
	private int flags;
	
	/** 
	 *  @param	filepos	Filename and the start position or null
	 *  @param	flags	Token attributes or 0
	 */
	public TxtToken(TxtFilePos filepos, int flags)
	{
		this.filepos= filepos;
		this.flags= flags;
	}
	
	/** Get the filename and the start position of the Token
	 */
	public TxtFilePos getFilePos()
	{
		return filepos;
	}

	/** Set the filename and the start position of the Token
	 */
	public void setFilePos(TxtFilePos filePos)
	{
		this.filepos= filePos;
	}

	/**
	 */	
	public int getFlags()
	{
		return flags;
	}

	/**
	 */	
	public void setFlags(int flags)
	{
		this.flags= flags;
	}

	/**
	 */	
	public void deleteBlanks()
	{
		flags &= ~PreBlanks;
	}

	/**
	 */	
	public boolean isAfterBlanks()
	{	return (flags & PreBlanks) != 0;
	}

	/**
	 */	
	public boolean isAfterNewLine()
	{	return (flags & PreLineBreak) != 0;
	}
	
	/**
	 */	
	public void setAfterNewLine(boolean is)
	{	if(is)
			flags |= PreLineBreak;
		else
			flags &= ~PreLineBreak;
	}
	
	/**
	 */	
	public boolean isAfterWhiteSpaces()
	{	return (flags & PreWhiteSpaces) != 0;
	}

	/**
	 */	
	public boolean isStartOfLine()
	{	return (flags & (PreLineBreak | PreFileStart)) != 0;
	}

	/**
	 */	
	public boolean isEndOfFile()
	{	return (flags & EndOfFile) != 0;
	}

	/** Die Position wird nicht verglichen 
	 */
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof TxtToken))
			return false;
		TxtToken token= (TxtToken)obj;
		return (token.flags == flags);
	}

	/**
	 */
	public Object clone()
	{
		try
		{	TxtToken twin= (TxtToken)super.clone();
			return twin;
		}catch(CloneNotSupportedException ex)
		{	// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}

	/** 
	 */
	public String toString()
	{
		if(isAfterNewLine())
			return isAfterBlanks()? "\n ": "\n";
		if(isAfterWhiteSpaces())
			return " ";
		return "";
	}
}
