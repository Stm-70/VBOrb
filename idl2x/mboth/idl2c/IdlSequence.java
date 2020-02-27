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
import mboth.util.*;
/** <sequence_type>
 *
 *  @author  Martin Both
 */
public class IdlSequence extends IdlIdentifier implements IdlType
{
 /** Is a complete type?
	 */
 private boolean bDefined;
 /** <simple_type_spec>
	 */
 private IdlType idlType;
 /** 0 = unbounded
	 */
 private int max;
 /** The first definition of this kind of sequence (ignoring boundaries)
	 */
 IdlSequence prevSeq;
 /** Is this sequence also a Java-RMI/IDL array definition?
	 */
 IdlValueBox boxedJavaArray;
 /** Read a <sequence_type>
	 *
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	keyword		Token of keyword is equal tRef.getOrReadToken(idlRd)
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		return type/null/exception or type/exception
	 *	@return				iSequence (not null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlSequence readIdlSequence(IdlScope surScope, String keyword,
  TxtTokenRef tRef, TxtTokenReader idlRd, boolean test)
  throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  IdlSequence iSequence;
  if(keyword == IdlSpecification.SEQUENCE)
  { iSequence= new IdlSequence(surScope, token.getFilePos());
   iSequence.readIdl(idlRd);
  }else
  { tRef.ungetToken(token);
   if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "<sequence_type> expected");
  }
  return iSequence;
 }
 /**
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	filePos		File position of the identifier
	 */
 private IdlSequence(IdlScope surScope, TxtFilePos filePos)
 { super(surScope, IdlSpecification.SEQUENCE, filePos);
 }
 /** Read sequence
	 *
	 *  @param	idlRd		IdlFile
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private void readIdl(TxtTokenReader idlRd) throws TxtReadException
 {
  // "<" <simple_type_spec> "," <positive_int_const> ">"
  // | "<" <simple_type_spec> ">"
  // "<"
  TxtToken token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '<')
  { throw new TxtReadException(token.getFilePos(),
    "\"<\" of sequence declaration expected");
  }
  // <simple_type_spec>
  TxtTokenRef tRef= new TxtTokenRef();
  token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  idlType= IdlBaseType.readSimpleType(getSurScope(), tRef, idlRd);
  if(idlType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), idlType);
  // ","
  token= tRef.getOrReadToken(idlRd);
  if(token instanceof TxtTokSepChar &&
   ((TxtTokSepChar)token).getChar() == ',')
  {
   // <positive_int_const>
   IdlConstType iConstType= IdlPositiveInt.readIdlPositiveInt(
    getSurScope(), token.getFilePos());
   IdlConstValue iConstValue= iConstType.readIdlConstValue(
    getSurScope(), tRef, idlRd);
   max= (int)iConstValue.getLong().longValue();
   token= tRef.getOrReadToken(idlRd);
  }
  // ">"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '>')
  { throw new TxtReadException(token.getFilePos(),
    "\">\" of sequence declaration expected");
  }
  bDefined= true;
  // Register only the first definition of this kind of sequence
  // as a new IDL definition but returning this sequence
  // because mapping is ignoring the boundaries.
  //
  IdlSpecification idlSpec= getIdlSpecification();
  Object obj= idlSpec.getPrevIdlDef(this);
  if(obj == null)
  { idlSpec.registerIdlDef(this);
  }else
  { this.prevSeq= (IdlSequence)obj;
  }
 }
 /**
	 */
 public void setJavaArray(IdlValueBox boxedJavaArray)
 {
  this.boxedJavaArray= boxedJavaArray;
  if(prevSeq != null)
   prevSeq.setJavaArray(boxedJavaArray);
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { StringBuffer sBuf;
  sBuf= new StringBuffer(IdlSpecification.SEQUENCE);
  sBuf.append('<');
  sBuf.append(idlType.getIdlName());
  if(sBuf.charAt(sBuf.length() - 1) == '>')
   sBuf.append(' ');
  sBuf.append('>');
  return sBuf.toString();
 }
 /** (IdlType)
	 *  Is a (structure or union) type currently under definition?
	 *
	 *	@return		isUnderDefinitionType
	 */
 public boolean isUnderDefinitionType()
 { return !bDefined;
 }
 /** (IdlType)
	 *  Is a complete type (e.g. to be a member of structure or union)?
	 *
	 *	@return		isCompleteType
	 */
 public boolean isCompleteType()
 { return bDefined &&
   (idlType.isCompleteType() || idlType.isUnderDefinitionType());
 }
 /** (IdlType)
	 *  Get the incomplete type (e.g. member of a sequence).
	 *
	 *	@return		Incomplete type or null
	 */
 public IdlType getIncompleteType()
 { return bDefined? idlType.getIncompleteType(): this;
 }
 /** (IdlType)
	 *  Is an anonymous type?
	 *
	 *	@return		isAnonymousType
	 */
 public boolean isAnonymousType()
 { return true;
 }
 /** (IdlType)
	 *  Is a local type?
	 *	@return		isLocalType
	 */
 public boolean isLocalType()
 { return idlType.isLocalType();
 }
 /** (IdlType)
	 *  Get the origin type of a typedef if not an array declarator.
	 *
	 *	@return		iType
	 */
 public IdlType getOriginIdlType()
 { return this;
 }
 /** C name (without general prefix) or null if unset
	 */
 private String sCName;
 /** (IdlDefinition)
	 *  Set the C Name
	 *
	 *	@param	CName		Can be "" if not IdlType
	 *	@return		== null, It was not too late to set
	 *				== CName, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 public String setCName(String CName)
 { if(prevSeq != null)
   return prevSeq.setCName(CName);
  if(this.sCName != null)
   return this.sCName;
  this.sCName= CName;
  return null;
 }
 /** (IdlDefinition)
	 *  Get the C Name to identify the definition
	 *
	 *	@param	withPrefix	With final prefix? The name without prefix is used
	 *  				to build complex names.
	 *	@return		C Name
	 */
 public String getCName(boolean withPrefix)
 {
  if(prevSeq != null)
   return prevSeq.getCName(withPrefix);
  if(this.sCName == null)
  { sCName= IdlSpecification.SEQUENCE;
   IdlType orgType= idlType.getOriginIdlType();
   if(orgType instanceof IdlValueBox)
   { if(((IdlValueBox)orgType).isJavaVBoxType())
    { sCName += "_boxed";
    }
   }
   sCName += "_" + idlType.getCName(false);
  }
  if(withPrefix)
   return CWriter.CORBA_ + this.sCName;
  return this.sCName;
 }
 /**
	 *  @param	cPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 public void writeC(String cPath) throws java.io.IOException
 {
  // (getScopedName() is not the ScopedName of the IdlType)
  System.out.println("Writing " + getIdlName() + " to `"
   + getCName(true) + ".c'");
  //CWriter out= new CWriter(cPath, getCName(true),
  //	getFilePos().getFileName());
 }
}
