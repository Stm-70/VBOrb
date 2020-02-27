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
import java.io.File;

/**
 * @author  Martin Both
 */

public class TxtPreProcessor implements TxtTokenReader
{
	/**
	 */
	private static String[] glIncludePaths;
	
	/**
	 */
	public static void addIncludePaths(String paths) throws TxtReadException
	{
		if(paths.length() == 0)
		{	throw new TxtReadException("-I", "Missing include path");
		}
		int oldLen= (glIncludePaths == null)? 0: glIncludePaths.length;
		StringTokenizer strtok= new StringTokenizer(paths, File.pathSeparator);
		String[] newIncludePaths= new String[oldLen + strtok.countTokens()];
		int i;
		for(i= 0; i < oldLen; i++)
		{	newIncludePaths[i]= glIncludePaths[i];
		}
		for(; strtok.hasMoreTokens(); i++)
		{	newIncludePaths[i]= strtok.nextToken();
		}
		glIncludePaths= newIncludePaths;
	}

	/**
	 */
	private static Hashtable glDefines;

	static
	{
		glDefines= new Hashtable();
		glDefines.put(TxtPreProConsts.LINE,
			new TxtPreProConsts(TxtPreProConsts.LINE));
		glDefines.put(TxtPreProConsts.FILE,
			new TxtPreProConsts(TxtPreProConsts.FILE));
		glDefines.put(TxtPreProConsts.DATE,
			new TxtPreProConsts(TxtPreProConsts.DATE));
		glDefines.put(TxtPreProConsts.TIME,
			new TxtPreProConsts(TxtPreProConsts.TIME));
	}
	
	/**
	 */
	public static void addglDefine(String define) throws TxtReadException
	{
		if(glDefines == null)
			glDefines= new Hashtable();
		int valueStart= define.indexOf('=');
		String text;
		if(valueStart >= 0)
		{	StringBuffer sb= new StringBuffer(define);
			sb.setCharAt(valueStart, ' ');
			text= sb.toString();
		}else
		{	text= define;
		}
		TxtReader trd= new TxtReader("-D" + define, text);
		TxtTokWord tokWord= readMacroIdentifier(trd.readToken());
		TxtPreProMacro macro= new TxtPreProMacro(tokWord, trd);
		if(!trd.readToken().isEndOfFile())
		{	throw new TxtReadException(tokWord.getFilePos(),
				"A new line is not allowed");
		}
		Object oldobj= glDefines.put(macro.getName(), macro);
		if(oldobj != null && !macro.equals(oldobj))
		{	throw new TxtReadException(
				macro.getFilePos(), "`" + macro.getName()
				+ "' redefined");
		}
	}

	/**
	 */
	private static TxtTokWord readMacroIdentifier(TxtToken token)
		throws TxtReadException
	{
		if(token.isEndOfFile() || token.isAfterNewLine())
		{	throw new TxtReadException(token.getFilePos(),
				"Missing macro identifier");
		}
		if(!(token instanceof TxtTokWord)
			|| ((TxtTokWord)token).getWord().equals("defined"))
		{	throw new TxtReadException(token.getFilePos(),
				"Invalid macro identifier");
		}
		return (TxtTokWord)token;
	}
	
	/** Pragma reader or null
	 */
	private TxtPreProPragma pragmaRd;

	/**
	 */
	private String homePath;
	
	/** 
	 */
	private TxtTokenReader startRd, ttrd;

	/** The previous ifList
	 */
	private Stack savedIfLists;
	
	/** Last `#if' position and branch or null
	 */
	private TxtPreProIfList ifList;
	
	/** Use to unread Token
	 */
	private TxtToken actToken, savedToken;
	
	/** Defined symbols
	 */
	private Hashtable defines;
	
	/** 
	 *  @param	txtName		Pseudo name
	 *  @param	text		Pseudo file
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public TxtPreProcessor(String txtName, String text) throws TxtReadException
	{
		homePath= ".";
		ttrd= startRd= new TxtReader(txtName, text);
		open();
	}

	/** 
	 *  @param	fileName	Filename
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public TxtPreProcessor(String fileName) throws TxtReadException
	{
		File file= new File(fileName);
		homePath= file.getParent();
		if(homePath == null)
			homePath= ".";
		ttrd= startRd= new TxtReader(fileName);
		open();
	}

	/** 
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	private void open() throws TxtReadException
	{
		defines= new Hashtable();
		if(glDefines != null)
		{	// JDK 1.2: defines.putAll(glDefines);
			for(Enumeration e= glDefines.keys(); e.hasMoreElements(); )
			{	Object key= e.nextElement();
				defines.put(key, glDefines.get(key));
			}
		}
		savedIfLists= new Stack();
	}

	/** Set pragma reader
	 */
	public void setPragmaReader(TxtPreProPragma pragmaRd)
	{
		this.pragmaRd= pragmaRd;
	}

