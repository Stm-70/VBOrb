/* Copyright (C) 1991-2012 Free Software Foundation, Inc.
   This file is part of the GNU C Library.

   The GNU C Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   The GNU C Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with the GNU C Library; if not, see
   <http://www.gnu.org/licenses/>.  */
/* This header is separate from features.h so that the compiler can
   include it implicitly at the start of every compilation.  It must
   not itself include <features.h> or any other header that includes
   <features.h> because the implicit include comes before any feature
   test macros that may be defined in a source file before it first
   explicitly includes a system header.  GCC knows the name of this
   header in order to preinclude it.  */
/* We do support the IEC 559 math functionality, real and complex.  */
/* wchar_t uses ISO/IEC 10646 (2nd ed., published 2011-03-15) /
   Unicode 6.0.  */
/* We do not support C11 <threads.h>.  */
/*
   Copyright (c) 1999-2003 Martin.Both

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public
   License as published by the Free Software Foundation; either
   version 2 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   Library General Public License for more details.
*/
package mboth.idl2c;
import java.util.Hashtable;
import mboth.util.*;
/**
 * @author  Martin Both
 *
 * Direct Known Subclasses:
 *     IdlModule, IdlInterface, IdlObjRefType, IdlOperation,
 *     IdlStruct, IdlUnion, IdlException, IdlValueType
 * Indirect Known Subclasses:
 *     IdlSpecification
 */
public class IdlScope extends IdlIdentifier
{
 /** Scope
	 */
 private Hashtable intrIdentifiers;
 private Hashtable defnIdentifiers;
 /** Scope level constants
	 */
 public final static int SL_DEFN= 1;
 public final static int SL_INHT= 2;
 public final static int SL_INTR= 4;
 public final static int SL_SURR= 8;
 /**
	 *  @param	identifier	Identifier
	 */
 public IdlScope(IdlIdentifier identifier)
 {
  super(identifier);
  this.intrIdentifiers= new Hashtable();
  this.defnIdentifiers= new Hashtable();
 }
 /** (3.15.3 Special Scoping Rules for Type Names, if this is
	 *  an interface or valuetype and iWord is found by searching
	 *  through surrounding scope, so introduce iWord in this scope.)
	 *
	 *  @param	iWord		Identifier searching for
	 *  @param	sls			Scope levels: SL_DEFN, SL_INHT, SL_INTR, SL_SURR
	 *						Main, inherited, introduced, surrounding scopes
	 *	@return				Identifier or null if not found
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public IdlIdentifier getIdentifier(IdlIdentifier iWord, int sls)
  throws TxtReadException
 {
  // System.out.println("getIdentifier: " + getClass().getName()
  //	+ " looking for `" + iWord.getUnEscName() + "', " + sls);
  String hashWord= iWord.getHashWord();
  IdlIdentifier identifier= null;
  if((sls & IdlScope.SL_DEFN) != 0)
  { identifier= (IdlIdentifier)defnIdentifiers.get(hashWord);
  }
  if(identifier == null && (sls & IdlScope.SL_INTR) != 0)
  { identifier= (IdlIdentifier)intrIdentifiers.get(hashWord);
  }
  if(identifier != null)
  { if(!identifier.getUnEscName().equals(iWord.getUnEscName()))
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "IDL identifier is `" + identifier.getUnEscName()
     + "' not `" + iWord.getUnEscName() + "'");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the first identifier definition"));
    throw ex;
   }
   return identifier;
  }
  if((sls & IdlScope.SL_SURR) != 0 && getSurScope() != null)
   return getSurScope().getIdentifier(iWord,
    IdlScope.SL_DEFN | IdlScope.SL_INHT | IdlScope.SL_SURR);
  return null;
 }
 /** 
	 *  @param	identifier	Identifier
	 *  @param	isDefn		Is a real definition or introduce identifier only?
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public void putIdentifier(IdlIdentifier identifier, boolean isDefn)
  throws TxtReadException
 {
  if(isDefn)
   defnIdentifiers.put(identifier.getHashWord(), identifier);
  else
   intrIdentifiers.put(identifier.getHashWord(), identifier);
 }
 /** Read a double colon "::"
	 *
	 *  @param	token		Last token
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	ungetColon	Single colon is possible
	 *	@return				true if "::" otherwise unget or Exception
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private boolean readDoubleColon(TxtTokenRef tRef, TxtTokenReader idlRd,
  boolean ungetColon) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ':')
  { tRef.ungetToken(token);
   return false;
  }
  TxtToken token2= idlRd.readToken();
  if(token2.isAfterWhiteSpaces() || !(token2 instanceof TxtTokSepChar)
   || ((TxtTokSepChar)token2).getChar() != ':')
  {
   if(!ungetColon)
    throw new TxtReadException(token2.getFilePos(),
     "\"::\" expected");
   idlRd.unreadToken();
   tRef.ungetToken(token);
   return false;
  }
  return true;
 }
 /** Read a scoped name and do a scope resolution
	 *
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	ungetColon	Single colon is possible
	 *  @param	test		Return null or exception at the beginning
	 *	@return				identifier (!= null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public IdlIdentifier readScopedName(TxtTokenRef tRef, TxtTokenReader idlRd,
  boolean ungetColon, boolean test) throws TxtReadException
 {
  TxtToken token;
  IdlScope startScope;
  if(readDoubleColon(tRef, idlRd, ungetColon)) // "::"?
  { // get file scope
   startScope= this;
   while(startScope.getSurScope() != null)
    startScope= startScope.getSurScope();
   token= idlRd.readToken();
  }else
  { // Maybe a single ":" is unread() and one token is unget() here
   startScope= null;
   token= tRef.getOrReadToken(idlRd);
   if(test && !(token instanceof TxtTokWord))
   { tRef.ungetToken(token);
    return null;
   }
  }
  IdlIdentifier word1= IdlIdentifier.readIdlWord(this, token);
  while(readDoubleColon(tRef, idlRd, ungetColon))
  { word1.addNext(IdlIdentifier.readIdlWord(this, idlRd.readToken()));
  }
  // Maybe a single ":" is unread() and one token is unget() here
  // Scope resolution
  IdlIdentifier identifier;
  if(startScope == null)
  { identifier= getIdentifier(word1,
    IdlScope.SL_DEFN | IdlScope.SL_INTR);
   if(identifier == null)
   { identifier= getIdentifier(word1,
     IdlScope.SL_INHT | IdlScope.SL_SURR);
    // Introduce the identifier of word1 in the current scope
    if(identifier != null)
     putIdentifier(identifier, false);
   }
  }else
  { identifier= startScope.getIdentifier(word1, IdlScope.SL_DEFN
    | IdlScope.SL_INHT);
  }
  for(IdlIdentifier word= word1; ; )
  { if(identifier == null)
   { throw new TxtReadException(word.getFilePos(),
     "Definition of identifier `" + word.getUnEscName()
     + "' not found");
   }
   if(word.getNext() == null)
    break;
   if(!(identifier instanceof IdlScope))
   { TxtReadException ex= new TxtReadException(word.getFilePos(),
     "Definition of identifier `" + word.getUnEscName()
     + "' forms no scope");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the identifier definition"));
    throw ex;
   }
   word= word.getNext();
   identifier= ((IdlScope)identifier).getIdentifier(word,
    IdlScope.SL_DEFN | IdlScope.SL_INHT);
  }
  return identifier;
 }
}
