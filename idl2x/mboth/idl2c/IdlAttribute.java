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
/** An attribute definition is logically equivalent to declaring a pair of
 *  accessor functions; one to retrieve the value of the attribute and one
 *  to set the value of the attribute.
 *
 *  @author  Martin Both
 */
public class IdlAttribute extends IdlIdentifier implements IdlDefinition
{
 /** 
	 */
 private boolean readonly;
 /** <param_type_spec>
	 */
 private IdlType idlType;
 /** Read attribute
	 *
	 *  @param	readonly	
	 *  @param	iType		IdlType
	 *  @param	idlScope	
	 *  @param	token		Last token
	 *  @param	idlRd		IdlFile
	 *	@return				iAttribute (not null)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlAttribute readIdlHead(boolean readonly, IdlType iType,
  IdlScope idlScope, TxtToken token, TxtTokenReader idlRd)
  throws TxtReadException
 {
  IdlAttribute iAttribute= new IdlAttribute(
   IdlIdentifier.readNewIdlWord(idlScope, token), readonly, iType);
  idlScope.putIdentifier(iAttribute, true);
  // Register the operation as a new IDL definition
  //
  idlScope.getIdlSpecification().registerIdlDef(iAttribute);
  return iAttribute;
 }
 /** 
	 *  @param	identifier		Identifier
	 *  @param	readonly	
	 *  @param	idlType	
	 */
 public IdlAttribute(IdlIdentifier identifier, boolean readonly,
  IdlType idlType)
 {
  super(identifier);
  this.readonly= readonly;
  this.idlType= idlType;
 }
 /** (IdlDefinition)
	 *  Get an IDL name to identify the definition uniquely
	 *  for a specific language mapping
	 *
	 *  @return		<sequence ::T>
	 */
 public String getIdlName()
 { return getScopedName().toString();
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
 /**
	 *  @param	out		
	 *
	 *	@exception	IOException	
	 */
 public void writeC(/*CWriter out*/) //throws java.io.IOException
 {
  String attrName= getUnEscName();
  System.out.println("Attribute: " + idlType.getCName(true)
   + " " + attrName);
/*???		String cName= getVbPropertyName();
		
		out.writeLine();
		out.write("'");
		if(readonly)
			out.write(IdlSpecification.READONLY+ " ");
		out.writeLine(IdlSpecification.ATTRIBUTE + " " + attrName);
		out.writeAttributeGet(attrName, cName, idlType);
		if(readonly)
			return;
		out.writeLine();
		out.writeAttributeLetOrSet(attrName, vName, idlType);
*/
 }
}
