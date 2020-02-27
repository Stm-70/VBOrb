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
import mboth.util.*;
/**
 * @author  Martin Both
 *
 * Direct Known Subclasses:
 *     IdlModule, IdlInterface, IdlStruct, IdlUnion,
 *     IdlException, IdlValueType
 * Indirect Known Subclasses:
 *     IdlSpecification, IdlObjRefType
 */
public abstract class IdlContainer extends IdlScope
 implements IdlDefinition
{
 /** Contained IdlDefinitions
	 */
 public IdlTypedef idlTypedefs;
 public IdlStruct idlStructs;
 public IdlUnion idlUnions;
 public IdlEnum idlEnums;
 public IdlConst idlConsts;
 public IdlException idlExceptions;
 /**
	 *  @param	identifier	Identifier
	 */
 public IdlContainer(IdlIdentifier identifier)
 {
  super(identifier);
 }
 /** (IdlDefinition)
	 *  Get an IDL name to identify the IDL definition uniquely
	 *  for a specific language mapping.
	 *
	 *  @return		e.g. "<sequence ::T>"
	 */
 public String getIdlName()
 { return getScopedName().toString();
 }
 /** Add typedef
	 *
	 *  @param	iTypedef
	 */
 public void addIdlTypedef(IdlTypedef iTypedef)
 {
  if(idlTypedefs == null)
  { idlTypedefs= iTypedef;
  }else
  { idlTypedefs.addNext(iTypedef);
  }
 }
 /** Add typedefs
	 *
	 *  @param	iTypedefs
	 */
 public void addIdlTypedefs(IdlTypedef iTypedefs)
 {
  if(idlTypedefs == null)
  { idlTypedefs= iTypedefs;
  }else
  { idlTypedefs.addNext(iTypedefs);
  }
 }
 /** Put struct
	 *
	 *  @param	iStruct	
	 */
 public void putIdlStruct(IdlStruct iStruct)
 {
  if(idlStructs == null)
  { idlStructs= iStruct;
  }else
  { idlStructs.putNext(iStruct);
  }
 }
 /** Put union
	 *
	 *  @param	iUnion	
	 */
 public void putIdlUnion(IdlUnion iUnion)
 {
  if(idlUnions == null)
  { idlUnions= iUnion;
  }else
  { idlUnions.putNext(iUnion);
  }
 }
 /** Put enum
	 *
	 *  @param	iEnum
	 */
 public void putIdlEnum(IdlEnum iEnum)
 {
  if(idlEnums == null)
  { idlEnums= iEnum;
  }else
  { idlEnums.putNext(iEnum);
  }
 }
 /** Add constant
	 *
	 *  @param	iConst
	 */
 public void addIdlConst(IdlConst iConst)
 {
  if(idlConsts == null)
  { idlConsts= iConst;
  }else
  { idlConsts.addNext(iConst);
  }
 }
 /** Add exception
	 *
	 *  @param	iException
	 */
 public void addIdlException(IdlException iException)
 {
  if(idlExceptions == null)
  { idlExceptions= iException;
  }else
  { idlExceptions.addNext(iException);
  }
 }
 /** VB name (without general prefix) or null if unset
	 */
 private String sVbName;
 /** (IdlDefinition)
	 *  Set the VB Name
	 *
	 *	@param	VbName		Can be "" if not IdlType
	 *	@return		== null, It was not too late to set
	 *				== VbName, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 public String setVbName(String VbName)
 {
  if(this.sVbName != null)
   return this.sVbName;
  this.sVbName= VbName;
  return null;
 }
 /** (IdlDefinition)
	 *  Get the Visual Basic Name to identify the definition
	 *
	 *	@param	withPrefix	With final prefix? The name without prefix
	 *  					is used to build complex names.
	 *	@return		Visual Basic Name
	 */
 public String getVbName(boolean withPrefix)
 {
  if(this.sVbName == null)
  { IdlContainer surContainer= this.getIdlContainer();
   if(surContainer == null)
    this.sVbName= ""; // IdlSpecification
   else
    this.sVbName= surContainer.getVbName(false)
     + getUnEscName();
  }
  if(withPrefix)
   return this.getVbClsPrefix() + this.sVbName;
  return this.sVbName;
 }
 /** Get the Visual Basic Module Name of this IDL container definition
	 *
	 *	@return		Visual Basic Module Name
	 */
 public String getVbModName()
 { return getVbModPrefix() + this.getVbName(false);
 }
 /** Write TypeId constant declaration
	 *  Const sTypeId As String = "..."
	 *  @param	out
	 *  @param	name		"interface", "struct", ...
	 *  @param	isPublic	"Public Const TypeId ..." or "Const sTypeId ..."
	 *
	 *	@exception	IOException
	 */
 public void writeConstScopeTypeId(VbWriter out, String name,
  boolean isPublic) throws java.io.IOException
 {
  out.writeLine("'" + name + " " + getScopedName().toString());
  out.writeConstTypeId(isPublic, getTypeId());
 }
 /**
	 *  @param	opts.vbPath		Prefix
	 *  @param	opts.srvout		Write additional server skeleton examples
	 *
	 *	@exception	IOException	
	 */
 public void writeVbFiles(MainOptions opts) throws java.io.IOException
 {
  // idlTypedefs
  for(IdlStruct def= idlStructs; def != null;
   def= (IdlStruct)def.getNext())
  { def.writeVbFiles(opts);
  }
  for(IdlUnion def= idlUnions; def != null;
   def= (IdlUnion)def.getNext())
  { def.writeVbFiles(opts);
  }
  // idlEnums
  // idlConsts
  for(IdlException def= idlExceptions; def != null;
   def= (IdlException)def.getNext())
  { def.writeVbFiles(opts);
  }
 }
 /**
	 *  @param	out		VbModWriter
	 *
	 *	@exception	IOException	
	 */
 public void writeVbModConsts(VbModWriter vbM) throws java.io.IOException
 {
  if(idlConsts != null)
  { vbM.writeLine();
   vbM.writeLine("'Constants");
  }
  for(IdlConst def= idlConsts; def != null;
   def= (IdlConst)def.getNext())
  { def.writeVbModDefs(vbM);
  }
  for(IdlEnum def= idlEnums; def != null;
   def= (IdlEnum)def.getNext())
  { def.writeVbModConsts(vbM);
  }
 }
 /**
	 *  @param	out		VbModWriter
	 *
	 *	@exception	IOException	
	 */
 public void writeVbModHelpers(VbModWriter vbM) throws java.io.IOException
 {
  for(IdlEnum def= idlEnums; def != null;
   def= (IdlEnum)def.getNext())
  { def.writeVbModHelpers(vbM);
  }
 }
}
