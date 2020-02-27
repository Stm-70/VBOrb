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
 *
 *  @author  Martin Both
 */
public class IdlValueBox extends IdlIdentifier implements IdlType
{
 /** <type_spec> excepting valuetype
	 */
 private IdlType idlType;
 /** Is a (boxedRMI sequence) Java-RMI/IDL array definition?
	 *	Or is a (boxedIDL) Java-RMI/IDL IDL entity type definition?
	 */
 private static final int NoJavaVBoxType= -1;
 private static final int JavaArrayType= 1;
 private static final int JavaIDLEntityType= 2;
 private int iJavaVBoxType;
 /** Read declarator of value_box <value_box_dcl>
	 *
	 *  @param	surContainer
	 *  @param	iWord		new <identifier>
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iValueBox (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlValueBox readIdlValueBox(IdlContainer surContainer,
  IdlIdentifier iWord, TxtTokenRef tRef, TxtTokenReader idlRd)
  throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readTypeSpec(surContainer, tRef, idlRd);
  // Anonymous type is allowed here
  IdlType oType= iType.getOriginIdlType();
  if(oType instanceof IdlValueType || oType instanceof IdlValueBox)
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Any IDL type may be used to declare a value box"
    + " except for a valuetype");
   ex.setNextException(new TxtReadException(iType.getFilePos(),
    "Position of the given type definition"));
   throw ex;
  }
  IdlValueBox iValueBox= new IdlValueBox(iWord, iType);
  surContainer.putIdentifier(iValueBox, true);
  // (Maybe tell sequence that it is a boxed JavaArray)
  // (Maybe tell IDL entity that it is a boxed IDL entity)
  if(iValueBox.getJavaVBoxType() == NoJavaVBoxType)
  { // Register this value_box as a new IDL definition
   //
   surContainer.getIdlSpecification().registerIdlDef(iValueBox);
  }
  return iValueBox;
 }
 /** 
	 *  @param	iWord		Identifier
	 *  @param	iType	
	 */
 protected IdlValueBox(IdlIdentifier iWord, IdlType iType)
 {
  super(iWord);
  this.idlType= iType;
 }
 /** Is a Java-RMI/IDL value box definition?
	 *
	 *	@return		JavaVBoxType
	 */
 public int getJavaVBoxType()
 { if(iJavaVBoxType != 0)
   return iJavaVBoxType;
  String sIdlName= this.getIdlName();
  if(sIdlName.startsWith("::org::omg::boxed"))
  { if(sIdlName.startsWith("::org::omg::boxedRMI::"))
   { if(idlType.getOriginIdlType() instanceof IdlSequence)
    { // Tell sequence that it is a boxed JavaArray
     ((IdlSequence)idlType.getOriginIdlType()).setJavaArray(this);
     iJavaVBoxType= JavaArrayType;
    }else
    { iJavaVBoxType= NoJavaVBoxType;
    }
   }else if(sIdlName.startsWith("::org::omg::boxedIDL::"))
   { if(idlType.getOriginIdlType() instanceof IdlStruct)
    { // Tell entity that it is a boxed Java IDL entity
     ((IdlStruct)idlType.getOriginIdlType()).setIDLEntityBox(this);
    }
    // valuetype _Any any;
    // #pragma ID _Any "RMI:org.omg.CORBA.Any:0000000000000000"
    // valuetype _TypeCode ::CORBA::TypeCode;
    // #pragma ID _TypeCode "RMI:org.omg.CORBA.TypeCode:0000000000000000"
    iJavaVBoxType= JavaIDLEntityType;
   }else
   { iJavaVBoxType= NoJavaVBoxType;
   }
  }else
  { iJavaVBoxType= NoJavaVBoxType;
  }
  return iJavaVBoxType;
 }
 /** Is a Java-RMI/IDL definition?
	 *
	 *	@return		isJavaVBoxType
	 */
 public boolean isJavaVBoxType()
 { return this.getJavaVBoxType() != NoJavaVBoxType;
 }
 /** Is a Java-RMI/IDL array definition?
	 *
	 *	@return		isBoxedRMIArray
	 */
 public boolean isBoxedRMIArray()
 { return this.getJavaVBoxType() == JavaArrayType;
 }
 /** Is a Java-RMI/IDL IDL entity type definition?
	 *
	 *	@return		isBoxedIDLEntity
	 */
 public boolean isBoxedIDLEntity()
 { return this.getJavaVBoxType() == JavaIDLEntityType;
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return getScopedName().toString();
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
 { return idlType.isCompleteType() || idlType.isUnderDefinitionType();
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
 { return false;
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
	 *  If an array declarator then return an array type.
	 *
	 *	@return		iType
	 */
 public IdlType getOriginIdlType()
 { return this;
 }
 /** VB value name
	 */
 private final static String LUNIQUEID= "lUniqueId";
 public final static String VALUE_= "Value_";
 public final static String VALUE= "Value";
 /** VB name (without general prefix) or null if unset
	 */
 private String sVbName;
 private String sVbClsName;
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
 { if(this.isJavaVBoxType())
   return idlType.setVbName(sVbName);
  if(this.sVbName != null || this.isBoxedIDLEntity())
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
 { if(this.isJavaVBoxType())
   return idlType.getVbName(bPrefix);
  if(sVbName == null)
   sVbName= this.getIdlContainer().getVbName(false)
    + getUnEscName();
  if(sVbClsName == null)
   sVbClsName= getVbClsPrefix() + sVbName;
  return bPrefix? sVbClsName: sVbName;
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
  out.writeLine(VbWriter.SET + " " + vbVariable + " = " + VbWriter.OIN
   + ".readValue(" + VbWriter.NEW + " " + getVbName(true) + ")");
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
  out.writeLine(VbWriter.CALL + " " + VbWriter.OOUT
   + ".writeValue(" + vbVariable + ")");
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
  // boxedRMIs are integrated in the sequence type
  // boxedIDLs are integrated in the IDL entity type
  // isBoxedRMIArray() ???
  // isBoxedIDLEntity() ???
  out.writeAssign(vbVariable, true);
  if(isBoxedRMIArray() || isBoxedIDLEntity())
  { // NOT IMPLEMENTED ???
   // sequence has to write a VB module with the RM/IDL-boxed TypeCode
   // mCB.tk_value = 29
   out.writeLine("oOrb.createPrimitiveTc(29) 'VBOrb.TCValueBase");
  }else
  { out.writeLine(this.getIdlContainer().getVbModName() + "."
    + getUnEscName() + "_" + VbWriter.TYPECODE + "()");
  }
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
  out.writeLine(VbWriter.OANY + "." + "getVal" + "()");
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
  out.writeLine(VbWriter.CALL + " " + VbWriter.OANY + "."
   + "insertVal" + "(" + vbVariable + ")");
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
    System.out.println("D Value box " + getScopedName()
     + " is included only");
   return;
  }
  // boxedRMIs are integrated in the sequence type
  // boxedIDLs are integrated in the IDL entity type
  if(this.isJavaVBoxType())
   return;
  // Write VB class
  this.writeVbCls(opts.vbPath);
  // Writing VB value type factory class
  IdlValueBase.writeFactoryCls(getScopedName().toString(),
   getFilePos().getFileName(), getVbName(true), opts.vbPath);
 }
 /** Write VB class
	 *
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbCls(String vbPath) throws java.io.IOException
 {
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing value box " + getScopedName()
    + " to `" + getVbName(true) + ".cls'");
  VbClsWriter vbC= new VbClsWriter(vbPath, getVbName(true),
   getFilePos().getFileName());
  // Implements cOrbValueBase
  vbC.writeLine();
  vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP + VbWriter.CORBVALUEBASE);
  // Write TypeId constant declaration
  vbC.writeLine();
  vbC.writeLine("'" + IdlSpecification.VALUETYPE + " " + getIdlName());
  vbC.writeConstTypeId(/* Public */ false, getTypeId());
  vbC.writeLine();
  vbC.writeVarLine(VbWriter.PRIVATE, LUNIQUEID, VbWriter.LONG);
  vbC.writeLine();
  vbC.writeVarLine(VbWriter.PRIVATE, VALUE_, idlType.getVbName(true));
  vbC.writeLine();
  String vbKind= VbWriter.GET;
  vbC.writeAttributeHead(/*vbInterface*/null, VALUE,
   VALUE, vbKind, idlType, /*vbOnErrGo*/false);
  vbC.writeAssign(VALUE, idlType.isVbSet());
  vbC.writeLine(VALUE_);
  vbC.writeAttributeTail(/*vbInterface*/null,
   VALUE, vbKind, idlType, /*vbOnErrGo*/false);
  vbC.writeLine();
  vbKind= idlType.isVbSet()? VbWriter.SET: VbWriter.LET;
  vbC.writeAttributeHead(/*vbInterface*/null, VALUE,
   VALUE, vbKind, idlType, /*vbOnErrGo*/false);
  vbC.writeAssign(VALUE_, idlType.isVbSet());
  vbC.writeLine(VALUE + "New");
  vbC.writeAttributeTail(/*vbInterface*/null,
   VALUE, vbKind, idlType, /*vbOnErrGo*/false);
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
  idlType.writeVbRead(vbC, VALUE_);
  vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".read", null);
  // Helper, writeMe()
  vbC.writeWriteMeHead(VbWriter.CORBVALUEBASE);
  idlType.writeVbWrite(vbC, VALUE_);
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
  vbC.close();
 }
 /**
	 *
	 *	@return	
	 */
 public boolean hasToWriteVbMod()
 {
  // boxedRMIs are integrated in the sequence type
  // boxedIDLs are integrated in the IDL entity type
  if(this.isJavaVBoxType())
   return false;
  return true;
 }
 /**
	 *  @param	vbM		VbModWriter
	 *
	 *	@exception	IOException	
	 */
 public void writeVbModHelpers(VbModWriter vbM) throws java.io.IOException
 {
  // boxedRMIs are integrated in the sequence type
  // boxedIDLs are integrated in the IDL entity type
  if(!hasToWriteVbMod())
   return;
  String sVbFuncName= this.getUnEscName() + "_" + VbWriter.TYPECODE;
  vbM.writeLine();
  vbM.writeLine("'ValueBox " + this.getUnEscName() + ", Helper");
  vbM.writeFuncHead(VbWriter.PUBLIC, sVbFuncName);
  vbM.writeFuncBody(VbWriter.CORBTYPECODE, true);
  vbM.writeDimLine(VbWriter.STYPEID, VbWriter.STRING);
  vbM.writeAssign(VbWriter.STYPEID, /*isVbSet*/false);
  vbM.writeLine("\"" + getTypeId() + "\"");
  vbM.writeDimLine("oOrb", "cOrbImpl");
  vbM.writeAssign("oOrb", /*isVbSet*/true);
  vbM.writeLine(IDL2VB.getVbOrbDLL() + ".defaultOrb()");
  vbM.writeLine("'Get previously created recursive or concrete TypeCode");
  vbM.writeDimLine("oRecTC", VbWriter.CORBTYPECODE);
  vbM.writeAssign("oRecTC", true);
  vbM.writeLine("oOrb.getRecursiveTC(" + VbWriter.STYPEID
   + ", 30) 'mCB.tk_value_box");
  vbM.writeIf("oRecTC" + VbWriter.SP + VbWriter.IS
   + VbWriter.SP + VbWriter.NOTHING);
  vbM.writeThen();
  vbM.writeLine("'Create place holder for TypeCode to avoid endless recursion");
  vbM.writeAssign("oRecTC", true);
  vbM.writeLine("oOrb.createRecursiveTc(" + VbWriter.STYPEID + ")");
  vbM.writeOnErrorLine("ErrRollback");
  vbM.writeLine("'Describe ValueBox");
  vbM.writeDimLine("oBoxedType", VbWriter.CORBTYPECODE);
  this.idlType.writeVbAssignTypeCode(vbM, "oBoxedType");
  vbM.writeLine("'Overwrite place holder");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP
   + "oRecTC.setRecTc2ValueBoxTc(\"" + getUnEscName()
   + "\", oBoxedType)");
  vbM.writeEndIf();
  vbM.writeAssign(sVbFuncName, true);
  vbM.writeLine("oRecTC");
  vbM.writeLine(VbWriter.EXIT + VbWriter.SP + VbWriter.FUNCTION);
  vbM.writeLabelLine("ErrRollback");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oRecTC.destroy");
  vbM.writeLabelLine(VbWriter.ERRHANDLER);
  vbM.writeErrReraiseLine(sVbFuncName);
  vbM.writeFuncTail(null, null);
 }
}
