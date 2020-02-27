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
/** Array
 *
 *  @author  Martin Both
 */
public class IdlArray implements IdlType
{
 /** File position of the "["
	 */
 private TxtFilePos filePos;
 /** File include level
	 */
 private int iFileIncludeLvl;
 /** <type_spec>
	 */
 private IdlType idlType;
 /** <fixed_array_size>
	 */
 private int arrSize;
 /** The first definition of this kind of Array (including size)
	 */
 IdlArray prevArr;
 /** Read an Array type definition if "[]" exists or return iType
	 *
	 *  @param	iType		<type_spec>
	 *  @param	surScope	
	 *  @param	tRef		Next token
	 *  @param	idlRd		IdlFile
	 *	@return				iArray or iType if only a simple declarator
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readIdlType(IdlType iType, IdlScope surScope,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  // <fixed_array_size>*
  // <fixed_array_size> ::= "[" <positive_int_const> "]"
  TxtToken token= tRef.getOrReadToken(idlRd);
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '[')
  {
   tRef.ungetToken(token);
   return iType;
  }
  if(iType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iType);
  // <positive_int_const>
  IdlConstType iConstType= IdlPositiveInt.readIdlPositiveInt(
   surScope, token.getFilePos());
  IdlConstValue iConstValue= iConstType.readIdlConstValue(
   surScope, tRef, idlRd);
  // "]"
  token= tRef.getOrReadToken(idlRd);
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ']')
  { throw new TxtReadException(token.getFilePos(),
    "\"]\" of array declaration expected");
  }
  IdlArray iArray= new IdlArray(iConstType.getFilePos(),
   iConstType.getFileIncludeLvl());
  iArray.idlType= IdlArray.readIdlType(iType, surScope, tRef, idlRd);
  iArray.arrSize= (int)iConstValue.getLong().longValue();
  // Register only the first definition of this kind of Array
  // as a new IDL definition.
  //
  IdlSpecification idlSpec= surScope.getIdlSpecification();
  Object obj= idlSpec.getPrevIdlDef(iArray);
  if(obj == null)
  { // new array definition
   idlSpec.registerIdlDef(iArray);
  }else
  { iArray.prevArr= (IdlArray)obj;
  }
  return iArray;
 }
 /**
	 *  @param	filePos		File position of the identifier
	 */
 private IdlArray(TxtFilePos filePos, int iFileIncludeLvl)
 { this.filePos= filePos;
  this.iFileIncludeLvl= iFileIncludeLvl;
 }
 /** Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @param		uppName
	 *  @return		<Array ::T>
	 */
 private String getIdlName(String uppName)
 { String iName= uppName + "[" + arrSize + "]";
  if(idlType instanceof IdlArray)
  { IdlArray subArray= (IdlArray)idlType;
   return subArray.getIdlName(iName);
  }
  return idlType.getIdlName() + iName;
 }
 /** (IdlType:IdlDefinition)
	 *  Get the filename and the start position of the definition
	 *
	 *  @return		Filename and position
	 */
 public TxtFilePos getFilePos()
 { return filePos;
 }
 /** (IdlType:IdlDefinition)
	 *  Get the file include level of the definition.
	 *
	 *  @return		0 = defined in main file
	 */
 public int getFileIncludeLvl()
 {
  return iFileIncludeLvl;
 }
 /** (IdlType:IdlDefinition)
	 *  Get the short definition name (without scope and leading `_')
	 *  This name is used by some messages.
	 *
	 *  @return		Short definition name
	 */
 public String getUnEscName()
 { StringBuffer sBuf= new StringBuffer();
  if(idlType != null)
  { sBuf.append(idlType.getUnEscName());
  }
  sBuf.append('[');
  sBuf.append(arrSize);
  sBuf.append(']');
  return sBuf.toString();
 }
 /** (IdlType:IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<Array ::T>
	 */
 public String getIdlName()
 { return getIdlName(" ");
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
 { return true;
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
 { if(prevArr != null)
   return prevArr.setCName(CName);
  if(this.sCName != null)
   return this.sCName;
  this.sCName= CName;
  return null;
 }
 /** (IdlDefinition)
	 *  Get the C Name to identify the definition
	 *
	 *	@param	withPrefix	With final prefix? The name without prefix is used
	 *  				to build complex names.
	 *	@return		C Name
	 */
 public String getCName(boolean withPrefix)
 {
  if(prevArr != null)
   return prevArr.getCName(withPrefix);
  if(this.sCName == null)
  { sCName= CWriter.ARRAY + "_" + idlType.getCName(false)
    + "_" + Integer.toString(arrSize);
  }
  if(withPrefix)
   return CWriter.CORBA_ + this.sCName;
  return this.sCName;
 }
 /**
	 *  @param	cPath		Prefix
	 *
	 *	@exception	IOException	
	 */
 public void writeC(String cPath) throws java.io.IOException
 {
  // (getScopedName() is not the ScopedName of the IdlType)
  System.out.println("Writing " + getIdlName() + " to `"
   + getCName(true) + ".c'");
  //CWriter out= new CWriter(cPath, getCName(true),
  //	getFilePos().getFileName());
 }
}
