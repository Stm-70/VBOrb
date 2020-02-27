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
import java.util.Hashtable;
import java.io.IOException;
import mboth.util.*;
/** A list of IdlDefinitions
 *  The module construct is used to scope OMG IDL identifiers
 *
 *  @author  Martin Both
 */
public class IdlModule extends IdlContainer
{
 /** Additional Contained IdlDefinitions
	 */
 private IdlModule idlModules;
 private IdlInterface idlInterfaces;
 private IdlValueBox idlValueBoxes;
 private IdlValueType idlValueTypes;
 /** 
	 *  @param	identifier	Identifier
	 */
 public IdlModule(IdlIdentifier identifier)
 {
  super(identifier);
 }
 /** (It's written like IdlInterface.readExport())
	 *  @param	idlRd		IdlFile
	 *	@return				Unused token or null
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public TxtToken readIdlDefinition(TxtTokenReader idlRd)
  throws TxtReadException
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
  }else if(keyword == IdlSpecification.ABSTRACT)
  { TxtToken token2= idlRd.readToken();
   String keyword2= IdlSpecification.readKeyword(token2, true);
   if(keyword2 == IdlSpecification.INTERFACE)
   { IdlInterface iInterface= IdlInterface.readIdlHead(this,
     idlRd.readToken(), idlRd);
    putIdlInterface(iInterface);
    // Read definition if not ';'
    token= iInterface.readIdl(idlRd);
   }else if(keyword2 == IdlSpecification.VALUETYPE)
   { TxtTokenRef tRef= new TxtTokenRef();
    IdlValueType iValueType= (IdlValueType)IdlValueType.readIdlHead(
     this, true, false, tRef, idlRd);
    putIdlValueType(iValueType);
    // Read definition if not ';'
    iValueType.readIdl(tRef, idlRd);
    token= tRef.getOrReadToken(idlRd);
   }else
   { idlRd.unreadToken();
    return token;
   }
  }else if(keyword == IdlSpecification.LOCAL)
  { TxtToken token2= idlRd.readToken();
   String keyword2= IdlSpecification.readKeyword(token2, true);
   if(keyword2 == IdlSpecification.INTERFACE)
   { IdlInterface iInterface= IdlObjRefType.readIdlHead(this,
     true, idlRd.readToken(), idlRd);
    putIdlInterface(iInterface);
    // Read definition if not ';'
    token= iInterface.readIdl(idlRd);
   }else
   { idlRd.unreadToken();
    return token;
   }
  }else if(keyword == IdlSpecification.MODULE)
  { token= readIdlModule(idlRd);
  }else if(keyword == IdlSpecification.INTERFACE)
  { IdlInterface iInterface= IdlObjRefType.readIdlHead(this,
    false, idlRd.readToken(), idlRd);
   putIdlInterface(iInterface);
   // Read definition if not ';'
   token= iInterface.readIdl(idlRd);
  }else if(keyword == IdlSpecification.CUSTOM)
  { TxtToken token2= idlRd.readToken();
   String keyword2= IdlSpecification.readKeyword(token2, true);
   if(keyword2 == IdlSpecification.VALUETYPE)
   { TxtTokenRef tRef= new TxtTokenRef();
    IdlValueType iValueType= (IdlValueType)IdlValueType.readIdlHead(
     this, false, true, tRef, idlRd);
    putIdlValueType(iValueType);
    // Read definition if not ';'
    iValueType.readIdl(tRef, idlRd);
    token= tRef.getOrReadToken(idlRd);
   }else
   { idlRd.unreadToken();
    return token;
   }
  }else if(keyword == IdlSpecification.VALUETYPE)
  { TxtTokenRef tRef= new TxtTokenRef();
   IdlType iType= IdlValueType.readIdlHead(this, false, false,
    tRef, idlRd);
   if(iType instanceof IdlValueType)
   { IdlValueType iValueType= (IdlValueType)iType;
    putIdlValueType(iValueType);
    // Read definition if not ';'
    iValueType.readIdl(tRef, idlRd);
   }else
   { IdlValueBox iValueBox= (IdlValueBox)iType;
    putIdlValueBox(iValueBox);
   }
   token= tRef.getOrReadToken(idlRd);
  }else
  { return token;
  }
  // ";"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ';')
  { throw new TxtReadException(token.getFilePos(),
    "\";\" expected");
  }
  return null;
 }
 /** Read a module definition.
	 *  Subsequent occurences of a module declaration with the same
	 *  identifier within the same scope reopens the module and hence
	 *  its scope, allowing additional definitions to be added to it.
	 *
	 *  @param	idlRd		IdlFile
	 *	@return				Unused token or null
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public TxtToken readIdlModule(TxtTokenReader idlRd) throws TxtReadException
 {
  IdlModule iModule;
  // <identifier>
  TxtToken token= idlRd.readToken();
  IdlIdentifier iWord= IdlIdentifier.readIdlWord(this, token);
  IdlIdentifier identifier= getIdentifier(iWord,
   IdlScope.SL_DEFN | IdlScope.SL_INTR);
  if(identifier != null)
  { if(!(identifier instanceof IdlModule))
   { TxtReadException ex= new TxtReadException(iWord.getFilePos(),
     "IDL identifier `" + identifier.getUnEscName()
     + "' redefined after use");
    ex.setNextException(new TxtReadException(identifier.getFilePos(),
     "Position of the first identifier definition"));
    throw ex;
   }
   // Reopen module
   iModule= (IdlModule)identifier;
   // Always use the first pragma settings
   // iModule.checkPragmaPrefix(iWord);
  }else
  { iModule= new IdlModule(iWord);
   putIdentifier(iModule, true);
   // The name of an interface, value type, struct, union, exception
   // or a module may not be redefined within the immediate scope of
   // the interface, value type, struct, union, exception, or the
   // module. That's why we introduce the identifier now.
   iModule.putIdentifier(iModule, false);
   this.addIdlModule(iModule);
  }
  // readModuleDefinitions()
  // "{"
  token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '{')
  { throw new TxtReadException(token.getFilePos(),
    "\"{\" of module body expected");
  }
  getIdlSpecification().setPragmaScope(iModule);
  // <definition>+
  if((token= iModule.readIdlDefinition(idlRd)) != null)
  { throw new TxtReadException(token.getFilePos(),
    "Unknown IDL definition");
  }
  while((token= iModule.readIdlDefinition(idlRd)) == null)
   ;
  // "}"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '}')
  { throw new TxtReadException(token.getFilePos(),
    "Module `" + iModule.getUnEscName()
    + "': Next IDL definition or \"}\" expected");
  }
  getIdlSpecification().setPragmaScope(this);
  return idlRd.readToken();
 }
 /** Add module
	 *
	 *  @param	iModule
	 */
 private void addIdlModule(IdlModule iModule)
 {
  if(idlModules == null)
  { idlModules= iModule;
  }else
  { idlModules.addNext(iModule);
  }
 }
 /** Put interface
	 *
	 *  @param	iInterface
	 */
 private void putIdlInterface(IdlInterface iInterface)
 {
  if(idlInterfaces == null)
  { idlInterfaces= iInterface;
  }else
  { idlInterfaces.putNext(iInterface);
  }
 }
 /** Put value box type
	 *
	 *  @param	iValueBox	
	 */
 private void putIdlValueBox(IdlValueBox iValueBox)
 {
  if(idlValueBoxes == null)
  { idlValueBoxes= iValueBox;
  }else
  { idlValueBoxes.putNext(iValueBox);
  }
 }
 /** Put value type
	 *
	 *  @param	iValueType	
	 */
 private void putIdlValueType(IdlValueType iValueType)
 {
  if(idlValueTypes == null)
  { idlValueTypes= iValueType;
  }else
  { idlValueTypes.putNext(iValueType);
  }
 }
 /**
	 *  @param	opts.out_path	Prefix
	 *  @param	opts.srv_out	Write additional server skeleton examples
	 *
	 *	@exception	IOException	
	 */
 public void writeCFiles(MainOptions opts) throws java.io.IOException
 {
  /** Write contained IdlDefinitions
		 */
//???		super.writeCFiles(opts);
  for(IdlModule def= idlModules; def != null;
   def= (IdlModule)def.getNext())
  { def.writeCFiles(opts);
  }
  //if(!(this instanceof IdlSpecification))
  if(getSurScope() == null)
  {
  }else
  { if(MainOptions.iLogLvl >= 4)
    System.out.println("I Writing module " + getScopedName());
  }
  for(IdlInterface iInterface= idlInterfaces; iInterface != null;
   iInterface= (IdlInterface)iInterface.getNext())
  { iInterface.writeC(opts.out_path);
   if(opts.srv_out)
    iInterface.writeCImpl(opts.out_path);
  }
/***???		for(IdlException def= idlExceptions; def != null;
			def= (IdlException)def.getNext())
		{	def.writeVb(vbPath);
		}
		for(IdlStruct def= idlStructs; def != null;
			def= (IdlStruct)def.getNext())
		{	def.writeVb(vbPath);
		}
		for(IdlUnion def= idlUnions; def != null;
			def= (IdlUnion)def.getNext())
		{	def.writeVbCls(vbPath);
		}
		for(IdlEnum def= idlEnums; def != null;
			def= (IdlEnum)def.getNext())
		{	def.writeVbCls(vbPath);
		}
***???*/
 }
}
