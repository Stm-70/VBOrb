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
/** <floating_pt_type> ::= "float"
 *                     |   "double"
 *                     |   "long" "double"
 *
 *  @author  Martin Both
 */
public class IdlDouble extends IdlIdentifier implements IdlConstType
{
 /** IdlName
	 */
 private String idlName;
 /** Read a <floating_pt_type> ::= "float"
	 *
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	filePos		File position of the identifier
	 */
 public static IdlDouble readIdlFloat(IdlScope idlScope, TxtFilePos filePos)
 { return new IdlDouble(idlScope, filePos, IdlSpecification.FLOAT);
 }
 /** Read a <floating_pt_type> ::= "double"
	 *
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	filePos		File position of the identifier
	 */
 public static IdlDouble readIdlDouble(IdlScope idlScope, TxtFilePos filePos)
 { return new IdlDouble(idlScope, filePos, IdlSpecification.DOUBLE);
 }
 /** Read a <floating_pt_type> ::= "long" "double"
	 *
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	filePos		File position of the identifier
	 */
 public static IdlDouble readIdlLongDouble(IdlScope idlScope,
  TxtFilePos filePos)
 { return new IdlDouble(idlScope, filePos, IdlSpecification.LONG
   + " " + IdlSpecification.DOUBLE);
 }
 /**
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	filePos		File position of the identifier
	 *  @param	idlName	
	 */
 protected IdlDouble(IdlScope surScope, TxtFilePos filePos, String idlName)
 { super(surScope, idlName, filePos);
  this.idlName= idlName;
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return idlName;
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
  if(iConstValue.getDouble() == null) // IDL double is Java Double
  { if(iConstValue.getLong() != null)
   { System.out.println(tRef.value.getFilePos().toString()
      + ": Converting integer into floating-point literal");
    iConstValue= new IdlConstValue(
     (double)iConstValue.getLong().longValue());
   }else
   { throw new TxtReadException(tRef.value.getFilePos(),
     "Type mismatch. Constant value must match double type.");
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
  if(iConstValue.getDouble() == null && iConstValue.getLong() == null)
  { throw new TxtReadException(tRef.value.getFilePos(),
    "Type mismatch. Constant value must match double type.");
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
 /** (IdlType)
	 *  Get the Visual Basic Name to identify the type
	 *
	 *	@bPrefix	With final prefix?
	 *	@return		Visual Basic Name
	 */
 public String getVbName(boolean bPrefix)
 { String sVbName;
  if(idlName == IdlSpecification.FLOAT)
   sVbName= VbWriter.SINGLE;
  else if(idlName == IdlSpecification.DOUBLE)
   sVbName= VbWriter.DOUBLE;
  else
   sVbName= bPrefix? VbWriter.CORBLONGDOUBLE:
    "Longdouble";
  return sVbName;
 }
 /** (IdlType)
	 *
	 *  @return				Assign by SET or LET?
	 */
 public boolean isVbSet()
 { return idlName != IdlSpecification.FLOAT
   && idlName != IdlSpecification.DOUBLE;
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
  String readFunc;
  boolean vbSet;
  if(idlName == IdlSpecification.FLOAT)
  { readFunc= "readFloat";
   vbSet= false;
  }else if(idlName == IdlSpecification.DOUBLE)
  { readFunc= "readDouble";
   vbSet= false;
  }else
  { readFunc= "readLongdouble";
   vbSet= true;
  }
  out.writeAssign(vbVariable, vbSet);
  out.writeLine(VbWriter.OIN + "." + readFunc + "()");
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
  String writeFunc;
  if(idlName == IdlSpecification.FLOAT)
   writeFunc= "writeFloat";
  else if(idlName == IdlSpecification.DOUBLE)
   writeFunc= "writeDouble";
  else
   writeFunc= "writeLongdouble";
  out.writeLine(VbWriter.CALL + " " + VbWriter.OOUT + "."
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
  String sTC;
  if(idlName == IdlSpecification.FLOAT)
   sTC= "6"; // mCB.tk_float = 6
  else if(idlName == IdlSpecification.DOUBLE)
   sTC= "7"; // mCB.tk_double = 7
  else
   sTC= "25"; // mCB.tk_longdouble = 25
  out.writeAssign(vbVariable, true);
  out.writeLine("oOrb.createPrimitiveTc(" + sTC + ") 'VBOrb.TC...");
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
  String getFunc;
  boolean vbSet;
  if(idlName == IdlSpecification.FLOAT)
  { getFunc= "getFloat";
   vbSet= false;
  }else if(idlName == IdlSpecification.DOUBLE)
  { getFunc= "getDouble";
   vbSet= false;
  }else
  { getFunc= "getLongdouble";
   vbSet= true;
  }
  out.writeAssign(vbVariable, vbSet);
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
  String insertFunc;
  if(idlName == IdlSpecification.FLOAT)
   insertFunc= "insertFloat";
  else if(idlName == IdlSpecification.DOUBLE)
   insertFunc= "insertDouble";
  else
   insertFunc= "insertLongdouble";
  out.writeLine(VbWriter.CALL + " " + VbWriter.OANY + "."
   + insertFunc + "(" + vbVariable + ")");
 }
}
