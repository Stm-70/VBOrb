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
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;
import mboth.util.*;
/** An interface is a reference to an object supporting that interface.
 *  An interface name is an IdlType (Object).
 *
 *  @author  Martin Both
 */
public class IdlObjRefType extends IdlInterface // IdlScope implements IdlType
{
 /** Either local or unconstrained interface?
	 */
 private boolean bLocal;
 /** Read an interface head.
	 *
	 *  @param	idlScope	
	 *  @param	bLocal		Constrained to a local object?
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iObjRefType (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlObjRefType readIdlHead(IdlScope idlScope,
  boolean bLocal, TxtToken token, TxtTokenReader idlRd)
  throws TxtReadException
 {
  IdlObjRefType iObjRefType;
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readIdlWord(idlScope, token);
  IdlIdentifier identifier= idlScope.getIdentifier(iWord,
   IdlScope.SL_DEFN | IdlScope.SL_INTR);
  if(identifier != null)
  { IdlInterface iInterface;
   if(!(identifier instanceof IdlInterface))
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "IDL identifier `" + identifier.getUnEscName()
     + "' redefined after use");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the first identifier definition"));
    throw ex;
   }
   // forward declared or already declared
   iInterface= (IdlInterface)identifier;
   if(iInterface.isAbstract() || bLocal != iInterface.isLocal())
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "Different abstract or local modifiers for interface `"
     + iInterface.getUnEscName() + "'");
    ex.setNextException(new TxtReadException(iInterface.getFilePos(),
     "Position of previous declaration"));
    throw ex;
   }
   iObjRefType= (IdlObjRefType)iInterface;
   iObjRefType.checkPragmaPrefix(iWord);
  }else
  { iObjRefType= new IdlObjRefType(iWord, bLocal);
   idlScope.putIdentifier(iObjRefType, true);
   // The name of an interface, valuetype, struct, union, exception
   // or a module may not be redefined within the immediate scope of
   // the interface, valuetype, struct, union, exception, or the
   // module. That's why we introduce the identifier now.
   iObjRefType.putIdentifier(iObjRefType, false);
  }
  return iObjRefType;
 }
 /** 
	 *  @param	identifier	Identifier
	 *  @param	bLocal		Local?
	 */
 protected IdlObjRefType(IdlIdentifier identifier, boolean bLocal)
 {
  super(identifier);
  this.bLocal= bLocal;
 }
 /** Is an abstract interface?
	 *
	 *	@return		bAbstract
	 */
 public boolean isAbstract()
 { return false;
 }
 /** Is a local interface?
	 *
	 *	@return		bLocal
	 */
 public boolean isLocal()
 { return bLocal;
 }
 /** (IdlType)
	 *  Is a local type?
	 *	@return		isLocalType
	 */
 public boolean isLocalType()
 { return bLocal;
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
  String sScopedName= getScopedName().toString();
  // Is a pseudo object?
  if(sScopedName.equals("::CORBA::TypeCode"))
  { out.write(VbWriter.SET + " " + vbVariable + " = ");
   out.writeLine(VbWriter.OIN + ".readTypeCode()");
  }else
  { out.write(VbWriter.SET + " " + vbVariable + " = "
    + getVbModName() + "." + VbWriter.NARROW + "(");
   out.writeLine(VbWriter.OIN + ".readObject())");
  }
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
  String sScopedName= getScopedName().toString();
  // Is a pseudo object?
  if(sScopedName.equals("::CORBA::TypeCode"))
  { out.writeLine(VbWriter.CALL + " " + VbWriter.OOUT
    + ".writeTypeCode(" + vbVariable + ")");
  }else
  { out.writeLine(VbWriter.CALL + " " + VbWriter.OOUT
    + ".writeObject(" + vbVariable + ")");
  }
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
  String sScopedName= getScopedName().toString();
  // Is a pseudo object?
  if(sScopedName.equals("::CORBA::TypeCode"))
  { out.writeAssign(vbVariable, true);
   // mCB.tk_TypeCode = 12
   out.writeLine("oOrb.createPrimitiveTc(12) 'VBOrb.TCTypeCode");
  }else
  { out.writeAssign(vbVariable, true);
   out.writeLine("oOrb.createInterfaceTc(\"" + getTypeId()
    + "\", \"" + getUnEscName() + "\")");
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
  String sScopedName= getScopedName().toString();
  // Is a pseudo object?
  if(sScopedName.equals("::CORBA::TypeCode"))
  { out.writeAssign(vbVariable, true);
   out.writeLine(VbWriter.OANY + ".getTypecode()");
  }else
  { out.writeAssign(vbVariable, true);
   out.writeLine(VbWriter.OANY + ".getReference()");
  }
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
  String sScopedName= getScopedName().toString();
  // Is a pseudo object?
  if(sScopedName.equals("::CORBA::TypeCode"))
  { out.writeLine(VbWriter.CALL + " " + VbWriter.OANY
    + ".insertTypecode(" + vbVariable + ")");
  }else
  { out.writeLine(VbWriter.CALL + " " + VbWriter.OANY
    + ".insertReference(" + vbVariable + ")");
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
   System.out.println("I Writing interface " + getScopedName()
    + " to `" + sVbModName + ".bas'");
  VbModWriter vbM= new VbModWriter(vbPath, sVbModName,
   getFilePos().getFileName());
  // First of all write constants, later it is not allowed.
  // Write TypeId constant declaration
  vbM.writeLine();
  this.writeConstScopeTypeId(vbM, IdlSpecification.INTERFACE,
   /* isPublic */ true);
  /* (IdlContainer)
		*/
  this.writeVbModConsts(vbM);
  /* (IdlContainer)
		*/
  this.writeVbModHelpers(vbM);
  vbM.writeLine();
  vbM.writeLine("'Helper");
  vbM.writeObjNarrow(true, getVbName(true));
  vbM.writeLine();
  vbM.writeLine("'Helper");
  vbM.writeObjNarrow(false, getVbName(true));
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
  // Writing VB interface class
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing interface " + getScopedName()
    + " to `" + getVbName(true) + ".cls'");
  VbClsWriter vbC= new VbClsWriter(vbPath, getVbName(true),
   getFilePos().getFileName());
  // Implements cOrbAbstractBase, cOrbObject
  vbC.writeLine();
  vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP
   + VbWriter.CORBABSTRACTBASE);
  vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP
   + VbWriter.CORBOBJECT);
  // Write TypeId constant declaration
  vbC.writeLine();
  this.writeConstScopeTypeId(vbC, IdlSpecification.INTERFACE,
   /* isPublic */ false);
  vbC.writeVarLine(VbWriter.PRIVATE, VbWriter.OOBJREF,
   VbWriter.CORBOBJREF);
  // Implements cOrbAbstractBase
  vbC.writeLine();
  vbC.writeFuncThisObject(VbWriter.FRIEND,
   "OrbAbstractBase", VbWriter.CORBABSTRACTBASE);
  // cOrbAbstractBase_isObjRef()
  vbC.writeAbstractBaseIsObjRef(true);
  // Implements cOrbObject
  vbC.writeLine();
  vbC.writeFuncThisObject(VbWriter.FRIEND,
   "OrbObject", VbWriter.CORBOBJECT);
  // cOrbObject_setObjRef()
  vbC.writeObjectSetObjRef(isLocal());
  // cOrbObject_getObjRef()
  vbC.writeObjectGetObjRef();
  // cOrbObject_getId()
  vbC.writeObjectGetId();
  // Helper, insert, Any???
  // Helper, extract, Any???
  getAttributes();
  for(int at= 0; at < iAllAttrs.length; at++)
  { IdlAttribute iAttr= iAllAttrs[at];
   iAttr.writeVbProps(vbC);
  }
  getOperations();
  for(int op= 0; op < iAllOps.length; op++)
  { IdlOperation iOp= iAllOps[op];
   iOp.writeVbSubOrFunc(vbC);
  }
  vbC.close();
 }
 /** Write additional server skeleton examples
	 *
	 *  @param	vbPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 protected void writeVbImplCls(String vbPath) throws IOException
 {
  if(getFileIncludeLvl() > IDL2VB.iGenIncludeLvl)
  { return;
  }
  // Only forward declaration?
  if(!isDefined())
  { return;
  }
  // File name and class name containing ImplTie
  String vbImplTieName= getVbName(true) + "ImplTie";
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing interface skeleton " + getScopedName()
    + " to `" + vbImplTieName + ".cls'");
  VbClsWriter out= new VbClsWriter(vbPath, vbImplTieName,
   getFilePos().getFileName());
  out.writeLine();
  out.writeLine("'You can use this class as a tie to your servant or as a servant example.");
  out.writeLine("'Using as a tie:");
  out.writeLine("' Your servant has to implement " + getVbName(true));
  out.writeLine("' To activate a servant via tie write following:");
  String sImplObjName= "o" + getVbName(false) + "ImplTie";
  out.writeLine("'  Dim " + sImplObjName + " As " + vbImplTieName);
  out.writeLine("'  Set " + sImplObjName + " = New " + vbImplTieName);
  out.writeLine("'  Call " + sImplObjName + ".setDelegate(oServant)");
  out.writeLine("'  Call oOrb.Connect(" + sImplObjName + ")");
  out.writeLine("'Using as a servant example:");
  sImplObjName= "o" + getVbName(false) + "Impl";
  String sImplClsName= getVbName(true) + "Impl";
  out.writeLine("' Rename class to " + sImplClsName + " and save the file as "
   + sImplClsName + ".cls.");
  out.writeLine("' Remove delegate stuff and write your own servant operations.");
  out.writeLine("' To activate a servant write following:");
  out.writeLine("'  Dim " + sImplObjName + " As " + sImplClsName);
  out.writeLine("'  Set " + sImplObjName + " = New " + sImplClsName);
  out.writeLine("'  Call oOrb.Connect(" + sImplObjName + ")");
  // Implements cOrbSkeleton
  out.writeLine();
  out.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP + VbWriter.CORBSKELETON);
  out.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP + getVbName(true));
  // Write TypeId constant declaration
  out.writeLine();
  this.writeConstScopeTypeId(out, IdlSpecification.INTERFACE,
   /* isPublic */ false);
  out.writeVarLine(VbWriter.PRIVATE, VbWriter.OOBJREF,
   VbWriter.CORBOBJREF);
  out.writeVarLine(VbWriter.PRIVATE, "oDelegate", getVbName(true));
  out.writeSetGetDelegate(getVbName(true));
  out.writeGetObjRef();
  out.writeGetThis(getVbName(true));
  // implements in VB funktioniert nicht, wenn implementierte
  // Schnittstelle Unterstriche in nur einer einzigen Sub aufweist.
  // Deshalb Ansprechen des anderen Interfaces nur "uber narrow m"oglich
  out.writeSkeletonProps(getTypeIds());
  boolean hasExceptions= false;
  getOperations();
  for(int op= 0; op < iAllOps.length; op++)
  { IdlOperation iOp= iAllOps[op];
   if(iOp.getIdlExceptions() != null)
   { hasExceptions= true;
    break;
   }
  }
  // cOrbSkeleton_execute()
  out.writeFuncExecuteHead();
  out.writeDimLine("oImpl_", getVbName(true));
  out.writeLine(VbWriter.SET + VbWriter.SP + "oImpl_" + VbWriter.SP
   + "=" + VbWriter.SP + VbWriter.IIF + "(" + "oDelegate"
   + VbWriter.SP + VbWriter.IS + VbWriter.SP + VbWriter.NOTHING
   + ", " + VbWriter.ME + ", " + "oDelegate" + ")");
  //if(hasExceptions)
  //{	out.writeDimLine(VbWriter.OEX, VbWriter.CORBEXCEPTION);
  //}
  out.writeLine(VbWriter.SELECT + VbWriter.SP + VbWriter.CASE
   + VbWriter.SP + VbWriter.SOPERATION);
  getAttributes();
  for(int at= 0; at < iAllAttrs.length; at++)
  { IdlAttribute iAttr= iAllAttrs[at];
   iAttr.writeVbImplExec(out);
  }
  int vbScopeNo= 1;
  getOperations();
  for(int op= 0; op < iAllOps.length; op++)
  { IdlOperation iOp= iAllOps[op];
   iOp.writeVbImplExec(out, vbScopeNo++);
  }
  out.writeLine(VbWriter.CASE + VbWriter.SP + VbWriter.ELSE);
  out.indent(true);
  out.write(VbWriter.CALL + VbWriter.SP
   + IDL2VB.getVbOrbDLL() + ".raiseBADOPERATION(1, "
   + IDL2VB.getVbOrbDLL() + ".CompletedNO, ");
  out.writeLine(VbWriter.SOPERATION + ")");
  out.indent(false);
  out.writeLine(VbWriter.END + VbWriter.SP + VbWriter.SELECT);
  out.writeLine(VbWriter.CORBSKELETON + "_" + VbWriter.EXECUTE
   + " = 0 'NO_EXCEPTION");
  if(hasExceptions)
  { out.writeLine(VbWriter.EXIT + VbWriter.SP + VbWriter.FUNCTION);
   out.indent(false);
   out.writeLine(VbWriter.USEREXWRITER + ":");
   out.indent(true);
   out.writeLine(VbWriter.CALL + VbWriter.SP
    + IDL2VB.getVbOrbDLL() + ".getException()."
    + VbWriter.WRITEME + "(" + VbWriter.OOUT + ")");
   out.writeLine(VbWriter.CORBSKELETON + "_" + VbWriter.EXECUTE
    + " = 1 'USER_EXCEPTION");
   out.writeExitEndNoUserEx(VbWriter.FUNCTION, getUnEscName()
    + ".execute(\" & " + VbWriter.SOPERATION + " & \")", null);
  }else
  { out.writeExitEnd(VbWriter.FUNCTION, getUnEscName()
    + ".execute(\" & " + VbWriter.SOPERATION + " & \")", null);
  }
  out.writeLine();
  out.writeLine("'Implements " + getVbName(true));
  getAttributes();
  for(int at= 0; at < iAllAttrs.length; at++)
  { IdlAttribute iAttr= iAllAttrs[at];
   iAttr.writeVbImplTie(getVbName(true), out);
  }
  getOperations();
  for(int op= 0; op < iAllOps.length; op++)
  { IdlOperation iOp= iAllOps[op];
   iOp.writeVbImplTie(getVbName(true), out);
  }
  out.close();
 }
}