	/** Get the last FilePos (including fromFilePos)
	 */
	public TxtFilePos getFilePos()
	{
		if(actToken != null)
		{	TxtFilePos fPos= actToken.getFilePos();
			if(fPos != null)
				return fPos;
		}
		// Nach einem unreadToken()
		if(savedToken != null)
		{	TxtFilePos fPos= savedToken.getFilePos();
			if(fPos != null)
				return fPos;
		}
		return startRd.getFilePos();
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
		actToken= ttrd.readToken();
		for(; ; )
		{	// System.out.println("\nactToken= " + actToken);
			if(actToken instanceof TxtTokWord)
			{
				// Wenn Makro, dann neues Token mit der aktuellen Zeilenposition
				// plus eventuell weitere Positionsangaben erzeugen.
				TxtTokWord actWord= (TxtTokWord)actToken;
				TxtPreProMacro macro= (TxtPreProMacro)defines.get(actWord.getWord());
				if(macro == null)
					break;
				if(!macro.open(actWord, ttrd))
					break;
				ttrd= macro;
				// System.out.println("EXPAND MACRO: " + macro.getName());
				actToken= ttrd.readToken();
				// Wert des Makros kann selbst wieder Makros enthalten
				continue;
			}
			if(actToken.isStartOfLine() && actToken instanceof TxtTokSepChar
				&& ((TxtTokSepChar)actToken).getChar() == '#')
			{
				TxtToken prprToken= ttrd.readToken();

				// Leere # Anweisung?
				if(prprToken.isEndOfFile() || prprToken.isAfterNewLine())
				{	actToken= prprToken;
					continue;
				}
				String directiveName= (prprToken instanceof TxtTokWord)?
					((TxtTokWord)prprToken).getWord(): null;
				if("define".equals(directiveName))
				{	// Ohne Makroexpansion den Rest der Zeile lesen
					TxtTokWord tokWord= readMacroIdentifier(ttrd.readToken());
					TxtPreProMacro macro= new TxtPreProMacro(tokWord, ttrd);
					// #define macro.getName()
					// Eintragen und pr"ufen ob "uberdefiniert wird
					Object oldobj= defines.put(macro.getName(), macro);
					if(oldobj != null && !macro.equals(oldobj))
					{	TxtReadException ex= new TxtReadException(
							macro.getFilePos(), "`" + macro.getName()
							+ "' redefined");
						ex.setNextException(new TxtReadException(
							((TxtPreProMacro)oldobj).getFilePos(),
							"this is the location of the previous definition"));
						throw ex;
					}
					actToken= ttrd.readToken();
					continue;
				}else if("undef".equals(directiveName))
				{	// Ohne Makroexpansion den Rest der Zeile lesen
					TxtTokWord tokWord= readMacroIdentifier(ttrd.readToken());
					// #undef tokWord
					defines.remove(tokWord.getWord());
					actToken= readNextBlockStart(false);
					continue;
				}else if("include".equals(directiveName))
				{	
					TxtTokenReader oldttrd= ttrd;
					ttrd= new TxtTokLineReader(oldttrd);
					boolean homeFirst;
					String filename;
					try
					{	TxtToken token= ttrd.readToken();
						if(token instanceof TxtTokSepChar
							&& ((TxtTokSepChar)token).getChar() == '<')
						{	// Ohne Makroexpansion den Rest der Zeile lesen
							homeFirst= false;
							StringBuffer sb= new StringBuffer();
							token= ttrd.readToken();
							while(!(token instanceof TxtTokSepChar)
								|| ((TxtTokSepChar)token).getChar() != '>')
							{	if(token.isEndOfFile())
								{	throw new TxtReadException(token.getFilePos(),
										"`>' missing");
								}						
								sb.append(token.toString());
								token= ttrd.readToken();
							}
							filename= sb.toString();
						}else
						{	ttrd.unreadToken();
							// Makros expandieren aber nur den Rest der Zeile lesen
							readToken();
							if(actToken instanceof TxtTokString)
							{	homeFirst= true;
								filename= ((TxtTokString)actToken).getUnEscString();
							}else
							{	throw new TxtReadException(actToken.getFilePos(),
									"Expecting \"FILENAME\" or <FILENAME>");
							}
						}
						readToken();
						if(!actToken.isEndOfFile())
						{	throw new TxtReadException(actToken.getFilePos(),
								"A new line expected");
						}
					}catch(TxtReadException ex)
					{	ex.setNextException(new TxtReadException(
							prprToken.getFilePos(), "Invalid `#include' directive"));
						throw ex;
					}finally
					{	ttrd= oldttrd;
					}
					// System.out.println(prprToken.getFilePos() + ": #include: "
					//	+ homeFirst + ", " + filename);
					openIncludeFile(prprToken.getFilePos(), filename, homeFirst);
					actToken= ttrd.readToken();
					continue;
				}else if("line".equals(directiveName))
				{	// Makros expandieren aber nur den Rest der Zeile lesen
					TxtTokenReader oldttrd= ttrd;
					ttrd= new TxtTokLineReader(oldttrd);
					int linediff;
					String filename;
					try
					{	readToken();
						if(actToken.isEndOfFile()
							|| !(actToken instanceof TxtTokDigits))
						{	throw new TxtReadException(actToken.getFilePos(),
								"Line number missing");
						}
						linediff= ((TxtTokDigits)actToken).getInt() - 1
							- prprToken.getFilePos().getLineNo();
						filename= null;
						readToken();
						if(!actToken.isEndOfFile())
						{	if(!(actToken instanceof TxtTokString))
							{	throw new TxtReadException(actToken.getFilePos(),
								"File name expected");
							}
							filename= ((TxtTokString)actToken).getUnEscString();
							readToken();
							if(!actToken.isEndOfFile())
							{	throw new TxtReadException(actToken.getFilePos(),
									"A new line expected");
							}
						}
					}catch(TxtReadException ex)
					{	ex.setNextException(new TxtReadException(
							prprToken.getFilePos(), "Invalid `#line' directive"));
						throw ex;
					}finally
					{	ttrd= oldttrd;
					}
					//System.out.println("#line: +" + linediff + ", " + filename);
					// ttrd has to be a TxtReader
					TxtReader trd= (TxtReader)ttrd;
					trd.setLineFile(trd.getLineNo() + linediff, filename);
					actToken= ttrd.readToken();
					continue;
				}else if("error".equals(directiveName))
				{	// Ohne Makroexpansion den Rest der Zeile lesen
					StringBuffer sb= new StringBuffer("#error");
					actToken= ttrd.readToken();
					while(!actToken.isEndOfFile() && !actToken.isAfterNewLine())
					{	sb.append(actToken.toString());
						actToken= ttrd.readToken();
					}
					throw new TxtReadException(prprToken.getFilePos(),
						sb.toString());
					// // actToken= ttrd.readToken();
					// continue;
				}else if("warning".equals(directiveName))
				{	// Ohne Makroexpansion den Rest der Zeile lesen
					StringBuffer sb= new StringBuffer("#warning");
					actToken= ttrd.readToken();
					while(!actToken.isEndOfFile() && !actToken.isAfterNewLine())
					{	sb.append(actToken.toString());
						actToken= ttrd.readToken();
					}
					//throw new TxtReadException(prprToken.getFilePos(),
					System.out.println(prprToken.getFilePos().toString() + ": "
						+ sb.toString());
					// actToken= ttrd.readToken();
					continue;
				}else if("pragma".equals(directiveName))
				{	// Makros expandieren aber nur den Rest der Zeile lesen
					TxtTokenReader oldttrd= ttrd;
					ttrd= new TxtTokLineReader(oldttrd);
					try
					{	if(pragmaRd != null)
							pragmaRd.readPragma(this);
						while(!actToken.isEndOfFile())
							readToken();
					}catch(TxtReadException ex)
					{	ex.setNextException(new TxtReadException(
							prprToken.getFilePos(), "Invalid `#pragma' directive"));
						throw ex;
					}finally
					{	ttrd= oldttrd;
					}
					actToken= ttrd.readToken();
					continue;
				}else if("if".equals(directiveName))
				{	// Makros expandieren aber nur den Rest der Zeile lesen
					TxtTokenReader oldttrd= ttrd;
					ttrd= new TxtTokLineReader(oldttrd);
					boolean ifbranch;
					try
					{	ifbranch= readExpression2(PRIOR_EX) != 0;
						readToken2();
						if(!actToken.isEndOfFile())
						{	throw new TxtReadException(actToken.getFilePos(),
								"A new line expected");
						}
					}catch(TxtReadException ex)
					{	ex.setNextException(new TxtReadException(
							prprToken.getFilePos(), "Invalid `#if' directive"));
						throw ex;
					}finally
					{	ttrd= oldttrd;
					}
					ifList= new TxtPreProIfList(ifList, (TxtTokWord)prprToken);
					ifList.setIfBranch(ifbranch);
					actToken= readNextBlockStart(!ifbranch);
					continue;
				}else if("ifdef".equals(directiveName))
				{	// Ohne Makroexpansion den Rest der Zeile lesen
					TxtTokWord tokWord= readMacroIdentifier(ttrd.readToken());
					// #ifdef tokWord
					boolean ifbranch= (defines.get(tokWord.getWord()) != null);
					//System.out.println("#ifdef " + tokWord + ", "
					//	+ ifbranch);
					ifList= new TxtPreProIfList(ifList, (TxtTokWord)prprToken);
					ifList.setIfBranch(ifbranch);
					actToken= readNextBlockStart(!ifbranch);
					continue;
				}else if("ifndef".equals(directiveName))
				{	// Ohne Makroexpansion den Rest der Zeile lesen
					TxtTokWord tokWord= readMacroIdentifier(ttrd.readToken());
					// #ifndef tokWord
					boolean ifbranch= (defines.get(tokWord.getWord()) == null);
					//System.out.println("#ifndef " + tokWord + ", "
					//	+ ifbranch);
					ifList= new TxtPreProIfList(ifList, (TxtTokWord)prprToken);
					ifList.setIfBranch(ifbranch);
					actToken= readNextBlockStart(!ifbranch);
					continue;
				}else if("elif".equals(directiveName))
				{	if(ifList == null)
					{	throw new TxtReadException(prprToken.getFilePos(),
							"`#elif' not within a conditional");
					}
					TxtTokWord condTok= ifList.getConditionalToken();
					if(condTok.getWord().equals("else"))
					{	TxtReadException ex= new TxtReadException(
							prprToken.getFilePos(), "`#elif' after `#else'");
						ex.setNextException(new TxtReadException(
							condTok.getFilePos(),
							"Position of the previous `#else'"));
						throw ex;
					}
					// Makros expandieren aber nur den Rest der Zeile lesen
					TxtTokenReader oldttrd= ttrd;
					ttrd= new TxtTokLineReader(oldttrd);
					boolean ifbranch2;
					try
					{	ifbranch2= readExpression2(PRIOR_EX) != 0;
						readToken2();
						if(!actToken.isEndOfFile())
						{	throw new TxtReadException(actToken.getFilePos(),
								"A new line expected");
						}
					}catch(TxtReadException ex)
					{	ex.setNextException(new TxtReadException(
							prprToken.getFilePos(), "Invalid `#elif' directive"));
						throw ex;
					}finally
					{	ttrd= oldttrd;
					}
					ifList.setConditionalToken((TxtTokWord)prprToken);
					boolean ifbranch= ifList.getIfBranch();
					// System.out.println(prprToken.getFilePos() + ": #elif "
					//	+ !ifbranch + ", " + ifbranch2);
					if(ifbranch)
						actToken= readNextBlockStart(ifbranch);
					else
					{	ifList.setIfBranch(ifbranch2);
						actToken= readNextBlockStart(!ifbranch2);
					}
					continue;
				}else if("else".equals(directiveName))
				{	if(ifList == null)
					{	throw new TxtReadException(prprToken.getFilePos(),
							"`#else' not within a conditional");
					}
					TxtTokWord condTok= ifList.getConditionalToken();
					if(condTok.getWord().equals("else"))
					{	TxtReadException ex= new TxtReadException(
							prprToken.getFilePos(), "`#else' after `#else'");
						ex.setNextException(new TxtReadException(
							condTok.getFilePos(),
							"Position of the previous `#else'"));
						throw ex;
					}
					ifList.setConditionalToken((TxtTokWord)prprToken);
					boolean ifbranch= ifList.getIfBranch();
					// System.out.println(prprToken.getFilePos() + ": #else "
					//	+ !ifbranch);
					actToken= readNextBlockStart(ifbranch);
					continue;
				}else if("endif".equals(directiveName))
				{	if(ifList == null)
					{	throw new TxtReadException(prprToken.getFilePos(),
							"`#endif' without `#if' conditional");
					}
					ifList= ifList.getOuterIfList();
					// System.out.println(prprToken.getFilePos() + ": #endif");
					actToken= readNextBlockStart(false);
					continue;
				}else	// Keine bekannte # Anweisung?
				{
					// UNKNOWN #-Token
					ttrd.unreadToken();
					break;
				}
			}
			if(actToken instanceof TxtTokEndOfFile)
			{
				// Main or included file?
				if(ttrd instanceof TxtReader)
				{	// Nicht alle #if abgeschlossen? Empty #if-list?
					if(ifList != null)
					{	TxtTokWord condTok= ifList.getConditionalToken();
						throw new TxtReadException(condTok.getFilePos(),
							"Unterminated `#" + condTok.getWord() + "'");
					}
					// Included file?
					if(!savedIfLists.empty())
					{	ifList= (TxtPreProIfList)savedIfLists.pop();
						if(pragmaRd != null)
							pragmaRd.closeIncludeFile();
					}
				}
				ttrd= ((TxtTokEndOfFile)actToken).getFromTokenReader();
				if(ttrd == null)
				{	ttrd= startRd;
					break;
				}
				// Problem bei
				// #define nichts
				// nichts-Macroaufruf am Anfang einer neuen Zeile
				boolean aNL= actToken.isAfterNewLine();
				actToken= ttrd.readToken();
				if(aNL)
					actToken.setAfterNewLine(aNL);
				continue;
			}
			break;
		}
		return actToken;
	}

