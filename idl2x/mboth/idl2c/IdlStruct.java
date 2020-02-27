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
package mboth.idl2c;
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
 /** (IdlType)
	 * Get the C Name to identify the type
	 *
	 *	@bPrefix	With final prefix?
	 *	@return		C Name
	 */
 public String getCName(boolean bPrefix)
 { if(bPrefix)
   return CWriter.CORBA_ +"struct???";//???
  return "struct???";//???
 }
}
