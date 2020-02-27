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
public class TxtFilePos implements Cloneable
{
	/** Filename or null
	 */
	private String filename;

	/** Line number or 0
	 */
	private int lineno;

	/** Character position or -1
	 */
	private int linepos;
	
	/** Included or Startposition
	 */
	private TxtFilePos fromFilePos;
	
	/** 
	 *  @param	filename	Filename
	 */
	public TxtFilePos(String filename)
	{
		this(filename, 0, -1, null);
	}

	/** 
	 *  @param	filename	Filename
	 *  @param	fromFilePos	Included from? or null
	 */
	public TxtFilePos(String filename, TxtFilePos fromFilePos)
	{
		this(filename, 0, -1, fromFilePos);
	}

	/** 
	 *  @param	filename	Filename
	 *  @param	lineno		Line number
	 */
	public TxtFilePos(String filename, int lineno)
	{
		this(filename, lineno, -1, null);
	}

	/** 
	 *  @param	filename	Filename
	 *  @param	lineno		Line number
	 *  @param	fromFilePos	Included from? or null
	 */
	public TxtFilePos(String filename, int lineno, TxtFilePos fromFilePos)
	{
		this(filename, lineno, -1, fromFilePos);
	}

	/** 
	 *  @param	filename	Filename
	 *  @param	lineno		Line number
	 *  @param	linepos		Line position
	 */
	public TxtFilePos(String filename, int lineno, int linepos)
	{
		this(filename, lineno, linepos, null);
	}
	
	/** 
	 *  @param	filename	Filename
	 *  @param	lineno		Line number
	 *  @param	linepos		Line position
	 *  @param	fromFilePos	Included from?
	 */
	public TxtFilePos(String filename, int lineno, int linepos,
		TxtFilePos fromFilePos)
	{
		this.filename= filename;
		this.lineno= lineno;
		this.linepos= linepos;
		this.fromFilePos= fromFilePos;
		if(fromFilePos == this)
			throw new InternalError();
	}

	/** 
	 */
	public String getFileName()
	{
		return filename;
	}
	
	/** 
	 */
	public int getLineNo()
	{
		return lineno;
	}

	/** 
	 */
	public int getLinePos()
	{
		return linepos;
	}

	/** Execute `#line' command
	 */
	public void setLineFile(int lineno, String filename)
	{
		this.lineno= lineno;
		if(filename != null)
			this.filename= filename;
	}
	
	/**
	 *  @param	fromFilePos		fromFilePos or null
	 */
	public void setFromFilePos(TxtFilePos fromFilePos)
	{
		this.fromFilePos= fromFilePos;
	}

	/**
	 *  @param	fromFilePos		fromFilePos or null
	 */
	public void addFromFilePos(TxtFilePos fromFilePos)
	{
		if(fromFilePos == this)
			throw new InternalError();
		TxtFilePos theEnd= this;
		// (theEnd.fromFilePos can be read, because it is of the same class)
		while(theEnd.fromFilePos != null)
			theEnd= theEnd.fromFilePos;
		theEnd.fromFilePos= fromFilePos;
	}

	/**
	 */
	public TxtFilePos getFromFilePos()
	{
		return fromFilePos;
	}

	/**
	 */
	public Object clone()
	{
		try
		{	TxtFilePos twin= (TxtFilePos)super.clone();
			if(fromFilePos != null)
				twin.fromFilePos= (TxtFilePos)fromFilePos.clone();
			return twin;
		}catch(CloneNotSupportedException ex)
		{	// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}
	
	/** (needed also for TxtReadException)
	 *  
	 *  @param	sb		
	 */
	private void appendFirstPosTo(StringBuffer sb)
	{
		sb.append(filename);
		if(lineno != 0)
		{	sb.append(":");
			sb.append(lineno);
			if(linepos != -1)
			{	sb.append("(");
				sb.append(linepos);
				sb.append(")");
			}
		}
	}
	
	/** xxx:111(22),
	 *  yyy:333(44)
	 */
	public void appendPosTo(StringBuffer sb, TxtFilePos afterStartPos)
	{
		if(fromFilePos != null && fromFilePos != afterStartPos)
		{	fromFilePos.appendPosTo(sb, afterStartPos);
			sb.append(",\n");
		}
		appendFirstPosTo(sb);
	}

	/** yyy:333(44)
	 *  (needed also for TxtReadException)
	 */
	public String firstPosToString()
	{
		StringBuffer sb= new StringBuffer();
		appendFirstPosTo(sb);
		return sb.toString();
	}

	/** 
	 */
	public boolean equals(Object obj)
	{
		if(!super.equals(obj) || !(obj instanceof TxtFilePos))
			return false;
		TxtFilePos fPos= (TxtFilePos)obj;
		if(fromFilePos != null)
		{	if(!fromFilePos.equals(fPos.fromFilePos))
				return false;
		}else
		{	if(fPos.fromFilePos != null)
				return false;
		}
		return fPos.lineno == lineno && fPos.linepos == linepos	
			&& filename.equals(fPos.filename);
	}

	/** xxx:111(22),
	 *  yyy:333(44)
	 */
	public String toString()
	{
		StringBuffer sb= new StringBuffer();
		appendPosTo(sb, null);
		return sb.toString();
	}
}
