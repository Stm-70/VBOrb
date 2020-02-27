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

import java.util.*;

/**
 * @author  Martin Both
 */

public interface TxtPreProPragma
{
	/**
	 *  @param	filename
	 */
	public void openIncludeFile(String filename);

	/**
	 */
	public void closeIncludeFile();

	/**
	 *  @param	ttRd		Don´t read after end of file
	 *
	 *  @exception	TxtReadException
	 */
	public void readPragma(TxtTokenReader ttRd) throws TxtReadException;
}
