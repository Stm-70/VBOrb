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

public class TxtPreProMacro implements TxtTokenReader
{
	/** Macro name and position
	 */
	private TxtTokWord tokName;
	
	/** #define macro(par1, ....)	Parameternamen und Position
	 *  #define macro()				[0], argDefines == null
	 *  #define macro				null, argDefines == null
	 */
	private TxtTokWord parNames[];

	/** Macro definition oder TxtToken[0]
	 */
	protected TxtToken defValue[];
	
	/** Macro defValue pointer
	 */
	protected int bufPtr;
	
	/** Macro is opened or null
	 */
	protected TxtTokEndOfFile lastValue;

	/** Position of the caller
	 */
	protected TxtFilePos callPos;

	/**
	 */
	private Hashtable argDefines;
	
	/** Arg value or null
	 */
	private TxtPreProMacro argValueRead;

	/** Use to unread Value
	 */
	private TxtToken actValue, savedValue;

	/** Use to unread Token
	 */
	private TxtToken actToken, savedToken;
	
	/** Read list of parameters after `#define macro('
	 *
	 *  @param	ttrd		Token reader ohne Macro-Expansion
	 */
	private static TxtTokWord[] readParameterList(TxtTokenReader ttrd)
		throws TxtReadException
	{
		ArrayList pars= new ArrayList();
		for(; ; )
		{	TxtToken token= ttrd.readToken();
			if(token.isEndOfFile() || token.isAfterNewLine()
				|| !(token instanceof TxtTokWord))
			{	if(pars.size() == 0 && token instanceof TxtTokSepChar
					&& ((TxtTokSepChar)token).getChar() == ')')
					break; // #define macro() xyz
				throw new TxtReadException(token.getFilePos(),
					"Invalid macro parameter name");
			}
			pars.add(token);
			token= ttrd.readToken();
			if(token.isEndOfFile() || token.isAfterNewLine()
				|| !(token instanceof TxtTokSepChar))
			{	throw new TxtReadException(token.getFilePos(),
					"Invalid macro parameter list");
			}
			char sepch= ((TxtTokSepChar)token).getChar();
			if(sepch == ')')
				break;
			if(sepch != ',')
			{	throw new TxtReadException(token.getFilePos(),
					"Invalid macro parameter list");
			}				
		}
		TxtTokWord[] parsRet= new TxtTokWord[pars.size()];
		pars.copyInto(parsRet);
		return parsRet;
	}
	
	/** Create macro definition
	 *  (Maybe it would better write a static method getInstance())
	 *  Read after `#define macro'
	 *
	 *  @param	tokName		Macro name and position
	 *  @param	ttrd		Token reader ohne Macro-Expansion
	 */
	public TxtPreProMacro(TxtTokWord tokName, TxtTokenReader ttrd)
		throws TxtReadException
	{
		this.tokName= tokName;

		TxtToken token= ttrd.readToken();
		if(!token.isEndOfFile() && !token.isAfterWhiteSpaces())
		{
			if(!(token instanceof TxtTokSepChar) ||
				((TxtTokSepChar)token).getChar() != '(')
			{	throw new TxtReadException(token.getFilePos(),
					"Invalid macro name");
			}
			// read identifier list
			parNames= readParameterList(ttrd);
			if(parNames.length > 0)
				argDefines= new Hashtable(parNames.length);
			token= ttrd.readToken();
		}
		ArrayList tokseq= new ArrayList();
		while(!token.isEndOfFile() && !token.isAfterNewLine())
		{
			tokseq.add(token);
			token= ttrd.readToken();
		}
		ttrd.unreadToken();
		defValue= new TxtToken[tokseq.size()];
		tokseq.copyInto(defValue);
	}

	/** 
	 *  @param	tokName	Macro name and position
	 */
	public TxtPreProMacro(TxtTokWord tokName, TxtToken[] defValue)
	{
		this.tokName= tokName;
		this.defValue= defValue;
	}

	/** 
	 */
	public String getName()
	{
		return tokName.getWord();
	}

	/** 
	 */
	public TxtFilePos getFilePos()
	{
		return tokName.getFilePos();
	}

