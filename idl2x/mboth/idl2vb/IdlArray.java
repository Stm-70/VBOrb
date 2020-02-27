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
/** Array
 *
 *  @author  Martin Both
 */
public class IdlArray implements IdlType
{
 /** File position of the "["
	 */
 private TxtFilePos filePos;
 /** File include level
	 */
 private int iFileIncludeLvl;
 /** <type_spec>
	 */
 private IdlType idlType;
 /** <fixed_array_size>
	 */
 private int arrSize;
 /** The first definition of this kind of Array (including size)
	 */
 IdlArray prevArr;
 /** Read an Array type definition if "[]" exists or return iType
	 *
	 *  @param	iType		<type_spec>
	 *  @param	surScope	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iArray or iType if only a simple declarator
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readIdlType(IdlType iType, IdlScope surScope,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  // <fixed_array_size>*
  // <fixed_array_size> ::= "[" <positive_int_const> "]"
  TxtToken token= tRef.getOrReadToken(idlRd);
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '[')
  {
   tRef.ungetToken(token);
   return iType;
  }
  if(iType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iType);
  // <positive_int_const>
  IdlConstType iConstType= IdlPositiveInt.readIdlPositiveInt(
   surScope, token.getFilePos());
  IdlConstValue iConstValue= iConstType.readIdlConstValue(
   surScope, tRef, idlRd);
  // "]"
  token= tRef.getOrReadToken(idlRd);
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ']')
  { throw new TxtReadException(token.getFilePos(),
    "\"]\" of array declaration expected");
  }
  IdlArray iArray= new IdlArray(iConstType.getFilePos(),
   iConstType.getFileIncludeLvl());
  // See also: IdlIdentifier.sVbClsPrefix
  IdlSpecification idlSpec= surScope.getIdlSpecification();
  iArray.sVbClsPrefix= idlSpec.getCurVbClsPrefix();
  iArray.idlType= IdlArray.readIdlType(iType, surScope, tRef, idlRd);
  iArray.arrSize= (int)iConstValue.getLong().longValue();
  // Register only the first definition of this kind of Array
  // as a new IDL definition.
  //
  Object obj= idlSpec.getPrevIdlDef(iArray);
  if(obj == null)
  { // new array definition
   idlSpec.registerIdlDef(iArray);
  }else
  { iArray.prevArr= (IdlArray)obj;
  }
  if(iArray.prevArr != null)
  { // See also: IdlIdentifier.checkPragmaVbPrefixes()
   if(!iArray.sVbClsPrefix.equals(iArray.prevArr.sVbClsPrefix))
   { TxtReadException ex= new TxtReadException(iArray.getFilePos(),
     "Attempt to assign a different pragma vbclsprefix ("
     + iArray.sVbClsPrefix + " != "
     + iArray.prevArr.sVbClsPrefix + ") to "
     + "an already declared array");
    ex.setNextException(new TxtReadException(
     iArray.prevArr.getFilePos(),
     "Position of the first array definition"));
    throw ex;
   }
  }
  return iArray;
 }
 /**
	 *  @param	filePos		File position of the identifier
	 */
 private IdlArray(TxtFilePos filePos, int iFileIncludeLvl)
 { this.filePos= filePos;
  this.iFileIncludeLvl= iFileIncludeLvl;
 }
 /** Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @param		uppName
	 *  @return		<Array ::T>
	 */
 private String getIdlName(String uppName)
 { String iName= uppName + "[" + arrSize + "]";
  if(idlType instanceof IdlArray)
  { IdlArray subArray= (IdlArray)idlType;
   return subArray.getIdlName(iName);
  }
  /*  getIdlName() is different herre because the VB mapping does not
		 *  support vbnames of typedefs. Otherwise you cannot assign
		 *  arrays of different type names and you have to use #pragma
		 *  very often.
		 */
  return idlType.getOriginIdlType().getIdlName() + iName;
 }
 /** (IdlType:IdlDefinition)
	 *  Get the filename and the start position of the definition
	 *
	 *  @return		Filename and position
	 */
 public TxtFilePos getFilePos()
 { return filePos;
 }
 /** (IdlType:IdlDefinition)
	 *  Get the file include level of the definition.
	 *
	 *  @return		0 = defined in main file
	 */
 public int getFileIncludeLvl()
 {
  return iFileIncludeLvl;
 }
 /** (IdlType:IdlDefinition)
	 *  Get the short definition name (without scope and leading `_')
	 *  This name is used by some messages.
	 *
	 *  @return		Short definition name
	 */
 public String getUnEscName()
 { StringBuffer sBuf= new StringBuffer();
  if(idlType != null)
  { sBuf.append(idlType.getUnEscName());
  }
  sBuf.append('[');
  sBuf.append(arrSize);
  sBuf.append(']');
  return sBuf.toString();
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<Array ::T>
	 */
 public String getIdlName()
 { return getIdlName(" ");
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
 { return idlType.isCompleteType();
 }
 /** (IdlType)
	 *  Get the incomplete type (e.g. member of a sequence).
	 *
	 *	@return		Incomplete type or null
	 */
 public IdlType getIncompleteType()
 { return idlType.getIncompleteType();
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
 /** 
	 */
 public final static String LENGTH= "Length";
 public final static String ARRCNT= "arrCnt";
 public final static String ARRLEN= "arrLen";
 public final static String ARRARR= "arrArr";
 public final static String SP= " ";
 /** See also: IdlIdentifier.sVbClsPrefix
	 *  #pragma vbclsprefix
	 *  or null
	 */
 private String sVbClsPrefix;
 /** See also: IdlIdentifier.getVbClsPrefix
	 *  Returns the vbclsprefix
	 *
	 *  @return				sVbClsPrefix
	 */
 public String getVbClsPrefix()
 { if(sVbClsPrefix == null)
   throw new InternalError("sVbClsPrefix is not set");
  return sVbClsPrefix;
 }
 /** VB name (without general prefix) or null if unset
	 */
 private String sVbName;
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
  if(prevArr != null)
   return prevArr.setVbName(sVbName);
  if(this.sVbName != null)
   return this.sVbName;
  this.sVbName= sVbName;
  return null;
 }
 /** (IdlDefinition)
	 *  Get the Visual Basic Name to identify the definition
	 *
	 *	@bPrefix	With final prefix?
	 *	@return		Visual Basic Name
	 */
 public String getVbName(boolean bPrefix)
 {
  if(prevArr != null)
   return prevArr.getVbName(bPrefix);
  if(sVbName == null)
  { // if "Arr" at the end than "Seq" also at the end
   sVbName= idlType.getVbName(false);
   IdlType orgType= idlType.getOriginIdlType();
   if(orgType instanceof IdlChar)
   { if(orgType.getIdlName() == IdlSpecification.WCHAR)
    { if(sVbName.equals(VbWriter.INTEGER))
      sVbName= "Wchar";
    }else
    { if(sVbName.equals(VbWriter.BYTE))
      sVbName= "Char";
    }
   }
   sVbName= sVbName + "Arr" + arrSize;
  }
  if(bPrefix)
   return getVbClsPrefix() + sVbName;
  return sVbName;
 }
 /** (IdlType)
	 *
	 *  @return				Assign by SET or LET?
	 */
 public boolean isVbSet()
 { return true;
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
  out.writeAssign(vbVariable, true);
  out.writeLine(VbWriter.NEW + " " + getVbName(true));
  out.writeLine(VbWriter.CALL + " " + vbVariable + "."
   + VbWriter.INITBYREAD + "(" + VbWriter.OIN + ")");
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
  out.writeLine(VbWriter.CALL + " " + vbVariable + "."
   + VbWriter.WRITEME + "(" + VbWriter.OOUT + ")");
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
  idlType.writeVbAssignTypeCode(out, vbVariable);
  out.writeAssign(vbVariable, true);
  out.writeLine("oOrb.createArrayTc(" + arrSize + ", "
   + vbVariable + ")");
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
  out.writeAssign(vbVariable, true);
  out.writeLine(VbWriter.NEW + " " + getVbName(true));
  out.writeLine(VbWriter.CALL + " " + vbVariable + "."
   + VbWriter.INITBYANY + "(" + VbWriter.OANY + ".currentComponent())");
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
  out.writeLine(VbWriter.CALL + " " + vbVariable + "."
   + VbWriter.INSERTINTOANY + "(" + VbWriter.OANY + ".currentComponent())");
 }
 /**
	 *  @param	opts.vbPath		Prefix
	 *  @param	opts.srvout		Write additional server skeleton examples
	 *
	 *	@exception	IOException	
	 */
 public void writeVbFiles(MainOptions opts) throws java.io.IOException
 {
  if(getFileIncludeLvl() > IDL2VB.iGenIncludeLvl)
  { if(MainOptions.iLogLvl >= 8)
    System.out.println("D " + getIdlName()
     + " is included only");
   return;
  }
  // Write VB class
  this.writeVbCls(opts.vbPath);
 }
 /**
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbCls(String vbPath) throws java.io.IOException
 {
  if(MainOptions.iLogLvl >= 4)
  { // (getScopedName() is not the ScopedName of the IdlType)
   System.out.println("I Writing " + getIdlName() + " to `"
    + getVbName(true) + ".cls'");
  }
  VbClsWriter vbC= new VbClsWriter(vbPath, getVbName(true),
   getFilePos().getFileName());
  vbC.writeLine();
  vbC.writeLine("'IDL Name: " + getIdlName());
  vbC.write(VbWriter.CONST + SP + ARRLEN + SP
   + VbWriter.AS + SP + VbWriter.LONG + " = " + arrSize);
  if(arrSize < 32768)
   vbC.write("&");
  vbC.writeLine();
  vbC.writeLine(VbWriter.PRIVATE + SP + ARRARR + "(0 " + VbWriter.TO + SP
   + ARRLEN + " - 1) " + VbWriter.AS + SP + idlType.getVbName(true));
  // Public Property Get Length() As Long
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PUBLIC, VbWriter.GET, LENGTH);
  vbC.writePropertyBody(VbWriter.LONG, true, false);
  vbC.writeLine(LENGTH + " = " + ARRLEN);
  vbC.writePropertyTail(null, null);
  // Public Property Get Item(ByVal index As Long) As ...
  vbC.writeLine();
  vbC.writeLine("'index must be in the range of 0 to Length - 1");
  vbC.writePropertyHead(VbWriter.PUBLIC, VbWriter.GET, "Item");
  vbC.write(VbWriter.BYVAL + " index " + VbWriter.AS
   + SP + VbWriter.LONG);
  vbC.writePropertyBody(idlType.getVbName(true), true, true);
  // Zuweisung ist stark Typeabh"angig!
  // Eigene Methode in IdlType einbauen???
  vbC.writeAssign("Item", idlType.isVbSet());
  vbC.writeLine(ARRARR + "(index)");
  vbC.writePropertyTail(getVbName(false) + ".Item." + VbWriter.GET, null);
  //  Public Property Let/Set Item(ByVal index As Long, ByVal Item As ...
  vbC.writeLine();
  vbC.writeLine("'index must be in the range of 0 to Length - 1");
  vbC.writePropertyHead(VbWriter.PUBLIC,
   idlType.isVbSet()? VbWriter.SET: VbWriter.LET, "Item");
  vbC.write(VbWriter.BYVAL + " index " + VbWriter.AS
   + SP + VbWriter.LONG + ", " + VbWriter.BYVAL + SP + "Item");
  vbC.writePropertyBody(idlType.getVbName(true), false, true);
  // Zuweisung ist stark Typeabh"angig!
  // Eigene Methode in IdlType einbauen???
  vbC.writeAssign(ARRARR + "(index)", idlType.isVbSet());
  vbC.writeLine("Item");
  vbC.writePropertyTail(getVbName(false) + ".Item."
   + (idlType.isVbSet()? VbWriter.SET: VbWriter.LET), null);
  // Public Function getItems(ByRef Arr() As ...
  vbC.writeLine();
  vbC.writeFuncHead(VbWriter.PUBLIC, "getItems");
  vbC.writeFuncArg(VbWriter.BYREF, "Arr()", idlType.getVbName(true),
   null);
  vbC.writeFuncBody(VbWriter.LONG, true);
  vbC.writeLine("getItems = " + ARRLEN);
  vbC.writeDimLine(ARRCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + ARRCNT + " = 0 " + VbWriter.TO
   + SP + ARRLEN + " - 1");
  vbC.indent(true);
  // Zuweisung ist stark Typeabh"angig!
  // Eigene Methode in IdlType einbauen???
  vbC.writeAssign("Arr(" + VbWriter.LBOUND + "(Arr) + "
   + ARRCNT + ")", idlType.isVbSet());
  vbC.writeLine(ARRARR + "(" + ARRCNT + ")");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + ARRCNT);
  vbC.writeFuncTail("getItems", null);
  // Public Sub setItems(ByRef Arr() As ...
  vbC.writeLine();
  vbC.writeSubHead(VbWriter.PUBLIC, "setItems");
  vbC.writeSubArg(VbWriter.BYREF, "Arr()", idlType.getVbName(true), null);
  vbC.writeSubBody(true);
  vbC.writeDimLine(ARRCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + ARRCNT + " = 0 " + VbWriter.TO
   + SP + ARRLEN + " - 1");
  vbC.indent(true);
  // Zuweisung ist stark Typeabh"angig!
  // Eigene Methode in IdlType einbauen???
  vbC.writeAssign(ARRARR + "(" + ARRCNT + ")", idlType.isVbSet());
  vbC.writeLine("Arr(" + VbWriter.LBOUND + "(Arr) + " + ARRCNT + ")");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + ARRCNT);
  vbC.writeSubTail("setItems");
  // Helper, initByRead()
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INITBYREAD);
  vbC.writeArg(VbWriter.BYVAL, VbWriter.OIN, VbWriter.CORBSTREAM, null);
  vbC.writeSubBody(true);
  // Optimize reading sequence of octets
  IdlType orgType= idlType.getOriginIdlType();
  if(orgType instanceof IdlOctet)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OIN
    + ".readOctets(" + ARRARR + ", " + ARRLEN + ")");
  }else if(orgType instanceof IdlChar &&
   orgType.getIdlName() != IdlSpecification.WCHAR)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OIN
    + ".readChars(" + ARRARR + ", " + ARRLEN + ")");
  }else
  { vbC.writeDimLine(ARRCNT, VbWriter.LONG);
   vbC.writeLine(VbWriter.FOR + SP + ARRCNT + " = 0 " + VbWriter.TO
    + SP + ARRLEN + " - 1");
   vbC.indent(true);
   idlType.writeVbRead(vbC, ARRARR + "(" + ARRCNT + ")");
   vbC.indent(false);
   vbC.writeLine(VbWriter.NEXT + SP + ARRCNT);
  }
  vbC.writeSubTail(getVbName(false) + ".read");
  // Helper, writeMe()
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.WRITEME);
  vbC.writeArg(VbWriter.BYVAL, VbWriter.OOUT, VbWriter.CORBSTREAM, null);
  vbC.writeSubBody(true);
  // Optimize writing sequence of octets
  if(orgType instanceof IdlOctet)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OOUT
    + ".writeOctets(" + ARRARR + ", " + ARRLEN + ")");
  }else if(orgType instanceof IdlChar &&
   orgType.getIdlName() != IdlSpecification.WCHAR)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OOUT
    + ".writeChars(" + ARRARR + ", " + ARRLEN + ")");
  }else
  { vbC.writeDimLine(ARRCNT, VbWriter.LONG);
   vbC.writeLine(VbWriter.FOR + SP + ARRCNT + " = 0 " + VbWriter.TO
    + SP + ARRLEN + " - 1");
   vbC.indent(true);
   idlType.writeVbWrite(vbC, ARRARR + "(" + ARRCNT + ")");
   vbC.indent(false);
   vbC.writeLine(VbWriter.NEXT + SP + ARRCNT);
  }
  vbC.writeSubTail(getVbName(false) + ".write");
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INITBYANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  vbC.writeDimLine(ARRCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + ARRCNT + " = 0 " + VbWriter.TO
   + SP + ARRLEN + " - 1");
  vbC.indent(true);
  idlType.writeVbFromAny(vbC, ARRARR + "(" + ARRCNT + ")");
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".nextPos");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + ARRCNT);
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail(VbWriter.INITBYANY);
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INSERTINTOANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  vbC.writeDimLine(ARRCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + ARRCNT + " = 0 " + VbWriter.TO
   + SP + ARRLEN + " - 1");
  vbC.indent(true);
  idlType.writeVbIntoAny(vbC, ARRARR + "(" + ARRCNT + ")");
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".nextPos");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + ARRCNT);
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail(VbWriter.INSERTINTOANY);
  vbC.close();
 }
}
