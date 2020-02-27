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
/** An attribute definition is logically equivalent to declaring a pair of
 *  accessor functions; one to retrieve the value of the attribute and one
 *  to set the value of the attribute.
 *
 *  @author  Martin Both
 */
public class IdlAttribute extends IdlIdentifier implements IdlDefinition
{
 /** 
	 */
 private boolean readonly;
 /** <param_type_spec>
	 */
 private IdlType idlType;
 /** Read attribute
	 *
	 *  @param	readonly	
	 *  @param	iType		IdlType
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iAttribute (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlAttribute readIdlHead(boolean readonly, IdlType iType,
  IdlScope idlScope, TxtToken token, TxtTokenReader idlRd)
  throws TxtReadException
 {
  IdlAttribute iAttribute= new IdlAttribute(
   IdlIdentifier.readNewIdlWord(idlScope, token), readonly, iType);
  idlScope.putIdentifier(iAttribute, true);
  // Register the operation as a new IDL definition
  //
  idlScope.getIdlSpecification().registerIdlDef(iAttribute);
  return iAttribute;
 }
 /** 
	 *  @param	identifier		Identifier
	 *  @param	readonly	
	 *  @param	idlType	
	 */
 public IdlAttribute(IdlIdentifier identifier, boolean readonly,
  IdlType idlType)
 {
  super(identifier);
  this.readonly= readonly;
  this.idlType= idlType;
 }
 /** (IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return getScopedName().toString();
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
   sVbName= IdlOperation.convertOpOrAttr2VB(sBuf.toString());
   if(VbWriter.hashResWord(sVbName) != null)
   { sVbName= sVbName + "Prop";
   }
  }
  if(bPrefix)
  { // checkMappingOfIdlDefs needs a unique name
   for(IdlScope iScope= getSurScope(); iScope != null;
    iScope= iScope.getSurScope())
   { if(iScope instanceof IdlContainer)
     return ((IdlContainer)iScope).getVbModName() + "."
      + sVbName;
   }
  }
  return sVbName;
 }
 /** VB property
	 *  @return				propName= attrName[Prop]
	 */
 public String getVbPropertyName()
 {
  return getVbName(false);
 }
 /**
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeVbProps(VbClsWriter out) throws java.io.IOException
 {
  String attrName= getUnEscName();
  String vbName= getVbPropertyName();
  out.writeLine();
  out.write("'");
  if(readonly)
   out.write(IdlSpecification.READONLY+ " ");
  out.writeLine(IdlSpecification.ATTRIBUTE + " " + attrName);
  out.writeAttributeGet(attrName, vbName, idlType);
  if(readonly)
   return;
  out.writeLine();
  out.writeAttributeLetOrSet(attrName, vbName, idlType);
 }
 /** Write additional server skeleton caller
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeVbImplExec(VbClsWriter out) throws IOException
 {
  String attrName= getUnEscName();
  String vbName= getVbPropertyName();
  out.writeLine(VbWriter.CASE + " \"_get_" + attrName + "\"");
  out.indent(true);
  out.writeDimLine(vbName, idlType.getVbName(true));
  if(idlType.isVbSet())
   out.write(VbWriter.SET + VbWriter.SP);
  out.writeLine(vbName + " = " + "oImpl_" + "." + vbName + "()");
  idlType.writeVbWrite(out, vbName);
  out.indent(false);
  if(readonly)
   return;
  out.writeLine(VbWriter.CASE + " \"_set_" + attrName + "\"");
  out.indent(true);
  out.writeDimLine(attrName + "New", idlType.getVbName(true));
  idlType.writeVbRead(out, attrName + "New");
  if(idlType.isVbSet())
   out.write(VbWriter.SET + VbWriter.SP);
  out.writeLine("oImpl_" + "." + vbName + "() = " + attrName + "New");
  out.indent(false);
 }
 /** Write additional server delegate function
	 *							vbInterface == null is nonsense here???
	 *  @param	vbInterface		Interface name or null if public
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeVbImplTie(String vbInterface, VbClsWriter out)
  throws IOException
 {
  String attrName= getUnEscName();
  String vbName= getVbPropertyName();
  out.writeLine();
  out.write("'");
  if(readonly)
   out.write(IdlSpecification.READONLY+ " ");
  out.writeLine(IdlSpecification.ATTRIBUTE + " " + attrName);
  out.writeAttributeHead(vbInterface, attrName, vbName,
   VbWriter.GET, idlType, true);
  out.writeLine("'Please write your own code here if using as servant example");
  out.writeIf("oDelegate Is Nothing"); out.writeThen();
  out.writeLine(VbWriter.CALL + VbWriter.SP + IDL2VB.getVbOrbDLL()
   + ".raiseNOIMPLEMENT(0, " + IDL2VB.getVbOrbDLL() + ".CompletedNO)");
  out.writeElse();
  if(idlType.isVbSet())
  { out.write(VbWriter.SET); out.write(VbWriter.SP);
  }
  out.writeLine(vbInterface + "_" + vbName + " = oDelegate." + vbName);
  out.writeEndIf();
  out.writeAttributeTail(vbInterface, vbName, VbWriter.GET, idlType, true);
  if(readonly)
   return;
  out.writeLine();
  String vbKind= idlType.isVbSet()? VbWriter.SET: VbWriter.LET;
  out.writeAttributeHead(vbInterface, attrName, vbName,
   vbKind, idlType, true);
  out.writeLine("'Please write your own code here if using as servant example");
  out.writeIf("oDelegate Is Nothing"); out.writeThen();
  out.writeLine(VbWriter.CALL + VbWriter.SP + IDL2VB.getVbOrbDLL()
   + ".raiseNOIMPLEMENT(0, " + IDL2VB.getVbOrbDLL() + ".CompletedNO)");
  out.writeElse();
  if(idlType.isVbSet())
  { out.write(VbWriter.SET); out.write(VbWriter.SP);
  }
  out.writeLine("oDelegate." + vbName + " = " + attrName + "New");
  out.writeEndIf();
  out.writeAttributeTail(vbInterface, vbName, vbKind, idlType, true);
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
  String attrName= getUnEscName();
  String vbName= getVbPropertyName();
  out.writeLine();
  out.write("'");
  if(readonly)
   out.write(IdlSpecification.READONLY+ " ");
  out.writeLine(IdlSpecification.ATTRIBUTE + " " + attrName);
  out.writeAttributeHead(vbInterface, attrName, vbName,
   VbWriter.GET, idlType, true);
  //do something
  out.writeLine("'Please write your code here after copying this file");
  out.writeLine("'instead of throwing an exception");
  out.writeLine(VbWriter.CALL + VbWriter.SP + IDL2VB.getVbOrbDLL()
   + ".raiseNOIMPLEMENT(0, " + IDL2VB.getVbOrbDLL() + ".CompletedNO)");
  out.writeAttributeTail(vbInterface, vbName, VbWriter.GET, idlType, true);
  if(readonly)
   return;
  out.writeLine();
  String vbKind= idlType.isVbSet()? VbWriter.SET: VbWriter.LET;
  out.writeAttributeHead(vbInterface, attrName, vbName,
   vbKind, idlType, true);
  //do something
  out.writeLine("'Please write your code here after copying this file");
  out.writeLine("'instead of throwing an exception");
  out.writeLine(VbWriter.CALL + VbWriter.SP + IDL2VB.getVbOrbDLL()
   + ".raiseNOIMPLEMENT(0, " + IDL2VB.getVbOrbDLL() + ".CompletedNO)");
  out.writeAttributeTail(vbInterface, vbName, vbKind, idlType, true);
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
  String attrName= getUnEscName();
  String vbName= getVbPropertyName();
  out.writeLine();
  out.write("'");
  if(readonly)
   out.write(IdlSpecification.READONLY+ " ");
  out.writeLine(IdlSpecification.ATTRIBUTE + " " + attrName);
  out.writeAttributeHead(vbInterface, attrName, vbName,
   VbWriter.GET, idlType, false);
  if(idlType.isVbSet())
  { out.write(VbWriter.SET); out.write(VbWriter.SP);
  }
  if(vbInterface == null)
   out.writeLine(vbName + " = " + vbName);
  else
   out.writeLine(vbInterface + "_" + vbName + " = " + vbName);
  out.writeAttributeTail(vbInterface, vbName, VbWriter.GET, idlType, false);
  if(readonly)
   return;
  out.writeLine();
  String vbKind= idlType.isVbSet()? VbWriter.SET: VbWriter.LET;
  out.writeAttributeHead(vbInterface, attrName, vbName,
   vbKind, idlType, false);
  if(idlType.isVbSet())
  { out.write(VbWriter.SET); out.write(VbWriter.SP);
  }
  out.writeLine(vbName + " = " + attrName + "New");
  out.writeAttributeTail(vbInterface, vbName, vbKind, idlType, false);
 }
}