	/** 
	 */
	public String[] getParameterNames()
	{
		if(parNames == null)
			return null;
		String[] names= new String[parNames.length];
		for(int i= 0; i < names.length; i++)
			names[i]= parNames[i].getWord();
		return names;
	}

	/**
	 */
	public boolean equals(Object obj)
	{
		if(obj == null || !(obj instanceof TxtPreProMacro))
			return false;
		TxtPreProMacro macro= (TxtPreProMacro)obj;
		if(!getName().equals(macro.getName()))
			return false;
		// Test parameter list
		if(macro.parNames != null)
		{	if(parNames == null || parNames.length != macro.parNames.length)
				return false;
			for(int i= 0; i < parNames.length; i++)
			{	if(!parNames[i].getWord().equals(macro.parNames[i].getWord()))
					return false;
			}
		}else if(parNames != null)
		{	return false;
		}
		// Test token sequence
		if(macro.defValue != null)
		{	if(defValue == null || defValue.length != macro.defValue.length)
				return false;
			for(int i= 0; i < defValue.length; i++)
			{	if(!defValue[i].equals(macro.defValue[i]))
					return false;
			}
		}else if(defValue != null)
		{	return false;
		}
		return true;
	}
	
	/** Open an argument value
	 *
	 *  @param	caller		Aufrufposition und Tokenflags
	 */
	private void openArgValue(TxtTokWord caller)
		throws TxtReadException
	{
		if(lastValue != null || parNames != null)
		{	throw new TxtReadException(getFilePos(),
				"Invalid open arg value `" + getName() + "'");
		}
		this.callPos= null; //caller.getFilePos();

		// Flags des ersten ValueToken auf die Flags des Aufrufers setzen.
		int callFlags= caller.getFlags();
		if(defValue.length > 0)
		{	defValue[0].setFlags(callFlags);
			callFlags= 0;
		}
		// Flags bei EndOfFile sind z.B. wichtig, wenn ein Macroaufruf von
		// `#define nichts' am Anfang einer Zeile steht
		this.lastValue= new TxtTokEndOfFile(caller.getFilePos(), callFlags);
		this.bufPtr= 0;
	}

