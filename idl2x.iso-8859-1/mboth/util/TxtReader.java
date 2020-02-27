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

import java.io.*;

/**
 * @author  Martin Both
 */

public class TxtReader implements TxtTokenReader
{
	private String name;		// Dateiname
	
	private Reader frd;
	
	/** fromReader oder null
	 */
	private TxtTokenReader fromReader;

	/** fromFilePos oder null
	 */
	private TxtFilePos fromFilePos;
	
	// Pufferaufbau:
	//
	//          BufPrevSize            BufSize
	//      +------------------+----------------------+
	//      ^          ^            ^            ^
	//  BufStart    BufPrev       BufPtr       BufEnd
	
	/** Anzahl der Zeichen die beim Lesen des n"achsten Blocks
	 *  von dem alten Block in den neuen Block stehen bleiben.
	 */
	private int bufPrevSize;

	/** Gr"o"se des Leseblocks
	 */
	private int bufSize;
	
	/** Input buffer
	 */
	private char bufStart[];
	
	/** Next byte to read
	 */
	private int bufPtr;
	
	/** Erstes Byte des Zur"ucklese-Puffers
	 */
	private int bufPrev;
	
	/** First byte after buffer (Ende der Daten)
	 */
	private int bufEnd;
	
	/** Anzahl der bisher vom unterliegendem Reader verarbeiteten Bytes
	 */
	private int posOffset;
	
	/** Dateiende noch nicht erreicht?
	 */
	private boolean posMore;

	/** Line counter
	 */
	private int lineno;
	private int linepos;		// BufPos when lineno is set
	
	/** First Token in a file?
	 */
	private int tokflags;

	/** Use to unread Token
	 */
	private TxtToken actToken, savedToken;
	
	/** 
	 */
	private TxtReadException constwarnex;
	
