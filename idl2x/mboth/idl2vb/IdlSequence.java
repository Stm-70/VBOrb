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
  if(iSequence.prevSeq != null)
  { // See also: IdlIdentifier.checkPragmaVbPrefixes()
   if(!iSequence.sVbClsPrefix.equals(iSequence.prevSeq.sVbClsPrefix))
   { TxtReadException ex= new TxtReadException(iSequence.getFilePos(),
     "Attempt to assign a different pragma vbclsprefix ("
     + iSequence.sVbClsPrefix + " != "
     + iSequence.prevSeq.sVbClsPrefix + ") to "
     + "an already declared sequence");
    ex.setNextException(new TxtReadException(
     iSequence.prevSeq.getFilePos(),
     "Position of the first sequence definition"));
    throw ex;
   }
  }
  return iSequence;
 }
 /**
	 *  @param	surScope	Surrounding scope, IdlSpecification or null
	 *  @param	filePos		File position of the identifier
	 */
 private IdlSequence(IdlScope surScope, TxtFilePos filePos)
 { super(surScope, IdlSpecification.SEQUENCE, filePos);
  // See also: IdlIdentifier.sVbClsPrefix
  IdlSpecification idlSpec= surScope.getIdlSpecification();
  this.sVbClsPrefix= idlSpec.getCurVbClsPrefix();
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
  /*  getIdlName() is different here because the VB mapping does not
		 *  support vbnames of typedefs. Otherwise you cannot assign
		 *  sequences of different type names and you have to use #pragma
		 *  very often.
		 */
  sBuf.append(idlType.getOriginIdlType().getIdlName());
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
 /** VB names
	 */
 private final static String LENGTH= "Length";
 private final static String MAXLEN= "maxLen";
 private final static String NEWLEN= "newLen";
 private final static String NEWSIZE= "newSize";
 private final static String SEQCNT= "seqCnt";
 private final static String SEQLEN= "seqLen";
 private final static String SEQBND= "seqBnd";
 private final static String SEQISDIM= "seqIsDim";
 private final static String SEQARR= "seqArr";
 private final static String SP= " ";
 private final static String LUNIQUEID= "lUniqueId";
 /** VB name (without general prefix) or null if unset
	 */
 private String sVbName;
 /** (IdlDefinition)
	 *  Set the Visual Basic Name
	 *  #pragma vbname <idldef> can change the name
	 *
	 *	@param	sVbName		Can be "" if not IdlType
	 *	@return		== null, It was not too late to set
	 *				== sVbName, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 public String setVbName(String sVbName)
 {
  if(prevSeq != null)
   return prevSeq.setVbName(sVbName);
  if(this.sVbName != null)
   return this.sVbName;
  this.sVbName= sVbName;
  return null;
 }
 /** (IdlDefinition)
	 *  Get the Visual Basic Name to identify the definition
	 *
	 *	@param	bPrefix	With final prefix? The name without prefix is used
	 *  				to build complex names.
	 *	@return		Visual Basic Name
	 */
 public String getVbName(boolean bPrefix)
 {
  if(prevSeq != null)
   return prevSeq.getVbName(bPrefix);
  if(sVbName == null)
  { // if "Seq" at the end than "Arr" also at the end
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
   if(orgType instanceof IdlValueBox)
   { if(((IdlValueBox)orgType).isJavaVBoxType())
    { sVbName= sVbName + "VBx";
    }
   }
   sVbName= sVbName + "Seq";
  }
  if(bPrefix)
  { return getVbClsPrefix() + sVbName;
  }
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
  out.writeLine(VbWriter.SET + " " + vbVariable + " = "
   + VbWriter.NEW + " " + getVbName(true));
  out.writeLine(VbWriter.CALL + " " + vbVariable + "."
   + VbWriter.INITBYREAD + "(" + VbWriter.OIN + ", " + max + ")");
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
   + VbWriter.WRITEME + "(" + VbWriter.OOUT + ", " + max + ")");
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
  out.writeLine("oOrb.createSequenceTc(" + max + ", "
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
  if(boxedJavaArray != null)
  {
   // Writing VB value type factory class
   IdlValueBase.writeFactoryCls(
    boxedJavaArray.getScopedName().toString(),
    boxedJavaArray.getFilePos().getFileName(),
    getVbName(true), opts.vbPath);
  }
 }
 /**
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbCls(String vbPath) throws java.io.IOException
 {
  // (getScopedName() is not the ScopedName of the IdlType)
  if(boxedJavaArray != null)
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Writing " + getIdlName()
     + " and " + boxedJavaArray.getScopedName() + " to `"
    + getVbName(true) + ".cls'");
  }else
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Writing " + getIdlName() + " to `"
     + getVbName(true) + ".cls'");
  }
  VbClsWriter vbC= new VbClsWriter(vbPath, getVbName(true),
   getFilePos().getFileName());
  if(boxedJavaArray != null)
  { // Implements cOrbValueBase
   vbC.writeLine();
   vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP + VbWriter.CORBVALUEBASE);
  }
  vbC.writeLine();
  vbC.writeLine("'IDL Name: " + getIdlName());
  // Write TypeId constant declaration
  if(boxedJavaArray != null)
  { vbC.writeLine("'" + IdlSpecification.VALUETYPE + " "
    + boxedJavaArray.getIdlName());
   vbC.writeConstTypeId(/* isPublic */ false, boxedJavaArray.getTypeId());
  }
  vbC.writeLine();
  vbC.writeVarLine(VbWriter.PRIVATE, SEQLEN, VbWriter.LONG);
  vbC.writeVarLine(VbWriter.PRIVATE, SEQBND, VbWriter.LONG);
  vbC.writeVarLine(VbWriter.PRIVATE, SEQARR + "()",
   idlType.getVbName(true));
  vbC.writeVarLine(VbWriter.PRIVATE, SEQISDIM, VbWriter.BOOLEAN);
  if(boxedJavaArray != null)
  { vbC.writeLine();
   vbC.writeVarLine(VbWriter.PRIVATE, LUNIQUEID, VbWriter.LONG);
  }
  // Public Property Get Length() As Long
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PUBLIC, VbWriter.GET, LENGTH);
  vbC.writePropertyBody(VbWriter.LONG, true, false);
  vbC.writeLine(LENGTH + " = " + SEQLEN);
  vbC.writePropertyTail(null, null);
  // Public Property Let Length(ByVal newLen As Long)
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PUBLIC, VbWriter.LET, LENGTH);
  vbC.write(VbWriter.BYVAL + SP + NEWLEN);
  vbC.writePropertyBody(VbWriter.LONG, false, true);
  vbC.writeIf(NEWLEN + " <= 0"); vbC.writeThen();
  vbC.writeLine(SEQLEN + " = 0");
  vbC.writeLine(VbWriter.ERASE + SP + SEQARR);
  vbC.writeLine(SEQISDIM + " = " + VbWriter.FALSE);
  vbC.indent(false);
  vbC.writeLine(VbWriter.ELSE);
  vbC.indent(true);
  vbC.writeIf(SEQBND + " > 0 "
   + VbWriter.AND + SP + NEWLEN + " > " + SEQBND); vbC.writeThen();
  vbC.write(VbWriter.CALL + SP + IDL2VB.getVbOrbDLL()
   + ".raiseBADPARAM(1, " + IDL2VB.getVbOrbDLL() + ".CompletedNO, ");
  vbC.writeLine(VbWriter.CSTR + "(" + NEWLEN + ") & \" > \" & "
   + VbWriter.CSTR + "(" + SEQBND + "))");
  vbC.writeEndIf();
  vbC.writeLine(SEQLEN + " = " + NEWLEN);
  vbC.writeIf(SEQISDIM); vbC.writeThen();
  vbC.writeIf(NEWLEN + " > " + VbWriter.UBOUND + "(" + SEQARR + ") + 1");
  vbC.writeThen();
  vbC.writeDimLine(NEWSIZE, VbWriter.LONG);
  vbC.writeLine(NEWSIZE + " = (" + VbWriter.UBOUND
   + "(" + SEQARR + ") * 3& + 3&) \\ 2&");
  vbC.writeIf(SEQBND + " > 0 " + VbWriter.AND
   + SP + NEWSIZE + " > " + SEQBND); vbC.writeThen();
  vbC.writeLine(NEWSIZE + " = " + SEQBND);
  vbC.writeEndIf();
  vbC.writeIf(NEWLEN + " > " + NEWSIZE);
  vbC.writeThen();
  vbC.writeLine(NEWSIZE + " = " + NEWLEN);
  vbC.writeEndIf();
  vbC.writeLine(VbWriter.REDIM + SP + VbWriter.PRESERVE
   + SP + SEQARR + "(0 " + VbWriter.TO + SP + NEWSIZE + " - 1)");
  vbC.writeEndIf();
  vbC.writeElse();
  vbC.writeLine(VbWriter.REDIM + SP + SEQARR + "(0 " + VbWriter.TO
   + SP + NEWLEN + " - 1)");
  vbC.writeLine(SEQISDIM + " = " + VbWriter.TRUE);
  vbC.writeEndIf();
  vbC.writeEndIf();
  vbC.writePropertyTail(getVbName(false) + "."
   + LENGTH + "." + VbWriter.LET, null);
  // Public Property Get Boundary() As Long
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PUBLIC, VbWriter.GET, "Boundary");
  vbC.writePropertyBody(VbWriter.LONG, true, false);
  vbC.writeLine("Boundary = " + SEQBND);
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
  vbC.writeLine(SEQARR + "(index)");
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
  vbC.writeAssign(SEQARR + "(index)", idlType.isVbSet());
  vbC.writeLine("Item");
  vbC.writePropertyTail(getVbName(false) + ".Item."
   + (idlType.isVbSet()? VbWriter.SET: VbWriter.LET), null);
  // Public Function getItems(ByRef Arr() As ...
  vbC.writeLine();
  vbC.writeFuncHead(VbWriter.PUBLIC, "getItems");
  vbC.writeFuncArg(VbWriter.BYREF, "Arr()", idlType.getVbName(true),
   null);
  vbC.writeFuncBody(VbWriter.LONG, true);
  vbC.writeLine("getItems = " + SEQLEN);
  vbC.writeDimLine(SEQCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + SEQCNT + " = 0 " + VbWriter.TO
   + SP + SEQLEN + " - 1");
  vbC.indent(true);
  // Zuweisung ist stark Typeabh"angig!
  // Eigene Methode in IdlType einbauen???
  vbC.writeAssign("Arr(" + VbWriter.LBOUND + "(Arr) + "
   + SEQCNT + ")", idlType.isVbSet());
  vbC.writeLine(SEQARR + "(" + SEQCNT + ")");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + SEQCNT);
  vbC.writeFuncTail("getItems", null);
  // Public Sub setItems(ByRef Arr() As ...
  vbC.writeLine();
  vbC.writeSubHead(VbWriter.PUBLIC, "setItems");
  vbC.writeSubArg(VbWriter.BYREF, "Arr()", idlType.getVbName(true),
   null);
  vbC.writeSubArg(VbWriter.BYVAL, NEWLEN, VbWriter.LONG, null);
  vbC.writeSubBody(true);
  vbC.writeLine(VbWriter.ME + "." + LENGTH + " = " + NEWLEN);
  vbC.writeDimLine(SEQCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + SEQCNT + " = 0 " + VbWriter.TO
   + SP + SEQLEN + " - 1");
  vbC.indent(true);
  // Zuweisung ist stark Typeabh"angig!
  // Eigene Methode in IdlType einbauen???
  vbC.writeAssign(SEQARR + "(" + SEQCNT + ")", idlType.isVbSet());
  vbC.writeLine("Arr(" + VbWriter.LBOUND + "(Arr) + " + SEQCNT + ")");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + SEQCNT);
  vbC.writeSubTail("setItems");
  // Helper, initByRead()
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INITBYREAD);
  vbC.writeArg(VbWriter.BYVAL, VbWriter.OIN, VbWriter.CORBSTREAM, null);
  vbC.writeSubArg(VbWriter.BYVAL, MAXLEN, VbWriter.LONG, null);
  vbC.writeSubBody(true);
  vbC.writeLine(SEQBND + " = " + MAXLEN);
  vbC.writeDimLine(NEWLEN, VbWriter.LONG);
  vbC.writeLine(NEWLEN + " = " + VbWriter.OIN
   + ".readUlong()");
  vbC.writeIf(SEQBND + " > 0 " + VbWriter.AND
   + SP + NEWLEN + " > " + SEQBND);
  vbC.writeThen();
  vbC.write(VbWriter.CALL + SP + IDL2VB.getVbOrbDLL()
   + ".raiseMARSHAL(1, " + IDL2VB.getVbOrbDLL() + ".CompletedNO, ");
  vbC.writeLine(VbWriter.CSTR + "(" + NEWLEN + ") & \" > \" & "
   + VbWriter.CSTR + "(" + SEQBND + "))");
  vbC.writeEndIf();
  vbC.writeLine(SEQISDIM + " = " + VbWriter.FALSE);
  vbC.writeLine(VbWriter.ME + "." + LENGTH + " = " + NEWLEN);
  // Optimize reading sequence of octets
  IdlType orgType= idlType.getOriginIdlType();
  if(orgType instanceof IdlOctet)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OIN
    + ".readOctets(" + SEQARR + ", " + SEQLEN + ")");
  }else if(orgType instanceof IdlChar &&
   orgType.getIdlName() != IdlSpecification.WCHAR)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OIN
    + ".readChars(" + SEQARR + ", " + SEQLEN + ")");
  }else
  { vbC.writeDimLine(SEQCNT, VbWriter.LONG);
   vbC.writeLine(VbWriter.FOR + SP + SEQCNT + " = 0 " + VbWriter.TO
    + SP + SEQLEN + " - 1");
   vbC.indent(true);
   idlType.writeVbRead(vbC, SEQARR + "(" + SEQCNT + ")");
   vbC.indent(false);
   vbC.writeLine(VbWriter.NEXT + SP + SEQCNT);
  }
  vbC.writeSubTail(getVbName(false) + ".read");
  // Helper, writeMe()
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.WRITEME);
  vbC.writeArg(VbWriter.BYVAL, VbWriter.OOUT, VbWriter.CORBSTREAM, null);
  vbC.writeSubArg(VbWriter.BYVAL, MAXLEN, VbWriter.LONG, null);
  vbC.writeSubBody(true);
  vbC.writeLine(SEQBND + " = " + MAXLEN);
  vbC.writeIf(SEQBND + " > 0 " + VbWriter.AND
   + SP + SEQLEN + " > " + SEQBND);
  vbC.writeThen();
  vbC.write(VbWriter.CALL + SP + IDL2VB.getVbOrbDLL()
   + ".raiseMARSHAL(1, " + IDL2VB.getVbOrbDLL() + ".CompletedNO, ");
  vbC.writeLine(VbWriter.CSTR + "(" + SEQLEN + ") & \" > \" & "
   + VbWriter.CSTR + "(" + SEQBND + "))");
  vbC.writeEndIf();
  vbC.writeLine(VbWriter.CALL + SP + VbWriter.OOUT
    + ".writeUlong(" + SEQLEN + ")");
  // Optimize writing sequence of octets
  if(orgType instanceof IdlOctet)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OOUT
    + ".writeOctets(" + SEQARR + ", " + SEQLEN + ")");
  }else if(orgType instanceof IdlChar &&
   orgType.getIdlName() != IdlSpecification.WCHAR)
  { vbC.writeLine(VbWriter.CALL + SP + VbWriter.OOUT
    + ".writeChars(" + SEQARR + ", " + SEQLEN + ")");
  }else
  { vbC.writeDimLine(SEQCNT, VbWriter.LONG);
   vbC.writeLine(VbWriter.FOR + SP + SEQCNT + " = 0 " + VbWriter.TO
    + SP + SEQLEN + " - 1");
   vbC.indent(true);
   idlType.writeVbWrite(vbC, SEQARR + "(" + SEQCNT + ")");
   vbC.indent(false);
   vbC.writeLine(VbWriter.NEXT + SP + SEQCNT);
  }
  vbC.writeSubTail(getVbName(false) + ".write");
  /* Public Sub initByAny(ByVal oAny As cOrbAny)
		*/
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INITBYANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  vbC.writeAssign(SEQBND, /*isVbSet*/false);
  vbC.writeLine(VbWriter.OANY + ".getOrigType.Length()");
  vbC.writeDimLine(NEWLEN, VbWriter.LONG);
  vbC.writeAssign(NEWLEN, /*isVbSet*/false);
  vbC.writeLine(VbWriter.OANY + ".seqGetLength()");
  vbC.writeIf(SEQBND + " > 0 " + VbWriter.AND
   + SP + NEWLEN + " > " + SEQBND);
  vbC.writeThen();
  vbC.write(VbWriter.CALL + SP + IDL2VB.getVbOrbDLL()
   + ".raiseMARSHAL(1, " + IDL2VB.getVbOrbDLL() + ".CompletedNO, ");
  vbC.writeLine(VbWriter.CSTR + "(" + NEWLEN + ") & \" > \" & "
   + VbWriter.CSTR + "(" + SEQBND + "))");
  vbC.writeEndIf();
  vbC.writeAssign(SEQISDIM, /*isVbSet*/false);
  vbC.writeLine(VbWriter.FALSE);
  vbC.writeAssign(VbWriter.ME + "." + LENGTH, /*isVbSet*/false);
  vbC.writeLine(NEWLEN);
  vbC.writeDimLine(SEQCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + SEQCNT + " = 0 " + VbWriter.TO
   + SP + SEQLEN + " - 1");
  vbC.indent(true);
  idlType.writeVbFromAny(vbC, SEQARR + "(" + SEQCNT + ")");
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".nextPos");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + SEQCNT);
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail(VbWriter.INITBYANY);
  /* Public Sub insertIntoAny(ByVal oAny As cOrbAny)
		*/
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INSERTINTOANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".seqSetLength(" + SEQLEN + ")");
  vbC.writeDimLine(SEQCNT, VbWriter.LONG);
  vbC.writeLine(VbWriter.FOR + SP + SEQCNT + " = 0 " + VbWriter.TO
   + SP + SEQLEN + " - 1");
  vbC.indent(true);
  idlType.writeVbIntoAny(vbC, SEQARR + "(" + SEQCNT + ")");
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".nextPos");
  vbC.indent(false);
  vbC.writeLine(VbWriter.NEXT + SP + SEQCNT);
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail(VbWriter.INSERTINTOANY);
  /* Implement methods to support Valuebase interface
		*/
  if(boxedJavaArray != null)
  {
   // Implements cOrbValueBase
   vbC.writeLine();
   vbC.writeFuncThisObject(VbWriter.FRIEND,
    "OrbValueBase", VbWriter.CORBVALUEBASE);
   // Helper, cOrbValueBase_UniqueId()
   String vbInterface= VbWriter.CORBVALUEBASE;
   String vbMethod= "UniqueId";
   vbC.writeLine();
   vbC.writeLine("'Helper");
   vbC.writeFuncHead(VbWriter.PRIVATE, vbInterface + "_" + vbMethod);
   vbC.writeFuncBody(VbWriter.LONG, false);
   vbC.writeIf(LUNIQUEID + " = 0");
   vbC.writeThen();
   vbC.writeLine(LUNIQUEID + " = " + IDL2VB.getVbOrbDLL()
    + ".getNextUniqueID()");
   vbC.writeEndIf();
   vbC.writeLine(vbInterface + "_" + vbMethod + " = " + LUNIQUEID);
   vbC.writeExitEnd(VbWriter.FUNCTION, null, null);
   // Helper, initByRead()
   vbC.writeInitByReadHead(VbWriter.CORBVALUEBASE, false);
   vbC.writeLine(VbWriter.CALL + " "
    + VbWriter.INITBYREAD + "(" + VbWriter.OIN + ", 0)");
   vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".read", null);
   // Helper, writeMe()
   vbC.writeWriteMeHead(VbWriter.CORBVALUEBASE);
   vbC.writeLine(VbWriter.CALL + " "
    + VbWriter.WRITEME + "(" + VbWriter.OOUT + ", 0)");
   vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".write", null);
   // getIds()
   final String ITEM= "Item";
   vbC.writeLine();
   vbC.writeFuncHead(VbWriter.PRIVATE, vbInterface + "_getIds");
   vbC.writeFuncArg(VbWriter.BYVAL, ITEM, VbWriter.INTEGER, null);
   vbC.writeFuncBody(VbWriter.STRING, false);
   vbC.writeLine(vbInterface + "_" + "getIds" + " = "
    + VbWriter.IIF + "(" + ITEM + " = 0, " + VbWriter.STYPEID
    + ", \"\")");
   vbC.writeFuncTail(null, null);
   vbC.writeLine();
   vbC.writeFuncHead(VbWriter.PRIVATE, vbInterface + "_isCustom");
   vbC.writeFuncBody(VbWriter.BOOLEAN, false);
   vbC.writeLine(vbInterface + "_isCustom = " + VbWriter.FALSE);
   vbC.writeFuncTail(null, null);
  }
  vbC.close();
 }
}
