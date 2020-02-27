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
public class IdlValStateDcl extends IdlDeclarator
{
 /** ( "public" | "private" )
	 */
 protected boolean bPublic;
 /** <state_member> ::= ( "public" | "private" )
	 *                     <type_spec> <declarators> ";"
	 *  Read <state_member> declarators
	 *
	 *  @param	bPublic		( "public" | "private" )
	 *  @param	surContainer	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				List of iVSDcls (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlValStateDcl readIdlDcls(boolean bPublic,
  IdlContainer surContainer, TxtTokenRef tRef, TxtTokenReader idlRd)
  throws TxtReadException
 {
  // <type_spec>
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readTypeSpec(surContainer, tRef, idlRd);
  if(!iType.isCompleteType())
   throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(),
    iType);
  // if(iType.isAnonymousType()) later in IdlValStateDcl.readIdlDcl()
  // <declarators>
  IdlValStateDcl iVSDcls= null;
  do
  { // <identifier>
   IdlValStateDcl iVSDcl= IdlValStateDcl.readIdlDcl(bPublic,
    iType, surContainer, tRef, idlRd);
   // Add member
   if(iVSDcls == null)
   { iVSDcls= iVSDcl;
   }else
   { iVSDcls.addNext(iVSDcl);
   }
   token= tRef.getOrReadToken(idlRd);
  }while(token instanceof TxtTokSepChar
   && ((TxtTokSepChar)token).getChar() == ',');
  // ";"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ';')
  { throw new TxtReadException(token.getFilePos(),
    "\";\" of state member declaration expected");
  }
  return iVSDcls;
 }
 /** Read <state_member> declarator
	 *
	 *  @param	bPublic		( "public" | "private" )
	 *  @param	iType		<type_spec>
	 *  @param	surScope	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iVSDcl (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlValStateDcl readIdlDcl(boolean bPublic, IdlType iType,
  IdlScope surScope, TxtTokenRef tRef, TxtTokenReader idlRd)
  throws TxtReadException
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
  IdlValStateDcl iVSDcl= new IdlValStateDcl(iWord, iType, bPublic);
  surScope.putIdentifier(iVSDcl, true);
  return iVSDcl;
 }
 /** 
	 *  @param	iWord		Identifier
	 *  @param	iType	
	 *  @param	bPublic	
	 */
 public IdlValStateDcl(IdlIdentifier iWord, IdlType iType, boolean bPublic)
 {
  super(iWord, iType);
  this.bPublic= bPublic;
 }
 /** 
	 *  @return			Is public?
	 */
 public boolean isPublic()
 { return bPublic;
 }
 /** (IdlDeclarator)
	 *  VB property
	 *  @return				...Prop
	 */
 public String getVbPropertyName()
 {
  String sVbPropertyName= IdlOperation.convertOpOrAttr2VB(getUnEscName());
  if(VbWriter.hashResWord(sVbPropertyName) == null)
   return sVbPropertyName;
  else
   return sVbPropertyName + "Prop";
 }
 /** 
	 *  @param	vbC		
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbPrivateStateVarDecl(VbClsWriter vbC)
  throws java.io.IOException
 {
  super.writeVbPrivateVarDecl(vbC);
 }
 /** Write our own properity Get/Let/Set methods.
	 *
	 *  @param	vbC		
	 *  @param	forcePublic		
	 *  @param	vbInterface		Interface name or null if public
	 *
	 *	@exception	IOException	
	 */
 public void writeVbStatePropFuncs(VbClsWriter vbC,
  boolean forcePublic, String vbInterface) throws java.io.IOException
 {
  super.writeVbPropFuncs(vbC, /*getOnly*/!bPublic && !forcePublic,
   vbInterface);
 }
}
