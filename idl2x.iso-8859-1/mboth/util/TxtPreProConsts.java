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

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

/**
 * @author  Martin Both
 */

public class TxtPreProConsts extends TxtPreProMacro
{
	/** __LINE__, __FILE__, __DATE__, __TIME__
	 */
	public static final String LINE= "__LINE__";
	public static final String FILE= "__FILE__";
	public static final String DATE= "__DATE__";
	public static final String TIME= "__TIME__";

	/** 
	 *  @param	tokName	Macro name and position
	 */
	public TxtPreProConsts(String name)
	{
		super(new TxtTokWord(new TxtFilePos("preprocessor-constant"), 0, name),
			new TxtToken[1]);
	}

	/**
	 */
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof TxtPreProConsts))
			return false;
		TxtPreProConsts macro= (TxtPreProConsts)obj;
		return getName().equals(macro.getName());
	}
	
	/** Open the macro and read the arguments
	 *
	 *  @param	caller		Aufrufposition und Tokenflags
	 *  @param	ttrd		Token reader ohne Macro-Expansion
	 *						f"ur das Einlesen der Parameter und f"ur EOF
	 */
	public boolean open(TxtTokWord caller, TxtTokenReader ttrd)
		throws TxtReadException
	{
		String constName= getName();
		TxtFilePos constPos= getFilePos();
		int constFlags= caller.getFlags();
		
		TxtFilePos lineFile= caller.getFilePos();
		if(lineFile == null)
			lineFile= constPos;
		while(lineFile.getFromFilePos() != null)
			lineFile= lineFile.getFromFilePos();
			
		if(constName.equals(LINE))
		{	defValue[0]= new TxtTokDigits(constPos, constFlags,
				String.valueOf(lineFile.getLineNo()));
		}else if(constName.equals(FILE))
		{	defValue[0]= new TxtTokString(constPos, constFlags, '"',
				lineFile.getFileName());
		}else if(constName.equals(DATE))
		{	SimpleDateFormat df= new SimpleDateFormat("MMM dd yyyy");
			// "Oct  6 1999"
			defValue[0]= new TxtTokString(constPos, constFlags, '"',
				df.format(new Date()));
		}else if(constName.equals(TIME))
		{	SimpleDateFormat df= new SimpleDateFormat("HH:mm:ss");
			// "16:52:45"
			defValue[0]= new TxtTokString(constPos, constFlags, '"',
				df.format(new Date()));
		}else
		{	throw new InternalError();
		}
		return super.open(caller, ttrd);
	}
}