	/**
	 */
	private TxtToken readNextBlockStart(boolean readOver)
		throws TxtReadException
	{
		TxtToken startToken= ttrd.readToken();
		if(!startToken.isEndOfFile() && !startToken.isAfterNewLine())
		{	throw new TxtReadException(startToken.getFilePos(),
				"A new line expected");
		}
		if(!readOver)	// Do not read over the branch?
			return startToken;
		int ifcnt= 0;
		// until #else, #elif, #endif
		do
		{	if(startToken instanceof TxtTokSepChar
				&& ((TxtTokSepChar)startToken).getChar() == '#')
			{
				TxtToken prprToken= ttrd.readToken();
				String directiveName= (prprToken instanceof TxtTokWord
					&& !prprToken.isAfterNewLine())?
					((TxtTokWord)prprToken).getWord(): null;
				// System.out.println(prprToken.getFilePos() + ": swallow #"
				//	+ directiveName);
				if("if".equals(directiveName))
				{	ifList= new TxtPreProIfList(ifList, (TxtTokWord)prprToken);
					ifcnt++;
				}else if("ifdef".equals(directiveName))
				{	ifList= new TxtPreProIfList(ifList, (TxtTokWord)prprToken);
					ifcnt++;
				}else if("ifndef".equals(directiveName))
				{	ifList= new TxtPreProIfList(ifList, (TxtTokWord)prprToken);
					ifcnt++;
				}else if("elif".equals(directiveName))
				{	if(ifcnt == 0)
					{	ttrd.unreadToken();
						break;
					}
					TxtTokWord condTok= ifList.getConditionalToken();
					if(condTok.getWord().equals("else"))
					{	TxtReadException ex= new TxtReadException(
							prprToken.getFilePos(), "`#elif' after `#else'");
						ex.setNextException(new TxtReadException(
							condTok.getFilePos(),
							"Position of the previous `#else'"));
						throw ex;
					}
					ifList.setConditionalToken((TxtTokWord)prprToken);
				}else if("else".equals(directiveName))
				{	if(ifcnt == 0)
					{	ttrd.unreadToken();
						break;
					}
					TxtTokWord condTok= ifList.getConditionalToken();
					if(condTok.getWord().equals("else"))
					{	TxtReadException ex= new TxtReadException(
							prprToken.getFilePos(), "`#else' after `#else'");
						ex.setNextException(new TxtReadException(
							condTok.getFilePos(),
							"Position of the previous `#else'"));
						throw ex;
					}
					ifList.setConditionalToken((TxtTokWord)prprToken);
				}else if("endif".equals(directiveName))
				{	if(ifcnt == 0)
					{	ttrd.unreadToken();
						break;
					}
					ifList= ifList.getOuterIfList();
					ifcnt--;
				}
				startToken= prprToken;
			}else
			{	startToken= ttrd.readToken();
			}
			while(!startToken.isEndOfFile() && !startToken.isAfterNewLine())
			{	startToken= ttrd.readToken();
			}
		}while(!startToken.isEndOfFile());
		return startToken;
	}

