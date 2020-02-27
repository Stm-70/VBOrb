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
public interface TxtTokenReader
{
	/** Get the next token
	 */
	public TxtToken readToken() throws TxtReadException;

	/** Save the last token
	 */
	public void unreadToken() throws TxtReadException;

	/** Get the last FilePos (including fromFilePos)
	 */
	public TxtFilePos getFilePos();
}
