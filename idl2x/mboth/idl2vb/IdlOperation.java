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
import java.io.IOException;
import mboth.util.*;
/** Operation declarations in OMG IDL are similar to C function declarations.
 *
 *  @author  Martin Both
 */
public class IdlOperation extends IdlScope implements IdlDefinition
{
 /**
	 */
 public final static String SP= " ";
 /** <op_attribute>
	 */
 private boolean oneway;
 /** <op_type_spec>
	 */
 private IdlType idlType;
 /** <parameter_dcls> or null
	 */
 protected IdlOpParameter idlOpParameters;
 /** Raises expressions or null
	 */
 protected IdlException rExceptions[];
 /** Context expressions
	 */
 //private IdlException idlExceptions;
 /** Read operation head
	 *
	 *  @param	idlScope	
	 *  @param	oneway	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		return op/null/exception or op/exception
	 *	@return				iOperation (not null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlOperation readIdlHead(IdlScope idlScope, boolean oneway,
  TxtTokenRef tRef, TxtTokenReader idlRd, boolean test)
  throws TxtReadException
 {
  // <op_type_spec>
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readOpType(idlScope, tRef, idlRd, test);
  if(iType == null) // test is true
   return null;
  if(!iType.isCompleteType())
   throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(), iType);
  if(iType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iType);
  // <identifier>
  token= tRef.getOrReadToken(idlRd);
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(idlScope, token);
  IdlOperation iOperation= new IdlOperation(iWord, oneway, iType);
  idlScope.putIdentifier(iOperation, true);
  return iOperation;
 }
 /** 
	 *  @param	identifier		Identifier
	 *  @param	oneway	
	 *  @param	idlType	
	 */
 public IdlOperation(IdlIdentifier identifier, boolean oneway,
  IdlType idlType)
 {
  super(identifier);
  this.oneway= oneway;
  this.idlType= idlType;
 }
 /** Read operation
	 *
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public void readIdl(TxtTokenRef tRef, TxtTokenReader idlRd)
  throws TxtReadException
 {
  // <parameter_dcls> [<raises_expr>] [<context_expr>]
  //
  TxtToken token= tRef.getOrReadToken(idlRd);
  // "(" <param_dcl> { "," <param_dcl> }* ")" | "(" ")"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '(')
  { throw new TxtReadException(token.getFilePos(),
    "\"(\" of parameter declaration expected");
  }
  token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ')')
  { for(; ; )
   { IdlOpParameter iOpParameter= IdlOpParameter.readIdlHead(
     this, token, idlRd);
    addIdlOpParameter(iOpParameter);
    //iOpParameter.readIdl(idlRd);
    token= idlRd.readToken();
    if(!(token instanceof TxtTokSepChar)
     || ((TxtTokSepChar)token).getChar() != ',')
     break;
    token= idlRd.readToken();
   }
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ')')
   { throw new TxtReadException(token.getFilePos(),
     "\",\" or \")\" of parameter declaration expected");
   }
  }
  // [<raises_expr>]
  //
  token= idlRd.readToken();
  String keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.RAISES)
  { token= idlRd.readToken();
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != '(')
   { throw new TxtReadException(token.getFilePos(),
     "\"(\" of raises expression expected");
   }
   do
   { token= tRef.getOrReadToken(idlRd);
    tRef.ungetToken(token);
    // (12) <scoped_name>
    IdlIdentifier identifier;
    identifier= getSurScope().readScopedName(tRef, idlRd, false, false);
    if(!(identifier instanceof IdlException))
    { TxtReadException ex= new TxtReadException(token.getFilePos(),
      "Scoped name of an IDL exception expected");
     ex.setNextException(new TxtReadException(identifier.getFilePos(),
      "Position where the last identifier "
      + "of the given scoped name is defined"));
     throw ex;
    }
    addRaiseException((IdlException)identifier);
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ')')
   { throw new TxtReadException(token.getFilePos(),
     "\",\" or \")\" of raises expression expected");
   }
   token= idlRd.readToken();
  }
  // [<context_expr>]
  //
  keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.CONTEXT)
  { token= idlRd.readToken();
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != '(')
   { throw new TxtReadException(token.getFilePos(),
     "\"(\" of context expression expected");
   }
   do
   { token= idlRd.readToken();
    // <string_literal>
    if(!(token instanceof TxtTokString))
    { throw new TxtReadException(token.getFilePos(),
      "String literal of context expression expected");
    }
    /*??? one or more context string literals "ghg" "hjh", ""
				*/
    String oneContextStr= IdlString.readStringValue(
     (TxtTokString)token, tRef, idlRd);
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ')')
   { throw new TxtReadException(token.getFilePos(),
     "\",\" or \")\" of context expression expected");
   }
  }else
  { tRef.ungetToken(token);
  }
  // Register the operation as a new IDL definition
  //
  getIdlSpecification().registerIdlDef(this);
 }
 /** Add operation parameter
	 *
	 *  @param	iOpParameter
	 */
 protected void addIdlOpParameter(IdlOpParameter iOpParameter)
 {
  if(idlOpParameters == null)
  { idlOpParameters= iOpParameter;
  }else
  { idlOpParameters.addNext(iOpParameter);
  }
 }
 /** Add raises expression
	 *
	 *  @param	iException
	 */
 protected void addRaiseException(IdlException iException)
 {
  if(rExceptions == null)
  { rExceptions= new IdlException[1];
   rExceptions[0]= iException;
  }else
  { IdlException oldExs[]= rExceptions;
   rExceptions= new IdlException[oldExs.length + 1];
   System.arraycopy(oldExs, 0, rExceptions, 0, oldExs.length);
   rExceptions[oldExs.length]= iException;
  }
 }
 /** Get raises expressions
	 *
	 *  @return		rExceptions or null if no raises expression
	 */
 public IdlException[] getIdlExceptions()
 {
  return rExceptions;
 }
 /** Add context expression
	 *
	 *  @param	iOpContext
	 */
