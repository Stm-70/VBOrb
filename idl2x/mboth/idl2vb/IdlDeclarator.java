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
/** see also: IdlTypedef
 *
 *  @author  Martin Both
 */
public class IdlDeclarator extends IdlIdentifier
{
 /** <type_spec>
	 */
 protected IdlType idlType;
 /** <member> ::= <type_spec> <declarators> ";"
	 *  Read <member> declarators
	 *
	 *  @param	surContainer	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				List of iDeclarators (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlDeclarator readIdlDcls(IdlContainer surContainer,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  // <type_spec>
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readTypeSpec(surContainer, tRef, idlRd);
  if(!iType.isCompleteType())
   throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(),
    iType);
  // if(iType.isAnonymousType()) later in IdlDeclarator.readIdlDcl()
  // <declarators>
  IdlDeclarator iDcls= null;
  do
  { // <identifier>
   IdlDeclarator iDcl= IdlDeclarator.readIdlDcl(iType, surContainer,
    tRef, idlRd);
   // Add member
   if(iDcls == null)
   { iDcls= iDcl;
   }else
   { iDcls.addNext(iDcl);
   }
   token= tRef.getOrReadToken(idlRd);
  }while(token instanceof TxtTokSepChar
   && ((TxtTokSepChar)token).getChar() == ',');
  // ";"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ';')
  { throw new TxtReadException(token.getFilePos(),
    "\";\" of member declaration expected");
  }
  return iDcls;
 }
 /** Read declarator
	 *
	 *  @param	iType		<type_spec>
	 *  @param	surScope	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iDeclarator (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlDeclarator readIdlDcl(IdlType iType, IdlScope surScope,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  // <identifier>
  TxtToken token= tRef.getOrReadToken(idlRd);
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(surScope, token);
  // <fixed_array_size>*
  token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  iType= IdlArray.readIdlType(iType, surScope, tRef, idlRd);
  if(iType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iType);
  IdlDeclarator iDeclarator= new IdlDeclarator(iWord, iType);
  surScope.putIdentifier(iDeclarator, true);
  return iDeclarator;
 }
 /** 
	 *  @param	iWord		Identifier
	 *  @param	iType	
	 */
 public IdlDeclarator(IdlIdentifier iWord, IdlType iType)
 {
  super(iWord);
  this.idlType= iType;
 }
 /** Get the IdlType to DIM and work with a VB variable
	 *  (needed by IdlUnion)
	 *  @return			IdlType of the parameter
	 */
 public IdlType getIdlType()
 { return idlType;
 }
 private String sVbPrivateVarName;
 /** VB property
	 *  @return				p_...
	 */
 public String getVbPropertyName()
 {
  String idlName= getUnEscName();
  if(VbWriter.hashResWord(idlName) == null)
   return idlName;
  else
   return "p_" + idlName;
 }
 /** VB private variable name
	 *  @return				p_..._
	 */
 public String getVbPrivateVarName()
 { if(sVbPrivateVarName == null)
   sVbPrivateVarName= getVbPropertyName() + "_";
  return sVbPrivateVarName;
 }
 /** Read the declarator
	 *  @param	vbC		
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbPrivateVarRead(VbClsWriter vbC)
  throws java.io.IOException
 {
  // CORBA arrays are mapped to seperate VB classes
  // Let/Set and Get properties of IdlDeclarator are coded
  // in IdlUnion also.
  idlType.writeVbRead(vbC, getVbPrivateVarName());
 }
 /** Write the declarator
	 *  @param	vbC		
	 *  @param	vbVariable	Name of the VB variable
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbPrivateVarWrite(VbClsWriter vbC)
  throws java.io.IOException
 {
  // CORBA arrays are mapped to seperate VB classes
  // Let/Set and Get properties of IdlDeclarator are coded
  // in IdlUnion also.
  idlType.writeVbWrite(vbC, getVbPrivateVarName());
 }
 /** 
	 *  @param	vbC		
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbPrivateVarDecl(VbClsWriter vbC)
  throws java.io.IOException
 {
  /* Public variables are always map internaly
		   to Get, Let, and Set properties. We use therefore private
		   variables and write our own properity Get/Let/Set methods.
		   This is also necessarily for valuetype inheritance.
		   See IdlValStateDcl.
		*/
  // CORBA arrays are mapped to seperate VB classes
  // This is also coded in IdlUnion for a local variable.
  vbC.writeVarLine(VbWriter.PRIVATE, getVbPrivateVarName(),
   idlType.getVbName(true));
 }
 /** Write our own properity Get/Let/Set methods.
	 *
	 *  @param	vbC		
	 *  @param	getOnly		
	 *  @param	vbInterface		Interface name or null if public
	 *
	 *	@exception	IOException	
	 */
 public void writeVbPropFuncs(VbClsWriter vbC,
  boolean getOnly, String vbInterface) throws java.io.IOException
 {
  String vbKind= VbWriter.GET;
  String vbPropertyName= getVbPropertyName();
  vbC.writeAttributeHead(vbInterface, getUnEscName(),
   vbPropertyName, vbKind, idlType, /*vbOnErrGo*/false);
  if(vbInterface == null)
  { vbC.writeAssign(vbPropertyName, idlType.isVbSet());
  }else
  { vbC.writeAssign(vbInterface + "_" + vbPropertyName,
    idlType.isVbSet());
  }
  vbC.writeLine(getVbPrivateVarName());
  vbC.writeAttributeTail(vbInterface,
   vbPropertyName, vbKind, idlType, /*vbOnErrGo*/false);
  if(getOnly)
   return;
  vbC.writeLine();
  vbKind= idlType.isVbSet()? VbWriter.SET: VbWriter.LET;
  vbC.writeAttributeHead(vbInterface, getUnEscName(),
   vbPropertyName, vbKind, idlType, /*vbOnErrGo*/false);
  vbC.writeAssign(getVbPrivateVarName(), idlType.isVbSet());
  vbC.writeLine(getUnEscName() + "New");
  vbC.writeAttributeTail(vbInterface,
   vbPropertyName, vbKind, idlType, /*vbOnErrGo*/false);
 }
}
