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
import java.io.IOException;
import mboth.util.*;
/** 
 *
 *  @author  Martin Both
 */
public interface IdlType extends IdlDefinition
{
 /** Is a (structure or union) type currently under definition?
	 *
	 *	@return		isUnderDefinitionType
	 */
 public boolean isUnderDefinitionType();
 /** Is a complete type (e.g. to be a member of structure or union)?
	 *  A forward declared struct or union is an incomplete type.
	 *  A struct or union under definition is an incomplete type.
	 *  A sequence of a struct or union under definition is a complete type.
	 *  A forward declared interface is complete enough to be a complete type.
	 *  struct Node						// struct or union
	 *  {	Node nextNode;				// Is not allowed (incomplete type)
	 *		sequence<Node, 1> nextNode;	// Is allowed
	 *  };
	 *  interface Node
	 *  {	Node nextNode();			// Is allowed
	 *  };
	 *
	 *	@return		isCompleteType
	 */
 public boolean isCompleteType();
 /** Get the incomplete type (e.g. member of a sequence).
	 *
	 *	@return		Incomplete type or null
	 */
 public IdlType getIncompleteType();
 /** Is an anonymous type?
	 *  (An other design would be to implement an IdlAnonymous interface)
	 *	@return		isAnonymousType
	 */
 public boolean isAnonymousType();
 /** Is a local type?
	 *  (Is a local interface or has a local interface member.)
	 *	@return		isLocalType
	 */
 public boolean isLocalType();
 /** Get the origin type of a typedef if not an array declarator.
	 *
	 *	@return		iType
	 */
 public IdlType getOriginIdlType();
 /**
	 *  @return				Assign by SET or LET?
	 */
 public boolean isVbSet();
 /** Write VB code reading the value from an oIn stream
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable getting the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbRead(VbClsWriter out, String vbVariable)
   throws java.io.IOException;
 /** Write VB code writing the value to an oOut stream
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable containing the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbWrite(VbClsWriter out, String vbVariable)
   throws java.io.IOException;
 /** Write VB code assigning the TypeCode to a VB variable
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable getting the TypeCode
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbAssignTypeCode(VbWriter out, String vbVariable)
   throws java.io.IOException;
 /** Write VB code extracting the value from an oAny to a VB variable
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable getting the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbFromAny(VbWriter out, String vbVariable)
   throws java.io.IOException;
 /** Write VB code inserting the value into an oAny
	 *  @param	out		
	 *  @param	vbVariable	Name of the VB variable containing the value
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbIntoAny(VbWriter out, String vbVariable)
   throws java.io.IOException;
}
