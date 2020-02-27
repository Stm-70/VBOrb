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
/** Enumeration
 *  Enumerated types consist of ordered lists of identifiers.
 *
 *  @author  Martin Both
 */
public class IdlEnum extends IdlIdentifier implements IdlSwitchType
{
 /** Not only forward declared?
	 */
 private boolean bDefined;
 /** Enumerators
	 */
 private IdlConst idlEnumerators;
 /** Read enumeration head
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *	@return				iEnum (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlEnum readIdlEnum(IdlScope idlScope, TxtToken token)
  throws TxtReadException
 {
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(idlScope, token);
  IdlEnum iEnum= new IdlEnum(iWord);
  idlScope.putIdentifier(iEnum, true);
  return iEnum;
 }
 /** 
	 *  @param	identifier		Identifier
	 */
 public IdlEnum(IdlIdentifier identifier)
 {
  super(identifier);
 }
 /** Read enumerators
	 *
	 *  @param	idlRd		IdlFile
	 *	@return				Unused token
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public TxtToken readIdl(TxtTokenReader idlRd) throws TxtReadException
 {
  // "{" <enumerator> { "," + <enumerator> }* "}"
  // "{"
  TxtToken token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '{')
  { throw new TxtReadException(token.getFilePos(),
    "\"{\" of enum declaration expected");
  }
  // 15.3.2.6 The first enum identifier has the numeric value zero (0).
  int value= -1;
  do
  { // <enumerator>
   token= idlRd.readToken();
   // <identifier>
   IdlConst iConst= IdlConst.readIdlConst(getSurScope(), token);
   iConst.setConstValue(this, ++value);
   // Add enumerator
   if(idlEnumerators == null)
   { idlEnumerators= iConst;
   }else
   { idlEnumerators.addNext(iConst);
   }
   token= idlRd.readToken();
  }while(token instanceof TxtTokSepChar
   && ((TxtTokSepChar)token).getChar() == ',');
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '}')
  { throw new TxtReadException(token.getFilePos(),
    "\"}\" of enum declaration expected");
  }
  bDefined= true;
  // Register the enum as a new IDL definition in the surrounding scope.
  // (Enums are like constants always living inside modules or interfaces
  // but also in structs or unions.)
  getSurScope().getIdlSpecification().registerIdlDef(this);
  return idlRd.readToken();
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return IdlSpecification.ENUM;
 }
 /** (IdlType)
	 *  Is a (structure or union) type currently under definition?
	 *
	 *	@return		isUnderDefinitionType
	 */
 public boolean isUnderDefinitionType()
 { return false;
 }
 /** (IdlType)
	 *  Is a complete type (e.g. to be a member of structure or union)?
	 *
	 *	@return		isCompleteType
	 */
 public boolean isCompleteType()
 { return bDefined;
 }
 /** (IdlType)
	 *  Get the incomplete type (e.g. member of a sequence).
	 *
	 *	@return		Incomplete type or null
	 */
 public IdlType getIncompleteType()
 { return isCompleteType()? null: this;
 }
 /** (IdlType)
	 *  Is an anonymous type?
	 *
	 *	@return		isAnonymousType
	 */
 public boolean isAnonymousType()
 { return false;
 }
 /** (IdlType)
	 *  Is a local type?
	 *	@return		isLocalType
	 */
 public boolean isLocalType()
 { return false;
 }
 /** (IdlType)
	 *  Get the origin type of a typedef if not an array declarator.
	 *
	 *	@return		iType
	 */
 public IdlType getOriginIdlType()
 { return this;
 }
 /** (IdlConstType)
	 *  Read <const_exp>
	 *
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	tRef		Next TxtToken, unread() is not allowed because
	 *						it is maybe already used to unread ´>>´ operator!
	 *  @param	idlRd		TxtTokenReader
	 *  @return				Result value
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public IdlConstValue readIdlConstValue(IdlScope idlScope, TxtTokenRef tRef,
  TxtTokenReader idlRd) throws TxtReadException
 { IdlConstValue iConstValue= IdlConstValue.readConstExpression(this,
   idlScope, tRef, idlRd, IdlConstValue.PRIOR_EX);
  if(iConstValue.getConstType() != this || iConstValue.getLong() == null)
  { throw new TxtReadException(tRef.value.getFilePos(),
    "Type mismatch. Constant value must be an enumerator of the enumeration.");
  }
  return iConstValue;
 }
 /** (IdlConstType)
	 *  Read a subexpression until finding an operator with less or equal
	 *  priority of prevPrior or unexpected token. If there is no value token
	 *  an exception is thrown.
	 *
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	tRef		Next TxtToken, unread() is not allowed because
	 *						it is maybe already used to unread ´>>´ operator!
	 *  @param	idlRd		TxtTokenReader
	 *	@param	prevPrior
	 *  @return				Result value
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public IdlConstValue readSubExpression(IdlScope idlScope, TxtTokenRef tRef,
  TxtTokenReader idlRd, int prevPrior) throws TxtReadException
 { IdlConstValue iConstValue= IdlConstValue.readConstExpression(this,
   idlScope, tRef, idlRd, prevPrior);
  if(iConstValue.getConstType() != this || iConstValue.getLong() == null)
  { throw new TxtReadException(tRef.value.getFilePos(),
    "Type mismatch. Constant value must be an enumerator of the enumeration.");
  }
  return iConstValue;
 }
 /** Get the Enumerators. Used by Union to introduce the identifiers
	 *  in the case labels.
	 */
 public IdlConst getEnumerators()
 { return idlEnumerators;
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
	 *	@param	withPrefix	With final prefix? The name without prefix
	 *  					is used to build complex names.
	 *	@return		C Name
	 */
 public String getCName(boolean withPrefix)
 { if(this.sCName == null)
  { this.sCName= this.getIdlContainer().getCName(false);
   if(this.sCName.length() == 0)
    this.sCName= this.getUnEscName();
   else
    this.sCName += "_" + getUnEscName();
   //???if(CWriter.hashResWord(sVbName) != null)
  }
  return this.sCName;
  // return CWriter.INT;
 }
}