	/** 
	 *  @param	txtName		Pseudo name
	 *  @param	text		Pseudo file
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public TxtReader(String txtName, String text) throws TxtReadException
	{
		name= txtName;
		if(text == null)
			throw new TxtReadException(name, "File not found");
		posMore= false;
		bufPrevSize= 0;
		bufStart= text.toCharArray();
		bufSize= bufStart.length;
		posOffset= bufSize;
		bufPrev= 0;
		bufEnd= bufSize;			/* Lesepuffer ist voll */
		bufPtr= 0;
		lineno= 1;
		tokflags= TxtToken.PreFileStart;
	}

	/** 
	 *  @param	fileName	Filename
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public TxtReader(String fileName) throws TxtReadException
	{
		open(fileName);
	}

	/** 
	 *  @param	fileName	Filename
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public TxtReader(String fileName, TxtFilePos fromFilePos,
		TxtTokenReader fromReader) throws TxtReadException
	{
		this.fromReader= fromReader;
		this.fromFilePos= fromFilePos;
		open(fileName);
	}

	/** 
	 *  @param	fileName		Filename
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	private void open(String fileName) throws TxtReadException
	{
		name= fileName;
		try
		{	frd= new FileReader(fileName);
		}catch(FileNotFoundException ex)
		{	throw new TxtReadException(new TxtFilePos(name, fromFilePos),
				"File not found");
		}
		posMore= true;
		posOffset= 0;
		bufPrevSize= 4;
		bufSize= 8192;
		bufStart= new char[bufPrevSize + bufSize];
		bufPrev= bufPrevSize;
		bufEnd= bufPrev;
		bufPtr= bufEnd;			/* Lesepuffer ist leer */
		lineno= 1;
		tokflags= TxtToken.PreFileStart;
	}

	/**
	 */
	public String getFileName()
	{
		return name;
	}
	
	/** Gibt die Anzahl der bisher gelesenen Zeichen zur"uck.
	 */
	public int getFilePtrPos()
	{
		return posOffset + bufPtr - bufEnd;
	}
	
	/** Auff"ullen des Lesepuffers mit dem n"achsten Block.
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	private boolean fill() throws TxtReadException
	{
		if(!posMore)
			return false;
		if(bufPtr < bufEnd)
			return true;
		
		if(bufEnd > bufPrevSize)
		{
			/* Bytes in den PrevPuffer "ubernehmen
			*/
			bufPtr= bufPrev;
			bufPrev= bufPrevSize - (bufEnd - bufPrev);
			if(bufPrev < 0)
			{
				bufPrev= 0;
				bufPtr= bufEnd - bufPrevSize;
			}
			for(int newPrev= bufPrev; newPrev < bufPrevSize; )
			{
				bufStart[newPrev++]= bufStart[bufPtr++];
			}
		}

		bufPtr= bufEnd= bufPrevSize;
		try
		{	int count= frd.read(bufStart, bufPrevSize, bufSize);
			if(count > 0)
			{
				bufEnd += count;	/* Puffer ist voll */
				posOffset += count;	/* count Bytes gelesen */
			}else
			{						/* Bei Dateiende */
				posMore= false;
			}
		}catch(IOException ex)
		{	throw new TxtReadException(new TxtFilePos(name, fromFilePos),
				"IO Exception, " + ex.getMessage());
		}
		return posMore;
	}
	
	/** N"achstes (eventuell zur"uckgestelltes) Zeichen lesen. Bei
	 *  "Uberschreitung des Dateiendes wird der Wert EOF zur"uckgeliefert.
	 *  Mehrmaliger Aufruf trotz EOF (end of file) garantiert m"oglich.
	 *  Mit unreadChar() k"onnen hiervon min. bufPrevSize Zeichen
	 *  zur"uckgestellt werden.
	 *
	 *  @return		Character, or -1 if end of stream has been reached
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public int readChar() throws TxtReadException
	{
		if(bufPtr >= bufEnd)		/* Puffer leer? */
		{
			if(!fill())				/* dann auff"ullen */
			{
				if(bufPtr > bufEnd)
				{	throw new TxtReadException(new TxtFilePos(name, fromFilePos),
						"Cannot read after end of file");
				}
				bufPtr++;			/* So EOF can be unread */
				return -1;			/* Dateiende erreicht! */
			}
		}
		return bufStart[bufPtr++];
	}
	
	/** Stellt Zeichen f"ur eine erneute Leseoperation zur"uck, die vorher
	 *  mit readChar() gelesen wurden, ist also tats"achlich das
	 *  Gegenteil von readChar(). Es k"onnen minimal bufPrevSize Zeichen
	 *  zur"uckgelesen werden. Nach einem file_seek() mit einer gr"o"seren
	 *  Distanz als bufPrevSize funktioniert die Funktion nicht.
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public void unreadChar() throws TxtReadException
	{
		if(bufPtr <= bufPrev)		/* Pufferanfang erreicht? */
		{	throw new TxtReadException(new TxtFilePos(name, fromFilePos),
				"unreadChar: prevbuf overflow");
		}
		bufPtr--;
	}

	/**
	 */
	public int getLineNo()
	{
		return lineno;
	}

	/**
	 */
	public int getColumnNo()
	{
		return posOffset + bufPtr - bufEnd - linepos;
	}
	
	/**
	 */
	public TxtFilePos getFilePos()
	{
		return new TxtFilePos(name, lineno, getColumnNo(), fromFilePos);
	}

	/** Auf Leerzeichen und Drucksteuerzeichen testen
	 *
	 *  @param	ch	Character or EOF
	 *  @return		true if the character is a blank
	 */
	public static boolean isBlank(int ch)
	{
		// (ch != -1 && Character.isWhitespace((char)ch)
		//	&& ch != '\n' && ch != '\r')
		if(ch < 0 || ch > ' ')
			return false;
		// u000B, VERTICAL TABULATION
		// u000C, FORM FEED
		return (ch == ' ' || ch == '\t' || ch == '\u000B' || ch == '\f');
	}

	/** Auf Zeilenumbruch testen
	 *
	 *  @param	ch	Character or EOF
	 *  @return		true if the character is a beginning line break
	 */
	public static boolean isLineBreak(int ch)
	{
		return (ch == '\n' || ch == '\r');
	}

	/** Zeilenumbruch "uberlesen und n"achstes Zeichen einlesen
	 *
	 *  @param	ch	First character or EOF
	 *  @return		Was a line break?
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public boolean swallowLineBreak(int ch) throws TxtReadException
	{
		if(ch == '\n')
		{	lineno++;
			linepos= getFilePtrPos();
			return true;
		}
		if(ch == '\r')
		{	if(readChar() != '\n')
				unreadChar();
			lineno++;
			linepos= getFilePtrPos();
			return true;
		}
		return false;
	}

	/** N"achstes (eventuell zur"uckgestelltes) Token-Zeichen lesen. Bei
	 *  "Uberschreitung des Dateiendes wird der Wert EOF zur"uckgeliefert.
	 *  Mit unreadChar() kann hiervon max. ein Zeichen zur"uckgestellt werden.
	 *
	 *  @return		Character, or -1 if end of stream has been reached
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public int readTokChar() throws TxtReadException
	{
		int ch;
		
		while((ch= readChar()) == '\\')
		{
			if(!swallowLineBreak(readChar()))
			{	unreadChar();
				break;
			}
		}
		return ch;
	}

	/** Stellt ein Zeichen f"ur eine erneute Leseoperation zur"uck, das vorher
	 *  mit readTokChar() gelesen wurden, ist also tats"achlich das
	 *  Gegenteil von readTokChar().
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public void unreadTokChar() throws TxtReadException
	{
		unreadChar();
	}

	/** C-Kommentar "uberlesen.
	 *
	 *  @param	iCh		First character or EOF
	 *  @return			Was a comment?
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public boolean swallowComment(int iCh) throws TxtReadException
	{
		if(iCh != '/')			// No comment?
			return false;
		iCh= readTokChar();		// read second character
		if(iCh == '/')
		{	do					// Kommentar bis zum Zeilenumbruch "uberlesen
			{	iCh= readTokChar();
			}while(!isLineBreak(iCh) && iCh != -1);
			unreadTokChar();
		}else if(iCh == '*')
		{	int startline= lineno;
			iCh= readTokChar();
			for(; ; )
			{	if(iCh == -1)
				{	throw new TxtReadException(
						new TxtFilePos(name, startline, fromFilePos),
						"Unterminated Comment");
				}
				if(!swallowLineBreak(iCh) && iCh == '*')
				{	iCh= readTokChar();
					if(iCh == '/')	// End of comment?
						break;
					continue;
				}
				iCh= readTokChar();
			}
		}else
		{	unreadTokChar();		// Es war kein Kommentar
			return false;
		}
		return true;
	}
	
	/** Execute `#line' command
	 */
	public void setLineFile(int lineno, String filename)
	{
		int linediff= lineno - this.lineno;
		this.lineno= lineno;
		if(filename != null)
			this.name= filename;
		
		if(savedToken != null)
		{
			TxtFilePos fPos= savedToken.getFilePos();
			fPos.setLineFile(fPos.getLineNo() + linediff, filename);
		}
	}
	
	/** Token lesen
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public TxtToken readToken() throws TxtReadException
	{
		if(savedToken != null)
		{
			actToken= savedToken;
			savedToken= null;
			tokflags= 0;
			return actToken;
		}
		
		int iCh= readTokChar();
		for(; ; )
		{	if(TxtReader.isBlank(iCh) || swallowComment(iCh))
			{	do
				{	iCh= readTokChar();
				}while(TxtReader.isBlank(iCh) || swallowComment(iCh));
				// unreadTokChar();
				tokflags |= TxtToken.PreBlanks;		// return tokBlank;
			}
			if(!swallowLineBreak(iCh))
				break;
			tokflags |= TxtToken.PreLineBreak;		// return tokLineBreak;
			iCh= readTokChar();
		}
		TxtFilePos tokpos= getFilePos();
		if(iCh == -1)
		{	actToken= new TxtTokEndOfFile(tokpos, tokflags, fromReader);
			tokflags= 0;
			return actToken;
		}
		if(Character.isJavaIdentifierStart((char)iCh))
		{
			StringBuffer sb= new StringBuffer();
			do
			{	sb.append((char)iCh);
				if((iCh= readTokChar()) == -1)
					break;
			}while(Character.isJavaIdentifierStart((char)iCh)
				|| ((char)iCh >= '0' && (char)iCh <= '9'));
			unreadTokChar();
			actToken= new TxtTokWord(tokpos, tokflags, sb.toString());
			tokflags= 0;
			return actToken;
		}
		if((char)iCh == '"' || (char)iCh == '\'')
		{
			char type= (char)iCh;
			StringBuffer sb= new StringBuffer();
			for(; ; )
			{
				if((iCh= readTokChar()) == -1)
				{
					TxtReadException ex;
					ex= new TxtReadException(tokpos,
						"Unterminated string or character constant");
					if(constwarnex != null)
					{	constwarnex.setNextException(ex);
						throw constwarnex;
					}
					throw ex;
				}
				if((char)iCh == type)
					break;
				if(TxtReader.isLineBreak(iCh))
				{	if(constwarnex == null)
						constwarnex= new TxtReadException(tokpos,
							"Warning: String or character constant with line break");
					if((char)iCh == '\r')
					{	if(readTokChar() != '\n')
						{	unreadTokChar();
						}else
						{	sb.append((char)iCh);
							iCh= '\n';
						}
					}
					lineno++;
					linepos= getFilePtrPos();
				}else if((char)iCh == '\\')
				{
					sb.append((char)iCh);
					
					TxtFilePos escpos= getFilePos();
					if((iCh= readTokChar()) == -1)
					{
						TxtReadException ex;
						ex= new TxtReadException(tokpos,
							"Unterminated string or character constant");
						if(constwarnex != null)
						{	constwarnex.setNextException(ex);
							throw constwarnex;
						}
						throw ex;
					}
				}
				sb.append((char)iCh);
			}
			actToken= new TxtTokString(tokpos, tokflags, type, sb.toString());
			tokflags= 0;
			return actToken;
		}
		if((char)iCh >= '0' && (char)iCh <= '9')
		{
			StringBuffer sb= new StringBuffer();
			do
			{	sb.append((char)iCh);
				if((iCh= readTokChar()) == -1)
					break;
			}while((char)iCh >= '0' && (char)iCh <= '9');
			unreadTokChar();
			actToken= new TxtTokDigits(tokpos, tokflags, sb.toString());
			tokflags= 0;
			return actToken;
		}
		actToken= new TxtTokSepChar(tokpos, tokflags, (char)iCh);
		tokflags= 0;
		return actToken;
	}

	/** Token zur"uckstellen
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
	public void unreadToken() throws TxtReadException
	{
		if(actToken == null)
		{	throw new TxtReadException(getFilePos(), "Missing Token to unread");
		}
		savedToken= actToken;
		actToken= null;
	}
}