	/** Lesen mit Parameter expandieren, Parameter operations,
	    aber ohne Submacro-Expansion
		F"ur jeden leeren Parameter wird jeweils ein EOF geliefert.
		@return		EndOfFile for every empty arg
	 */
	public TxtToken readValue() throws TxtReadException
	{
		if(savedValue != null)
		{
			actValue= savedValue;
			savedValue= null;
			return actValue;
		}
		if(argValueRead != null)
		{
			actValue= argValueRead.readValue();
			if(!(actValue instanceof TxtTokEndOfFile))
			{	if(!(argValueRead.readValue() instanceof TxtTokEndOfFile))
				{	argValueRead.unreadValue();
					return actValue;
				}
			}
			argValueRead= null;
			// last value of argument in actValue or
			// EndOfFile value if empty argument value
		}else
		{
			// Ende erreicht?
			if(this.bufPtr >= this.defValue.length)
			{	if(this.lastValue == null)
				{	throw new TxtReadException(getFilePos(),
						"Cannot read after end of macro definition");
				}
				// Close macro value reader
				//
				actValue= this.lastValue;
				this.lastValue= null;
				if(argDefines != null)
					argDefines.clear();
				return actValue;
			}
			actValue= this.defValue[this.bufPtr++];
			if(callPos != null)
			{
				actValue= (TxtToken)actValue.clone();
				TxtFilePos fPos= actValue.getFilePos();
				if(fPos == null)
				{	fPos= callPos;
				}else
				{	fPos= (TxtFilePos)fPos.clone();
					fPos.setFromFilePos(callPos);
				}
				actValue.setFilePos(fPos);
			}
		
			if(argDefines != null)
			{
				if(actValue instanceof TxtTokWord)
				{
					// argument --> argumentvalue
					//
					TxtTokWord actWord= (TxtTokWord)actValue;
					TxtPreProMacro macro= (TxtPreProMacro)argDefines.get(actWord.getWord());
					if(macro != null)
					{	macro.openArgValue(actWord);
						argValueRead= macro;
						// System.out.println(this.getName()
						//	+ ": EXPAND ARGVALUE: " + macro.getName());
						return readValue();
					}
				}else if(actValue instanceof TxtTokSepChar
					&& ((TxtTokSepChar)actValue).getChar() == '#')
				{
					// #argument --> "argumentvalue"
					// (gcc bug if arg == \)
					//
					TxtReadException ex= new TxtReadException(actValue.getFilePos(),
						"`#' operator is not followed by a macro argument name");
					if(this.bufPtr >= this.defValue.length)
						throw ex;
					TxtToken parValue= this.defValue[this.bufPtr++];
					if(parValue.isAfterWhiteSpaces() || !(parValue instanceof TxtTokWord))
						throw ex;
					TxtTokWord actWord= (TxtTokWord)parValue;
					if(argDefines == null)
						throw ex;
					TxtPreProMacro macro= (TxtPreProMacro)argDefines.get(actWord.getWord());
					if(macro == null)
						throw ex;
					macro.openArgValue(actWord);
					// System.out.println(this.getName()
					//	+ ": EXPAND #ARGVALUE: " + macro.getName());
					StringBuffer sb= new StringBuffer();
					TxtToken argValue= macro.readValue();
					while(!(argValue instanceof TxtTokEndOfFile))
					{	sb.append(argValue.toString());
						argValue= macro.readValue();
					}
					actValue= new TxtTokString(actValue.getFilePos(),
						actValue.getFlags(), '"', sb.toString());
				}
			}
		}
		
		// Folgt ##?
		if(this.bufPtr + 1 >= this.defValue.length)
			return actValue;
		TxtToken tstValue= this.defValue[this.bufPtr];
		if(!(tstValue instanceof TxtTokSepChar)
			|| ((TxtTokSepChar)tstValue).getChar() != '#')
			return actValue;
		tstValue= this.defValue[this.bufPtr + 1];
		if(!(tstValue instanceof TxtTokSepChar)
			|| ((TxtTokSepChar)tstValue).getChar() != '#'
			|| ((TxtTokSepChar)tstValue).isAfterWhiteSpaces())
			return actValue;
		this.bufPtr += 2;
		if(this.bufPtr >= this.defValue.length)
			throw new TxtReadException(tstValue.getFilePos(),
				"`##' at end of macro definition");
		tstValue= this.defValue[this.bufPtr];
		tstValue.deleteBlanks();
		tstValue= actValue;
		readValue();
		// System.out.println(getName() + ": JOIN: " + tstValue
		//	+ " + " + actValue);
		// Token tstValue and actValue joinable?
		if(tstValue instanceof TxtTokEndOfFile)
		{	; // actValue
		}else if(tstValue instanceof TxtTokDigits &&
			actValue instanceof TxtTokDigits)
		{	actValue= new TxtTokDigits(tstValue.getFilePos(),
				tstValue.getFlags(), ((TxtTokDigits)tstValue).getDigits()
				+ ((TxtTokDigits)actValue).getDigits());
		}else if(tstValue instanceof TxtTokWord &&
			actValue instanceof TxtTokDigits)
		{	actValue= new TxtTokWord(tstValue.getFilePos(),
				tstValue.getFlags(), ((TxtTokWord)tstValue).getWord()
				+ ((TxtTokDigits)actValue).getDigits());
		}else if(tstValue instanceof TxtTokWord &&
			actValue instanceof TxtTokWord)
		{	actValue= new TxtTokWord(tstValue.getFilePos(),
				tstValue.getFlags(), ((TxtTokWord)tstValue).getWord()
				+ ((TxtTokWord)actValue).getWord());
		}else
		{	unreadValue();
			actValue= tstValue;
		}
		return actValue;
	}
	
	/** Value zur"uckstellen
	 */
	public void unreadValue() throws TxtReadException
	{
		if(actValue == null)
		{	throw new TxtReadException(getFilePos(),
				"Missing Value to unread");
		}
		savedValue= actValue;
		actValue= null;
	}

