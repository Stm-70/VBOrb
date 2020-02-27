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
 *
 *  @author  Martin Both
 */
public class IdlValueBox extends IdlIdentifier implements IdlType
{
 /** <type_spec> excepting valuetype
	 */
 private IdlType idlType;
 /** Is a (boxedRMI sequence) Java-RMI/IDL array definition?
	 *	Or is a (boxedIDL) Java-RMI/IDL IDL entity type definition?
	 */
 private static final int NoJavaVBoxType= -1;
 private static final int JavaArrayType= 1;
 private static final int JavaIDLEntityType= 2;
 private int iJavaVBoxType;
 /** Read declarator of value_box <value_box_dcl>
	 *
	 *  @param	surContainer
	 *  @param	iWord		new <identifier>
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iValueBox (never null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlValueBox readIdlValueBox(IdlContainer surContainer,
  IdlIdentifier iWord, TxtTokenRef tRef, TxtTokenReader idlRd)
  throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readTypeSpec(surContainer, tRef, idlRd);
  // Anonymous type is allowed here
  IdlType oType= iType.getOriginIdlType();
  if(oType instanceof IdlValueType || oType instanceof IdlValueBox)
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Any IDL type may be used to declare a value box"
    + " except for a valuetype");
   ex.setNextException(new TxtReadException(iType.getFilePos(),
    "Position of the given type definition"));
   throw ex;
  }
  IdlValueBox iValueBox= new IdlValueBox(iWord, iType);
  surContainer.putIdentifier(iValueBox, true);
  // (Maybe tell sequence that it is a boxed JavaArray)
  // (Maybe tell IDL entity that it is a boxed IDL entity)
  if(iValueBox.getJavaVBoxType() == NoJavaVBoxType)
  { // Register this value_box as a new IDL definition
   //
   surContainer.getIdlSpecification().registerIdlDef(iValueBox);
  }
  return iValueBox;
 }
 /** 
	 *  @param	iWord		Identifier
	 *  @param	iType	
	 */
 protected IdlValueBox(IdlIdentifier iWord, IdlType iType)
 {
  super(iWord);
  this.idlType= iType;
 }
 /** Is a Java-RMI/IDL value box definition?
	 *
	 *	@return		JavaVBoxType
	 */
 public int getJavaVBoxType()
 { if(iJavaVBoxType != 0)
   return iJavaVBoxType;
  String sIdlName= this.getIdlName();
  if(sIdlName.startsWith("::org::omg::boxed"))
  { if(sIdlName.startsWith("::org::omg::boxedRMI::"))
   { if(idlType.getOriginIdlType() instanceof IdlSequence)
    { // Tell sequence that it is a boxed JavaArray
     ((IdlSequence)idlType.getOriginIdlType()).setJavaArray(this);
     iJavaVBoxType= JavaArrayType;
    }else
    { iJavaVBoxType= NoJavaVBoxType;
    }
   }else if(sIdlName.startsWith("::org::omg::boxedIDL::"))
   { if(idlType.getOriginIdlType() instanceof IdlStruct)
    { // Tell entity that it is a boxed Java IDL entity
     ((IdlStruct)idlType.getOriginIdlType()).setIDLEntityBox(this);
    }
    // valuetype _Any any;
    // #pragma ID _Any "RMI:org.omg.CORBA.Any:0000000000000000"
    // valuetype _TypeCode ::CORBA::TypeCode;
    // #pragma ID _TypeCode "RMI:org.omg.CORBA.TypeCode:0000000000000000"
    iJavaVBoxType= JavaIDLEntityType;
   }else
   { iJavaVBoxType= NoJavaVBoxType;
   }
  }else
  { iJavaVBoxType= NoJavaVBoxType;
  }
  return iJavaVBoxType;
 }
 /** Is a Java-RMI/IDL definition?
	 *
	 *	@return		isJavaVBoxType
	 */
 public boolean isJavaVBoxType()
 { return this.getJavaVBoxType() != NoJavaVBoxType;
 }
 /** Is a Java-RMI/IDL array definition?
	 *
	 *	@return		isBoxedRMIArray
	 */
 public boolean isBoxedRMIArray()
 { return this.getJavaVBoxType() == JavaArrayType;
 }
 /** Is a Java-RMI/IDL IDL entity type definition?
	 *
	 *	@return		isBoxedIDLEntity
	 */
 public boolean isBoxedIDLEntity()
 { return this.getJavaVBoxType() == JavaIDLEntityType;
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
 { return idlType.isCompleteType() || idlType.isUnderDefinitionType();
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
 { return this;
 }
 /** C name (without general prefix) or null if unset
	 */
 private String sCName;
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
}