//???
//	private void addIdlOpContext(IdlOpContext iOpContext)
//	{
//		if(idlOpContexts == null)
//		{	idlOpContexts= iOpContext;
//		}else
//		{	idlOpContexts.addNext(iOpContext);
//		}
//	}
 /** (IdlDefinition)
	 *  Get an IdlName to identify the operation definition
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return getScopedName().toString();
 }
 /** Remove all Underscores in the name of operations and
	 *  attributes and set the next character to uppercase.
	 */
 public static String convertOpOrAttr2VB(String idlName)
 { StringBuffer sBuf= new StringBuffer(idlName.length());
  boolean ufound= false;
  for(int i= 0; i < idlName.length(); i++)
  { if(idlName.charAt(i) == '_')
   { ufound= true;
   }else if(!ufound)
   { sBuf.append(idlName.charAt(i));
   }else
   { sBuf.append(Character.toUpperCase(idlName.charAt(i)));
    ufound= false;
   }
  }
  return sBuf.toString();
 }
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
 { if(sVbName == null)
  { // Append names until the highest scope (next IdlContainer) is reached
   StringBuffer sBuf= new StringBuffer(getUnEscName());
   for(IdlScope iScope= getSurScope(); iScope != null;
    iScope= iScope.getSurScope())
   { if(iScope instanceof IdlContainer)
     break;
    sBuf.insert(0, iScope.getUnEscName());
   }
   sVbName= convertOpOrAttr2VB(sBuf.toString());
   if(VbWriter.hashResWord(sVbName) != null)
   { boolean isSub= (idlType instanceof IdlVoid);
    sVbName= sVbName + (isSub? "Sub": "Func");
   }
  }
  if(bPrefix)
  { // Method checkMappingOfIdlDefs() needs a unique name
   for(IdlScope iScope= getSurScope(); iScope != null;
    iScope= iScope.getSurScope())
   { if(iScope instanceof IdlContainer)
     return ((IdlContainer)iScope).getVbModName() + "."
      + sVbName;
   }
  }
  return sVbName;
 }
 /** VB sub or function
	 *  @return				vbSubFuncName= opName[Sub|Func]
	 */
 public String getVbSubFuncName()
 {
  return getVbName(false);
 }
 /**
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeVbSubOrFunc(VbClsWriter out) throws java.io.IOException
 {
  boolean isSub= (idlType instanceof IdlVoid);
  String opName= getUnEscName();
  String vbSFName= getVbSubFuncName();
  out.writeLine();
  out.write("'");
  if(oneway)
   out.write(IdlSpecification.ONEWAY+ " ");
  out.write(opName + "()");
  if(getIdlExceptions() != null)
  { out.write(VbWriter.SP + IdlSpecification.RAISES + "(");
   for(int i= 0; i < getIdlExceptions().length; i++)
   { if(i > 0)
     out.write(", ");
    out.write(getIdlExceptions()[i].getVbName(true));
   }
   out.write(")");
  }
  out.writeLine();
  boolean hasResults;
  if(isSub)
  { hasResults= false;
   out.writeSubHead(VbWriter.PUBLIC, vbSFName);
  }else
  { hasResults= true;
   out.writeFuncHead(VbWriter.PUBLIC, vbSFName);
  }
  //if(getIdlExceptions() != null)
  //	out.writeArg(VbWriter.BYREF, VbWriter.OEX, VbWriter.CORBEXCEPTION,
  //		null);
  boolean hasInArgs= false;
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { opPar.writeVbArg(out);
   if(opPar.isOut())
    hasResults= true;
   if(opPar.isIn())
    hasInArgs= true;
  }
  if(isSub)
   out.writeSubBody(true);
  else
   out.writeFuncBody(idlType.getVbName(true), true);
  out.writeVbRequest(opName, hasInArgs, oneway);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { // Write all in and inout parameters
   if(opPar.isIn())
   { IdlType iType= opPar.getIdlType();
    iType.writeVbWrite(out, opPar.getVbArgName());
   }
  }
  out.writeVbInvokeReq(hasResults, getIdlExceptions());
  if(!isSub)
  { idlType.writeVbRead(out, vbSFName);
  }
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { // Read all out and inout parameters
   if(opPar.isOut())
   { IdlType iType= opPar.getIdlType();
    iType.writeVbRead(out, opPar.getVbArgName());
   }
  }
  if(getIdlExceptions() != null)
   out.writeEndIf(); // End of reading exceptions by InvokeReq()
  if(isSub)
   out.writeSubTail(vbSFName);
  else
   out.writeFuncTail(vbSFName, idlType.isVbSet()? vbSFName: null);
 }
 /** Write additional server skeleton caller
	 *  @param	out		
	 *  @param	vbScopeNo	"a1_", "a2_", ...
	 *
	 *	@exception	IOException	
	 */
 public void writeVbImplExec(VbClsWriter out, int vbScopeNo)
  throws IOException
 {
  boolean isSub= (idlType instanceof IdlVoid);
  String opName= getUnEscName();
  String vbSFName= getVbSubFuncName();
  String vbScopePrefix= "a" + vbScopeNo + "_";
  //String vbExDimName= (getIdlExceptions() == null)? null: VbWriter.OEX;
  out.writeLine(VbWriter.CASE + " \"" + opName + "\"");
  out.indent(true);
  // DIM a1_...
  // oIn.read...()
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { String vbArgDimName= vbScopePrefix + opPar.getUnEscName();
   IdlType iType= opPar.getIdlType();
   out.writeDimLine(vbArgDimName, iType.getVbName(true));
   if(opPar.isIn())
   { iType.writeVbRead(out, vbArgDimName);
   }
  }
  //if(vbExDimName != null)
  //	out.writeDimLine(vbExDimName, VbWriter.CORBEXCEPTION);
  if(isSub)
  { out.write(VbWriter.CALL + VbWriter.SP);
  }else
  { out.writeDimLine(vbSFName, idlType.getVbName(true));
   if(idlType.isVbSet())
    out.write(VbWriter.SET + VbWriter.SP);
   out.write(vbSFName + " = ");
  }
  out.write("oImpl_" + "." + vbSFName);
  if(!isSub || idlOpParameters != null /*|| vbExDimName != null*/)
   out.write("(");
  //if(vbExDimName != null)
  //	out.writeArg(null, vbExDimName, null, null);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { String vbArgDimName= vbScopePrefix + opPar.getUnEscName();
   out.writeArg(null, vbArgDimName, null, null);
  }
  if(!isSub || idlOpParameters != null /*|| vbExDimName != null*/)
   out.write(")");
  out.writeLine();
  // Exception handling
  //if(getIdlExceptions() == null)
  //	//out.writeIfExGoTo();
  //}else
  //{	out.writeIf(VbWriter.NOT + VbWriter.SP + vbExDimName + VbWriter.SP
  //		+ VbWriter.IS + VbWriter.SP + VbWriter.NOTHING);
  //	out.writeLine(VbWriter.SP + VbWriter.THEN + VbWriter.SP
  //		+ VbWriter.GOTO + VbWriter.SP + VbWriter.USEREXWRITER);
  //}
  if(!isSub)
   idlType.writeVbWrite(out, vbSFName);
  // oOut.write...()
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { if(opPar.isOut())
   { String vbArgDimName= vbScopePrefix + opPar.getUnEscName();
    IdlType iType= opPar.getIdlType();
    iType.writeVbWrite(out, vbArgDimName);
   }
  }
  out.indent(false);
 }
 /** Write additional server delegate function
	 *  @param	vbInterface		Interface name or null if public
	 *							vbInterface == null is nonsense here???
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeVbImplTie(String vbInterface, VbClsWriter out)
  throws IOException
 {
  boolean isSub= (idlType instanceof IdlVoid);
  String opName= getUnEscName();
  String vbSFName= getVbSubFuncName();
  out.writeLine();
  out.write("'");
  if(oneway)
   out.write(IdlSpecification.ONEWAY+ " ");
  out.write(opName + "()");
  if(getIdlExceptions() != null)
  { out.write(VbWriter.SP + IdlSpecification.RAISES + "(");
   for(int i= 0; i < getIdlExceptions().length; i++)
   { if(i > 0)
     out.write(", ");
    out.write(getIdlExceptions()[i].getVbName(true));
   }
   out.write(")");
  }
  out.writeLine();
  if(vbInterface == null)
  { if(isSub)
   { out.writeSubHead(VbWriter.PUBLIC, vbSFName);
   }else
   { out.writeFuncHead(VbWriter.PUBLIC, vbSFName);
   }
  }else
  { if(isSub)
   { out.writeSubHead(VbWriter.PRIVATE,
     vbInterface + "_" + vbSFName);
   }else
   { out.writeFuncHead(VbWriter.PRIVATE,
     vbInterface + "_" + vbSFName);
   }
  }
  //if(getIdlExceptions() != null)
  //	out.writeArg(VbWriter.BYREF, VbWriter.OEX, VbWriter.CORBEXCEPTION,
  //		null);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { opPar.writeVbArg(out);
  }
  if(isSub)
   out.writeSubBody(true);
  else
   out.writeFuncBody(idlType.getVbName(true), true);
  out.writeLine("'Please write your own code here if using as servant example");
  out.writeIf("oDelegate Is Nothing"); out.writeThen();
  out.writeLine(VbWriter.CALL + VbWriter.SP + IDL2VB.getVbOrbDLL()
   + ".raiseNOIMPLEMENT(0, " + IDL2VB.getVbOrbDLL() + ".CompletedNO)");
  out.writeElse();
  if(isSub)
  { out.write(VbWriter.CALL);
  }else
  { if(idlType.isVbSet())
   { out.write(VbWriter.SET); out.write(VbWriter.SP);
   }
   if(vbInterface == null)
    out.write(vbSFName + " =");
   else
    out.write(vbInterface + "_" + vbSFName + " =");
  }
  out.write(VbWriter.SP + "oDelegate." + vbSFName);
  if(!isSub /*|| getIdlExceptions() != null*/ || idlOpParameters != null)
   out.write("(");
  //if(getIdlExceptions() != null)
  //	out.writeArg(null, VbWriter.OEX, null, null);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { out.writeArg(null, opPar.getVbArgName(), null, null);
  }
  if(!isSub /*|| getIdlExceptions() != null*/ || idlOpParameters != null)
   out.write(")");
  out.writeLine();
  out.writeEndIf();
  if(isSub)
   out.writeSubTail(vbSFName);
  else
  { if(vbInterface == null)
   { out.writeFuncTail(vbSFName,
     idlType.isVbSet()? vbSFName: null);
   }else
   { out.writeFuncTail(vbSFName,
     idlType.isVbSet()? vbInterface + "_" + vbSFName: null);
   }
  }
 }
 /** Write additional implement examples or write valuetype example
	 *  @param	vbInterface		Interface name or null if public
	 *							vbInterface != null is nonsense here???
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeVbImplExample(String vbInterface, VbClsWriter out)
  throws IOException
 {
  boolean isSub= (idlType instanceof IdlVoid);
  String opName= getUnEscName();
  String vbSFName= getVbSubFuncName();
  out.writeLine();
  out.write("'");
  if(oneway)
   out.write(IdlSpecification.ONEWAY+ " ");
  out.write(opName + "()");
  if(getIdlExceptions() != null)
  { out.write(VbWriter.SP + IdlSpecification.RAISES + "(");
   for(int i= 0; i < getIdlExceptions().length; i++)
   { if(i > 0)
     out.write(", ");
    out.write(getIdlExceptions()[i].getVbName(true));
   }
   out.write(")");
  }
  out.writeLine();
  if(vbInterface == null)
  { if(isSub)
   { out.writeSubHead(VbWriter.PUBLIC, vbSFName);
   }else
   { out.writeFuncHead(VbWriter.PUBLIC, vbSFName);
   }
  }else
  { if(isSub)
   { out.writeSubHead(VbWriter.PRIVATE,
     vbInterface + "_" + vbSFName);
   }else
   { out.writeFuncHead(VbWriter.PRIVATE,
     vbInterface + "_" + vbSFName);
   }
  }
  //if(getIdlExceptions() != null)
  //	out.writeArg(VbWriter.BYREF, VbWriter.OEX, VbWriter.CORBEXCEPTION,
  //		null);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { opPar.writeVbArg(out);
  }
  if(isSub)
   out.writeSubBody(true);
  else
   out.writeFuncBody(idlType.getVbName(true), true);
  //do something
  out.writeLine("'Please write your code here after copying this file");
  out.writeLine("'instead of throwing an exception");
  out.write("'e.g. ");
  if(isSub)
  { out.write(VbWriter.CALL);
  }else
  { if(idlType.isVbSet())
   { out.write(VbWriter.SET); out.write(VbWriter.SP);
   }
   if(vbInterface == null)
    out.write(vbSFName + " =");
   else
    out.write(vbInterface + "_" + vbSFName + " =");
  }
  out.write(VbWriter.SP + "oDelegate." + vbSFName);
  if(!isSub /*|| getIdlExceptions() != null*/ || idlOpParameters != null)
   out.write("(");
  //if(getIdlExceptions() != null)
  //	out.writeArg(null, VbWriter.OEX, null, null);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { out.writeArg(null, opPar.getVbArgName(), null, null);
  }
  if(!isSub /*|| getIdlExceptions() != null*/ || idlOpParameters != null)
   out.write(")");
  out.writeLine();
  out.writeLine(VbWriter.CALL + VbWriter.SP + IDL2VB.getVbOrbDLL()
   + ".raiseNOIMPLEMENT(0, " + IDL2VB.getVbOrbDLL() + ".CompletedNO)");
  if(isSub)
   out.writeSubTail(vbSFName);
  else
  { if(vbInterface == null)
   { out.writeFuncTail(vbSFName,
     idlType.isVbSet()? vbSFName: null);
   }else
   { out.writeFuncTail(vbSFName,
     idlType.isVbSet()? vbInterface + "_" + vbSFName: null);
   }
  }
 }
 /** Write implementation of interfaces for valuetypes
	 *  @param	vbInterface		Interface name or null if public
	 *							vbInterface == null is nonsense here???
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeVbValImpl(String vbInterface, VbClsWriter out)
  throws IOException
 {
  boolean isSub= (idlType instanceof IdlVoid);
  String opName= getUnEscName();
  String vbSFName= getVbSubFuncName();
  out.writeLine();
  out.write("'");
  if(oneway)
   out.write(IdlSpecification.ONEWAY+ " ");
  out.write(opName + "()");
  if(getIdlExceptions() != null)
  { out.write(VbWriter.SP + IdlSpecification.RAISES + "(");
   for(int i= 0; i < getIdlExceptions().length; i++)
   { if(i > 0)
     out.write(", ");
    out.write(getIdlExceptions()[i].getVbName(true));
   }
   out.write(")");
  }
  out.writeLine();
  if(vbInterface == null)
  { if(isSub)
   { out.writeSubHead(VbWriter.PUBLIC, vbSFName);
   }else
   { out.writeFuncHead(VbWriter.PUBLIC, vbSFName);
   }
  }else
  { if(isSub)
   { out.writeSubHead(VbWriter.PRIVATE,
     vbInterface + "_" + vbSFName);
   }else
   { out.writeFuncHead(VbWriter.PRIVATE,
     vbInterface + "_" + vbSFName);
   }
  }
  //if(getIdlExceptions() != null)
  //	out.writeArg(VbWriter.BYREF, VbWriter.OEX, VbWriter.CORBEXCEPTION,
  //		null);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { opPar.writeVbArg(out);
  }
  if(isSub)
   out.writeSubBody(false);
  else
   out.writeFuncBody(idlType.getVbName(true), false);
  if(isSub)
  { out.write(VbWriter.CALL);
  }else
  { if(idlType.isVbSet())
   { out.write(VbWriter.SET); out.write(VbWriter.SP);
   }
   if(vbInterface == null)
    out.write(vbSFName + " =");
   else
    out.write(vbInterface + "_" + vbSFName + " =");
  }
  out.write(VbWriter.SP + vbSFName);
  if(!isSub /*|| getIdlExceptions() != null*/ || idlOpParameters != null)
   out.write("(");
  //if(getIdlExceptions() != null)
  //	out.writeArg(null, VbWriter.OEX, null, null);
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { out.writeArg(null, opPar.getVbArgName(), null, null);
  }
  if(!isSub /*|| getIdlExceptions() != null*/ || idlOpParameters != null)
   out.write(")");
  out.writeLine();
  if(isSub)
   out.writeSubTail(null);
  else
   out.writeFuncTail(null, null);
 }
}
