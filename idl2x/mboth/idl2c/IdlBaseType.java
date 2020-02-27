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
public class IdlBaseType
{
 /** Read a <base_type_spec>.
	 *
	 *  @param	idlScope	
	 *  @param	keyword
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		Return null or exception
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readBaseType(IdlScope idlScope, String keyword,
  TxtTokenRef tRef, TxtTokenReader idlRd, boolean test)
  throws TxtReadException
 {
  if(keyword == IdlSpecification.FLOAT)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlDouble.readIdlFloat(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.DOUBLE)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlDouble.readIdlDouble(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.LONG)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   TxtToken token2= idlRd.readToken();
   String keyword2= IdlSpecification.readKeyword(token2, true);
   if(keyword2 == IdlSpecification.DOUBLE)
   { return IdlDouble.readIdlLongDouble(idlScope, token.getFilePos());
   }else if(keyword2 == IdlSpecification.LONG)
   { return IdlLongLong.readIdlLongLong(idlScope, token.getFilePos());
   }else
   { idlRd.unreadToken();
    return IdlLong.readIdlLong(idlScope, token.getFilePos());
   }
  }else if(keyword == IdlSpecification.SHORT)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlShort.readIdlShort(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.UNSIGNED)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   TxtToken token2= idlRd.readToken();
   String keyword2= IdlSpecification.readKeyword(token2, true);
   if(keyword2 == IdlSpecification.SHORT)
   { return IdlShort.readIdlUnsignedShort(idlScope, token.getFilePos());
   }else if(keyword2 == IdlSpecification.LONG)
   { token2= idlRd.readToken();
    keyword2= IdlSpecification.readKeyword(token2, true);
    if(keyword2 == IdlSpecification.LONG)
    { return IdlLongLong.readIdlUnsignedLongLong(idlScope,
      token.getFilePos());
    }else
    { idlRd.unreadToken();
     return IdlLong.readIdlUnsignedLong(idlScope,
      token.getFilePos());
    }
   }else
   { throw new TxtReadException(token2.getFilePos(),
     "`short', `long' or `long long' expected");
   }
  }else if(keyword == IdlSpecification.CHAR)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlChar.readIdlChar(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.WCHAR)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlChar.readIdlWChar(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.BOOLEAN)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlBoolean.readIdlBoolean(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.OCTET)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlOctet.readIdlOctet(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.ANY)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlAny.readIdlAny(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.OBJECT)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlObject.readIdlObject(idlScope, token.getFilePos());
  }else if(keyword == IdlSpecification.VALUEBASE)
  { TxtToken token= tRef.getOrReadToken(idlRd);
   return IdlValueBase.readIdlValueBase(idlScope, token.getFilePos());
  }else
  { if(test)
    return null;
   TxtToken token= tRef.getOrReadToken(idlRd);
   throw new TxtReadException(token.getFilePos(),
    "<base_type_spec> expected");
  }
 }
 /** Read a <param_type_spec>.
	 *
	 *  @param	idlScope	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		return type/null/exception or type/exception
	 *	@return				idlType (not null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readParamType(IdlScope idlScope, TxtTokenRef tRef,
  TxtTokenReader idlRd, boolean test) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  String keyword= IdlSpecification.readKeyword(token, true);
  tRef.ungetToken(token);
  IdlType idlType= readBaseType(idlScope, keyword, tRef, idlRd, true);
  if(idlType != null)
  { return idlType;
  }
  idlType= IdlString.readIdlString(idlScope, keyword, tRef, idlRd, true);
  if(idlType != null)
  { return idlType;
  }
  // (12) <scoped_name>
  IdlIdentifier identifier= idlScope.readScopedName(tRef, idlRd, false, true);
  if(identifier == null)
  { if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "Parameter type specification expected");
  }
  if(!(identifier instanceof IdlType))
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Scoped name of an IDL type expected");
   ex.setNextException(new TxtReadException(identifier.getFilePos(),
    "Position where the last identifier "
    + "of the given scoped name is defined"));
   throw ex;
  }
  //if(!test)
  //{	if!((IdlType)identifier).isParamType) test???
  //	{	TxtReadException ex= new TxtReadException(token.getFilePos(),
  //			"Parameter type specification expected");
  //		ex.setNextException(new TxtReadException(identifier.getFilePos(),
  //			"Position of the given type definition"));
  //		throw ex;
  //	}
  //}
  return (IdlType)identifier;
 }
 /** Read a <op_type_spec>.
	 *
	 *  @param	idlScope	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		return type/null/exception or type/exception
	 *	@return				idlType (not null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readOpType(IdlScope idlScope, TxtTokenRef tRef,
  TxtTokenReader idlRd, boolean test) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  String keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.VOID)
  { return IdlVoid.readIdlVoid(idlScope, token.getFilePos());
  }
  tRef.ungetToken(token);
  IdlType idlType= readParamType(idlScope, tRef, idlRd, true);
  if(idlType == null)
  { if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "Operation type specification expected");
  }
  //if!((IdlType)identifier).isParamType) struct, test???
  //{	TxtReadException ex= new TxtReadException(token.getFilePos(),
  //		"Operation type specification expected");
  //	ex.setNextException(new TxtReadException(identifier.getFilePos(),
  //		"Position of the given type definition"));
  //	throw ex;
  //}
  return idlType;
 }
 /** Read a <const_type>.
	 *
	 *  @param	idlScope	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *	@return				idlType or exception
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readConstType(IdlScope idlScope,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  String keyword= IdlSpecification.readKeyword(token, true);
  tRef.ungetToken(token);
  IdlType iType= readBaseType(idlScope, keyword, tRef, idlRd, true);
  if(iType != null)
  { if(iType.getOriginIdlType() instanceof IdlConstType)
    return iType;
   throw new TxtReadException(token.getFilePos(),
    "Constant type specification expected");
  }
  iType= IdlString.readIdlString(idlScope, keyword, tRef, idlRd, true);
  if(iType != null)
  { return iType;
  }
  iType= IdlFixed.readIdlFixedConst(idlScope, keyword, tRef, idlRd, true);
  if(iType != null)
  { return iType;
  }
  // (12) <scoped_name>
  IdlIdentifier identifier= idlScope.readScopedName(tRef, idlRd, false,
   true);
  if(identifier == null)
  { throw new TxtReadException(token.getFilePos(),
    "Constant type specification expected");
  }
  if(identifier instanceof IdlType &&
   ((IdlType)identifier).getOriginIdlType() instanceof IdlConstType)
   return (IdlType)identifier;
  TxtReadException ex= new TxtReadException(token.getFilePos(),
   "Constant type specification expected");
  ex.setNextException(new TxtReadException(identifier.getFilePos(),
   "Position where the last identifier "
   + "of the given scoped name is defined"));
  throw ex;
 }
 /** Read a <switch_type_spec>.
	 *
	 *  @param	iContainer	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *	@return				idlType or exception
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readSwitchType(IdlContainer iContainer,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  String keyword= IdlSpecification.readKeyword(token, true);
  // enum
  if(keyword == IdlSpecification.ENUM)
  { IdlEnum iEnum= IdlEnum.readIdlEnum(iContainer,
    tRef.getOrReadToken(idlRd));
   iContainer.putIdlEnum(iEnum);
   token= iEnum.readIdl(idlRd);
   tRef.ungetToken(token);
   return iEnum;
  }
  tRef.ungetToken(token);
  // long, long long, short,
  // unsigned long unsigned long long, unsigned short,
  // char, wchar, boolean
  IdlType iType= readBaseType(iContainer, keyword, tRef, idlRd, true);
  if(iType != null)
  { if(iType.getOriginIdlType() instanceof IdlSwitchType)
    return iType;
   throw new TxtReadException(token.getFilePos(),
    "Switch type specification expected");
  }
  // (12) <scoped_name>
  IdlIdentifier identifier= iContainer.readScopedName(tRef, idlRd, false,
   true);
  if(identifier == null)
  { throw new TxtReadException(token.getFilePos(),
    "Switch type specification expected");
  }
  if(identifier instanceof IdlType &&
   ((IdlType)identifier).getOriginIdlType() instanceof IdlSwitchType)
   return (IdlType)identifier;
  TxtReadException ex= new TxtReadException(token.getFilePos(),
   "Switch type specification expected");
  ex.setNextException(new TxtReadException(identifier.getFilePos(),
   "Position where the last identifier "
   + "of the given scoped name is defined"));
  throw ex;
 }
 /** Read a <simple_type_spec>.
	 *
	 *  @param	idlScope	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *	@return				idlType (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readSimpleType(IdlScope idlScope, TxtTokenRef tRef,
  TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  String keyword= IdlSpecification.readKeyword(token, true);
  tRef.ungetToken(token);
  // <simple_type_spec> ::= <base_type_spec> | <template_type_spec>
  //                       | <scoped_name>
  IdlType idlType= readBaseType(idlScope, keyword, tRef, idlRd, true);
  if(idlType != null)
  { return idlType;
  }
  // <template_type_spec> ::= <sequence_type> | <string_type>
  //		| <wide_string_type> | <fixed_pt_type>
  idlType= IdlSequence.readIdlSequence(idlScope, keyword, tRef, idlRd,
   true);
  if(idlType != null)
  { return idlType;
  }
  idlType= IdlString.readIdlString(idlScope, keyword, tRef, idlRd, true);
  if(idlType != null)
  { return idlType;
  }
  idlType= IdlFixed.readIdlFixed(idlScope, keyword, tRef, idlRd, true);
  if(idlType != null)
  { return idlType;
  }
  // (12) <scoped_name>
  IdlIdentifier identifier= idlScope.readScopedName(tRef, idlRd, false, true);
  if(identifier == null)
  { throw new TxtReadException(token.getFilePos(),
    "Type specification expected");
  }
  if(!(identifier instanceof IdlType))
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Scoped name of an IDL type expected");
   ex.setNextException(new TxtReadException(identifier.getFilePos(),
    "Position where the last identifier "
    + "of the given scoped name is defined"));
   throw ex;
  }
  // Any kind of type is possible here
  return (IdlType)identifier;
 }
 /** Read a <type_spec>.
	 *
	 *  @param	iContainer	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *	@return				idlType (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlType readTypeSpec(IdlContainer iContainer, TxtTokenRef tRef,
  TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  // <type_spec> ::= <constr_type_spec> | <simple_type_spec>
  // <constr_type_spec> ::= <struct_type> | <union_type> | <enum_type>
  //
  String keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.STRUCT)
  { IdlStruct iStruct= IdlStruct.readIdlHead(iContainer,
    tRef.getOrReadToken(idlRd), idlRd);
   iContainer.putIdlStruct(iStruct);
   token= iStruct.readIdl(idlRd);
   tRef.ungetToken(token);
   return iStruct;
  }else if(keyword == IdlSpecification.UNION)
  { IdlUnion iUnion= IdlUnion.readIdlHead(iContainer,
    tRef.getOrReadToken(idlRd), idlRd);
   token= iUnion.readIdl(idlRd);
   iContainer.putIdlUnion(iUnion);
   tRef.ungetToken(token);
   return iUnion;
  }else if(keyword == IdlSpecification.ENUM)
  { IdlEnum iEnum= IdlEnum.readIdlEnum(iContainer,
    tRef.getOrReadToken(idlRd));
   iContainer.putIdlEnum(iEnum);
   token= iEnum.readIdl(idlRd);
   tRef.ungetToken(token);
   return iEnum;
  }else
  { tRef.ungetToken(token);
   return readSimpleType(iContainer, tRef, idlRd);
  }
 }
 /** Build an incomplete type exception
	 *  if(!iType.isCompleteType())
	 *      throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(), iType);
	 *
	 *  @param	iType	
	 *	@return				Exception
	 */
 public static TxtReadException buildIncompleteTypeEx(TxtFilePos filePos,
  IdlType iType)
 {
  IdlType uType= iType.getIncompleteType();
  TxtReadException ex;
  String exMsg= "Type `" + iType.getUnEscName()
   + "' is incomplete";
  if(iType != uType)
   exMsg += ", definition of `" + uType.getUnEscName()
    + "' not known";
  ex= new TxtReadException(filePos, exMsg);
  ex.setNextException(new TxtReadException(iType.getFilePos(),
     "Position of the incomplete type definition"));
  return ex;
 }
}
