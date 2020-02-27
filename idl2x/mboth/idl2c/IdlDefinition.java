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
/** All stuff that has an equivalent for a specific language
 *  Implemented by IDL containers and all IDL types.
 *
 *  @author  Martin Both
 */
public interface IdlDefinition
{
 /** Get the filename and the start position of the definition
	 *
	 *  @return		Filename and position
	 */
 public TxtFilePos getFilePos();
 /** Get the file include level of the definition.
	 *
	 *  @return		0 = defined in main file
	 */
 public int getFileIncludeLvl();
 /** Get the short definition name (without scope and leading `_')
	 *
	 *  @return		Short definition name
	 */
 public String getUnEscName();
 /** Get an IDL name to identify the IDL definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		e.g. "<sequence ::T>"
	 */
 public String getIdlName();
 /** Set the C Name
	 *  #pragma cname <idldef> can change the name
	 *
	 *	@param	CName		Can be "" if not IdlType
	 *	@return		== null, It was not too late to set
	 *				== CName, It is unsetable
	 *				otherwise, The old name: Cannot set twice
	 */
 public String setCName(String CName);
 /** Get the C Name to identify the definition
	 *	#pragma cname can change the name before the first call
	 *
	 *	@param	withPrefix	With final prefix? The name without prefix
	 *  					is used to build complex names.
	 *	@return		C Name
	 */
 public String getCName(boolean withPrefix);
}
