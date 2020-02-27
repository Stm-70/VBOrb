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
/** 
 *
 *  @author  Martin Both
 */
public class IdlConst extends IdlIdentifier implements IdlDefinition
{
 /** <const_type> or a typedef
	 */
 private IdlType iConstTypeDef;
 /** iConstValue contains <const_type> and <const_exp>
	 */
 private IdlConstValue iConstValue;
 /** Read const identifier without type and value
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *	@return				iConst (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlConst readIdlConst(IdlScope idlScope, TxtToken token)
  throws TxtReadException
 {
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(idlScope, token);
  IdlConst iConst= new IdlConst(iWord);
  idlScope.putIdentifier(iConst, true);
  // Register this constant as a new IDL definition
  // (Constants are always inside modules or other scopes.)
  idlScope.getIdlSpecification().registerIdlDef(iConst);
  return iConst;
 }
 /** Read const type, identifier and expression
	 *
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	tRef		Next TxtToken, unread() is not allowed because
	 *						it is maybe already used to unread ´>>´ operator!
	 *  @param	idlRd		TxtTokenReader
	 *	@return				iConst (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlConst readIdlConst(IdlScope idlScope, TxtTokenRef tRef,
  TxtTokenReader idlRd) throws TxtReadException
 {
  // Read a <const_type>.
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iConstTypeDef= IdlBaseType.readConstType(idlScope, tRef, idlRd);
  if(iConstTypeDef.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iConstTypeDef);
  IdlConstType iConstType= (IdlConstType)iConstTypeDef.getOriginIdlType();
  // Read <identifier>
  token= tRef.getOrReadToken(idlRd);
  // System.out.println("const identifier: " + token);
  IdlConst iConst= readIdlConst(idlScope, token);
  iConst.iConstTypeDef= iConstTypeDef;
  // "="
  token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '=')
  { throw new TxtReadException(token.getFilePos(),
    "\"=\" of constant declaration expected");
  }
  // <const_exp>
  iConst.iConstValue= iConstType.readIdlConstValue(idlScope, tRef, idlRd);
  // System.out.println("= " + iConst.iConstValue.toString());
  return iConst;
 }
 /** 
	 *  @param	identifier		Identifier
	 */
 private IdlConst(IdlIdentifier identifier)
 {
  super(identifier);
 }
 /** (IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return getScopedName().toString();
 }
 /** 
	 *	@param	iConstType		IdlConstType
	 *	@param	value			Value
	 */
 public void setConstValue(IdlConstType iConstType, long value)
 { iConstTypeDef= iConstType;
  iConstValue= new IdlConstValue(value);
  iConstValue.setConstType(iConstType);
 }
 /** 
	 *	@return				IdlType or null if not yet defined
	 */
 public IdlType getConstTypeDef()
 { return iConstTypeDef;
 }
 /** 
	 *	@return				Value or null if not yet defined
	 */
 public IdlConstValue getConstValue()
 { return iConstValue;
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
 { if(this.sCName != null)
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
  if(this.sCName == null)
  { sCName= getUnEscName();
  }
  if(withPrefix)
   return /*IdlIdentifier*/this.getIdlContainer().getCName(withPrefix)
    + this.sCName;
  return this.sCName;
 }
 /** ???
	 */
 public void writeC()
 {
  IdlConstValue iConstValue= getConstValue();
  System.out.println("Const: " + getUnEscName()
   + " " + getUnEscName() + "= " + iConstValue);
 }
}
