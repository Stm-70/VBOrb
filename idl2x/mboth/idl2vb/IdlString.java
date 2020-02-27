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
/** <string_type> ::= "string" "<" <positive_int_const> ">" | "string"
 *  <wide_string_type> ::= "wstring" "<" <positive_int_const> ">" | "wstring"
 *
 *  @author  Martin Both
 */
public class IdlString extends IdlIdentifier implements IdlConstType
{
 /** Read a <string> value
	 *
	 *  @param	strToken	First string token
	 *  @param	tRef		Maybe next unused token
	 *  @param	idlRd		IdlFile
	 *	@return				String
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static String readStringValue(TxtTokString strToken,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  StringBuffer strVal= new StringBuffer();
  for(; ; )
  { if(strToken.getBoundary() != '\"')
   { throw new TxtReadException(strToken.getFilePos(),
     "Found string characters not surrounded by double quotes");
   }
   strVal.append(strToken.getUnEscString());
   TxtToken token= tRef.getOrReadToken(idlRd);
   if(token instanceof TxtTokString)
    strToken= (TxtTokString)token;
   else
   { tRef.ungetToken(token);
    break;
   }
  }
  return strVal.toString();
 }
 /** Wide?
	 */
 private boolean wide;
 /** maximum size
	 */
 private int size;
 /** Read a <string_type> or a <wide_string_type>
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
 public static IdlString readIdlString(IdlScope surScope, String keyword,
  TxtTokenRef tRef, TxtTokenReader idlRd, boolean test)
  throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  IdlString iString;
  if(keyword == IdlSpecification.STRING)
  { iString= new IdlString(surScope, token.getFilePos(), false);
  }else if(keyword == IdlSpecification.WSTRING)
  { iString= new IdlString(surScope, token.getFilePos(), true);
  }else
  { tRef.ungetToken(token);
   if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "<string_type> expected");
  }
  token= idlRd.readToken();
  if(token instanceof TxtTokSepChar
   && ((TxtTokSepChar)token).getChar() == '<')
  { // <positive_int_const>
   IdlConstType iConstType= IdlPositiveInt.readIdlPositiveInt(
    surScope, token.getFilePos());
   IdlConstValue iConstValue= iConstType.readIdlConstValue(
    surScope, tRef, idlRd);
   iString.size= (int)iConstValue.getLong().longValue();
   token= tRef.getOrReadToken(idlRd);
   // ">"
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != '>')
   { throw new TxtReadException(token.getFilePos(),
     "\">\" of string declaration expected");
   }
  }else
  { idlRd.unreadToken();
  }
  return iString;
 }
 /**
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	filePos		File position of the keyword
	 *  @param	wide	
	 */
 protected IdlString(IdlScope surScope, TxtFilePos filePos, boolean wide)
 {
  super(surScope, wide? IdlSpecification.WSTRING: IdlSpecification.STRING,
   filePos);
  this.wide= wide;
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return wide? IdlSpecification.WSTRING: IdlSpecification.STRING;
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
 { return size != 0;
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
  if(iConstValue.getString() == null)
  { throw new TxtReadException(tRef.value.getFilePos(),
    "Type mismatch. Constant value must match string type.");
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
  if(iConstValue.getString() == null)
  { throw new TxtReadException(tRef.value.getFilePos(),
    "Type mismatch. Constant value must match string type.");
  }
  return iConstValue;
 }
 /** (IdlDefinition)
	 *  Set the Visual Basic Name
	 *
	 *	@param	sVbName		Can be "" if not IdlType
	 *	@return		== null, It was not too late to set
	 *				== sVbName, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 public String setVbName(String sVbName)
 {
  return sVbName;
 }
 /** (IdlDefinition)
	 *  Get the Visual Basic Name to identify the definition
	 *
	 *	@param	bPrefix	With final prefix? The name without prefix is used
	 *  				to build complex names.
	 *	@return		Visual Basic Name
	 */
 public String getVbName(boolean bPrefix)
 { if(bPrefix)
   return VbWriter.STRING; // wide/notwide
  return wide? "Wstring": VbWriter.STRING;
 }
 /** (IdlType)
	 *
	 *  @return				Assign by SET or LET?
	 */
 public boolean isVbSet()
 { return false;
 }
 /** (IdlType)
	 *
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbRead(VbClsWriter out, String vbVariable)
   throws java.io.IOException
 {
  String readFunc= wide? "readWstring": "readString";
  out.writeAssign(vbVariable, false);
  StringBuffer sBuf= new StringBuffer();
  sBuf.append(VbClsWriter.OIN);
  sBuf.append(".");
  sBuf.append(readFunc);
  sBuf.append("(");
  if(size > 0)
  { sBuf.append(Integer.toString(size));
  }
  sBuf.append(")");
  out.writeLine(sBuf.toString());
 }
 /** (IdlType)
	 *
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbWrite(VbClsWriter out, String vbVariable)
   throws java.io.IOException
 {
  String writeFunc= wide? "writeWstring": "writeString";
  out.writeLine(VbWriter.CALL + " " + VbClsWriter.OOUT + "."
   + writeFunc + "(" + vbVariable + ")");
 }
 /** (IdlType)
	 *  Write VB code assigning the TypeCode to a VB variable
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable getting the TypeCode
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbAssignTypeCode(VbWriter out, String vbVariable)
   throws java.io.IOException
 {
  String sTC= wide? "createWstringTc": "createStringTc";
  out.writeAssign(vbVariable, true);
  out.writeLine("oOrb." + sTC + "(" + size + ")");
 }
 /** (IdlType)
	 *  Write VB code extracting the value from an oAny to a VB variable
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable getting the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbFromAny(VbWriter out, String vbVariable)
   throws java.io.IOException
 {
  String getFunc= wide? "getWstring": "getString";
  out.writeAssign(vbVariable, false);
  out.writeLine(VbWriter.OANY + "." + getFunc + "()");
 }
 /** (IdlType)
	 *  Write VB code inserting the value into an oAny
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable containing the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbIntoAny(VbWriter out, String vbVariable)
   throws java.io.IOException
 {
  String insertFunc= wide? "insertWstring": "insertString";
  out.writeLine(VbWriter.CALL + " " + VbWriter.OANY + "."
   + insertFunc + "(" + vbVariable + ")");
 }
}
