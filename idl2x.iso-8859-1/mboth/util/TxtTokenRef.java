/*
   Copyright (c) 2000 Martin.Both

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

/** Holding a TxtToken reference
 *
 * @author  Martin Both
 */
public class TxtTokenRef
{
	/** TxtToken
	 */
	public TxtToken value;

	/** Get next token and clear value
	 */
	public TxtToken getOrReadToken(TxtTokenReader idlRd) throws TxtReadException
	{
		TxtToken token;
		if(value == null)
		{	token= idlRd.readToken();
		}else
		{	token= value;
			value= null;
		}
		return token;
	}

	/** Unget token
	 */
	public void ungetToken(TxtToken token) throws TxtReadException
	{
		if(value != null)
			throw new TxtReadException(value.getFilePos(),
				"Cannot unget Token twice");
		value= token;
	}
}
