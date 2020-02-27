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
public class ChainException extends java.lang.Exception
{
	private ChainException next;

	/** Construct first ChainException
	 *
	 *  @param message	Detail Message or null
	 */
	public ChainException(String message)
	{
		super(message);
	}

	/** Construct a fully-specified ChainException
	 *
	 *  @param message	Detail Message or null
	 *  @param nextEx	Next Exception or null
	 */
/*	public ChainException(String message, ChainException nextEx)
	{
		super(message);
		next= nextEx;
	}
*/
	/**
	 * Get the exception chained to this one. 
	 *
	 * @return the next ChainException in the chain, null if none
	 */
	public ChainException getNextException()
	{
		return next;
	}

	/**
	 * Add an ChainException to the end of the chain.
	 *
	 * @param ex the new end of the ChainException chain
	 */
	public synchronized void setNextException(ChainException ex)
	{
		ChainException theEnd= this;
		while(theEnd.next != null)
			theEnd= theEnd.next;
		theEnd.next= ex;
	}
}
