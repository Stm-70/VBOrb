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
/** Exception declarations permit the declaration of struct-like
 *  data structures which may be returned to indicate that an exceptional
 *  condition has occurred during the performance of a request.
 *
 *  @author  Martin Both
 */
public class IdlException extends IdlContainer implements IdlDefinition
{
 /** Members
	 */
 private IdlDeclarator idlMembers;
 /** Read exception head
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iException (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlException readIdlHead(IdlScope idlScope, TxtToken token,
  TxtTokenReader idlRd) throws TxtReadException
 {
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(idlScope, token);
  IdlException iException= new IdlException(iWord);
  idlScope.putIdentifier(iException, true);
  // The name of an interface, valuetype, struct, union, exception
  // or a module may not be redefined within the immediate scope of
  // the interface, valuetype, struct, union, exception, or the
  // module. That's why we introduce the identifier now.
  iException.putIdentifier(iException, false);
  return iException;
 }
 /** 
	 *  @param	identifier		Identifier
	 */
 private IdlException(IdlIdentifier identifier)
 {
  super(identifier);
 }
 /** Read exception members
	 *
	 *  @param	idlRd		IdlFile
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public void readIdl(TxtTokenReader idlRd) throws TxtReadException
 {
  // "{" <member>* "}"
  // "{"
  TxtToken token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '{')
  { throw new TxtReadException(token.getFilePos(),
    "\"{\" of exception declaration expected");
  }
  // "}"
  TxtTokenRef tRef= new TxtTokenRef();
  token= tRef.getOrReadToken(idlRd);
  while(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '}')
  {
   // <member> ::= <type_spec> <declarators> ";"
   // <type_spec>
   tRef.ungetToken(token);
   IdlType iType= IdlBaseType.readTypeSpec(this, tRef, idlRd);
   if(!iType.isCompleteType())
    throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(), iType);
   // if(iType.isAnonymousType()) later in IdlDeclarator.readIdlDcl()
   do
   { // <identifier> [ "[" <positive_int_const> "]" + ]
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
   token= idlRd.readToken();
  }
  // Register the exception as a new IDL definition
  //
  getIdlSpecification().registerIdlDef(this);
 }
 /** Reserved words
	 */
 public final static String SSOURCE= "sSource";
 public final static String SPOSTDESCR= "sPostDescr";
 private final static String SOURCE= "Source";
 private final static String DESCRIPTION= "Description";
 private final static String SOURCEPREFIX= "SourcePrefix";
 private final static String POSTDESCR= "PostDescr";
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
 { return super.getVbName(withPrefix);
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
    System.out.println("D Exception " + getScopedName()
     + " is included only");
   return;
  }
  /** Write contained IdlDefinitions
		 */
  super.writeVbFiles(opts);
  // Write VB module
  this.writeVbMod(opts.vbPath);
  // Write VB class
  this.writeVbCls(opts.vbPath);
 }
 /** Write VB module
	 *
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbMod(String vbPath) throws java.io.IOException
 {
  /* if(idlConsts == null && idlEnums == null)
			return;
		*/
  String sVbModName= getVbModName();
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing exception " + getScopedName()
    + " to `" + sVbModName + ".bas'");
  VbModWriter vbM= new VbModWriter(vbPath, sVbModName,
   getFilePos().getFileName());
  // First of all write constants, later it is not allowed.
  // Write TypeId constant declaration
  vbM.writeLine();
  this.writeConstScopeTypeId(vbM, IdlSpecification.EXCEPTION,
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
   + ", 22) 'mCB.tk_except");
  vbM.writeIf("oRecTC" + VbWriter.SP + VbWriter.IS
   + VbWriter.SP + VbWriter.NOTHING);
  vbM.writeThen();
  vbM.writeLine("'Create place holder for TypeCode to avoid endless recursion");
  vbM.writeAssign("oRecTC", true);
  vbM.writeLine("oOrb.createRecursiveTc(" + VbWriter.TYPEID + ")");
  vbM.writeOnErrorLine("ErrRollback");
  vbM.writeLine("'Describe exception members");
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
   + "oRecTC.setRecTc2ExceptionTc(\"" + getUnEscName() + "\", oMemSeq)");
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
  vbM.writeLine("'Helper, oAny.writeValue() -> exception.initByRead()");
  vbM.writeFuncHead(VbWriter.PUBLIC, "extractFromAny");
  vbM.writeFuncArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY,
   null);
  vbM.writeFuncBody(getVbName(true), false);
  vbM.writeDimLine("oException", getVbName(true));
  vbM.writeAssign("oException", true);
  vbM.writeLine(VbWriter.NEW + VbWriter.SP + getVbName(true));
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oException" + "."
   + VbWriter.INITBYANY + "(" + VbWriter.OANY + ")");
  vbM.writeAssign("extractFromAny", true);
  vbM.writeLine("oException");
  vbM.writeFuncTail(null, null);
  vbM.writeLine();
  vbM.writeLine("'Helper, exception.writeMe() -> oAny.initByReadValue()");
  vbM.writeFuncHead(VbWriter.PUBLIC, "cloneAsAny");
  vbM.writeFuncArg(VbWriter.BYVAL, "oException", getVbName(true),
   null);
  vbM.writeFuncBody(VbWriter.CORBANY, true);
  vbM.writeDimLine(VbWriter.OANY, VbWriter.CORBANY);
  vbM.writeAssign(VbWriter.OANY, true);
  vbM.writeLine(VbWriter.NEW + VbWriter.SP + VbWriter.CORBANY);
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + VbWriter.OANY
   + ".initByDefaultValue(" + VbWriter.TYPECODE + "())");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oException"
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
  String vbWriteName;
  vbWriteName= getVbName(true);
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing exception " + getScopedName()
    + " to `" + vbWriteName + ".cls'");
  VbClsWriter vbC= new VbClsWriter(vbPath, vbWriteName,
   getFilePos().getFileName());
  // Implements cOrbException
  vbC.writeLine();
  vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP
   + VbWriter.CORBEXCEPTION);
  // Write TypeId constant declaration
  vbC.writeLine();
  this.writeConstScopeTypeId(vbC, IdlSpecification.EXCEPTION,
   /* isPublic */ false);
  vbC.writeVarLine(VbWriter.PRIVATE, SSOURCE, VbWriter.STRING);
  vbC.writeVarLine(VbWriter.PRIVATE, SPOSTDESCR, VbWriter.STRING);
  if(idlMembers != null)
  { vbC.writeLine();
   vbC.writeLine("'Member(s)");
   for(IdlDeclarator def= idlMembers; def != null;
    def= (IdlDeclarator)def.getNext())
   { def.writeVbPrivateVarDecl(vbC);
   }
  }
  // Implements cOrbException
  vbC.writeLine();
  vbC.writeFuncThisObject(VbWriter.PUBLIC,
   "OrbException", VbWriter.CORBEXCEPTION);
  if(idlMembers != null)
  { for(IdlDeclarator def= idlMembers; def != null;
    def= (IdlDeclarator)def.getNext())
   { vbC.writeLine();
    vbC.writeLine("'Member: " + def.getUnEscName());
    def.writeVbPropFuncs(vbC,
     /*getOnly*/false, /*vbInterface*/null);
   }
  }
  // Get TypeId
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PRIVATE, VbWriter.GET,
   VbWriter.CORBEXCEPTION + "_" + VbWriter.TYPEID);
  vbC.writePropertyBody(VbWriter.STRING, true, false);
  vbC.writeLine(VbWriter.CORBEXCEPTION + "_" + VbWriter.TYPEID
   + " = " + VbWriter.STYPEID);
  vbC.writePropertyTail(null, null);
  // Get Source
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PRIVATE, VbWriter.GET,
   VbWriter.CORBEXCEPTION + "_" + SOURCE);
  vbC.writePropertyBody(VbWriter.STRING, true, false);
  vbC.writeLine(VbWriter.CORBEXCEPTION + "_" + SOURCE
   + " = " + SSOURCE);
  vbC.writePropertyTail(null, null);
  // Get Description
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PRIVATE, VbWriter.GET,
   VbWriter.CORBEXCEPTION + "_" + DESCRIPTION);
  vbC.writePropertyBody(VbWriter.STRING, true, false);
  vbC.write(VbWriter.CORBEXCEPTION + "_" + DESCRIPTION
   + " = \"CORBA User Exception: [\" & " + VbWriter.STYPEID
   + " & \"], \" & ");
  vbC.write(VbWriter.TYPENAME + "(" + VbWriter.ME + ") & ");
  vbC.writeLine(SPOSTDESCR);
  vbC.writePropertyTail(null, null);
  // addInfos()
  vbC.writeLine();
  vbC.writeSubHead(VbWriter.PRIVATE, VbWriter.CORBEXCEPTION + "_"
   + "addInfos");
  vbC.writeSubArg(VbWriter.BYREF, SOURCEPREFIX, VbWriter.STRING,
   "\"\"");
  vbC.writeSubArg(VbWriter.BYREF, POSTDESCR, VbWriter.STRING,
   "\"\"");
  vbC.writeSubBody(false);
  vbC.writeIf(SSOURCE + " = \"\""); vbC.writeThen();
   vbC.writeLine(SSOURCE + " = " + SOURCEPREFIX);
  vbC.writeElseIf(SOURCEPREFIX + " <> \"\""); vbC.writeThen();
   vbC.writeLine(SSOURCE + " = " + SOURCEPREFIX
    + " & \": \" & " + SSOURCE);
  vbC.writeEndIf();
  vbC.writeIf(POSTDESCR + " <> \"\""); vbC.writeThen();
   vbC.writeLine(SPOSTDESCR + " = " + SPOSTDESCR
    + " & \", \" & " + POSTDESCR);
  vbC.writeEndIf();
  vbC.writeSubTail(null);
  // Helper, initByRead()
  vbC.writeInitByReadHead(VbWriter.CORBEXCEPTION, false);
  if(idlMembers == null)
   vbC.writeLine("'There is nothing to read from oIn because this exception has no members");
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.writeVbPrivateVarRead(vbC);
  }
  vbC.writeSubTail(getUnEscName() + ".read");
  // Helper, writeMe()
  vbC.writeWriteMeHead(VbWriter.CORBEXCEPTION);
  vbC.writeLine(VbWriter.CALL + " " + VbWriter.OOUT
   + ".writeString(" + VbWriter.STYPEID + ")");
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.writeVbPrivateVarWrite(vbC);
  }
  vbC.writeSubTail(getUnEscName() + ".write");
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INITBYANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(/*vbOnErrGo*/idlMembers != null);
  if(idlMembers == null)
   vbC.writeLine("'There is nothing to get from oAny because this exception has no members");
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.getIdlType().writeVbFromAny(vbC, def.getVbPrivateVarName());
   vbC.writeLine(VbWriter.CALL + VbWriter.SP
    + VbWriter.OANY + ".nextPos");
  }
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail((idlMembers != null)? VbWriter.INITBYANY: null);
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INSERTINTOANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(/*vbOnErrGo*/idlMembers != null);
  if(idlMembers == null)
   vbC.writeLine("'There is nothing to insert into oAny because this exception has no members");
  for(IdlDeclarator def= idlMembers; def != null;
   def= (IdlDeclarator)def.getNext())
  { def.getIdlType().writeVbIntoAny(vbC, def.getVbPrivateVarName());
   vbC.writeLine(VbWriter.CALL + VbWriter.SP
    + VbWriter.OANY + ".nextPos");
  }
  vbC.writeLine(VbWriter.CALL + VbWriter.SP
   + VbWriter.OANY + ".rewind");
  vbC.writeSubTail((idlMembers != null)? VbWriter.INSERTINTOANY: null);
  vbC.close();
 }
}
