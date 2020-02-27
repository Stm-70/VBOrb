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
/** An abstract interface is either a valuetype supporting that interface
 *  or a reference to an object supporting that interface.
 *  An abstract interface name is an IdlType (AbstractBase).
 *
 *  @author  Martin Both
 */
public class IdlInterface extends IdlContainer implements IdlType
{
 /** Not only forward declared?
	 *  It is illegal to inherit from a forward-declared interface
	 *  whose definition has not yet been seen.
	 */
 private TxtFilePos defFilePos;
 private boolean bDefined;
 /** Direct base interfaces or null
	 */
 protected IdlInterface baseInterfaces[];
 /** Collection of all TypeIds, sTypeIds[0] is the most derived TypeId
	 */
 protected String sTypeIds[];
 /** Additional Contained IdlDefinitions
	 *  in <interface_body>
	 */
 protected IdlAttribute idlAttributes;
 protected IdlOperation idlOperations;
 /** Collect all interface attributes (including inherit attributes)
	 */
 private Hashtable idlCollAttrs;
 private ArrayList idlAttrArr;
 protected IdlAttribute iAllAttrs[];
 /** Collect all interface operations (including inherit operations)
	 */
 private Hashtable idlCollOps;
 private ArrayList idlOpArr;
 protected IdlOperation iAllOps[];
 /** Read an interface head.
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iInterface (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlInterface readIdlHead(IdlScope idlScope,
  TxtToken token, TxtTokenReader idlRd) throws TxtReadException
 {
  IdlInterface iInterface;
  // <identifier>
  IdlIdentifier iWord= IdlIdentifier.readIdlWord(idlScope, token);
  IdlIdentifier identifier= idlScope.getIdentifier(iWord,
   IdlScope.SL_DEFN | IdlScope.SL_INTR);
  if(identifier != null)
  { if(!(identifier instanceof IdlInterface))
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "IDL identifier `" + identifier.getUnEscName()
     + "' redefined after use");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the first identifier definition"));
    throw ex;
   }
   // forward declared or already declared
   iInterface= (IdlInterface)identifier;
   if(!iInterface.isAbstract() || iInterface.isLocal())
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "Different abstract or local modifiers for interface `"
     + iInterface.getUnEscName() + "'");
    ex.setNextException(new TxtReadException(iInterface.getFilePos(),
     "Position of previous declaration"));
    throw ex;
   }
   iInterface.checkPragmaPrefix(iWord);
  }else
  { iInterface= new IdlInterface(iWord);
   idlScope.putIdentifier(iInterface, true);
   // The name of an interface, valuetype, struct, union, exception
   // or a module may not be redefined within the immediate scope of
   // the interface, valuetype, struct, union, exception, or the
   // module. That's why we introduce the identifier now.
   iInterface.putIdentifier(iInterface, false);
  }
  return iInterface;
 }
 /** 
	 *  @param	identifier	Identifier
	 */
 protected IdlInterface(IdlIdentifier identifier)
 {
  super(identifier);
  idlCollAttrs= new Hashtable();
  idlAttrArr= new ArrayList();
  idlCollOps= new Hashtable();
  idlOpArr= new ArrayList();
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
  // 2. Search inherited scope, base interfaces
  if((sls & IdlScope.SL_INHT) != 0 && baseInterfaces != null)
  { for(int i= 0; i < baseInterfaces.length; i++)
   { IdlIdentifier identifier2=
     baseInterfaces[i].getIdentifier(iWord,
      IdlScope.SL_DEFN | SL_INHT);
    if(identifier2 == null)
     continue;
    if(identifier == null)
     identifier= identifier2;
    else if(identifier != identifier2)
    { // ambiguous, if the name is declared as a constant,
     // type or exception in more than one base interface.
     TxtReadException ex= new TxtReadException(iWord.getFilePos(),
      "IDL identifier `" + identifier.getUnEscName()
      + "' is ambiguous, declared in more than one"
      + " base interface. Please qualify"
      + " with the right interface name");
     ex.setNextException(new TxtReadException(identifier.getFilePos(),
      "Position of the first identifier definition"));
     ex.setNextException(new TxtReadException(identifier2.getFilePos(),
      "Position of the second identifier definition"));
     throw ex;
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
 /** Read an interface definition if not ';'.
	 *
	 *  @param	idlRd		IdlFile
	 *	@return				Unused token
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public TxtToken readIdl(TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= idlRd.readToken();
  // <forward_dcl>?
  if(token instanceof TxtTokSepChar &&
   ((TxtTokSepChar)token).getChar() == ';')
  { return token;
  }
  if(bDefined)
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Redefinition of interface `" + getUnEscName() + "'");
   ex.setNextException(new TxtReadException(defFilePos,
    "Position of previous definition"));
   throw ex;
  }
  defFilePos= token.getFilePos();
  // <interface_header>
  // [ <interface_inheritance_spec> ]
  // Collect all base interface TypeIDs
  if(token instanceof TxtTokSepChar &&
   ((TxtTokSepChar)token).getChar() == ':')
  { // <interface_inheritance_spec>
   // <interface_name> {"," <interface_name> }*
   TxtTokenRef tRef= new TxtTokenRef();
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
  // "{" <interface_body> "}"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '{')
  { throw new TxtReadException(token.getFilePos(),
    "\"{\" of interface body expected");
  }
  getIdlSpecification().setPragmaScope(this);
  // <interface_body>
  while((token= readExport(idlRd)) == null)
   ;
  // "}"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '}')
  { throw new TxtReadException(token.getFilePos(),
    "Interface `" + this.getUnEscName()
    + "': Next export definition or \"}\" expected");
  }
  bDefined= true;
  // Leaving the scope of this interface
  //
  IdlSpecification iSpecification= getIdlSpecification();
  iSpecification.setPragmaScope(getSurScope());
  // Register this interface as a new IDL definition
  //
  iSpecification.registerIdlDef(this);
  return idlRd.readToken();
 }
 /** (It's written like IdlModule.readIdlDefinition())
	 *  @param	idlRd		IdlFile
	 *	@return				Unused token or null
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 private TxtToken readExport(TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= idlRd.readToken();
  String keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.TYPEDEF)
  { // <type_declarator> ::= <type_spec> <declarators>
   // <type_spec>
   TxtTokenRef tRef= new TxtTokenRef();
   IdlType iType= IdlBaseType.readTypeSpec(this, tRef, idlRd);
   // Anonymous type is allowed here
   do
   { // <identifier>
    IdlTypedef iTypedef= IdlTypedef.readIdlTypedef(iType,
     this, tRef, idlRd);
    // Add typedef
    this.addIdlTypedef(iTypedef);
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
  }else if(keyword == IdlSpecification.STRUCT)
  { IdlStruct iStruct= IdlStruct.readIdlHead(this, idlRd.readToken(),
    idlRd);
   putIdlStruct(iStruct);
   token= iStruct.readIdl(idlRd);
  }else if(keyword == IdlSpecification.UNION)
  { IdlUnion iUnion= IdlUnion.readIdlHead(this, idlRd.readToken(),
    idlRd);
   putIdlUnion(iUnion);
   token= iUnion.readIdl(idlRd);
  }else if(keyword == IdlSpecification.ENUM)
  { IdlEnum iEnum= IdlEnum.readIdlEnum(this, idlRd.readToken());
   putIdlEnum(iEnum);
   token= iEnum.readIdl(idlRd);
  }else if(keyword == IdlSpecification.NATIVE)
  { IdlNative iNative= IdlNative.readIdlNative(this, idlRd.readToken());
   token= idlRd.readToken();
  }else if(keyword == IdlSpecification.CONST)
  { TxtTokenRef tRef= new TxtTokenRef();
   IdlConst iConst= IdlConst.readIdlConst(this, tRef, idlRd);
   this.addIdlConst(iConst);
   token= tRef.getOrReadToken(idlRd);
  }else if(keyword == IdlSpecification.EXCEPTION)
  { IdlException iException= IdlException.readIdlHead(this,
    idlRd.readToken(), idlRd);
   addIdlException(iException);
   iException.readIdl(idlRd);
   token= idlRd.readToken();
  }else if(keyword == IdlSpecification.READONLY)
  { TxtToken token2= idlRd.readToken();
   String keyword2= IdlSpecification.readKeyword(token2, true);
   if(keyword2 == IdlSpecification.ATTRIBUTE)
   { readIdlAttribute(true, idlRd);
    token= idlRd.readToken();
   }else
   { idlRd.unreadToken();
    return token;
   }
  }else if(keyword == IdlSpecification.ATTRIBUTE)
  { readIdlAttribute(false, idlRd);
   token= idlRd.readToken();
  }else if(keyword == IdlSpecification.ONEWAY)
  { TxtTokenRef tRef= new TxtTokenRef();
   IdlOperation iOperation= IdlOperation.readIdlHead(this, true,
    tRef, idlRd, false);
   addIdlOperation(iOperation);
   iOperation.readIdl(tRef, idlRd);
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
   token= tRef.getOrReadToken(idlRd);
  }
  // ";"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ';')
  { throw new TxtReadException(token.getFilePos(),
    "\";\" expected");
  }
  return null;
 }
 /** Read attributes.
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
     "Redefine attribute `" + name2 + "' in a derived interface");
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
 /** Add base interface
	 *
	 *  @param	token
	 *  @param	iInterface
	 */
 private void addBaseInterface(TxtToken token, IdlInterface iInterface)
  throws TxtReadException
 {
  if(isAbstract())
  { if(!iInterface.isAbstract())
   { TxtReadException ex= new TxtReadException(token.getFilePos(),
     "An abstract interface may only inherit from other"
     + " abstract interfaces");
    ex.setNextException(new TxtReadException(iInterface.getFilePos(),
     "Position of the other interface definition"));
    throw ex;
   }
  }
  if(!iInterface.isDefined()) // Prevent loops
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "It is illegal to inherit from a forward-declared "
    + "interface whose definition has not yet been seen");
   ex.setNextException(new TxtReadException(iInterface.getFilePos(),
    "Position of the forward declaration"));
   throw ex;
  }
  // Perform more checks
  if(baseInterfaces != null)
  { for(int i= 0; i < baseInterfaces.length; i++)
   { // An interface may not be specified as a direct base interface
    // more than once.
    if(baseInterfaces[i] == iInterface)
    { throw new TxtReadException(token.getFilePos(),
      "It is illegal to specify an interface as a direct "
      + "base interface more than once");
    }
   }
  }
  inheritIdlAttributes(token, iInterface.getAttributes());
  inheritIdlOperations(token, iInterface.getOperations());
  if(baseInterfaces == null)
  { baseInterfaces= new IdlInterface[1];
   baseInterfaces[0]= iInterface;
  }else
  { IdlInterface oldInts[]= baseInterfaces;
   baseInterfaces= new IdlInterface[oldInts.length + 1];
   System.arraycopy(oldInts, 0, baseInterfaces, 0, oldInts.length);
   baseInterfaces[oldInts.length]= iInterface;
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
    "Redefine operation `" + name2 + "' in a derived interface");
   ex.setNextException(new TxtReadException(iOp1.getFilePos(),
    "Position of the inherit operation definition"));
   throw ex;
  }
  idlCollOps.put(name2, iOperation);
  idlOpArr.add(iOperation);
 }
 /** Inherit attribute(s)
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
 /** Get all TypeIds
	 *
	 *	@return		sTypeIds, sTypeIds[0] is the most derived TypeId
	 */
 public String[] getTypeIds()
 { if(sTypeIds != null)
   return sTypeIds;
  if(baseInterfaces != null && baseInterfaces.length > 0)
  { Hashtable collTypeIds= new Hashtable();
   for(int bi= 0; bi < baseInterfaces.length; bi++)
   { String sTypeIds[]= baseInterfaces[bi].getTypeIds();
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
 /** Is an abstract interface?
	 *
	 *	@return		bAbstract
	 */
 public boolean isAbstract()
 { return true;
 }
 /** Is a local interface?
	 *
	 *	@return		Always false for an abstract interface
	 */
 public boolean isLocal()
 { return false;
 }
 /** (IdlType)
	 *  Is a (structure or union) type currently under definition?
	 *
	 *	@return		isUnderDefinitionType
	 */
 public boolean isUnderDefinitionType()
 { return false; // a forward declared interface is complete enough
      // to be a type
 }
 /** (IdlType)
	 *  Is a complete type (e.g. to be a member of structure or union)?
	 *
	 *	@return		isCompleteType
	 */
 public boolean isCompleteType()
 { return true; // a forward declared interface is complete enough
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
 { return false;
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
  out.write(VbWriter.SET + " " + vbVariable + " = "
   + getVbModName() + "." + VbWriter.NARROW + "(");
  out.writeLine(VbWriter.OIN + ".readAbstract(" + VbWriter.NOTHING + "))");
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
   + ".writeAbstract(" + vbVariable + ")");
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
  out.writeLine("oOrb.createAbstractInterfaceTc(\"" + getTypeId()
   + "\", \"" + getUnEscName() + "\")");
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
  out.writeLine(VbWriter.OANY + ".getAbstract()");
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
  out.writeLine(VbWriter.CALL + " " + VbWriter.OANY
   + ".insertAbstract(" + vbVariable + ")");
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
    System.out.println("D Interface " + getScopedName()
     + " is included only");
   return;
  }
  // Only forward declaration?
  if(!isDefined())
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Interface " + getScopedName()
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
  // Write VB class
  if(opts.srvout)
   this.writeVbImplCls(opts.vbPath);
  for(IdlException def= idlExceptions; def != null;
   def= (IdlException)def.getNext())
  { def.writeVbFiles(opts);
  }
  /* Is done in IdlSpecification
		for(IdlStruct def= idlStructs; def != null;
			def= (IdlStruct)def.getNext())
		{	def.writeVbFiles(opts);
		}
		*/
  /* Is done in IdlSpecification
		for(IdlUnion def= idlUnions; def != null;
			def= (IdlUnion)def.getNext())
		{	def.writeVbFiles(opts);
		}
		*/
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
   System.out.println("I Writing abstract interface "
    + getScopedName() + " to `" + sVbModName + ".bas'");
  VbModWriter vbM= new VbModWriter(vbPath, sVbModName,
   getFilePos().getFileName());
  // First of all write constants, later it is not allowed.
  // Write TypeId constant declaration
  vbM.writeLine();
  this.writeConstScopeTypeId(vbM,
   IdlSpecification.INTERFACE + " " + IdlSpecification.ABSTRACT,
   /* isPublic */ true);
  /* (IdlContainer)
		*/
  this.writeVbModConsts(vbM);
  /* (IdlContainer)
		*/
  this.writeVbModHelpers(vbM);
  vbM.writeLine();
  vbM.writeLine("'Helper");
  vbM.writeAbsNarrow(true, getVbName(true));
  vbM.writeLine();
  vbM.writeLine("'Helper");
  vbM.writeAbsNarrow(false, getVbName(true));
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
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing abstract interface "
    + getScopedName() + " to `" + getVbName(true) + ".cls'");
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
 protected void writeVbImplCls(String vbPath) throws java.io.IOException
 {
  // No skeletons for abstract interfaces
 }
}
