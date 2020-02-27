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
/** This class handles "regular" valuetypes, abstract valuetypes, and
 *  forward declarations. Boxed valuetypes are handled by IdlValueBox.
 *
 *  @author  Martin Both
 */
public class IdlValueType extends IdlContainer implements IdlType
{
 /** An abstract valuetype may not be instantiated. No <state_member>
	 *  or <initializers> may be specified.
	 */
 private boolean bAbstract;
 /** Valuetype uses custom marshaling? Is called custom valuetype.
	 *  (truncatable modifier may not be used in <value_inheritance_spec.)
	 */
 private boolean bCustom;
 /** Is a local type?
	 */
 private boolean bLocal;
 private boolean bLocalChecked;
 /** Not only forward declared?
	 *  It is illegal to inherit from a forward-declared valuetype
	 *  whose definition has not yet been seen.
	 */
 private TxtFilePos defFilePos;
 private boolean bDefined;
 /** May not be used if this is a custom value
	 */
 private boolean bTruncatable;
 /** <value_inheritance_spec>
	 *  Direct base valuetypes or null
	 */
 private IdlValueType idlValueTypes[];
 /** Supports [abstract] interfaces or null
	 */
 private IdlInterface idlInterfaces[];
 /** Collection of all TypeIds, sTypeIds[0] is the most derived TypeId
	 */
 private String sTypeIds[];
 /** Additional Contained IdlDefinitions
	 *  in <export>
	 */
 private IdlAttribute idlAttributes;
 private IdlOperation idlOperations;
 /** State members or null, <state_member>*
	 */
 private IdlValStateDcl idlStateDcls;
 /** Initializers or null, <init_dcl>*
	 */
 private IdlValInitOp idlValInitOps;
 /** Collect all valuetype (local) attributes (including inherit attributes)
	 */
 private Hashtable idlCollAttrs;
 private ArrayList idlAttrArr;
 private IdlAttribute iAllAttrs[];
 /** Collect all valuetype (local) operations (including inherit operations)
	 */
 private Hashtable idlCollOps;
 private ArrayList idlOpArr;
 private IdlOperation iAllOps[];
 /** Collect all valuetype (local) state member (including inherit members)
	 */
 private Hashtable idlCollSts;
 private ArrayList idlStArr;
 private IdlValStateDcl iAllSts[];
 /** Read a valuetype head or a complete value box.
	 *  <definition> ::= <...> ";" | <interface> | <module> | <value> ";"
	 *  <value> ::= ( <value_dcl> | <value_abs_dcl> | <value_box_dcl> |
	 *                <value_forward_dcl> )
	 *
	 *  @param	surContainer	Information about the surrounding scope
	 *  @param	bAbstract	
	 *  @param	bCustom	
	 *  @param	tRef		Next TxtToken
	 *  @param	idlRd		IdlFile
	 *	@return				iType (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readIdlHead(IdlContainer surContainer,
  boolean bAbstract, boolean bCustom,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  IdlValueType iValueType;
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readIdlWord(surContainer, token);
  IdlIdentifier identifier= surContainer.getIdentifier(iWord,
   IdlScope.SL_DEFN | IdlScope.SL_INTR);
  if(identifier != null)
  { if(identifier instanceof IdlValueBox)
   { IdlValueBox iValueBox= (IdlValueBox)identifier;
    TxtReadException ex= new TxtReadException(token.getFilePos(),
     "Redefinition of value box `" + identifier.getUnEscName()
     + "'");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of previous definition"));
    throw ex;
   }
   if(!(identifier instanceof IdlValueType))
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "IDL identifier `" + identifier.getUnEscName()
     + "' redefined after use");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the first identifier definition"));
    throw ex;
   }
   // valuetype is forward declared or already declared
   iValueType= (IdlValueType)identifier;
   if(bAbstract != iValueType.bAbstract ||
    bCustom != iValueType.bCustom)
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "Different abstract or custom modifiers for valuetype `"
     + iValueType.getUnEscName() + "'");
    ex.setNextException(new TxtReadException(iValueType.getFilePos(),
     "Position of previous declaration"));
    throw ex;
   }
   iValueType.checkPragmaPrefix(iWord);
  }else
  { // Check if value box <type_spec>
   // or valuetype (";" | ":" | "{" | "supports" )
   if(!bAbstract && !bCustom)
   { token= tRef.getOrReadToken(idlRd);
    if(token instanceof TxtTokSepChar)
    { char sepCh= ((TxtTokSepChar)token).getChar();
     if(sepCh == ':')
     { TxtToken token2= idlRd.readToken();
      if(!token2.isAfterWhiteSpaces()
       && token2 instanceof TxtTokSepChar
       && ((TxtTokSepChar)token2).getChar() == ':')
      { idlRd.unreadToken();
       tRef.ungetToken(token);
       return IdlValueBox.readIdlValueBox(surContainer,
        iWord, tRef, idlRd);
      }
      idlRd.unreadToken();
     }else if(sepCh != ';' && sepCh != '{')
     { tRef.ungetToken(token);
      return IdlValueBox.readIdlValueBox(surContainer,
       iWord, tRef, idlRd);
     }
    }else if(token instanceof TxtTokWord)
    { String keyword= IdlSpecification.readKeyword(token, true);
     if(keyword != IdlSpecification.SUPPORTS)
     { tRef.ungetToken(token);
      return IdlValueBox.readIdlValueBox(surContainer,
       iWord, tRef, idlRd);
     }
    }else
    { tRef.ungetToken(token);
     return IdlValueBox.readIdlValueBox(surContainer,
      iWord, tRef, idlRd);
    }
    tRef.ungetToken(token);
   }
   iValueType= new IdlValueType(iWord, bAbstract, bCustom);
   surContainer.putIdentifier(iValueType, true);
   // The name of an interface, valuetype, struct, union, exception
   // or a module may not be redefined within the immediate scope of
   // the interface, valuetype, struct, union, exception, or the
   // module. That's why we introduce the identifier now.
   iValueType.putIdentifier(iValueType, false);
  }
  return iValueType;
 }
 /** 
	 *  @param	identifier	Identifier
	 *  @param	bAbstract	Abstract?
	 *  @param	bCustom		Custom marshaling?
	 */
 protected IdlValueType(IdlIdentifier identifier, boolean bAbstract,
  boolean bCustom)
 {
  super(identifier);
  idlCollAttrs= new Hashtable();
  idlAttrArr= new ArrayList();
  idlCollOps= new Hashtable();
  idlOpArr= new ArrayList();
  idlCollSts= new Hashtable();
  idlStArr= new ArrayList();
  this.bAbstract= bAbstract;
  this.bCustom= bCustom;
 }
 /** (IdlScope)
	 *  (3.15.3 Special Scoping Rules for Type Names, if this is
	 *  an interface or valuetype and iWord is found by searching
	 *  through surrounding scope, so introduce iWord in this scope.)
	 *
	 *  @param	iWord		Identifier searching for
	 *  @param	sls			Scope levels: SL_DEFN, SL_INHT, SL_INTR, SL_SURR
	 *						Main, inherited, introduced, surrounding scopes
	 *	@return				Identifier or null if not found
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public IdlIdentifier getIdentifier(IdlIdentifier iWord, int sls)
  throws TxtReadException
 {
  // 1. Search scope of myself
  IdlIdentifier identifier= super.getIdentifier(iWord,
   sls & (IdlScope.SL_DEFN | IdlScope.SL_INTR));
  if(identifier != null)
   return identifier;
  // 2. Search inherited scope, base valuetypes and supported interfaces
  if((sls & IdlScope.SL_INHT) != 0)
  { if(idlValueTypes != null)
   { for(int i= 0; i < idlValueTypes.length; i++)
    { IdlIdentifier identifier2=
      idlValueTypes[i].getIdentifier(iWord,
       IdlScope.SL_DEFN | SL_INHT);
     if(identifier2 == null)
      continue;
     if(identifier == null)
      identifier= identifier2;
     else if(identifier != identifier2)
     { // ambiguous, if the name is declared as a constant,
      // type or exception in more than one base valuetype.
      TxtReadException ex= new TxtReadException(iWord.getFilePos(),
       "IDL identifier `" + identifier.getUnEscName()
       + "' is ambiguous, declared in more than one"
       + " base valuetypes. Please qualify"
       + " with the right valuetype name");
      ex.setNextException(new TxtReadException(identifier.getFilePos(),
       "Position of the first identifier definition"));
      ex.setNextException(new TxtReadException(identifier2.getFilePos(),
       "Position of the second identifier definition"));
      throw ex;
     }
    }
   }
   if(idlInterfaces != null)
   { for(int i= 0; i < idlInterfaces.length; i++)
    { IdlIdentifier identifier2=
      idlInterfaces[i].getIdentifier(iWord,
       IdlScope.SL_DEFN | SL_INHT);
     if(identifier2 == null)
      continue;
     if(identifier == null)
      identifier= identifier2;
     else if(identifier != identifier2)
     { // ambiguous, if the name is declared as a constant,
      // type or exception in more than one supported interface.
      TxtReadException ex= new TxtReadException(iWord.getFilePos(),
       "IDL identifier `" + identifier.getUnEscName()
       + "' is ambiguous, declared in more than one"
       + " base valuetype or interface. Please qualify"
       + " with the right valuetype or interface name");
      ex.setNextException(new TxtReadException(identifier.getFilePos(),
       "Position of the first identifier definition"));
      ex.setNextException(new TxtReadException(identifier2.getFilePos(),
       "Position of the second identifier definition"));
      throw ex;
     }
    }
   }
   if(identifier != null)
    return identifier;
  }
  // 3. Search surrounding scopes
  if((sls & IdlScope.SL_SURR) != 0 && getSurScope() != null)
  { identifier= getSurScope().getIdentifier(iWord,
    IdlScope.SL_DEFN | IdlScope.SL_INHT | IdlScope.SL_SURR);
   if(identifier != null)
   { // Introduce the identifier, see explanation in head above
    putIdentifier(identifier, false);
    return identifier;
   }
  }
  return null;
 }
 /** Not only forward declared?
	 *
	 *	@return		isDefined
	 */
 public boolean isDefined()
 { return bDefined;
 }
 /** ";" | [ <value_inheritance_spec> ] "{" <value_element>* "}"
	 *  Read a valuetype definition if not ';'.
	 *
	 *	@param	tRef		Unused token
	 *  @param	idlRd		IdlFile
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public void readIdl(TxtTokenRef tRef, TxtTokenReader idlRd)
  throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  // <value_forward_dcl>?
  if(token instanceof TxtTokSepChar &&
   ((TxtTokSepChar)token).getChar() == ';')
  { tRef.ungetToken(token);
   return;
  }
  if(bDefined)
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Redefinition of valuetype `" + getUnEscName() + "'");
   ex.setNextException(new TxtReadException(defFilePos,
    "Position of previous definition"));
   throw ex;
  }
  defFilePos= token.getFilePos();
  // [ <value_inheritance_spec> ]
  // Collect all base valuetypes and interfaces
  if(token instanceof TxtTokSepChar &&
   ((TxtTokSepChar)token).getChar() == ':')
  { // [ ":" [ "truncatable" ] <value_name> { "," <value_name> }* ]
   token= tRef.getOrReadToken(idlRd);
   if(IdlSpecification.readKeyword(token, true) ==
    IdlSpecification.TRUNCATABLE)
   { if(bCustom)
    { throw new TxtReadException(token.getFilePos(),
      "The truncatable modifier may not be used if the"
      + " valuetype being defined is a custom value");
    }
    bTruncatable= true;
   }else
    tRef.ungetToken(token);
   do
   { token= tRef.getOrReadToken(idlRd);
    tRef.ungetToken(token);
    IdlIdentifier identifier= readScopedName(tRef, idlRd, false, false);
    if(!(identifier instanceof IdlValueType))
    { TxtReadException ex= new TxtReadException(token.getFilePos(),
      "Scoped name of an IDL valuetype expected");
     ex.setNextException(new TxtReadException(identifier.getFilePos(),
      "Position where the last identifier "
      + "of the given scoped name is defined"));
     throw ex;
    }
    IdlValueType directBaseValueType= (IdlValueType)identifier;
    addBaseValueType(token, directBaseValueType);
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
  }
  if(IdlSpecification.readKeyword(token, true) ==
   IdlSpecification.SUPPORTS)
  { // [ "supports" <interface_name> { "," <interface_name> }* ]
   do
   { token= tRef.getOrReadToken(idlRd);
    tRef.ungetToken(token);
    IdlIdentifier identifier= readScopedName(tRef, idlRd, false, false);
    if(!(identifier instanceof IdlInterface))
    { TxtReadException ex= new TxtReadException(token.getFilePos(),
      "Scoped name of an IDL interface expected");
     ex.setNextException(new TxtReadException(identifier.getFilePos(),
      "Position where the last identifier "
      + "of the given scoped name is defined"));
     throw ex;
    }
    IdlInterface directBaseInterface= (IdlInterface)identifier;
    addBaseInterface(token, directBaseInterface);
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
  }
  // "{" <value_element>* "}"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '{')
  { throw new TxtReadException(token.getFilePos(),
    "\"{\" of valuetype body expected");
  }
  getIdlSpecification().setPragmaScope(this);
  // <value_element>*
  while((token= readValueElement(idlRd)) == null)
   ;
  // "}"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '}')
  { throw new TxtReadException(token.getFilePos(),
    "Valuetype `" + this.getUnEscName()
    + "': Next value element definition or \"}\" expected");
  }
  bDefined= true;
  // Leaving the scope of this valuetype
  //
  IdlSpecification iSpecification= getIdlSpecification();
  iSpecification.setPragmaScope(getSurScope());
  // Register this valuetype as a new IDL definition
  //
  iSpecification.registerIdlDef(this);
 }
 /** <value_element> ::= <export> | <state_member> | <init_dcl>
	 *  A value can contain all the elements that an interface can
	 *  as well as the definition of state members, and initializers
	 *  for that state.
	 *  (It's written like IdlModule.readIdlDefinition())
	 *  
	 *  @param	idlRd		IdlFile
	 *	@return				Unused token or null
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private TxtToken readValueElement(TxtTokenReader idlRd)
  throws TxtReadException
 {
  String kindOfDeclaration;
  TxtToken token= idlRd.readToken();
  String keyword= IdlSpecification.readKeyword(token, true);
  if(bAbstract)
  { if(keyword == IdlSpecification.PUBLIC ||
    keyword == IdlSpecification.PRIVATE ||
    keyword == IdlSpecification.FACTORY)
   { throw new TxtReadException(token.getFilePos(),
     "An abstract valuetype cannot have <state_members>"
     + " or <initializers>");
   }
  }else
  { if(keyword == IdlSpecification.PUBLIC)
   { IdlValStateDcl iVSDcls= IdlValStateDcl.readIdlDcls(true,
     this, new TxtTokenRef(), idlRd);
    addIdlStateDcls(iVSDcls);
    return null;
   }
   if(keyword == IdlSpecification.PRIVATE)
   { IdlValStateDcl iVSDcls= IdlValStateDcl.readIdlDcls(false,
     this, new TxtTokenRef(), idlRd);
    addIdlStateDcls(iVSDcls);
    return null;
   }
   if(keyword == IdlSpecification.FACTORY)
   { TxtTokenRef tRef= new TxtTokenRef();
    IdlValInitOp iValInitOp= IdlValInitOp.readIdlHead(this, tRef,
     idlRd);
    addIdlValInitOp(iValInitOp);
    iValInitOp.readIdl(tRef, idlRd);
    return null;
   }
  }
  if(keyword == IdlSpecification.TYPEDEF)
  { // <type_spec> <declarators>
   TxtTokenRef tRef= new TxtTokenRef();
   IdlTypedef iTypedefs= IdlTypedef.readIdlTypedefs(this, tRef, idlRd);
   this.addIdlTypedefs(iTypedefs);
   kindOfDeclaration= keyword;
   token= tRef.getOrReadToken(idlRd);
  }else if(keyword == IdlSpecification.STRUCT)
  { IdlStruct iStruct= IdlStruct.readIdlHead(this, idlRd.readToken(),
    idlRd);
   putIdlStruct(iStruct);
   token= iStruct.readIdl(idlRd);
   kindOfDeclaration= keyword;
  }else if(keyword == IdlSpecification.UNION)
  { IdlUnion iUnion= IdlUnion.readIdlHead(this, idlRd.readToken(),
    idlRd);
   putIdlUnion(iUnion);
   token= iUnion.readIdl(idlRd);
   kindOfDeclaration= keyword;
  }else if(keyword == IdlSpecification.ENUM)
  { IdlEnum iEnum= IdlEnum.readIdlEnum(this, idlRd.readToken());
   putIdlEnum(iEnum);
   token= iEnum.readIdl(idlRd);
   kindOfDeclaration= keyword;
  }else if(keyword == IdlSpecification.NATIVE)
  { IdlNative iNative= IdlNative.readIdlNative(this, idlRd.readToken());
   token= idlRd.readToken();
   kindOfDeclaration= keyword;
  }else if(keyword == IdlSpecification.CONST)
  { TxtTokenRef tRef= new TxtTokenRef();
   IdlConst iConst= IdlConst.readIdlConst(this, tRef, idlRd);
   this.addIdlConst(iConst);
   token= tRef.getOrReadToken(idlRd);
   kindOfDeclaration= keyword;
  }else if(keyword == IdlSpecification.EXCEPTION)
  { IdlException iException= IdlException.readIdlHead(this,
    idlRd.readToken(), idlRd);
   addIdlException(iException);
   iException.readIdl(idlRd);
   token= idlRd.readToken();
   kindOfDeclaration= keyword;
  }else if(keyword == IdlSpecification.READONLY)
  { TxtToken token2= idlRd.readToken();
   String keyword2= IdlSpecification.readKeyword(token2, true);
   if(keyword2 == IdlSpecification.ATTRIBUTE)
   { readIdlAttribute(true, idlRd);
    kindOfDeclaration= keyword2;
    token= idlRd.readToken();
   }else
   { idlRd.unreadToken();
    return token;
   }
  }else if(keyword == IdlSpecification.ATTRIBUTE)
  { readIdlAttribute(false, idlRd);
   kindOfDeclaration= keyword;
   token= idlRd.readToken();
  }else if(keyword == IdlSpecification.ONEWAY)
  { TxtTokenRef tRef= new TxtTokenRef();
   IdlOperation iOperation= IdlOperation.readIdlHead(this, true,
    tRef, idlRd, false);
   addIdlOperation(iOperation);
   iOperation.readIdl(tRef, idlRd);
   kindOfDeclaration= "operation";
   token= tRef.getOrReadToken(idlRd);
  }else
  { TxtTokenRef tRef= new TxtTokenRef();
   tRef.ungetToken(token);
   IdlOperation iOperation= IdlOperation.readIdlHead(this, false,
    tRef, idlRd, true);
   if(iOperation == null)
    return tRef.getOrReadToken(idlRd); // token not read
   addIdlOperation(iOperation);
   iOperation.readIdl(tRef, idlRd);
   kindOfDeclaration= "operation";
   token= tRef.getOrReadToken(idlRd);
  }
  // ";"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ';')
  { throw new TxtReadException(token.getFilePos(),
    "\";\" of " + kindOfDeclaration + " declaration expected");
  }
  return null;
 }
 /** Read attribute operations.
	 *
	 *  @param	readonly	
	 *  @param	idlRd		IdlFile
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private void readIdlAttribute(boolean readonly, TxtTokenReader idlRd)
  throws TxtReadException
 {
  // <param_type_spec>
  TxtTokenRef tRef= new TxtTokenRef();
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readParamType(this, tRef, idlRd, false);
  if(!iType.isCompleteType())
   throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(), iType);
  if(iType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iType);
  // <simple_declarator> { "," <simple_declarator> }*
  do
  { // <identifier>
   token= tRef.getOrReadToken(idlRd);
   IdlAttribute iAttribute= IdlAttribute.readIdlHead(readonly, iType,
    this, token, idlRd);
   // add attribute
   if(idlAttributes == null)
   { idlAttributes= iAttribute;
   }else
   { idlAttributes.addNext(iAttribute);
   }
   String name2= iAttribute.getUnEscName();
   IdlAttribute iAttr1= (IdlAttribute)idlCollAttrs.get(name2);
   if(iAttr1 != null && iAttribute != iAttr1)
   { TxtReadException ex= new TxtReadException(token.getFilePos(),
     "Redefine attribute `" + name2 + "' in a derived valuetype");
    ex.setNextException(new TxtReadException(iAttr1.getFilePos(),
     "Position of the inherit attribute definition"));
    throw ex;
   }
   idlCollAttrs.put(name2, iAttribute);
   idlAttrArr.add(iAttribute);
   token= idlRd.readToken();
  }while(token instanceof TxtTokSepChar
   && ((TxtTokSepChar)token).getChar() == ',');
  idlRd.unreadToken();
 }
 /** Add base valuetype
	 *
	 *  @param	token
	 *  @param	iValueType
	 */
 private void addBaseValueType(TxtToken token, IdlValueType iValueType)
  throws TxtReadException
 {
  if(bAbstract)
  { if(!iValueType.bAbstract)
   { TxtReadException ex= new TxtReadException(token.getFilePos(),
     "An abstract valuetype may only inherit from other"
     + " abstract valuetype");
    ex.setNextException(new TxtReadException(iValueType.getFilePos(),
     "Position of the other valuetype definition"));
    throw ex;
   }
  }
  if(!bCustom && iValueType.bCustom)
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "A non-custom valuetype may not inherit from other"
    + " custom valuetypes");
   ex.setNextException(new TxtReadException(iValueType.getFilePos(),
    "Position of the other valuetype definition"));
   throw ex;
  }
  if(!iValueType.isDefined()) // Prevent loops
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "It is illegal to inherit from a forward-declared "
    + "valuetype whose definition has not yet been seen");
   ex.setNextException(new TxtReadException(iValueType.getFilePos(),
    "Position of the forward declaration"));
   throw ex;
  }
  // Perform more checks
  if(idlValueTypes != null)
  { for(int i= 0; i < idlValueTypes.length; i++)
   { // An valuetype may not be specified as a direct base valuetype
    // more than once.
    if(idlValueTypes[i] == iValueType)
    { throw new TxtReadException(token.getFilePos(),
      "It is illegal to specify a valuetype as a direct "
      + "base valuetype more than once");
    }
   }
   if(!iValueType.bAbstract)
   { throw new TxtReadException(token.getFilePos(),
     "A stateful value must be the first element"
     + " in the inheritance list");
   }
  }else
  { // Check first value in the inheritance list
   if(bTruncatable)
   { if(iValueType.bAbstract)
    { throw new TxtReadException(token.getFilePos(),
      "Expecting non abstract value after `truncatable'");
    }
    if(!iValueType.bTruncatable && iValueType.idlValueTypes != null
     && !iValueType.idlValueTypes[0].bAbstract)
    { throw new TxtReadException(token.getFilePos(),
      "Cannot truncate to the specified value,"
      + " all the intervening types in the inheritance"
      + " hierachy must be truncatable");
    }
   }
  }
  inheritIdlAttributes(token, iValueType.getAttributes());
  inheritIdlOperations(token, iValueType.getOperations());
  inheritIdlStateDcls(token, iValueType.getStateDcls());
  if(idlValueTypes == null)
  { idlValueTypes= new IdlValueType[1];
   idlValueTypes[0]= iValueType;
  }else
  { IdlValueType oldInts[]= idlValueTypes;
   idlValueTypes= new IdlValueType[oldInts.length + 1];
   System.arraycopy(oldInts, 0, idlValueTypes, 0, oldInts.length);
   idlValueTypes[oldInts.length]= iValueType;
  }
 }
 /** Add base interface
	 *
	 *  @param	token
	 *  @param	iInterface
	 */
 private void addBaseInterface(TxtToken token, IdlInterface iInterface)
  throws TxtReadException
 {
  if(!iInterface.isDefined()) // Prevent loops
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "It is illegal to inherit from a forward-declared "
    + "interface whose definition has not yet been seen");
   ex.setNextException(new TxtReadException(iInterface.getFilePos(),
    "Position of the forward declaration"));
   throw ex;
  }
  // Perform more checks
  if(idlInterfaces != null)
  { for(int i= 0; i < idlInterfaces.length; i++)
   { // An interface may not be specified as a direct base interface
    // more than once.
    if(idlInterfaces[i] == iInterface)
    { throw new TxtReadException(token.getFilePos(),
      "It is illegal to specify an interface as a direct "
      + "base interface more than once");
    }
    if(!iInterface.isAbstract() && !idlInterfaces[i].isAbstract())
    { throw new TxtReadException(token.getFilePos(),
      "Values cannot support more than one non-abstract"
      + " interface");
    }
   }
  }
  inheritIdlAttributes(token, iInterface.getAttributes());
  inheritIdlOperations(token, iInterface.getOperations());
  if(idlInterfaces == null)
  { idlInterfaces= new IdlInterface[1];
   idlInterfaces[0]= iInterface;
  }else
  { IdlInterface oldInts[]= idlInterfaces;
   idlInterfaces= new IdlInterface[oldInts.length + 1];
   System.arraycopy(oldInts, 0, idlInterfaces, 0, oldInts.length);
   idlInterfaces[oldInts.length]= iInterface;
  }
 }
 /** Add state member(s)
	 *
	 *  @param	iVSDcls		List of <state_member> declarators
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private void addIdlStateDcls(IdlValStateDcl iVSDcls)
  throws TxtReadException
 {
  if(idlStateDcls == null)
  { idlStateDcls= iVSDcls;
  }else
  { idlStateDcls.addNext(iVSDcls);
  }
  do
  { String name2= iVSDcls.getUnEscName();
   IdlValStateDcl iSt1= (IdlValStateDcl)idlCollSts.get(name2);
   if(iSt1 != null && iVSDcls != iSt1)
   { TxtReadException ex= new TxtReadException(iVSDcls.getFilePos(),
     "Redefine state member `" + name2 + "' in a derived valuetype");
    ex.setNextException(new TxtReadException(iSt1.getFilePos(),
     "Position of the inherit state member definition"));
    throw ex;
   }
   idlCollSts.put(name2, iVSDcls);
   idlStArr.add(iVSDcls);
   iVSDcls= (IdlValStateDcl)iVSDcls.getNext();
  }while(iVSDcls != null);
 }
 /** Add valuetype initializer
	 *
	 *  @param	iValInitOp		Valuetype initializer
	 */
 private void addIdlValInitOp(IdlValInitOp iValInitOp)
 {
  if(idlValInitOps == null)
  { idlValInitOps= iValInitOp;
  }else
  { idlValInitOps.addNext(iValInitOp);
  }
 }
 /** Add operation
	 *
	 *  @param	iOperation	
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private void addIdlOperation(IdlOperation iOperation)
  throws TxtReadException
 {
  if(idlOperations == null)
  { idlOperations= iOperation;
  }else
  { idlOperations.addNext(iOperation);
  }
  String name2= iOperation.getUnEscName();
  IdlOperation iOp1= (IdlOperation)idlCollOps.get(name2);
  if(iOp1 != null && iOperation != iOp1)
  { TxtReadException ex= new TxtReadException(iOperation.getFilePos(),
    "Redefine operation `" + name2 + "' in a derived valuetype");
   ex.setNextException(new TxtReadException(iOp1.getFilePos(),
    "Position of the inherit operation definition"));
   throw ex;
  }
  idlCollOps.put(name2, iOperation);
  idlOpArr.add(iOperation);
 }
 /** Inherit attribute operation(s)
	 *
	 *  @param	token
	 *  @param	iAttrs2	
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private void inheritIdlAttributes(TxtToken token, IdlAttribute iAttrs2[])
  throws TxtReadException
 {
  for(int at= 0; at < iAttrs2.length; at++)
  { IdlAttribute iAttr2= iAttrs2[at];
   String name2= iAttr2.getUnEscName();
   IdlAttribute iAttr1= (IdlAttribute)idlCollAttrs.get(name2);
   if(iAttr1 != null && iAttr2 != iAttr1)
   { TxtReadException ex= new TxtReadException(token.getFilePos(),
     "Inherit two attributes with the same name `" + name2 + "'");
    ex.setNextException(new TxtReadException(iAttr1.getFilePos(),
     "Position of the first attribute definition"));
    ex.setNextException(new TxtReadException(iAttr2.getFilePos(),
     "Position of the second attribute definition"));
    throw ex;
   }
   idlCollAttrs.put(name2, iAttr2);
   idlAttrArr.add(iAttr2);
  }
 }
 /** Inherit operation(s)
	 *
	 *  @param	token
	 *  @param	iOps2	
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private void inheritIdlOperations(TxtToken token, IdlOperation iOps2[])
  throws TxtReadException
 {
  for(int op= 0; op < iOps2.length; op++)
  { IdlOperation iOp2= iOps2[op];
   String name2= iOp2.getUnEscName();
   IdlOperation iOp1= (IdlOperation)idlCollOps.get(name2);
   if(iOp1 != null && iOp2 != iOp1)
   { TxtReadException ex= new TxtReadException(token.getFilePos(),
     "Inherit two operations with the same name `" + name2 + "'");
    ex.setNextException(new TxtReadException(iOp1.getFilePos(),
     "Position of the first operation definition"));
    ex.setNextException(new TxtReadException(iOp2.getFilePos(),
     "Position of the second operation definition"));
    throw ex;
   }
   idlCollOps.put(name2, iOp2);
   idlOpArr.add(iOp2);
  }
 }
 /** Inherit state member(s)
	 *
	 *  @param	token
	 *  @param	iSts2	
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private void inheritIdlStateDcls(TxtToken token, IdlValStateDcl iSts2[])
  throws TxtReadException
 {
  for(int st= 0; st < iSts2.length; st++)
  { IdlValStateDcl iSt2= iSts2[st];
   // Following error message should never come. Reasons:
   // - A stateful value must be the first element in the
   //   inheritance list
   // - Redefine state members in a derived valuetype is an extra
   //   error message
   String name2= iSt2.getUnEscName();
   IdlValStateDcl iSt1= (IdlValStateDcl)idlCollSts.get(name2);
   if(iSt1 != null && iSt2 != iSt1)
   { TxtReadException ex= new TxtReadException(token.getFilePos(),
     "Inherit two states with the same name `" + name2 + "'");
    ex.setNextException(new TxtReadException(iSt1.getFilePos(),
     "Position of the first state member definition"));
    ex.setNextException(new TxtReadException(iSt2.getFilePos(),
     "Position of the second state member definition"));
    throw ex;
   }
   idlCollSts.put(name2, iSt2);
   idlStArr.add(iSt2);
  }
 }
 /** Get all TypeIds
	 *
	 *	@return		sTypeIds, sTypeIds[0] is the most derived TypeId
	 */
 public String[] getTypeIds()
 { if(sTypeIds != null)
   return sTypeIds;
  if(idlValueTypes != null && idlValueTypes.length > 0)
  { Hashtable collTypeIds= new Hashtable();
   for(int bi= 0; bi < idlValueTypes.length; bi++)
   { String sTypeIds[]= idlValueTypes[bi].getTypeIds();
    for(int ti= 0; ti < sTypeIds.length; ti++)
    { // put(, null) --> NullPointerException in JDK HP-UX C.01.17
     collTypeIds.put(sTypeIds[ti], sTypeIds[ti]);
    }
   }
   sTypeIds= new String[1 + collTypeIds.size()];
   Enumeration en= collTypeIds.keys();
   for(int ti= 1; en.hasMoreElements(); ti++)
   { sTypeIds[ti]= (String)en.nextElement();
   }
  }else
  { sTypeIds= new String[1];
  }
  sTypeIds[0]= getTypeId();
  return sTypeIds;
 }
 /** Get all attributes
	 *
	 *	@return		iAllAttrs
	 */
 public IdlAttribute[] getAttributes()
 { if(iAllAttrs != null)
   return iAllAttrs;
  iAllAttrs= new IdlAttribute[idlAttrArr.size()];
  idlAttrArr.copyInto(iAllAttrs);
  /* unordered:
		iAllAttrs= new IdlAttribute[idlCollAttrs.size()];
		Enumeration en= idlCollAttrs.elements();
		for(int at= 0; en.hasMoreElements(); at++)
		{	iAllAttrs[at]= (IdlAttribute)en.nextElement();
		}
		*/
  idlCollAttrs= null;
  idlAttrArr= null;
  return iAllAttrs;
 }
 /** Get all operations
	 *
	 *	@return		iAllOps
	 */
 public IdlOperation[] getOperations()
 { if(iAllOps != null)
   return iAllOps;
  iAllOps= new IdlOperation[idlOpArr.size()];
  idlOpArr.copyInto(iAllOps);
  /* unordered:
		iAllOps= new IdlOperation[idlCollOps.size()];
		Enumeration en= idlCollOps.elements();
		for(int op= 0; en.hasMoreElements(); op++)
		{	iAllOps[op]= (IdlOperation)en.nextElement();
		}
		*/
  idlCollOps= null;
  idlOpArr= null;
  return iAllOps;
 }
 /** Get all state members
	 *
	 *	@return		iAllSts
	 */
 public IdlValStateDcl[] getStateDcls()
 { if(iAllSts != null)
   return iAllSts;
  iAllSts= new IdlValStateDcl[idlStArr.size()];
  idlStArr.copyInto(iAllSts);
  /* unordered:
		iAllSts= new IdlValStateDcl[idlCollSts.size()];
		Enumeration en= idlCollSts.elements();
		for(int st= 0; en.hasMoreElements(); st++)
		{	iAllSts[st]= (IdlValStateDcl)en.nextElement();
		}
		*/
  idlCollSts= null;
  idlStArr= null;
  return iAllSts;
 }
 /** Is an abstract valuetype?
	 *
	 *	@return		bAbstract
	 */
 public boolean isAbstract()
 { return bAbstract;
 }
 /** Is a custom valuetype?
	 *
	 *	@return		bCustom
	 */
 public boolean isCustom()
 { return bCustom;
 }
 /** Is a truncatable valuetype?
	 *
	 *	@return		bTruncatable
	 */
 public boolean isTruncatable()
 { return bTruncatable;
 }
 /** (IdlType)
	 *  Is a (structure or union) type currently under definition?
	 *
	 *	@return		isUnderDefinitionType
	 */
 public boolean isUnderDefinitionType()
 { return false; // a forward declared valuetype is complete enough
      // to be a type
 }
 /** (IdlType)
	 *  Is a complete type (e.g. to be a member of structure or union)?
	 *
	 *	@return		isCompleteType
	 */
 public boolean isCompleteType()
 { return true; // a forward declared valuetype is complete enough
      // to be a type
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
   /*???
			for(IdlDeclarator iDcl= idlMembers; iDcl != null;
				iDcl= (IdlDeclarator)iDcl.getNext())
			{	if(iDcl.getIdlType().isLocalType())
				{	bLocal= true;
					break;
				}
			}
			???*/
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
 {
  return super.setVbName(sVbName);
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
   + VbWriter.OIN + ".readValue(" + VbWriter.NEW + " "
   + getVbName(true) + ")");
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
  out.writeLine(VbWriter.CALL + " " + VbWriter.OOUT
   + ".writeValue(" + vbVariable + ")");
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
  out.writeLine(VbWriter.OANY + "." + "getVal" + "()");
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
  out.writeLine(VbWriter.CALL + " " + VbWriter.OANY + "."
   + "insertVal" + "(" + vbVariable + ")");
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
    System.out.println("D Valuetype " + getScopedName()
     + " is included only");
   return;
  }
  // Only forward declaration?
  if(!isDefined())
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Valuetype " + getScopedName()
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
  // Writing VB value type factory class
  IdlValueBase.writeFactoryCls(getScopedName().toString(),
   getFilePos().getFileName(), getVbName(true), opts.vbPath);
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
   System.out.println("I Writing valuetype " + getScopedName()
    + " to `" + sVbModName + ".bas'");
  VbModWriter vbM= new VbModWriter(vbPath, sVbModName,
   getFilePos().getFileName());
  // First of all write constants, later it is not allowed.
  // Write TypeId constant declaration
  vbM.writeLine();
  this.writeConstScopeTypeId(vbM, IdlSpecification.VALUETYPE,
   /* isPublic */ true);
  /* (IdlContainer)
		*/
  this.writeVbModConsts(vbM);
  /* (IdlContainer)
		*/
  this.writeVbModHelpers(vbM);
  /*** ???
		vbM.writeLine();
		vbM.writeLine("'Helper");
		vbM.writeFuncHead(VbWriter.PUBLIC, VbWriter.NARROW);
		vbM.writeFuncArg(VbWriter.BYVAL, VbWriter.OBJREF, VbWriter.CORBOBJREF,
			null);
		vbM.writeFuncBody(getVbName(true), false);
		String newVbObjRefName= "o" + getVbName(false);
		vbM.writeDimLine(newVbObjRefName, getVbName(true));
		vbM.writeLine(VbWriter.SET + VbWriter.SP + newVbObjRefName
			+ " = " + VbWriter.NEW + VbWriter.SP + getVbName(true));
		vbM.writeLine(VbWriter.SET + VbWriter.SP + newVbObjRefName
			+ " = " + newVbObjRefName + "." + VbWriter.NARROW + "("
			+ VbWriter.OBJREF + ")");
		vbM.writeLine(VbWriter.SET + VbWriter.SP + VbWriter.NARROW
			+ " = " + newVbObjRefName);
		vbM.writeFuncTail(null, null);
		****/
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
   + ", 29) 'mCB.tk_value");
  vbM.writeIf("oRecTC" + VbWriter.SP + VbWriter.IS
   + VbWriter.SP + VbWriter.NOTHING);
  vbM.writeThen();
  vbM.writeLine("'Create place holder for TypeCode to avoid endless recursion");
  vbM.writeAssign("oRecTC", true);
  vbM.writeLine("oOrb.createRecursiveTc(" + VbWriter.TYPEID + ")");
  vbM.writeOnErrorLine("ErrRollback");
  vbM.writeLine("'Describe value members");
  vbM.writeDimLine("oMemSeq", "cCBValueMemberSeq");
  vbM.writeAssign("oMemSeq", true);
  vbM.writeLine(VbWriter.NEW + VbWriter.SP + "cCBValueMemberSeq");
  vbM.writeAssign("oMemSeq.Length", false);
  getStateDcls();
  vbM.writeLine(Integer.toString(this.iAllSts.length));
  for(int iLen= 0; iLen < this.iAllSts.length; iLen++)
  { IdlValStateDcl def= this.iAllSts[iLen];
   String sItem= "oMemSeq.Item(" + Integer.toString(iLen) + ")";
   vbM.writeAssign(sItem, true);
   vbM.writeLine(VbWriter.NEW + VbWriter.SP + "cCBValueMember");
   vbM.writeAssign(sItem + ".name", false);
   vbM.writeLine("\"" + def.getUnEscName() + "\"");
   //vbM.writeAssign(sItem + ".id", false);
   //vbM.writeLine("\"" + def.getTypeId() + "\"");
   //vbM.writeAssign(sItem + ".defined_in", false);
   //vbM.writeLine("\"\" '???");
   //vbM.writeAssign(sItem + ".version", false);
   //vbM.writeLine("\"\" '???");
   def.getIdlType().writeVbAssignTypeCode(vbM, sItem + ".p_type");
   vbM.writeAssign(sItem + ".access", false);
   if(!def.isPublic())
    vbM.writeLine("0 'PRIVATE_MEMBER");
   else
    vbM.writeLine("1 'PUBLIC_MEMBER");
  }
  vbM.writeLine("'Overwrite place holder");
  vbM.writeDimLine("iTypeModifier", VbWriter.INTEGER);
  vbM.writeAssign("iTypeModifier", false);
  if(this.bCustom)
   vbM.writeLine("1 'VM_CUSTOM");
  else if(this.bAbstract)
   vbM.writeLine("2 'VM_ABSTRACT");
  else if(this.bTruncatable)
   vbM.writeLine("3 'VM_TRUNCATABLE");
  else
   vbM.writeLine("0 'VM_NONE");
  vbM.writeDimLine("oConcreteBase", VbWriter.CORBTYPECODE);
  if(this.idlValueTypes == null)
  { vbM.writeAssign("oConcreteBase", true);
   vbM.writeLine(VbWriter.NOTHING);
  }else
  { idlValueTypes[0].writeVbAssignTypeCode(vbM, "oConcreteBase");
  }
  vbM.writeLine(VbWriter.CALL + VbWriter.SP
   + "oRecTC.setRecTc2ValueTc(\"" + getUnEscName()
   + "\", iTypeModifier, oConcreteBase, oMemSeq)");
  vbM.writeEndIf();
  vbM.writeAssign(VbWriter.TYPECODE, true);
  vbM.writeLine("oRecTC");
  vbM.writeLine(VbWriter.EXIT + VbWriter.SP + VbWriter.FUNCTION);
  vbM.writeLabelLine("ErrRollback");
  vbM.writeLine(VbWriter.CALL + VbWriter.SP + "oRecTC.destroy");
  vbM.writeLabelLine(VbWriter.ERRHANDLER);
  vbM.writeErrReraiseLine(VbWriter.TYPECODE);
  vbM.writeFuncTail(null, null);
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
  // Write VB valuetype class
  String vbWriteName;
  boolean bSimpleValue= IDL2VB.bSimpleValues && !isCustom();
  if(bSimpleValue)
  { vbWriteName= getVbName(true);
   if(MainOptions.iLogLvl >= 4)
    System.out.println("I Writing valuetype " + getScopedName()
     + " as simple to `" + vbWriteName + ".cls'");
  }else
  { vbWriteName= getVbName(true) + "Example";
   if(MainOptions.iLogLvl >= 4)
    System.out.println("I Writing valuetype " + getScopedName()
     + " as example to `" + vbWriteName + ".cls'");
  }
  VbClsWriter vbC= new VbClsWriter(vbPath, vbWriteName,
   getFilePos().getFileName());
  if(!bSimpleValue)
  { vbC.writeLine();
   vbC.writeLine("'You can use this class as a value example:");
   vbC.writeLine("' Rename class to " + getVbName(true)
    + " and save the file as " + getVbName(true) + ".cls.");
   vbC.writeLine("' Finally write your own local value operations.");
  }
  /* Implements cOrbAbstractBase, cOrbValueBase
		*/
  vbC.writeLine();
  vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP
   + VbWriter.CORBABSTRACTBASE);
  vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP
   + VbWriter.CORBVALUEBASE);
  /* Implements direct base valuetypes
		*/
  if(idlValueTypes != null)
  { for(int i= 0; i < idlValueTypes.length; i++)
   { IdlValueType iValueType= idlValueTypes[i];
    vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP
     + iValueType.getVbName(true));
   }
  }
  /* Implements [abstract] interfaces
		*/
  if(idlInterfaces != null)
  { for(int i= 0; i < idlInterfaces.length; i++)
   { IdlInterface iInterface= idlInterfaces[i];
    vbC.writeLine(VbWriter.IMPLEMENTS + VbWriter.SP
     + iInterface.getVbName(true));
   }
  }
  // Write TypeId constant declaration
  vbC.writeLine();
  this.writeConstScopeTypeId(vbC, IdlSpecification.VALUETYPE,
   /* isPublic */ false);
  getStateDcls();
  if(iAllSts.length != 0)
  { vbC.writeLine();
   vbC.writeLine("'State member(s)");
   for(int st= 0; st < iAllSts.length; st++)
   { IdlValStateDcl iStateDcl= iAllSts[st];
    iStateDcl.writeVbPrivateStateVarDecl(vbC);
   }
  }
  // Implements cOrbAbstractBase
  vbC.writeLine();
  vbC.writeFuncThisObject(VbWriter.FRIEND,
   "OrbAbstractBase", VbWriter.CORBABSTRACTBASE);
  // cOrbAbstractBase_isObjRef()
  vbC.writeAbstractBaseIsObjRef(false);
  // Implements cOrbValueBase
  vbC.writeLine();
  vbC.writeFuncThisObject(VbWriter.FRIEND,
   "OrbValueBase", VbWriter.CORBVALUEBASE);
  String vbInterface= VbWriter.CORBVALUEBASE;
  vbC.writeLine();
  vbC.writeFuncHead(VbWriter.PRIVATE, vbInterface + "_UniqueId");
  vbC.writeFuncBody(VbWriter.LONG, false);
  vbC.writeLine(vbInterface + "_UniqueId = " + IDL2VB.getVbOrbDLL()
   + ".getNextUniqueId()");
  vbC.writeFuncTail(null, null);
  // Helper, initByRead()
  vbC.writeInitByReadHead(VbWriter.CORBVALUEBASE, false);
  getStateDcls();
  for(int st= 0; st < iAllSts.length; st++)
  { IdlValStateDcl iStateDcl= iAllSts[st];
   iStateDcl.writeVbPrivateVarRead(vbC);
  }
  vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".read", null);
  // Helper, writeMe()
  vbC.writeWriteMeHead(VbWriter.CORBVALUEBASE);
  getStateDcls();
  for(int st= 0; st < iAllSts.length; st++)
  { IdlValStateDcl iStateDcl= iAllSts[st];
   iStateDcl.writeVbPrivateVarWrite(vbC);
  }
  vbC.writeExitEnd(VbWriter.SUB, getUnEscName() + ".write", null);
  // getIds()
  final String ITEM= "Item";
  String sTypeIds[]= getTypeIds();
  vbC.writeLine();
  vbC.writeFuncHead(VbWriter.PRIVATE, vbInterface + "_getIds");
  vbC.writeFuncArg(VbWriter.BYVAL, ITEM, VbWriter.INTEGER, null);
  vbC.writeFuncBody(VbWriter.STRING, false);
  vbC.writeLine(VbWriter.SELECT + VbWriter.SP + VbWriter.CASE
   + VbWriter.SP + ITEM);
  for(int ti= 0; ti < sTypeIds.length; ti++)
  { String sTypeId;
   if(ti == 0)
   { sTypeId= VbWriter.STYPEID;
   }else
   { sTypeId= "\"" + sTypeIds[ti] + "\"";
   }
   vbC.writeLine(VbWriter.CASE + VbWriter.SP + ti + ": "
    + vbInterface + "_" + "getIds" + " = " + sTypeId);
  }
  vbC.writeLine(VbWriter.END + VbWriter.SP + VbWriter.SELECT);
  vbC.writeFuncTail(null, null);
  // isCustom()
  vbC.writeLine();
  vbC.writeFuncHead(VbWriter.PRIVATE, vbInterface + "_isCustom");
  vbC.writeFuncBody(VbWriter.BOOLEAN, false);
  vbC.writeLine(vbInterface + "_isCustom = "
   + (isCustom()? VbWriter.TRUE: VbWriter.FALSE));
  vbC.writeFuncTail(null, null);
  /* State member(s)
		*/
  getStateDcls();
  if(iAllSts.length != 0)
  { for(int st= 0; st < iAllSts.length; st++)
   { IdlValStateDcl iStateDcl= iAllSts[st];
    vbC.writeLine();
    vbC.writeLine("'State member: " + iStateDcl.getUnEscName());
    iStateDcl.writeVbStatePropFuncs(vbC,
     /*forcePublic*/bSimpleValue, /*vbInterface*/null);
   }
  }
  /* Implements direct base valuetypes
		*/
  if(idlValueTypes != null)
  { for(int i= 0; i < idlValueTypes.length; i++)
   { IdlValueType iValueType= idlValueTypes[i];
    /* State member(s)
				*/
    iValueType.getStateDcls();
    if(iValueType.iAllSts.length != 0)
    { for(int st= 0; st < iValueType.iAllSts.length; st++)
     { IdlValStateDcl iStateDcl= iValueType.iAllSts[st];
      vbC.writeLine();
      vbC.writeLine("'State member: " + iStateDcl.getUnEscName());
      iStateDcl.writeVbStatePropFuncs(vbC,
       /*forcePublic*/bSimpleValue,
       /*vbInterface*/iValueType.getVbName(true));
     }
    }
   }
  }
  /* Implements [abstract] interfaces
		*/
  if(idlInterfaces != null)
  { for(int i= 0; i < idlInterfaces.length; i++)
   { IdlInterface iInterface= idlInterfaces[i];
    vbC.writeLine();
    vbC.writeFuncThisObject(VbWriter.FRIEND,
     iInterface.getVbName(false),
     iInterface.getVbName(true));
    IdlAttribute[] iAllAttrs= iInterface.getAttributes();
    for(int at= 0; at < iAllAttrs.length; at++)
    { IdlAttribute iAttr= iAllAttrs[at];
     iAttr.writeVbValImpl(iInterface.getVbName(true), vbC);
    }
    IdlOperation[] iAllOps= iInterface.getOperations();
    for(int op= 0; op < iAllOps.length; op++)
    { IdlOperation iOp= iAllOps[op];
     iOp.writeVbValImpl(iInterface.getVbName(true), vbC);
    }
   }
  }
  // Helper, insert, Any???
  // Helper, extract, Any???
  for(IdlValInitOp iValInitOp= idlValInitOps; iValInitOp != null;
   iValInitOp= (IdlValInitOp)iValInitOp.getNext())
  { iValInitOp.writeVbImplExample(null, vbC);
  }
  getAttributes();
  for(int at= 0; at < iAllAttrs.length; at++)
  { IdlAttribute iAttr= iAllAttrs[at];
   iAttr.writeVbImplExample(null, vbC);
  }
  getOperations();
  for(int op= 0; op < iAllOps.length; op++)
  { IdlOperation iOp= iAllOps[op];
   iOp.writeVbImplExample(null, vbC);
  }
  vbC.close();
 }
}
