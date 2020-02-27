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
 /** C name (without general prefix) or null if unset
	 */
 protected String sCName;
 /** (IdlDefinition)
	 *  Set the C Name
	 *
	 *	@param	CName		Can be "" if not IdlType
	 *	@return		== null, It was not too late to set
	 *				== CName, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 public String setCName(String CName)
 { if(this.sCName != null)
   return this.sCName;
  this.sCName= CName;
  return null;
 }
 /** (IdlDefinition)
	 *  Get the C Name to identify the definition
	 *
	 *	@param	withPrefix	With final prefix? The name without prefix
	 *  					is used to build complex names.
	 *	@return		C Name
	 */
 public String getCName(boolean withPrefix)
 { if(this.sCName == null)
  { this.sCName= this.getIdlContainer().getCName(false);
   if(this.sCName.length() == 0)
    this.sCName= this.getUnEscName();
   else
    this.sCName += "_" + getUnEscName();
   //???if(CWriter.hashResWord(sVbName) != null)
  }
  return this.sCName;
 }
 /**
	 *  @param	opts.out_path	Prefix
	 *  @param	opts.srv_out	Write additional server skeleton examples
	 *
	 *	@exception	IOException	
	 */
/*???	public void writeCFiles(MainOptions opts) throws java.io.IOException
	{	
		// idlTypedefs
		for(IdlStruct def= idlStructs; def != null;
			def= (IdlStruct)def.getNext())
		{	def.writeVbFiles(opts);
		}
		for(IdlUnion def= idlUnions; def != null;
			def= (IdlUnion)def.getNext())
		{	def.writeVbFiles(opts);
		}
		// idlEnums
		// idlConsts
		for(IdlException def= idlExceptions; def != null;
			def= (IdlException)def.getNext())
		{	def.writeVbFiles(opts);
		}
	}
*/
}
