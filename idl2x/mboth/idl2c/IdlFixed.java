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
import java.math.*; // BigDecimal
/** <fixed_pt_type> ::= "fixed" "<" <positive_int_const> ">" ","
 *                      <positive_int_const> ">"
 *  <fixed_pt_const_type> ::= "fixed"
 *
 *  @author  Martin Both
 */
public class IdlFixed extends IdlIdentifier implements IdlConstType
{
 /** Number of digits
	 */
 private int digits;
 /** Scale (constants with negativ scale are permitted)
	 */
 private int scale;
 /** Read a <fixed_pt_const_type> ::= "fixed"
	 *
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	keyword		Token of keyword is equal tRef.getOrReadToken(idlRd)
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		return type/null/exception or type/exception
	 *	@return				idlType (not null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlFixed readIdlFixedConst(IdlScope surScope, String keyword,
  TxtTokenRef tRef, TxtTokenReader idlRd, boolean test)
  throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  if(keyword != IdlSpecification.FIXED)
  { tRef.ungetToken(token);
   if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "<fixed_pt_const_type> expected");
  }
  IdlFixed iFixed= new IdlFixed(surScope, token.getFilePos());
  return iFixed;
 }
 /** Read a <fixed_pt_type>
	 *
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	keyword		Token of keyword is equal tRef.getOrReadToken(idlRd)
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		return type/null/exception or type/exception
	 *	@return				idlType (not null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlFixed readIdlFixed(IdlScope surScope, String keyword,
  TxtTokenRef tRef, TxtTokenReader idlRd, boolean test)
  throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  if(keyword != IdlSpecification.FIXED)
  { tRef.ungetToken(token);
   if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "<fixed_pt_const_type> expected");
  }
  IdlFixed iFixed= new IdlFixed(surScope, token.getFilePos());
  token= idlRd.readToken();
  // "<"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '<')
  { throw new TxtReadException(token.getFilePos(),
    "\"<\" of fixed data type declaration expected");
  }
  // <positive_int_const>
  IdlConstType iConstType= IdlPositiveInt.readIdlPositiveInt(
   surScope, token.getFilePos());
  IdlConstValue iConstValue= iConstType.readIdlConstValue(
   surScope, tRef, idlRd);
  iFixed.digits= (int)iConstValue.getLong().longValue();
  token= tRef.getOrReadToken(idlRd);
  // ","
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ',')
  { throw new TxtReadException(token.getFilePos(),
    "\",\" of fixed data type declaration expected");
  }
  // <positive_int_const>
  iConstType= IdlPositiveInt.readIdlPositiveInt(
   surScope, token.getFilePos());
  iConstValue= iConstType.readIdlConstValue(
   surScope, tRef, idlRd);
  iFixed.scale= (int)iConstValue.getLong().longValue();
  token= tRef.getOrReadToken(idlRd);
  // ">"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '>')
  { throw new TxtReadException(token.getFilePos(),
    "\">\" of fixed data type declaration expected");
  }
  /* All intermediate computations shall be performed using double
		   precision (i.e., 62 digit) arithmetic. If an individual
		   computation between a pair of fixed-point literals actually
		   generates more than 31 significant digits, then a 31-digit result
		   is retained as follows: fixed<d,s> => fixed<31, 31-d+s>
		*/
  if(iFixed.digits > 31)
  { // The fixed data type represents a fixed-point decimal number
   // of up to 31 significant digits.
   throw new TxtReadException(token.getFilePos(),
    "Fixed data type digits out of range `" + iFixed.digits
    + " > 31'");
  }
  /* The scale factor is a non-negative integer less than or equal
		   to the total number of digits (note that constants with effectively
		   n e g a t i v e  scale, such as 10000, are always permitted).
		*/
  if(iFixed.scale > iFixed.digits)
  { throw new TxtReadException(token.getFilePos(),
    "Scale factor `" + iFixed.scale + "' of fixed data type"
    + " must be less than or equal to the total number of digits `"
    + iFixed.digits + "'");
  }
  return iFixed;
 }
 /**
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	filePos		File position of the keyword
	 */
 protected IdlFixed(IdlScope surScope, TxtFilePos filePos)
 {
  super(surScope, IdlSpecification.FIXED, filePos);
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { StringBuffer sBuf;
  sBuf= new StringBuffer(IdlSpecification.FIXED);
  sBuf.append('<').append(digits).append(',');
  sBuf.append(scale).append('>');
  return sBuf.toString();
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
 { return true;
 }
 /** (IdlType)
	 *  Get the incomplete type (e.g. member of a sequence).
	 *
	 *	@return		Incomplete type or null
	 */
 public IdlType getIncompleteType()
 { return null; // isCompleteType()? null: this;
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
  if(iConstValue.getDecimal() == null)
  { if(iConstValue.getDouble() != null)
   { System.out.println(tRef.value.getFilePos().toString()
     + ": Converting floating-point into fixed-point literal");
    iConstValue= new IdlConstValue(
     new BigDecimal(iConstValue.getDouble().doubleValue()));
   }else
   { throw new TxtReadException(tRef.value.getFilePos(),
     "Type mismatch. Constant value must match fixed data type.");
   }
  }
  iConstValue.setConstType(this);
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
  if(iConstValue.getDecimal() == null && iConstValue.getDouble() == null)
  { throw new TxtReadException(tRef.value.getFilePos(),
    "Type mismatch. Constant value must match fixed data type.");
  }
  return iConstValue;
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
 { return CName;
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
  { sCName= IdlSpecification.FIXED + "_" + Integer.toString(digits);
   if(scale >= 0)
    sCName += "_" + Integer.toString(scale);
   else
    sCName += "n" + Integer.toString(-scale);
  }
  //if(withPrefix)
   return CWriter.CORBA_ + this.sCName;
  //return this.sCName;
 }
}
