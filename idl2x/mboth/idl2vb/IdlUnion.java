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
public class IdlUnion extends IdlContainer implements IdlType
{
 /** Not only forward declared?
	 */
 private TxtFilePos defFilePos;
 private boolean bDefined;
 /** Type of the discriminator
	 */
 private IdlType iSwitchTypeDef;
 private IdlSwitchType origSwitchType;
 /** "case" <const_exp> ":"
	 */
 private IdlConstValue eltLblVals[][];
 /** <element_spec> ";"
	 */
 private IdlDeclarator eltCase[];
 /** "default" ":" <element_spec> ";"
	 */
 private IdlDeclarator eltDefault;
 /** Is a local type?
	 */
 private boolean bLocal;
 private boolean bLocalChecked;
 /** Read union head
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iUnion (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlUnion readIdlHead(IdlScope idlScope, TxtToken token,
  TxtTokenReader idlRd) throws TxtReadException
 {
  IdlUnion iUnion;
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readIdlWord(idlScope, token);
  IdlIdentifier identifier= idlScope.getIdentifier(iWord,
   IdlScope.SL_DEFN | IdlScope.SL_INTR);
  if(identifier != null)
  { if(!(identifier instanceof IdlUnion))
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "IDL identifier `" + identifier.getUnEscName()
     + "' redefined after use");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the first identifier definition"));
    throw ex;
   }
   // forward declared or already declared
   iUnion= (IdlUnion)identifier;
   iUnion.checkPragmaPrefix(iWord);
  }else
  { iUnion= new IdlUnion(iWord);
   idlScope.putIdentifier(iUnion, true);
   // The name of an interface, valuetype, struct, union, exception
   // or a module may not be redefined within the immediate scope of
   // the interface, valuetype, struct, union, exception, or the
   // module. That's why we introduce the identifier now.
   iUnion.putIdentifier(iUnion, false);
  }
  return iUnion;
 }
 /** 
	 *  @param	identifier		Identifier
	 */
 private IdlUnion(IdlIdentifier identifier)
 {
  super(identifier);
 }
 /** Read union members definition if not ';'.
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
  // "switch" "(" <switch_type_spec> ")" "{" <switch_body> "}"		
  // "switch"
  String keyword= IdlSpecification.readKeyword(token, true);
  if(keyword != IdlSpecification.SWITCH)
  { throw new TxtReadException(token.getFilePos(),
    "\"switch\" of union declaration expected");
  }
  // "("
  token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '(')
  { throw new TxtReadException(token.getFilePos(),
    "\"(\" of union declaration expected");
  }
  getIdlSpecification().setPragmaScope(this);
  // Read a <switch_type_spec>.
  TxtTokenRef tRef= new TxtTokenRef();
  iSwitchTypeDef= IdlBaseType.readSwitchType(this, tRef, idlRd);
  origSwitchType= (IdlSwitchType)iSwitchTypeDef.getOriginIdlType();
  // ")"
  token= tRef.getOrReadToken(idlRd);
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ')')
  { throw new TxtReadException(token.getFilePos(),
    "\")\" of union declaration expected");
  }
  // "{"
  token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '{')
  { throw new TxtReadException(token.getFilePos(),
    "\"{\" of union declaration expected");
  }
  // <switch_body> "}"
  // <switch_body> ::= <case>+
  ArrayList eltLblValsArr= new ArrayList();
  ArrayList eltCaseArr= new ArrayList();
  token= idlRd.readToken();
  do
  { // <case> ::= <case_label>+ <element_spec> ";"
   boolean isDefaultBranch= false;
   IdlConstValue lblVals[]= null;
   ArrayList lblValsArr= new ArrayList();
   for(; ; )
   { // <case_label> ::= "case" <const_exp> ":" | "default" ":"
    keyword= IdlSpecification.readKeyword(token, true);
    if(keyword == IdlSpecification.CASE)
    { // "case" <const_exp>
     IdlConstValue iConstValue= origSwitchType.readIdlConstValue(
      this, tRef, idlRd);
     token= tRef.getOrReadToken(idlRd);
     lblValsArr.add(iConstValue);
  // ??? doppelte Werte??? eigen Hashtable???
    }else if(keyword == IdlSpecification.DEFAULT)
    { // "default"
     if(isDefaultBranch || eltDefault != null)
     { throw new TxtReadException(token.getFilePos(),
       "Multiple default labels in union `"
       + getUnEscName() + "'");
     }
     token= idlRd.readToken();
     isDefaultBranch= true;
    }else
    { break;
    }
    // ":"
    if(!(token instanceof TxtTokSepChar) ||
     ((TxtTokSepChar)token).getChar() != ':')
    { throw new TxtReadException(token.getFilePos(),
      "\":\" of union label declaration expected");
    }
    token= idlRd.readToken();
   }
   if(isDefaultBranch)
   {
   }else if(lblValsArr.size() > 0)
   { lblVals= new IdlConstValue[lblValsArr.size()];
    lblValsArr.copyInto(lblVals);
    // ??? doppelte Werte in allen lblVals[]???
   }else
   { throw new TxtReadException(token.getFilePos(),
     "\"case\" or \"default\" of union declaration expected");
   }
   // <element_spec> ";" ::=
   // <type_spec>
   tRef.ungetToken(token);
   IdlType iType= IdlBaseType.readTypeSpec(this, tRef, idlRd);
   if(!iType.isCompleteType())
    throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(),
     iType);
   // if(iType.isAnonymousType()) later in IdlDeclarator.readIdlDcl()
   // <declarator> ::=
   //     <identifier> [ "[" <positive_int_const> "]" + ]
   IdlDeclarator iElement= IdlDeclarator.readIdlDcl(iType,
    this, tRef, idlRd);
   // Add element case
   if(isDefaultBranch)
   { eltDefault= iElement;
   }else
   { eltLblValsArr.add(lblVals);
    eltCaseArr.add(iElement);
   }
   // ";"
   token= tRef.getOrReadToken(idlRd);
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ';')
   { throw new TxtReadException(token.getFilePos(),
     "\";\" of union case declaration expected");
   }
   // "}"
   token= idlRd.readToken();
  }while(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '}');
  eltLblVals= new IdlConstValue[eltLblValsArr.size()][];
  eltLblValsArr.copyInto(eltLblVals);
  eltCase= new IdlDeclarator[eltCaseArr.size()];
  eltCaseArr.copyInto(eltCase);
  bDefined= true;
  // Leaving the scope of this union
  //
  IdlSpecification iSpecification= getIdlSpecification();
  iSpecification.setPragmaScope(getSurScope());
  // Register the union as a new IDL definition
  //
  iSpecification.registerIdlDef(this);
  return idlRd.readToken();
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
   if(eltDefault != null && eltDefault.getIdlType().isLocalType())
    bLocal= true;
   else
   { for(int i= 0; i < eltCase.length; i++)
    { if(eltCase[i].getIdlType().isLocalType())
     { bLocal= true;
      break;
     }
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
 /** VB words
	 */
 private final static String DISCRIMINATOR= "Discriminator";
 private final static String UNIONDISCR= "unionDiscr";
 private final static String UNIONVALUE= "unionValue";
 private final static String IS= "is_";
 private final static String GET= "get_";
 private final static String SET= "set_";
 private final static String SP= " ";
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
  out.writeLine(VbWriter.SET + SP + vbVariable + " = "
   + VbWriter.NEW + SP + getVbName(true));
  out.writeLine(VbWriter.CALL + SP + vbVariable + "."
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
  out.writeLine(VbWriter.CALL + SP + vbVariable + "."
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
  out.writeAssign(vbVariable, /*isVbSet*/true);
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
  out.writeAssign(vbVariable, /*isVbSet*/true);
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
    System.out.println("D Union " + getScopedName()
     + " is included only");
   return;
  }
  // Only forward declaration?
  if(!bDefined)
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Union " + getScopedName()
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
   System.out.println("I Writing union " + getScopedName()
    + " to `" + sVbModName + ".bas'");
  VbModWriter vbM= new VbModWriter(vbPath, sVbModName,
   getFilePos().getFileName());
  // First of all write constants, later it is not allowed.
  // Write TypeId constant declaration
  vbM.writeLine();
  this.writeConstScopeTypeId(vbM, IdlSpecification.UNION,
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
  vbM.writeAssign("oOrb", /*isVbSet*/true);
  vbM.writeLine(IDL2VB.getVbOrbDLL() + ".defaultOrb()");
  vbM.writeLine("'Get previously created recursive or concrete TypeCode");
  vbM.writeDimLine("oRecTC", VbWriter.CORBTYPECODE);
  vbM.writeAssign("oRecTC", /*isVbSet*/true);
  vbM.writeLine("oOrb.getRecursiveTC(" + VbWriter.TYPEID
   + ", 16) 'mCB.tk_union");
  vbM.writeIf("oRecTC" + VbWriter.SP + VbWriter.IS
   + VbWriter.SP + VbWriter.NOTHING);
  vbM.writeThen();
  vbM.writeLine("'Create place holder for TypeCode to avoid endless recursion");
  vbM.writeAssign("oRecTC", /*isVbSet*/true);
  vbM.writeLine("oOrb.createRecursiveTc(" + VbWriter.TYPEID + ")");
  vbM.writeOnErrorLine("ErrRollback");
  vbM.writeDimLine("oDiscTC", VbWriter.CORBTYPECODE);
  iSwitchTypeDef.writeVbAssignTypeCode(vbM, "oDiscTC");
  vbM.writeLine("'Describe union members");
  vbM.writeDimLine("oMemSeq", "cCBUnionMemberSeq");
  vbM.writeAssign("oMemSeq", /*isVbSet*/true);
  vbM.writeLine(VbWriter.NEW + VbWriter.SP + "cCBUnionMemberSeq");
  vbM.writeAssign("oMemSeq.Length", /*isVbSet*/false);
  int iLen= 0;
  for(int ec= 0; ec < eltCase.length; ec++)
  { // eltCase[ec], eltLblVals[ec]
   iLen += eltLblVals[ec].length;
  }
  if(eltDefault != null)
  { iLen++;
  }
  vbM.writeLine(Integer.toString(iLen));
  vbM.writeDimLine(VbWriter.OANY, VbWriter.CORBANY);
  iLen= 0;
  for(int ec= 0; ec < eltCase.length; ec++)
  { // eltCase[ec], eltLblVals[ec]
   for(int lc= 0; lc < eltLblVals[ec].length; lc++)
   { /* eltLblVals[ec][lc] */
    String sItem= "oMemSeq.Item(" + Integer.toString(iLen) + ")";
    vbM.writeAssign(sItem, /*isVbSet*/true);
    vbM.writeLine(VbWriter.NEW + VbWriter.SP + "cCBUnionMember");
    vbM.writeAssign(VbWriter.OANY, /*isVbSet*/true);
    vbM.writeLine(VbWriter.NEW + VbWriter.SP + VbWriter.CORBANY);
    iSwitchTypeDef.writeVbIntoAny(vbM,
     eltLblVals[ec][lc].getVbValueStr());
    vbM.writeAssign(sItem + ".label", /*isVbSet*/true);
    vbM.writeLine(VbWriter.OANY);
    vbM.writeAssign(sItem + ".name", /*isVbSet*/false);
    vbM.writeLine("\"" + eltCase[ec].getUnEscName() + "\"");
    eltCase[ec].getIdlType().writeVbAssignTypeCode(vbM, sItem + ".p_type");
    iLen++;
   }
  }
  if(eltDefault != null)
  { String sItem= "oMemSeq.Item(" + Integer.toString(iLen) + ")";
   vbM.writeAssign(sItem, /*isVbSet*/true);
   vbM.writeLine(VbWriter.NEW + VbWriter.SP + "cCBUnionMember");
   vbM.writeAssign(VbWriter.OANY, /*isVbSet*/true);
   vbM.writeLine(VbWriter.NEW + VbWriter.SP + VbWriter.CORBANY);
   // Default value???
   iSwitchTypeDef.writeVbIntoAny(vbM, Integer.toString(-1));//???
   vbM.writeAssign(sItem + ".label", /*isVbSet*/true);
   vbM.writeLine(VbWriter.OANY);
   vbM.writeAssign(sItem + ".name", /*isVbSet*/false);
   vbM.writeLine("\"" + eltDefault.getUnEscName() + "\"");
   eltDefault.getIdlType().writeVbAssignTypeCode(vbM, sItem + ".p_type");
   iLen++;
  }
  vbM.writeLine("'Overwrite place holder");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP
   + "oRecTC.setRecTc2UnionTc(\"" + getUnEscName()
   + "\", oDiscTC, oMemSeq)");
  vbM.writeEndIf();
  vbM.writeAssign(VbWriter.TYPECODE, /*isVbSet*/true);
  vbM.writeLine("oRecTC");
  vbM.writeLine(VbWriter.EXIT + VbWriter.SP + VbWriter.FUNCTION);
  vbM.writeLabelLine("ErrRollback");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oRecTC.destroy");
  vbM.writeLabelLine(VbWriter.ERRHANDLER);
  vbM.writeErrReraiseLine(VbWriter.TYPECODE);
  vbM.writeFuncTail(null, null);
  vbM.close();
 }
 /**
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbCls(String vbPath) throws java.io.IOException
 {
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing union " + getScopedName() + " to `"
    + getVbName(true) + ".cls'");
  VbClsWriter vbC= new VbClsWriter(vbPath, getVbName(true),
   getFilePos().getFileName());
  // Write TypeId constant declaration
  vbC.writeLine();
  this.writeConstScopeTypeId(vbC, IdlSpecification.UNION,
   /* isPublic */ false);
  vbC.writeLine();
  vbC.writeVarLine(VbWriter.PRIVATE, UNIONDISCR,
   iSwitchTypeDef.getVbName(true));
  vbC.writeVarLine(VbWriter.PRIVATE, UNIONVALUE, VbWriter.VARIANT);
  vbC.writeLine();
  // Get Discriminator
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PUBLIC, VbWriter.GET, DISCRIMINATOR);
  vbC.writePropertyBody(iSwitchTypeDef.getVbName(true), true, false);
  vbC.writeAssign(DISCRIMINATOR, /*isVbSet*/false);
  vbC.writeLine(UNIONDISCR);
  vbC.writePropertyTail(null, null);
  // Set Discriminator
  vbC.writeLine();
  vbC.writePropertyHead(VbWriter.PUBLIC, VbWriter.LET, DISCRIMINATOR);
  vbC.write(UNIONDISCR + "New");
  vbC.writePropertyBody(iSwitchTypeDef.getVbName(true), false, false);
  vbC.writeAssign(UNIONDISCR, /*isVbSet*/false);
  vbC.writeLine(UNIONDISCR + "New");
  vbC.writePropertyTail(null, null);
  // Helpers to access the value (eltCase, eltDefault)
  // Because elements are identifiers in the same scope
  // they are unique.
  for(int ec= 0; ec < eltCase.length; ec++)
   this.writeVbAccessor(vbC, eltCase[ec], eltLblVals[ec]);
  if(eltDefault != null)
   this.writeVbAccessor(vbC, eltDefault, null);
  // Helper, initByRead()
  vbC.writeInitByReadHead(VbWriter.PUBLIC, false);
  iSwitchTypeDef.writeVbRead(vbC, UNIONDISCR);
  vbC.writeLine(VbWriter.SELECT + SP + VbWriter.CASE + SP + UNIONDISCR);
  for(int ec= 0; ec < eltCase.length; ec++)
  { // eltCase[ec], eltLblVals[ec]
   for(int lc= 0; lc < eltLblVals[ec].length; lc++)
   { if(lc == 0)
     vbC.write(VbWriter.CASE + SP);
    else
     vbC.write("," + SP);
    eltLblVals[ec][lc].writeVbValue(vbC);
   }
   vbC.writeLine();
   this.writeVbEltRead(vbC, eltCase[ec]);
  }
  // No exception if Case Else branch and eltDefault == null
  if(eltDefault != null)
  { vbC.writeLine(VbWriter.CASE + SP + VbWriter.ELSE);
   this.writeVbEltRead(vbC, eltDefault);
  }
  vbC.writeLine(VbWriter.END + SP + VbWriter.SELECT);
  vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".read", null);
  // Helper, writeMe()
  vbC.writeWriteMeHead(VbWriter.PUBLIC);
  iSwitchTypeDef.writeVbWrite(vbC, UNIONDISCR);
  vbC.writeLine(VbWriter.SELECT + SP + VbWriter.CASE + SP + UNIONDISCR);
  for(int ec= 0; ec < eltCase.length; ec++)
  { // eltCase[ec], eltLblVals[ec]
   for(int lc= 0; lc < eltLblVals[ec].length; lc++)
   { if(lc == 0)
     vbC.write(VbWriter.CASE + SP);
    else
     vbC.write("," + SP);
    eltLblVals[ec][lc].writeVbValue(vbC);
   }
   vbC.writeLine();
   this.writeVbEltWrite(vbC, eltCase[ec]);
  }
  // No exception if Case Else branch and eltDefault == null
  if(eltDefault != null)
  { vbC.writeLine(VbWriter.CASE + SP + VbWriter.ELSE);
   this.writeVbEltWrite(vbC, eltDefault);
  }
  vbC.writeLine(VbWriter.END + SP + VbWriter.SELECT);
  vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".write", null);
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INITBYANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  //iSwitchTypeDef.writeVbFromAny(vbC, UNIONDISCR);
  vbC.writeLine("'???");
  // ???
  vbC.writeSubTail(VbWriter.INITBYANY);
  vbC.writeLine();
  vbC.writeLine("'Helper");
  vbC.writeSubHead(VbWriter.PUBLIC, VbWriter.INSERTINTOANY);
  vbC.writeSubArg(VbWriter.BYVAL, VbWriter.OANY, VbWriter.CORBANY, null);
  vbC.writeSubBody(true);
  //iSwitchTypeDef.writeVbIntoAny(vbC, UNIONDISCR);
  vbC.writeLine("'???");
  // ???
  vbC.writeSubTail(VbWriter.INSERTINTOANY);
  vbC.close();
 }
 /**
	 *  @param	vbC			VbClsWriter
	 *  @param	elt			IdlDeclarator
	 *  @param	lblVals		IdlConstValue[] or null
	 *
	 *	@exception	IOException	
	 */
 private void writeVbAccessor(VbClsWriter vbC, IdlDeclarator elt,
  IdlConstValue lblVals[]) throws java.io.IOException
 {
  String vbFuncName;
  // CORBA arrays are mapped to seperate VB classes
  // Declarations of variables should written like IdlDeclarator
  IdlType idlType= elt.idlType;
  String valName= elt.getUnEscName();
  if(VbWriter.hashResWord(valName) != null)
   valName= "v_" + valName;
  // is_...
  vbFuncName= IS + elt.getUnEscName();
  vbC.writeLine();
  vbC.write(VbWriter.PUBLIC + SP + VbWriter.FUNCTION + SP);
  vbC.write(vbFuncName + "(");
  vbC.writeLine(")" + SP + VbWriter.AS + SP + VbWriter.BOOLEAN);
  vbC.indent(true);
  vbC.writeAssign(vbFuncName, /*isVbSet*/false);
  if(lblVals == null) // default branch?
  { int lc= 0;
   for(int ec= 0; ec < eltLblVals.length; ec++)
   { for(int i= 0; i < eltLblVals[ec].length; i++)
    { if(lc > 0)
      vbC.write(SP + VbWriter.AND + SP);
     lc++;
     if(origSwitchType instanceof IdlBoolean)
     { if(eltLblVals[ec][i].getBoolean().booleanValue())
       vbC.write(VbWriter.NOT + SP);
      vbC.write(UNIONDISCR);
     }else
     { vbC.write("(" + UNIONDISCR + " <> ");
      eltLblVals[ec][i].writeVbValue(vbC);
      vbC.write(")");
     }
    }
   }
   if(lc == 0)
   { vbC.write(VbWriter.TRUE);
   }
  }else
  { for(int i= 0; i < lblVals.length; i++)
   { if(i > 0)
     vbC.write(SP + VbWriter.OR + SP);
    if(origSwitchType instanceof IdlBoolean)
    { if(!lblVals[i].getBoolean().booleanValue())
      vbC.write(VbWriter.NOT + SP);
     vbC.write(UNIONDISCR);
    }else
    { vbC.write("(" + UNIONDISCR + " = ");
     lblVals[i].writeVbValue(vbC);
     vbC.write(")");
    }
   }
  }
  vbC.writeLine();
  vbC.indent(false);
  vbC.writeLine(VbWriter.END + SP + VbWriter.FUNCTION);
  // get_...
  vbFuncName= GET + elt.getUnEscName();
  vbC.writeLine();
  vbC.write(VbWriter.PUBLIC + SP + VbWriter.FUNCTION + SP);
  vbC.write(vbFuncName + "(");
  vbC.writeLine(")" + SP + VbWriter.AS + SP + idlType.getVbName(true));
  vbC.indent(true);
  vbC.writeAssign(vbFuncName, idlType.isVbSet());
  vbC.writeLine(UNIONVALUE);
  vbC.indent(false);
  vbC.writeLine(VbWriter.END + SP + VbWriter.FUNCTION);
  //vbC.writeExitEnd(...);
  // set_...
  vbFuncName= SET + elt.getUnEscName();
  vbC.writeLine();
  vbC.write(VbWriter.PUBLIC + SP + VbWriter.SUB + SP);
  vbC.write(vbFuncName + "(");
  vbC.write(VbWriter.BYVAL + SP + valName + SP + VbWriter.AS + SP
   + idlType.getVbName(true));
  vbC.writeLine(")");
  vbC.indent(true);
  vbC.write(UNIONDISCR + SP + "=" + SP);
  if(lblVals == null) // default branch?
  { if(origSwitchType instanceof IdlBoolean)
   { if(eltLblVals.length == 0 ||
     eltLblVals[0][0].getBoolean().booleanValue())
     vbC.write(VbWriter.FALSE);
    else
     vbC.write(VbWriter.TRUE);
   }else
   { vbC.write("-1"); // ??? if not equal a not default label value
        // maximal Wert des jeweiligen Typs verwenden???
// Beim Einlesen der Union bereits einen Default-Wert festlegen???
   }
  }else
  { lblVals[0].writeVbValue(vbC);
  }
  vbC.writeLine();
  vbC.writeAssign(UNIONVALUE, idlType.isVbSet()); vbC.writeLine(valName);
  vbC.indent(false);
  vbC.writeLine(VbWriter.END + SP + VbWriter.SUB);
  //vbC.writeExitEnd(...);
 }
 /**
	 *  @param	vbC			VbClsWriter
	 *  @param	elt			IdlDeclarator
	 *
	 *	@exception	IOException	
	 */
 private void writeVbEltRead(VbClsWriter vbC, IdlDeclarator elt)
  throws java.io.IOException
 {
  // Is coded like writeVbPrivateVarDecl() of IdlDeclarator
  // CORBA arrays are mapped to seperate VB classes
  vbC.indent(true);
  IdlType iType= elt.getIdlType();
  if(iType.isVbSet())
  { String vbName= "v_" + elt.getUnEscName();
   vbC.writeDimLine(vbName, iType.getVbName(true));
   iType.writeVbRead(vbC, vbName);
   vbC.writeAssign(UNIONVALUE, /*isVbSet*/true); vbC.writeLine(vbName);
  }else
  { iType.writeVbRead(vbC, UNIONVALUE);
  }
  vbC.indent(false);
 }
 /**
	 *  @param	vbC			VbClsWriter
	 *  @param	elt			IdlDeclarator
	 *
	 *	@exception	IOException	
	 */
 private void writeVbEltWrite(VbClsWriter vbC, IdlDeclarator elt)
  throws java.io.IOException
 {
  // writeString(unionValue) do not work because of ByRef problems
  // in VB 5. So we use:
  //   Dim vbName As iType
  //   Let/Set vbName = unionValue
  //   writeType(vbName)
  vbC.indent(true);
  IdlType iType= elt.getIdlType();
  String vbName= "v_" + elt.getUnEscName();
  // Is coded like writeVbPrivateVarDecl() of IdlDeclarator
  // CORBA arrays are mapped to seperate VB classes
  vbC.writeDimLine(vbName, iType.getVbName(true));
  vbC.writeAssign(vbName, iType.isVbSet());
  vbC.writeLine(UNIONVALUE);
  iType.writeVbWrite(vbC, vbName);
  vbC.indent(false);
 }
}