	/** Token zur"uckstellen
	 */
	public void unreadToken() throws TxtReadException
	{
		if(actToken == null)
		{	throw new TxtReadException(getFilePos(), "Missing Token to unread");
		}
		savedToken= actToken;
		actToken= null;
	}

	/**
	 */
	private void openIncludeFile(TxtFilePos fromPos, String filename,
		boolean homeFirst) throws TxtReadException
	{
		TxtReader trd;
		TxtReadException sumEx= null;
		int incPathCnt= (glIncludePaths == null)? 0: glIncludePaths.length;
		boolean homeTest= true;
		for(int i= 0; homeTest || i < incPathCnt; )
		{
			String testpath;
			if(homeTest && homeFirst)
			{	testpath= homePath + File.separator + filename;
				homeTest= false;
			}else if(i < incPathCnt)
			{	testpath= glIncludePaths[i] + File.separator + filename;
				i++;
			}else
			{	testpath= homePath + File.separator + filename;
				homeTest= false;
			}
			// System.out.println("try " + testpath);
			try
			{	// Die fromFilePos von this.ttrd zeigt leider etwas hinter
				// der Position vom #include, deshalb fromPos
				trd= new TxtReader(testpath, fromPos, this.ttrd);
				this.ttrd= trd;
				savedIfLists.push(ifList);
				ifList= null;
				if(pragmaRd != null)
					pragmaRd.openIncludeFile(testpath);
				return;
			}catch(TxtReadException ex)
			{	if(sumEx == null)
					sumEx= ex;
				else
					sumEx.setNextException(ex);
			}
		}
		sumEx.setNextException(new TxtReadException(fromPos,
			"`#include " + filename + "' failed"));
		throw sumEx;
	}

