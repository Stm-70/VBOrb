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
public class IdlTypedef extends IdlDeclarator implements IdlType
{
 /** <type_declarator> ::= <type_spec> <declarators>
	 *  Read type and declarators of typedef
	 *
	 *  @param	surContainer	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iTypedefs (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlTypedef readIdlTypedefs(IdlContainer surContainer,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  // <type_spec>
  IdlType iType= IdlBaseType.readTypeSpec(surContainer, tRef, idlRd);
  // Anonymous type is allowed here
  // <declarators>
  TxtToken token;
  IdlTypedef iTypedefs= null;
  do
  { // <identifier>
   IdlTypedef iTypedef= IdlTypedef.readIdlTypedef(iType,
    surContainer, tRef, idlRd);
   // Add typedef
   if(iTypedefs == null)
   { iTypedefs= iTypedef;
   }else
   { iTypedefs.addNext(iTypedef);
   }
   token= tRef.getOrReadToken(idlRd);
  }while(token instanceof TxtTokSepChar
   && ((TxtTokSepChar)token).getChar() == ',');
  tRef.ungetToken(token);
  return iTypedefs;
 }
 /** Read declarator of typedef
	 *
	 *  @param	iType		<type_spec>, can be an anonymous type
	 *  @param	surScope	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iTypedef (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlTypedef readIdlTypedef(IdlType iType, IdlScope surScope,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  // <identifier>
  TxtToken token= tRef.getOrReadToken(idlRd);
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(surScope, token);
  // <fixed_array_size>*
  iType= IdlArray.readIdlType(iType, surScope, tRef, idlRd);
  IdlTypedef iTypedef= new IdlTypedef(iWord, iType);
  surScope.putIdentifier(iTypedef, true);
  // Register this typedef as a new IDL definition
  //
  surScope.getIdlSpecification().registerIdlDef(iTypedef);
  return iTypedef;
 }
 /** 
	 *  @param	identifier		Identifier
	 *  @param	iType	
	 */
 protected IdlTypedef(IdlIdentifier identifier, IdlType iType)
 {
  super(identifier, iType);
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
 { return idlType.getOriginIdlType(); // if idlType is also a typedef
 }
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
  return idlType.setVbName(sVbName);
 }
 /** (IdlDefinition)
	 *  Get the Visual Basic Name to identify the definition
	 *
	 *	@param	bPrefix	With final prefix? The name without prefix is used
	 *  				to build complex names.
	 *	@return		Visual Basic Name
	 */
 public String getVbName(boolean bPrefix)
 { return idlType.getVbName(bPrefix);
 }
 /** (IdlType)
	 *
	 *  @return				Assign by SET or LET?
	 */
 public boolean isVbSet()
 { return idlType.isVbSet();
 }
 /** (IdlType)
	 *	Write VB code reading the value from an oIn stream
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable getting the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbRead(VbClsWriter out, String vbVariable)
   throws java.io.IOException
 {
  // array, see IdlDeclarator
  idlType.writeVbRead(out, vbVariable);
 }
 /** (IdlType)
	 *	Write VB code writing the value to an oOut stream
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable containing the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbWrite(VbClsWriter out, String vbVariable)
   throws java.io.IOException
 {
  // array, see IdlDeclarator
  idlType.writeVbWrite(out, vbVariable);
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
  out.writeLine("oOrb.createAliasTc(\"" + getTypeId()
   + "\", \"" + getUnEscName() + "\", "
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
  idlType.writeVbFromAny(out, vbVariable);
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
  idlType.writeVbIntoAny(out, vbVariable);
 }
}
