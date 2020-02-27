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
 *  @author  Martin Both
 */
public class IdlStruct extends IdlContainer implements IdlType
{
 /** Not only forward declared?
	 */
 private TxtFilePos defFilePos;
 private boolean bDefined;
 /** Members
	 */
 private IdlDeclarator idlMembers;
 /** Is a local type?
	 */
 private boolean bLocal;
 private boolean bLocalChecked;
 /** Is this struct also a Java-RMI/IDL IDL entity definition?
	 */
 IdlValueBox boxedIDLEntity;
 /** Read struct head
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iStruct (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlStruct readIdlHead(IdlScope idlScope, TxtToken token,
  TxtTokenReader idlRd) throws TxtReadException
 {
  IdlStruct iStruct;
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readIdlWord(idlScope, token);
  IdlIdentifier identifier= idlScope.getIdentifier(iWord,
   IdlScope.SL_DEFN | IdlScope.SL_INTR);
  if(identifier != null)
  { if(!(identifier instanceof IdlStruct))
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "IDL identifier `" + identifier.getUnEscName()
     + "' redefined after use");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the first identifier definition"));
    throw ex;
   }
   // forward declared or already declared
   iStruct= (IdlStruct)identifier;
   iStruct.checkPragmaPrefix(iWord);
  }else
  { iStruct= new IdlStruct(iWord);
   idlScope.putIdentifier(iStruct, true);
   // The name of an interface, valuetype, struct, union, exception
   // or a module may not be redefined within the immediate scope of
   // the interface, valuetype, struct, union, exception, or the
   // module. That's why we introduce the identifier now.
   // "Cannot redefine name of the struct ...
   // within the immediate scope of the struct"
   iStruct.putIdentifier(iStruct, false);
  }
  return iStruct;
 }
 /** 
	 *  @param	identifier		Identifier
	 */
 private IdlStruct(IdlIdentifier identifier)
 {
  super(identifier);
 }
 /** Read struct members definition if not ';'.
	 *
	 *  @param	idlRd		IdlFile
	 *	@return				Unused token
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public TxtToken readIdl(TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= idlRd.readToken();
  // <constr_forward_decl>
  if(token instanceof TxtTokSepChar &&
   ((TxtTokSepChar)token).getChar() == ';')
  { return token;
  }
  if(bDefined)
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Redefinition of struct `" + getUnEscName() + "'");
   ex.setNextException(new TxtReadException(defFilePos,
    "Position of previous definition"));
   throw ex;
  }
  defFilePos= token.getFilePos();
  // "{" <member>+ "}"
  // "{"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '{')
  { throw new TxtReadException(token.getFilePos(),
    "\"{\" of struct declaration expected");
  }
  getIdlSpecification().setPragmaScope(this);
  // <member>
  TxtTokenRef tRef= new TxtTokenRef();
  token= tRef.getOrReadToken(idlRd);
  do
  { // <member> ::= <type_spec> <declarators> ";"
   // <type_spec>
   tRef.ungetToken(token);
   IdlType iType= IdlBaseType.readTypeSpec(this, tRef, idlRd);
   if(!iType.isCompleteType())
    throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(),
     iType);
   // if(iType.isAnonymousType()) later in IdlDeclarator.readIdlDcl()
   do
   { // <identifier>
    IdlDeclarator iMember= IdlDeclarator.readIdlDcl(iType,
     this, tRef, idlRd);
    // Add member
    if(idlMembers == null)
    { idlMembers= iMember;
    }else
    { idlMembers.addNext(iMember);
    }
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ';')
   { throw new TxtReadException(token.getFilePos(),
     "\";\" of member declaration expected");
   }
   // "}"
   token= idlRd.readToken();
  }while(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '}');
  bDefined= true;
  // Leaving the scope of this struct
  //
  IdlSpecification iSpecification= getIdlSpecification();
  iSpecification.setPragmaScope(getSurScope());
  // Register the struct as a new IDL definition
  //
  iSpecification.registerIdlDef(this);
  return idlRd.readToken();
 }
 /**
	 */
 public void setIDLEntityBox(IdlValueBox boxedIDLEntity)
 {
  this.boxedIDLEntity= boxedIDLEntity;
 }
 /** (IdlType)
	 *  Is a (structure or union) type currently under definition?
	 *
	 *	@return		isUnderDefinitionType
	 */
 public boolean isUnderDefinitionType()
 { return !bDefined && defFilePos != null;
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
 { if(!bLocalChecked)
  { bLocalChecked= true;
   for(IdlDeclarator iDcl= idlMembers; iDcl != null;
    iDcl= (IdlDeclarator)iDcl.getNext())
   { if(iDcl.getIdlType().isLocalType())
    { bLocal= true;
     break;
    }
   }
  }
  return bLocal;
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
 private final static String LUNIQUEID= "lUniqueId";
 /**
	 */
 private String vbClsName;
 /** (IdlDefinition)
	 *  Set the Visual Basic Name
	 *
	 *	@param	sVbName		Can be "" if not IdlType
	 *	@return		== null, It was not too late to set
	 *				== sVbName, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 public String setVbName(String sVbName)
 { return super.setVbName(sVbName);
 }
 /** (IdlDefinition)
	 *  Get the Visual Basic Name to identify the definition
	 *
	 *	@param	bPrefix	With final prefix? The name without prefix is used
	 *  				to build complex names.
	 *	@return		Visual Basic Name
	 */
 public String getVbName(boolean withPrefix)
 { if(vbClsName == null)
   vbClsName= super.getVbName(true);
  return withPrefix? vbClsName: super.getVbName(false);
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
  out.writeAssign(vbVariable, true);
  out.writeLine(getVbModName() + "." + VbWriter.TYPECODE + "()");
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
    System.out.println("D Struct " + getScopedName()
     + " is included only");
   return;
  }
  // Only forward declaration?
  if(!bDefined)
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Struct " + getScopedName()
     + " is only forward declared");
   return;
  }
  /** Write contained IdlDefinitions
		 */
  super.writeVbFiles(opts);
  // Write VB module
  this.writeVbMod(opts.vbPath);
  // Write VB class
  this.writeVbCls(opts.vbPath);
  if(boxedIDLEntity != null)
  {
   // Writing VB value type factory class
   IdlValueBase.writeFactoryCls(
    boxedIDLEntity.getScopedName().toString(),
    boxedIDLEntity.getFilePos().getFileName(),
    getVbName(true), opts.vbPath);
  }
 }
 /** Write VB module
	 *
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbMod(String vbPath) throws java.io.IOException
 {
  String sVbModName= getVbModName();
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing struct " + getScopedName() + " to `"
    + sVbModName + ".bas'");
  VbModWriter vbM= new VbModWriter(vbPath, sVbModName,
   getFilePos().getFileName());
  // First of all write constants, later it is not allowed.
  // Write TypeId constant declaration
  vbM.writeLine();
  this.writeConstScopeTypeId(vbM, IdlSpecification.STRUCT,
   /* isPublic */ true);
  /* (IdlContainer)
		*/
  this.writeVbModConsts(vbM);
  /* (IdlContainer)
		*/
  this.writeVbModHelpers(vbM);
  vbM.writeLine();
  vbM.writeLine("'Helper");
  vbM.writeFuncHead(VbWriter.PUBLIC, VbWriter.TYPECODE);
  vbM.writeFuncBody(VbWriter.CORBTYPECODE, true);
  vbM.writeDimLine("oOrb", "cOrbImpl");
  vbM.writeAssign("oOrb", true);
  vbM.writeLine(IDL2VB.getVbOrbDLL() + ".defaultOrb()");
  vbM.writeLine("'Get previously created recursive or concrete TypeCode");
  vbM.writeDimLine("oRecTC", VbWriter.CORBTYPECODE);
  vbM.writeAssign("oRecTC", true);
  vbM.writeLine("oOrb.getRecursiveTC(" + VbWriter.TYPEID
   + ", 15) 'mCB.tk_struct");
  vbM.writeIf("oRecTC" + VbWriter.SP + VbWriter.IS
   + VbWriter.SP + VbWriter.NOTHING);
  vbM.writeThen();
  vbM.writeLine("'Create place holder for TypeCode to avoid endless recursion");
  vbM.writeAssign("oRecTC", true);
  vbM.writeLine("oOrb.createRecursiveTc(" + VbWriter.TYPEID + ")");
  vbM.writeOnErrorLine("ErrRollback");
  vbM.writeLine("'Describe struct members");
  vbM.writeDimLine("oMemSeq", "cCBStructMemberSeq");
  vbM.writeAssign("oMemSeq", true);
  vbM.writeLine(VbWriter.NEW + VbWriter.SP + "cCBStructMemberSeq");
  vbM.writeAssign("oMemSeq.Length", false);
  int iLen= 0;
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { iLen++;
  }
  vbM.writeLine(Integer.toString(iLen));
  iLen= 0;
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { String sItem= "oMemSeq.Item(" + Integer.toString(iLen) + ")";
   vbM.writeAssign(sItem, true);
   vbM.writeLine(VbWriter.NEW + VbWriter.SP + "cCBStructMember");
   vbM.writeAssign(sItem + ".name", false);
   vbM.writeLine("\"" + def.getUnEscName() + "\"");
   def.getIdlType().writeVbAssignTypeCode(vbM, sItem + ".p_type");
   iLen++;
  }
  vbM.writeLine("'Overwrite place holder");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP
   + "oRecTC.setRecTc2StructTc(\"" + getUnEscName() + "\", oMemSeq)");
  vbM.writeEndIf();
  vbM.writeAssign(VbWriter.TYPECODE, true);
  vbM.writeLine("oRecTC");
  vbM.writeLine(VbWriter.EXIT + VbWriter.SP + VbWriter.FUNCTION);
  vbM.writeLabelLine("ErrRollback");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oRecTC.destroy");
  vbM.writeLabelLine(VbWriter.ERRHANDLER);
  vbM.writeErrReraiseLine(VbWriter.TYPECODE);
  vbM.writeFuncTail(null, null);
  vbM.writeLine();
  vbM.writeLine("'Helper, oAny.writeValue() -> struct.initByRead()");
  vbM.writeFuncHead(VbWriter.PUBLIC, "extractFromAny");
  vbM.writeFuncArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY,
   null);
  vbM.writeFuncBody(getVbName(true), false);
  vbM.writeDimLine("oStruct", getVbName(true));
  vbM.writeAssign("oStruct", true);
  vbM.writeLine(VbWriter.NEW + VbWriter.SP + getVbName(true));
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oStruct" + "."
   + VbWriter.INITBYANY + "(" + VbWriter.OANY + ")");
  vbM.writeAssign("extractFromAny", true);
  vbM.writeLine("oStruct");
  vbM.writeFuncTail(null, null);
  vbM.writeLine();
  vbM.writeLine("'Helper, struct.writeMe() -> oAny.initByReadValue()");
  vbM.writeFuncHead(VbWriter.PUBLIC, "cloneAsAny");
  vbM.writeFuncArg(VbWriter.BYVAL, "oStruct", getVbName(true),
   null);
  vbM.writeFuncBody(VbWriter.CORBANY, true);
  vbM.writeDimLine(VbWriter.OANY, VbWriter.CORBANY);
  vbM.writeAssign(VbWriter.OANY, true);
  vbM.writeLine(VbWriter.NEW + VbWriter.SP + VbWriter.CORBANY);
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + VbWriter.OANY
   + ".initByDefaultValue(" + VbWriter.TYPECODE + "())");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oStruct"
   + "." + VbWriter.INSERTINTOANY + "(" + VbWriter.OANY + ")");
  vbM.writeAssign("cloneAsAny", true);
  vbM.writeLine(VbWriter.OANY);
  vbM.writeFuncTail("cloneAsAny", null);
  vbM.close();
 }
 /** Write VB class
	 *
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbCls(String vbPath) throws java.io.IOException
 {
  if(boxedIDLEntity != null)
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Writing struct " + getScopedName()
     + " and " + boxedIDLEntity.getScopedName()
     + " to `" + getVbName(true) + ".cls'");
  }else
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Writing struct " + getScopedName()
     + " to `" + getVbName(true) + ".cls'");
  }
  VbClsWriter vbC= new VbClsWriter(vbPath, getVbName(true),
   getFilePos().getFileName());
  if(boxedIDLEntity != null)
  { // Implements cOrbValueBase
   vbC.writeLine();
   vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP + VbWriter.CORBVALUEBASE);
  }
  // Write TypeId constant declaration
  vbC.writeLine();
  vbC.writeLine("'" + IdlSpecification.STRUCT + " "
   + getScopedName().toString());
  if(boxedIDLEntity != null)
  { vbC.writeLine("'" + IdlSpecification.VALUETYPE + " "
    + boxedIDLEntity.getIdlName());
   vbC.writeConstTypeId(/* isPublic */ false, boxedIDLEntity.getTypeId());
  }else
  { vbC.writeConstTypeId(/* isPublic */ false, getTypeId());
  }
  vbC.writeLine();
  vbC.writeLine("'Member(s)");
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.writeVbPrivateVarDecl(vbC);
  }
  if(boxedIDLEntity != null)
  { vbC.writeLine();
   vbC.writeVarLine(VbWriter.PRIVATE, LUNIQUEID, VbWriter.LONG);
  }
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { vbC.writeLine();
   vbC.writeLine("'Member: " + def.getUnEscName());
   def.writeVbPropFuncs(vbC,
    /*getOnly*/false, /*vbInterface*/null);
  }
  // Helper, initByRead()
  vbC.writeInitByReadHead(VbWriter.PUBLIC, false);
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.writeVbPrivateVarRead(vbC);
  }
  vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".read", null);
  // Helper, writeMe()
  vbC.writeWriteMeHead(VbWriter.PUBLIC);
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.writeVbPrivateVarWrite(vbC);
  }
  vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".write", null);
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INITBYANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.getIdlType().writeVbFromAny(vbC, def.getVbPrivateVarName());
   vbC.writeLine(VbWriter.CALL + VbWriter.SP
    + VbWriter.OANY + ".nextPos");
  }
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail(VbWriter.INITBYANY);
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INSERTINTOANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.getIdlType().writeVbIntoAny(vbC, def.getVbPrivateVarName());
   vbC.writeLine(VbWriter.CALL + VbWriter.SP
    + VbWriter.OANY + ".nextPos");
  }
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail(VbWriter.INSERTINTOANY);
  if(boxedIDLEntity != null)
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
    + VbWriter.INITBYREAD + "(" + VbWriter.OIN + ")");
   vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".read", null);
   // Helper, writeMe()
   vbC.writeWriteMeHead(VbWriter.CORBVALUEBASE);
   vbC.writeLine(VbWriter.CALL + " "
    + VbWriter.WRITEME + "(" + VbWriter.OOUT + ")");
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