	/** Use to unread 2 Tokens
	 */
	private TxtToken savedToken2;
	private int unreadCnt;

	/** Token zur"uckstellen
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public void unreadToken2() throws TxtReadException
	{
		if(unreadCnt != 1)
		{	unreadToken();
			// System.out.println("unread21: " + savedToken);
			actToken= savedToken2;
			unreadCnt++;
			return;
		}
		savedToken2= actToken;
		// System.out.println("unread22: " + savedToken2);
		actToken= null;
		unreadCnt++;
		return;
	}

	/** Token lesen
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public TxtToken readToken2() throws TxtReadException
	{
		if(unreadCnt > 1)
		{	actToken= savedToken2;
			// System.out.println("read22: " + actToken);
			unreadCnt--;
			return actToken;
		}
		savedToken2= actToken;
		readToken();
		// System.out.println("read21: " + actToken);
		unreadCnt= 0;
		return actToken;
	}

	/**
	 */
	private final static int PRIOR_EX= 0;	// To execute all operators
	private final static int PRIOR_CM= 1;	// ´,´ and ´?:´ operator
	private final static int PRIOR_LOR=2;	// ´||´
	private final static int PRIOR_LAND=3;	// ´&&´
	private final static int PRIOR_OR=4;	// ´|´
	private final static int PRIOR_XOR=5;	// ´^´
	private final static int PRIOR_AND=6;	// ´&´
	private final static int PRIOR_EQ=7;	// ´==´ and  ´!=´
	private final static int PRIOR_CP=8;	// ´>´, ´<´, ´>=´, ´<=´
	private final static int PRIOR_SHIFT=9;	// ´>>´ and ´<<´
	private final static int PRIOR_AS=10;	// ´+´ and ´-´
	private final static int PRIOR_MD=11;	// ´*´, ´/´, ´%´
	private final static int PRIOR_UN=12;	// ´+´, ´-´, ´~´, ´!´, defined
	
