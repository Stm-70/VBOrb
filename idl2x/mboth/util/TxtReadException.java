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
public class TxtReadException extends ChainException
{
	/** FilePos
	 */
	private TxtFilePos filePos;

	/** Construct a fully-specified TxtReadException
	 *  with different line number
	 *
	 *  @param filename	Filename
	 *  @param lineno	line number
	 *  @param reason	A description of the exception 
	 */
	public TxtReadException(String filename, int lineno, String reason)
	{
		super(reason);
		filePos= new TxtFilePos(filename, lineno);
	}

	/** Construct an TxtReadException without line number
	 *
	 *  @param filename	Filename
	 *  @param reason	A description of the exception 
	 */
	public TxtReadException(String filename, String reason)
	{
		super(reason);
		filePos= new TxtFilePos(filename);
	}

	/** Construct a fully-specified TxtReadException
	 *
	 *  @param filepos	Filename and position
	 *  @param reason	A description of the exception 
	 */
	public TxtReadException(TxtFilePos filePos, String reason)
	{
		super(reason);
		this.filePos= filePos;
	}

	/**
	 */
	public TxtFilePos getFilePos()
	{	return filePos;
	}
	
	/** Overwrites the getMessage() of this throwable object to add
	 *  the filePos.
	 *
	 * @return  the detail message of this <code>Throwable</code>,
	 *          or <code>null</code> if this <code>Throwable</code> does not
	 *          have a detail message.
	 */
	public String getMessage()
	{
		if(filePos == null)
			return super.getMessage();
		if(getNextException() != null &&
			getNextException() instanceof TxtReadException)
		{	TxtReadException rdEx2= (TxtReadException)getNextException();
			
			TxtFilePos frPosEnd= filePos;
			frPosEndLoop: do
			{	for(TxtFilePos frPos2= rdEx2.filePos;
					frPos2 != null; frPos2= frPos2.getFromFilePos())
				{
					if(frPosEnd.equals(frPos2))
						break frPosEndLoop;
				}
				frPosEnd= frPosEnd.getFromFilePos();
			}while(frPosEnd != null);
			if(filePos == frPosEnd)
				return super.getMessage();
			StringBuffer sb= new StringBuffer();
			filePos.appendPosTo(sb, frPosEnd);
			sb.append(": ");
			sb.append(super.getMessage());
			return sb.toString();
		}
		return filePos.toString() + ": " + super.getMessage();
	}
}
