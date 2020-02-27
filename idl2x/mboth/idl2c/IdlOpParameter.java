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
import java.io.IOException;
import mboth.util.*;
/** 
 *  @author  Martin Both
 */
public class IdlOpParameter extends IdlIdentifier
{
 /** A parameter declaration must have a directional attribute that informs
	 *  the communication service in both the client and the serer of the
	 *  direction in which the parameter is to be passed.
	 */
 private boolean aIn, aOut;
 /** <param_type_spec>
	 */
 private IdlType idlType;
 /** Read an operation parameter
	 *
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iOpParameter (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlOpParameter readIdlHead(IdlScope idlScope,
  TxtToken token, TxtTokenReader idlRd) throws TxtReadException
 {
  // <param_attribute>
  boolean aIn, aOut;
  String keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.IN)
  { aIn= true; aOut= false;
  }else if(keyword == IdlSpecification.OUT)
  { aIn= false; aOut= true;
  }else if(keyword == IdlSpecification.INOUT)
  { aIn= true; aOut= true;
  }else
  { throw new TxtReadException(token.getFilePos(),
    "`in', `out' or `inout' expected");
  }
  // <param_type_spec>
  TxtTokenRef tRef= new TxtTokenRef();
  token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readParamType(idlScope, tRef, idlRd, false);
  if(!iType.isCompleteType())
   throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(), iType);
  if(iType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iType);
  // <simple_declarator> ::= <identifier>
  token= tRef.getOrReadToken(idlRd);
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(idlScope, token);
  IdlOpParameter iOpParameter= new IdlOpParameter(iWord, aIn, aOut, iType);
  idlScope.putIdentifier(iOpParameter, true);
  return iOpParameter;
 }
 /** 
	 *  @param	identifier		Identifier
	 *  @param	aIn	
	 *  @param	aOut
	 *  @param	idlType	
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public IdlOpParameter(IdlIdentifier identifier, boolean aIn, boolean aOut,
  IdlType idlType)
 {
  super(identifier.getSurScope(), identifier.getEscName(),
   identifier.getFilePos());
  this.aIn= aIn;
  this.aOut= aOut;
  this.idlType= idlType;
 }
 /** 
	 *  @return			
	 */
 public boolean isIn()
 { return aIn;
 }
 /** 
	 *  @return			
	 */
 public boolean isOut()
 { return aOut;
 }
 /** Get the IdlType to DIM and work with a VB variable
	 *
	 *  @return			IdlType of the parameter
	 */
 public IdlType getIdlType()
 { return idlType;
 }
 /** C parameter
	 *  @return				_...
	 */
 public String getCParName()
 {
  String parName= getUnEscName();
  if(CWriter.hashResWord(parName) == null)
   return parName;
  else
   return "_" + parName;
 }
 /** 
	 *  @param	out		
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeCPar(/* CWriter out*/) //throws java.io.IOException
 {
  System.out.print(idlType.getCName(true));
  System.out.print(aOut? " *": " ");
  System.out.println(getCParName());
 }
}
