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
package mboth.idl2vb;
import mboth.util.*;
/**
 * @author  Martin Both
 */
public class IdlIdentifier
{
 /** Surrounding scope, IdlSpecification or null
	 */
 private IdlScope surScope;
 /** Identifier (with leading `_')
	 */
 private String name;
 /** File position of the identifier
	 */
 private TxtFilePos filePos;
 /** File include level
	 */
 private int iFileIncludeLvl;
 /** Lower case identifier (without leading `_')
	 */
 private String hashWord;
 /** If part of a list or null
	 */
 private IdlIdentifier next;
 /** #pragma ID scoped_name sTypeId
	 *  or null
	 */
 private String sTypeId;
 /** #pragma prefix sTypePrefix
	 *  or null
	 */
 private String sTypePrefix;
 private boolean isScopeTypePrefix;
 /** #pragma version scoped_name sTypeVersion
	 *  or null
	 */
 private String sTypeVersion;
 /** #pragma vbclsprefix
	 *  or null
	 *  IdlSequence has direct access to this.
	 */
 protected String sVbClsPrefix;
 /** #pragma vbmodprefix
	 *  or null
	 */
 private String sVbModPrefix;
 /** Read new IdlIdentifier but don't put the identifier in the scope
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *	@return				iWord (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlIdentifier readNewIdlWord(IdlScope idlScope,
  TxtToken token) throws TxtReadException
 {
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readIdlWord(idlScope, token);
  IdlIdentifier identifier= idlScope.getIdentifier(iWord,
   IdlScope.SL_DEFN | IdlScope.SL_INTR);
  if(identifier != null)
  { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
    "IDL identifier `" + identifier.getUnEscName()
    + "' redefined after use");
   ex.setNextException(new TxtReadException(identifier.getFilePos(),
    "Position of the first identifier definition"));
   throw ex;
  }
  return iWord;
 }
 /** Read an IdlIdentifier
	 *  Leading '__' and keywords are not allowed.
	 *
	 *  @param	surScope	Surrounding scope, never null
	 *  @param	token		TxtTokWord
	 *	@return				iWord (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlIdentifier readIdlWord(IdlScope surScope,
  TxtToken token) throws TxtReadException
 {
  if(!(token instanceof TxtTokWord))
  { throw new TxtReadException(token.getFilePos(),
    "IDL identifier expected");
  }
  // <identifier>
  IdlIdentifier iWord= new IdlIdentifier(surScope,
   /*name*/((TxtTokWord)token).getWord(), token.getFilePos());
  if(iWord.hashWord.length() == 0 || iWord.hashWord.charAt(0) == '_')
  { throw new TxtReadException(token.getFilePos(),
    "IDL identifier expected");
  }
  String keyword= IdlSpecification.hashKeyword(iWord.name);
  if(keyword != null)
  { throw new TxtReadException(token.getFilePos(),
    "IDL identifier `" + iWord.name
    + "' collides with the keyword `" + keyword + "'");
  }
  return iWord;
 }
 /** This constructor is used by readed IdlWords
	 *
	 *  @param	identifier	Identifier
	 */
 protected IdlIdentifier(IdlIdentifier identifier)
 {
  this(identifier.getSurScope(), identifier.getEscName(),
   identifier.getFilePos());
  this.iFileIncludeLvl= identifier.iFileIncludeLvl;
  this.sTypePrefix= identifier.sTypePrefix;
  this.sVbClsPrefix= identifier.sVbClsPrefix;
  this.sVbModPrefix= identifier.sVbModPrefix;
 }
 /** This constructor is used by IdlIdentifier.readIdlWord()
	 *  and keyword constructors only.
	 *
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	name		Identifier (with leading `_')
	 *						(Keyword possible if IdlType)
	 *  @param	filePos		File position of the identifier
	 */
 protected IdlIdentifier(IdlScope surScope, String name, TxtFilePos filePos)
 {
  this.surScope= surScope;
  this.name= name;
  this.filePos= filePos;
  this.hashWord= getUnEscName().toLowerCase();
  if(surScope == null)
  {
   this.iFileIncludeLvl= 0;
   this.sTypePrefix= "";
   this.sVbClsPrefix= IDL2VB.getVbClsPrefixDefault();
   this.sVbModPrefix= IDL2VB.getVbModPrefixDefault();
  }else
  {
   IdlSpecification idlSpec= surScope.getIdlSpecification();
   this.iFileIncludeLvl= idlSpec.getIncludeLevel();
   // Required for IdlArray, IdlException, IdlInterface,
   //    IdlModule, IdlObjRefType, IdlStruct, IdlUnion,
   //    IdlValueBox, IdlValueType
   this.sTypePrefix= idlSpec.getCurTypePrefix();
   this.sVbClsPrefix= idlSpec.getCurVbClsPrefix();
   this.sVbModPrefix= idlSpec.getCurVbModPrefix();
  }
 }
 /** Get the surrounding scope
	 */
 public IdlScope getSurScope()
 {
  return surScope;
 }
 /** Get the IdlContainer
	 */
 public IdlContainer getIdlContainer()
 {
  for(IdlScope iScope= surScope; iScope != null;
   iScope= iScope.getSurScope())
  { if(iScope instanceof IdlContainer)
    return (IdlContainer)iScope;
  }
  return null; /* must be null for IdlIdentifier.getCName() */
 }
 /** Get the IdlSpecification
	 */
 public IdlSpecification getIdlSpecification()
 {
  for(IdlScope iScope= surScope; iScope != null;
   iScope= iScope.getSurScope())
  { if(iScope instanceof IdlSpecification)
    return (IdlSpecification)iScope;
  }
  return (IdlSpecification)this;
 }
 /** Get the name (with leading `_')
	 */
 public String getEscName()
 {
  return name;
 }
 /** Get the name (without leading `_')
	 */
 public String getUnEscName()
 {
  if(name.charAt(0) == '_')
   return name.substring(1);
  return name;
 }
 /** Get the hashWord
	 */
 public String getHashWord()
 {
  return hashWord;
 }
 /** Get the filename and the start position of the Token
	 */
 public TxtFilePos getFilePos()
 {
  return filePos;
 }
 /** Get the file include level of the definition.
	 *
	 *  @return		0 = defined in main file
	 */
 public int getFileIncludeLvl()
 {
  return iFileIncludeLvl;
 }
 /** If part of a list or null
	 */
 public IdlIdentifier getNext()
 { return next;
 }
 /** Add one or a list of identifiers
	 *  @param	next	
	 */
 public void addNext(IdlIdentifier next)
 { IdlIdentifier theEnd= this;
  while(theEnd.next != null)
   theEnd= theEnd.next;
  theEnd.next= next;
 }
 /** 
	 *  @param	next	
	 */
 public void putNext(IdlIdentifier value)
 { IdlIdentifier theEnd= this;
  while(theEnd != value)
  { if(theEnd.next == null)
   { theEnd.next= value;
    break;
   }
   theEnd= theEnd.next;
  }
 }
 /** 
	 */
 public String toString()
 {
  return getEscName();
 }
 /** 
	 *  @return				::...
	 */
 public StringBuffer getScopedName()
 {
  if(surScope == null)
  { return new StringBuffer();
  }
  StringBuffer sBuf= surScope.getScopedName();
  sBuf.append("::");
  sBuf.append(getUnEscName());
  return sBuf;
 }
 /** After reading a forward-declared identifier the second time
	 */
 protected void checkPragmaPrefix(IdlIdentifier newWord)
  throws TxtReadException
 {
  if(this.isScopeTypePrefix)
   return;
  String sCurrentTypePrefix= newWord.sTypePrefix;
  if(!sCurrentTypePrefix.equals(this.sTypePrefix))
  { TxtReadException ex= new TxtReadException(newWord.getFilePos(),
    "Attempt to assign a different pragma prefix ("
    + sCurrentTypePrefix + " != " + this.sTypePrefix + ") to "
    + "a forward-declared identifier");
   ex.setNextException(new TxtReadException(this.getFilePos(),
    "Position of the first identifier definition"));
   throw ex;
  }
 }
 /** Set the ScopeTypePrefix
	 *  "typeprefix" <scoped_name> <string_literal> can change the name
	 *
	 *	@param	sTypePrefix		
	 *	@return		== null, It was not too late to set
	 *				== sTypePrefix, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 protected String setScopeTypePrefix(String sTypePrefix)
 {
/*???		TxtReadException ex= new TxtReadException(errorPos,
			"Attempt to assign a different pragma prefix ("
			+ sCurrentTypePrefix + " != " + this.sTypePrefix + ") to "
			+ "a forward-declared identifier");
		ex.setNextException(new TxtReadException(this.getFilePos(),
			"Position of the first identifier definition"));
*/
  if(this.isScopeTypePrefix)
   return this.sTypePrefix;
  // Overwrite the pragma prefix without comments
  this.sTypePrefix= sTypePrefix;
  this.isScopeTypePrefix= true;
  return null;
 }
 /** 
	 *  @return				prefix + "/" + name
	 *  					or name
	 *  					or null if IdlSpecification
	 */
 protected void appendTypeName(StringBuffer sBuf)
 {
  if(surScope == null)
   return;
  if(surScope.getSurScope() != null)
  { surScope.appendTypeName(sBuf);
   sBuf.append("/");
  }
  sBuf.append(getUnEscName());
 }
 /** 
	 */
 public void setPragmaVersion(TxtFilePos fPos, String sPragmaVersion)
  throws TxtReadException
 {
  if(sTypeId != null || sTypeVersion != null)
   throw new TxtReadException(fPos, "Attempt to assign "
    +"a version to the same IDL construct a second time.");
  sTypeVersion= sPragmaVersion;
 }
 /** 
	 */
 public void setPragmaTypeId(TxtFilePos fPos, String sPragmaTypeId)
  throws TxtReadException
 {
  if(sTypeId != null)
  { if(sTypeId.equals(sPragmaTypeId))
    return;
   throw new TxtReadException(fPos, "Attempt to assign "
    +"a repository ID to the same IDL construct a second time.");
  }
  sTypeId= sPragmaTypeId;
 }
 /** 
	 *  @return				format:prefix/name:version
	 */
 public String getTypeId()
 {
  if(sTypeId != null)
   return sTypeId;
  StringBuffer sBuf= new StringBuffer("IDL:");
  if(sTypePrefix != null && sTypePrefix.length() != 0)
  { sBuf.append(sTypePrefix);
   sBuf.append("/");
  }
  appendTypeName(sBuf);
  sBuf.append(":");
  sBuf.append((sTypeVersion == null)? "1.0" : sTypeVersion);
  // System.out.println("*** TypeId: " + sBuf.toString());
  return sBuf.toString();
 }
 /** 
	 */
 protected void checkPragmaVbPrefixes(IdlIdentifier newWord)
  throws TxtReadException
 {
  String sCurVbClsPrefix= newWord.getVbClsPrefix();
  if(!sCurVbClsPrefix.equals(this.sVbClsPrefix))
  { TxtReadException ex= new TxtReadException(newWord.getFilePos(),
    "Attempt to assign a different pragma vbclsprefix ("
    + sCurVbClsPrefix + " != " + this.sVbClsPrefix + ") to "
    + "an already declared identifier");
   ex.setNextException(new TxtReadException(this.getFilePos(),
    "Position of the first identifier definition"));
   throw ex;
  }
  String sCurVbModPrefix= newWord.getVbModPrefix();
  if(!sCurVbModPrefix.equals(this.sVbModPrefix))
  { TxtReadException ex= new TxtReadException(newWord.getFilePos(),
    "Attempt to assign a different pragma vbmodprefix ("
    + sCurVbModPrefix + " != " + this.sVbModPrefix + ") to "
    + "an already declared identifier");
   ex.setNextException(new TxtReadException(this.getFilePos(),
    "Position of the first identifier definition"));
   throw ex;
  }
 }
 /** Returns the vbclsprefix
	 *
	 *  @return				sVbClsPrefix
	 */
 public String getVbClsPrefix()
 { if(sVbClsPrefix == null)
   throw new InternalError("sVbClsPrefix is not set");
  return sVbClsPrefix;
 }
 /** Returns the vbmodprefix
	 *
	 *  @return				sVbModPrefix
	 */
 public String getVbModPrefix()
 { if(sVbModPrefix == null)
   throw new InternalError("sVbModPrefix is not set");
  return sVbModPrefix;
 }
}
