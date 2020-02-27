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
 /**
	 *  @param	cPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 public void writeC(String cPath) throws IOException
 {
  System.out.println("Writing interface " + getScopedName() + " to `"
   + getCName(true) + ".c'");
  for(IdlAttribute def= idlAttributes; def != null;
   def= (IdlAttribute)def.getNext())
  { def.writeC(/*out*/);
  }
  for(IdlOperation def= idlOperations; def != null;
   def= (IdlOperation)def.getNext())
  { def.writeC(/*out*/);
  }
/*???		for(IdlException def= idlExceptions; def != null;
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
		{	def.writeVbMod(vbPath);
		}
*/
 }
 /** Write additional server skeleton examples
	 *
	 *  @param	cPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 public void writeCImpl(String cPath) throws IOException
 {
  String cImplName= getCName(true) + "ImplExample";
  System.out.println("Writing interface " + getScopedName() + " to `"
   + cImplName + ".c'");
 }
}