	/** Read an expression until finding an operator with less or equal
	 *  priority of prevPrior or unexpected token
	 *
	 *	@param		prevPrior
	 *	@return				result
	 *
	 *	@exception	TxtReadException
	 */
	private long readExpression2(int prevPrior) throws TxtReadException
	{
		long result;
		
		/* Zuerst wird eine Variable, eine Funktion
		   oder ein Wert erwartet.
		*/
		readToken2();	// Read actToken with macro expansion
		if(actToken instanceof TxtTokSepChar)
		{	char sepCh= ((TxtTokSepChar)actToken).getChar();
			if(sepCh == '+')
			{	result= readExpression2(PRIOR_UN);
			}else if(sepCh == '-')
			{	result= -readExpression2(PRIOR_UN);
			}else if(sepCh == '~')
			{	result= ~readExpression2(PRIOR_UN);
			}else if(sepCh == '!')
			{	result= (readExpression2(PRIOR_UN) != 0)? 0: 1;
			}else if(sepCh == '(')
			{	result= readExpression2(PRIOR_EX);
				readToken2();
				if(!(actToken instanceof TxtTokSepChar) ||
					((TxtTokSepChar)actToken).getChar() != ')')
				{	throw new TxtReadException(actToken.getFilePos(),
						"`)' expected");
				}
			}else
			{	throw new TxtReadException(actToken.getFilePos(),
					"Integer expression expected");
			}
		}else if(actToken instanceof TxtTokDigits)
		{	// Integer Constant
			TxtFilePos dPos= actToken.getFilePos();
			String digits= ((TxtTokDigits)actToken).getDigits();
			// Read Suffix
			String suffix;
			readToken2();
			if(!actToken.isAfterWhiteSpaces() &&
				actToken instanceof TxtTokWord)
			{	suffix= ((TxtTokWord)actToken).getWord();
			}else
			{	unreadToken2();
				suffix= null;
			}
			// System.out.println("Digits= " + digits);
			// Octal or Hex?
			if(digits.charAt(0) == '0')
			{	// Hex?
				int digit;
				if(digits.length() == 1 && suffix != null && suffix.length() > 1
					&& (suffix.charAt(0) == 'x' || suffix.charAt(0) == 'X')
					&& (digit= Character.digit(suffix.charAt(1), 16)) >= 0)
				{	// 0xf...
					result= digit;
					int pos= 2;
					while(pos < suffix.length()
						&& (digit= Character.digit(suffix.charAt(pos), 16)) >= 0)
					{	result *= 16;
						result += digit;
						pos++;
					}
					suffix= suffix.substring(pos);
				}else
				{	// Octal
					result= 0;
					for(int pos= 0; pos < digits.length(); pos++)
					{	digit= Character.digit(digits.charAt(pos), 8);
						if(digit < 0)
						{	throw new TxtReadException(dPos,
								"Octal digits expected");
						}
						result *= 8;
						result += digit;
					}
				}
			}else
			{	// Decimal
				result= 0;
				for(int pos= 0; pos < digits.length(); pos++)
				{	result *= 10;
					result += Character.digit(digits.charAt(pos), 10);
				}
			}
			// Integer Suffix: u, U, l, L
			if(suffix != null && suffix.length() > 0)
			{	// System.out.println("Integer Suffix= " + suffix);
				if(suffix.length() > 2 ||
					!(((suffix.charAt(0) == 'u' || suffix.charAt(0) == 'U')
						&& (suffix.length() == 1 ||
						suffix.charAt(1) == 'l' || suffix.charAt(1) == 'L'))
					|| ((suffix.charAt(0) == 'l' || suffix.charAt(0) == 'L')
						&& (suffix.length() == 1 ||
						suffix.charAt(1) == 'u' || suffix.charAt(1) == 'U'))))
				{	
					throw new TxtReadException(actToken.getFilePos(),
						"Invalid integer suffix `" + suffix + "'");
				}
			}
		}else if(actToken instanceof TxtTokWord)
		{	if(((TxtTokWord)actToken).getWord().equals("defined"))
			{	// Reading without macro expansion
				TxtToken token= ttrd.readToken();
				boolean bracket= false;
				if(token instanceof TxtTokSepChar &&
					((TxtTokSepChar)token).getChar() == '(')
				{	bracket= true;
					token= ttrd.readToken();
				}
				TxtTokWord tokWord= readMacroIdentifier(token);
				result= (defines.get(tokWord.getWord()) != null)? 1: 0;
				if(bracket)
				{	readToken2();
					if(!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != ')')
					{	throw new TxtReadException(actToken.getFilePos(),
							"`)' expected");
					}
				}
			}else
			{	throw new TxtReadException(actToken.getFilePos(),
					"Integer expression expected");
			}
		}else if(actToken instanceof TxtTokEndOfFile)
		{	throw new TxtReadException(actToken.getFilePos(),
				"Missing an integer expression");
		}else
		{	throw new TxtReadException(actToken.getFilePos(),
				"Integer expression expected");
		}
		
		/* Nachdem der erste Wert oder die erste Funktion eingelesen wurde...
		*/
		for(; ; )
		{	readToken2();
			// System.out.println("readOp: " + result + " " + actToken);
			if(actToken instanceof TxtTokSepChar)
			{	char sepCh= ((TxtTokSepChar)actToken).getChar();
				if(sepCh == '+')
				{	if(prevPrior >= PRIOR_AS)
						break;
					result += readExpression2(PRIOR_AS);
				}else if(sepCh == '-')
				{	if(prevPrior >= PRIOR_AS)
						break;
					result -= readExpression2(PRIOR_AS);
				}else if(sepCh == '*')
				{	if(prevPrior >= PRIOR_MD)
						break;
					result *= readExpression2(PRIOR_MD);
				}else if(sepCh == '/')
				{	if(prevPrior >= PRIOR_MD)
						break;
					long result2= readExpression2(PRIOR_MD);
					if(result2 == 0)
					{	throw new TxtReadException(actToken.getFilePos(),
							"Division by zero");
					}
					result /= result2;
				}else if(sepCh == '%')
				{	if(prevPrior >= PRIOR_MD)
						break;
					result %= readExpression2(PRIOR_MD);
				}else if(sepCh == '^')
				{	if(prevPrior >= PRIOR_XOR)
						break;
					result ^= readExpression2(PRIOR_XOR);
				}else if(sepCh == ',')
				{	if(prevPrior >= PRIOR_CM)
						break;
					result= readExpression2(PRIOR_CM);
				}else if(sepCh == '?')
				{	if(prevPrior >= PRIOR_CM)
						break;
					long cond= result;
					result= readExpression2(PRIOR_EX);
					readToken2();
					if(!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != ':')
					{	throw new TxtReadException(actToken.getFilePos(),
							"`:' expected");
					}
					if(cond != 0)
					{	readExpression2(PRIOR_CM);
					}else
					{	result= readExpression2(PRIOR_CM);
					}
				}else if(sepCh == '&')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '&')
					{	unreadToken2();
						if(prevPrior >= PRIOR_AND)
							break;
						result &= readExpression2(PRIOR_AND);
					}else
					{	if(prevPrior >= PRIOR_LAND)
						{	unreadToken2();
							break;
						}
						if(result == 0)
						{	result= 0; readOverExpression2(PRIOR_LAND);
						}else
						{	result= (readExpression2(PRIOR_LAND) != 0)? 1: 0;
						}
					}
				}else if(sepCh == '|')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '|')
					{	unreadToken2();
						if(prevPrior >= PRIOR_OR)
							break;
						result |= readExpression2(PRIOR_OR);
					}else
					{	if(prevPrior >= PRIOR_LOR)
						{	unreadToken2();
							break;
						}
						if(result != 0)
						{	result= 1; readOverExpression2(PRIOR_LOR);
						}else
						{	result= (readExpression2(PRIOR_LOR) != 0)? 1: 0;
						}
					}
				}else if(sepCh == '=')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '=')
					{	unreadToken2();
						break;
					}else
					{	if(prevPrior >= PRIOR_EQ)
						{	unreadToken2();
							break;
						}
						result= (result == readExpression2(PRIOR_EQ))? 1: 0;
					}
				}else if(sepCh == '!')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '=')
					{	unreadToken2();
						break;
					}else
					{	if(prevPrior >= PRIOR_EQ)
						{	unreadToken2();
							break;
						}
						result= (result != readExpression2(PRIOR_EQ))? 1: 0;
					}
				}else if(sepCh == '>')
				{	readToken2();
					if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '>')
					{	if(prevPrior >= PRIOR_SHIFT)
							break;
						result >>= (int)readExpression2(PRIOR_SHIFT);
					}else if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '=')
					{	if(prevPrior >= PRIOR_CP)
							break;
						result= (result >= readExpression2(PRIOR_CP))? 1: 0;
					}else
					{	unreadToken2();
						if(prevPrior >= PRIOR_CP)
						{	unreadToken2();
							break;
						}
						result= (result > readExpression2(PRIOR_CP))? 1: 0;
					}
				}else if(sepCh == '<')
				{	readToken2();
					if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '<')
					{	if(prevPrior >= PRIOR_SHIFT)
							break;
						result <<= (int)readExpression2(PRIOR_SHIFT);
					}else if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '=')
					{	if(prevPrior >= PRIOR_CP)
							break;
						result= (result <= readExpression2(PRIOR_CP))? 1: 0;
					}else
					{	unreadToken2();
						if(prevPrior >= PRIOR_CP)
						{	unreadToken2();
							break;
						}
						result= (result < readExpression2(PRIOR_CP))? 1: 0;
					}
				}else
				{	break;
				}
			}else
			{	break;
			}
		}
		// System.out.println("unreadOp: " + result + " " + actToken);
		unreadToken2();
		return result;
	}

	/** Read over an expression until finding an operator with less or equal
	 *  priority of prevPrior or unexpected token
	 *
	 *	@param		prevPrior
	 *	@param		value
	 *	@return				result
	 *
	 *	@exception	TxtReadException
	 */
	private void readOverExpression2(int prevPrior) throws TxtReadException
	{
		/* Zuerst wird eine Variable, eine Funktion
		   oder ein Wert erwartet.
		*/
		readToken2();	// Read actToken with macro expansion
		if(actToken instanceof TxtTokSepChar)
		{	char sepCh= ((TxtTokSepChar)actToken).getChar();
			if(sepCh == '+')
			{	readOverExpression2(PRIOR_UN);
			}else if(sepCh == '-')
			{	readOverExpression2(PRIOR_UN);
			}else if(sepCh == '~')
			{	readOverExpression2(PRIOR_UN);
			}else if(sepCh == '!')
			{	readOverExpression2(PRIOR_UN);
			}else if(sepCh == '(')
			{	readOverExpression2(PRIOR_EX);
				readToken2();
				if(!(actToken instanceof TxtTokSepChar) ||
					((TxtTokSepChar)actToken).getChar() != ')')
				{	throw new TxtReadException(actToken.getFilePos(),
						"`)' expected");
				}
			}else
			{	throw new TxtReadException(actToken.getFilePos(),
					"Integer expression expected");
			}
		}else if(actToken instanceof TxtTokDigits)
		{	// Integer Constant
			TxtFilePos dPos= actToken.getFilePos();
			String digits= ((TxtTokDigits)actToken).getDigits();
			// Read Suffix
			String suffix;
			readToken2();
			if(!actToken.isAfterWhiteSpaces() &&
				actToken instanceof TxtTokWord)
			{	suffix= ((TxtTokWord)actToken).getWord();
			}else
			{	unreadToken2();
				suffix= null;
			}
			// System.out.println("Digits= " + digits);
			// Octal or Hex?
			if(digits.charAt(0) == '0')
			{	// Hex?
				if(digits.length() == 1 && suffix != null && suffix.length() > 1
					&& (suffix.charAt(0) == 'x' || suffix.charAt(0) == 'X')
					&& Character.digit(suffix.charAt(1), 16) >= 0)
				{	// 0xf...
					int pos= 2;
					while(pos < suffix.length()
						&& Character.digit(suffix.charAt(pos), 16) >= 0)
					{	pos++;
					}
					suffix= suffix.substring(pos);
				}else
				{	// Octal
					for(int pos= 0; pos < digits.length(); pos++)
					{	if(Character.digit(digits.charAt(pos), 8) < 0)
						{	throw new TxtReadException(dPos,
								"Octal digits expected");
						}
					}
				}
			}else
			{	// Decimal
			}
			// Integer Suffix: u, U, l, L
			if(suffix != null && suffix.length() > 0)
			{	// System.out.println("Integer Suffix= " + suffix);
				if(suffix.length() > 2 ||
					!(((suffix.charAt(0) == 'u' || suffix.charAt(0) == 'U')
						&& (suffix.length() == 1 ||
						suffix.charAt(1) == 'l' || suffix.charAt(1) == 'L'))
					|| ((suffix.charAt(0) == 'l' || suffix.charAt(0) == 'L')
						&& (suffix.length() == 1 ||
						suffix.charAt(1) == 'u' || suffix.charAt(1) == 'U'))))
				{	
					throw new TxtReadException(actToken.getFilePos(),
						"Invalid integer suffix `" + suffix + "'");
				}
			}
		}else if(actToken instanceof TxtTokWord)
		{	if(((TxtTokWord)actToken).getWord().equals("defined"))
			{	// Reading without macro expansion
				TxtToken token= ttrd.readToken();
				boolean bracket= false;
				if(token instanceof TxtTokSepChar &&
					((TxtTokSepChar)token).getChar() == '(')
				{	bracket= true;
					token= ttrd.readToken();
				}
				TxtTokWord tokWord= readMacroIdentifier(token);
				if(bracket)
				{	readToken2();
					if(!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != ')')
					{	throw new TxtReadException(actToken.getFilePos(),
							"`)' expected");
					}
				}
			}
			// readOver assumes that a word is an integer
		}else if(actToken instanceof TxtTokEndOfFile)
		{	throw new TxtReadException(actToken.getFilePos(),
				"Missing an integer expression");
		}else
		{	throw new TxtReadException(actToken.getFilePos(),
				"Integer expression expected");
		}
		
		/* Nachdem der erste Wert oder die erste Funktion eingelesen wurde...
		*/
		for(; ; )
		{	readToken2();
			if(actToken instanceof TxtTokSepChar)
			{	char sepCh= ((TxtTokSepChar)actToken).getChar();
				// System.out.println("OpOver= " + sepCh);
				if(sepCh == '+' || sepCh == '-')
				{	if(prevPrior >= PRIOR_AS)
						break;
					readOverExpression2(PRIOR_AS);
				}else if(sepCh == '*' || sepCh == '/' || sepCh == '%')
				{	if(prevPrior >= PRIOR_MD)
						break;
					readOverExpression2(PRIOR_MD);
				}else if(sepCh == '^')
				{	if(prevPrior >= PRIOR_XOR)
						break;
					readOverExpression2(PRIOR_XOR);
				}else if(sepCh == ',')
				{	if(prevPrior >= PRIOR_CM)
						break;
					readOverExpression2(PRIOR_CM);
				}else if(sepCh == '?')
				{	if(prevPrior >= PRIOR_CM)
						break;
					readOverExpression2(PRIOR_EX);
					readToken2();
					if(!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != ':')
					{	throw new TxtReadException(actToken.getFilePos(),
							"`:' expected");
					}
					readOverExpression2(PRIOR_CM);
				}else if(sepCh == '&')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '&')
					{	unreadToken2();
						if(prevPrior >= PRIOR_AND)
							break;
						readOverExpression2(PRIOR_AND);
					}else
					{	if(prevPrior >= PRIOR_LAND)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_LAND);
					}
				}else if(sepCh == '|')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '|')
					{	unreadToken2();
						if(prevPrior >= PRIOR_OR)
							break;
						readOverExpression2(PRIOR_OR);
					}else
					{	if(prevPrior >= PRIOR_LOR)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_LOR);
					}
				}else if(sepCh == '=')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '=')
					{	unreadToken2();
						break;
					}else
					{	if(prevPrior >= PRIOR_EQ)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_EQ);
					}
				}else if(sepCh == '!')
				{	readToken2();
					if(actToken.isAfterWhiteSpaces() ||
						!(actToken instanceof TxtTokSepChar) ||
						((TxtTokSepChar)actToken).getChar() != '=')
					{	unreadToken2();
						break;
					}else
					{	if(prevPrior >= PRIOR_EQ)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_EQ);
					}
				}else if(sepCh == '>')
				{	readToken2();
					if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '>')
					{	if(prevPrior >= PRIOR_SHIFT)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_SHIFT);
					}else if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '=')
					{	if(prevPrior >= PRIOR_CP)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_CP);
					}else
					{	unreadToken2();
						if(prevPrior >= PRIOR_CP)
							break;
						readOverExpression2(PRIOR_CP);
					}
				}else if(sepCh == '<')
				{	readToken2();
					if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '<')
					{	if(prevPrior >= PRIOR_SHIFT)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_SHIFT);
					}else if(!actToken.isAfterWhiteSpaces() &&
						(actToken instanceof TxtTokSepChar) &&
						((TxtTokSepChar)actToken).getChar() == '=')
					{	if(prevPrior >= PRIOR_CP)
						{	unreadToken2();
							break;
						}
						readOverExpression2(PRIOR_CP);
					}else
					{	unreadToken2();
						if(prevPrior >= PRIOR_CP)
							break;
						readOverExpression2(PRIOR_CP);
					}
				}else
				{	break;
				}
			}else
			{	break;
			}
		}
		unreadToken2();
	}
}