	/** Read the macro arguments without macro expansion
	 *
	 *  @param	startPos	Position of the `('
	 *  @param	ttrd		Token reader ohne Macro-Expansion
	 */
	private void readArguments(TxtFilePos startPos, TxtTokenReader ttrd)
		throws TxtReadException
	{
		// Parameter token sequence holen
		// Verschachtelte runde Klammern einlesen
		// Makros in Parameter d"urfen noch nicht expandiert werden.
		// parNames.length can be 0
		int parCnt= 0;
		do
		{	ArrayList tokseq= new ArrayList();
			int brCnt= 0;
			for(; ; )
			{	TxtToken token= ttrd.readToken();
				if(!(token instanceof TxtTokSepChar))
				{
					if(token.isEndOfFile())
					{	TxtFilePos fpos= (TxtFilePos)token.getFilePos().clone();
						fpos.setFromFilePos(startPos);
						throw new TxtReadException(fpos,
							"Unterminated call of macro `"
							+ this.getName() + "'");
					}
				}else
				{	char sepCh= ((TxtTokSepChar)token).getChar();
					if(sepCh == '(')
					{	brCnt++;
					}else if(brCnt <= 0)
					{	if(sepCh == ')')
						{	if(parCnt + 1 >= parNames.length)
								break;
							TxtFilePos fpos= (TxtFilePos)token.getFilePos().clone();
							fpos.setFromFilePos(startPos);
							throw new TxtReadException(fpos,
								"Macro `" + this.getName()
								+ "' used with only "
								+ (parCnt + 1) + " args");
						}else if(sepCh == ',')
						{	if(parCnt + 1 < parNames.length)
								break;
							TxtFilePos fpos= (TxtFilePos)token.getFilePos().clone();
							fpos.setFromFilePos(startPos);
							throw new TxtReadException(fpos,
								"Macro `" + this.getName()
								+ "' used with more than "
								+ parNames.length + " args");
						}
					}else if(sepCh == ')')
					{	brCnt--;
					}
				}
				tokseq.add(token);
				// System.out.println(this.getName() + ": ARG(" +(parCnt+1)
				//	+ "): " + token);
			}
			TxtToken[] newValue= new TxtToken[tokseq.size()];
			tokseq.copyInto(newValue);
			if(parNames.length == 0)
			{	// #define macro() xyz
				if(newValue.length > 0)
				{	throw new TxtReadException(startPos,
						"Arguments given to macro `" + this.getName() + "'");
				}
			}else
			{	TxtPreProMacro argMacro=
					new TxtPreProMacro(parNames[parCnt], newValue);
				argDefines.put(argMacro.getName(), argMacro);
			}
			parCnt++;
		}while(parCnt < parNames.length);
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
		// Nur Makros expandieren, die nicht in Arbeit sind
		if(lastValue != null)
			return false;
		if(parNames != null)
		{
			TxtToken token= ttrd.readToken();
			if(!(token instanceof TxtTokSepChar)
				|| ((TxtTokSepChar)token).getChar() != '(')
			{	ttrd.unreadToken();
				return false;
			}
			try
			{	readArguments(token.getFilePos(), ttrd);
			}catch(TxtReadException ex)
			{	ex.setNextException(new TxtReadException(getFilePos(),
					"Position of the called macro definition"));
				throw ex;
			}
		}
		this.callPos= caller.getFilePos();
		// Flags des ersten ValueToken auf die Flags des Aufrufers setzen.
		int callFlags= caller.getFlags();
		if(defValue.length > 0)
		{	defValue[0].setFlags(callFlags);
			callFlags= 0;
		}
		// Flags bei EndOfFile sind z.B. wichtig, wenn ein Macroaufruf von
		// `#define nichts' am Anfang einer Zeile steht
		this.lastValue= new TxtTokEndOfFile(caller.getFilePos(), callFlags, ttrd);
		this.bufPtr= 0;
		return true;
	}

	/** Token lesen
	 */
	public TxtToken readToken() throws TxtReadException
	{
		if(savedToken != null)
		{
			actToken= savedToken;
			savedToken= null;
			return actToken;
		}
		// read actToken
		// Swallow empty values
		do
		{	actToken= readValue();
		}while(actToken instanceof TxtTokEndOfFile && lastValue != null);
		return actToken;
	}

	/** Token zur"uckstellen
	 */
	public void unreadToken() throws TxtReadException
	{
		if(actToken == null)
		{	throw new TxtReadException(getFilePos(),
				"Missing Token to unread");
		}
		savedToken= actToken;
		actToken= null;
	}
}
