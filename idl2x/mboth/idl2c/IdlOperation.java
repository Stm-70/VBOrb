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
/** Operation declarations in OMG IDL are similar to C function declarations.
 *
 *  @author  Martin Both
 */
public class IdlOperation extends IdlScope implements IdlDefinition
{
 /**
	 */
 public final static String SP= " ";
 /** <op_attribute>
	 */
 private boolean oneway;
 /** <op_type_spec>
	 */
 private IdlType idlType;
 /** <parameter_dcls> or null
	 */
 protected IdlOpParameter idlOpParameters;
 /** Raises expressions or null
	 */
 protected IdlException rExceptions[];
 /** Context expressions
	 */
 //private IdlException idlExceptions;
 /** Read operation head
	 *
	 *  @param	idlScope	
	 *  @param	oneway	
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *  @param	test		return op/null/exception or op/exception
	 *	@return				iOperation (not null if !test)
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlOperation readIdlHead(IdlScope idlScope, boolean oneway,
  TxtTokenRef tRef, TxtTokenReader idlRd, boolean test)
  throws TxtReadException
 {
  // <op_type_spec>
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlType iType= IdlBaseType.readOpType(idlScope, tRef, idlRd, test);
  if(iType == null) // test is true
   return null;
  if(!iType.isCompleteType())
   throw IdlBaseType.buildIncompleteTypeEx(token.getFilePos(), iType);
  if(iType.isAnonymousType())
   IdlSpecification.showAnonymousType(token.getFilePos(), iType);
  // <identifier>
  token= tRef.getOrReadToken(idlRd);
  IdlIdentifier iWord= IdlIdentifier.readNewIdlWord(idlScope, token);
  IdlOperation iOperation= new IdlOperation(iWord, oneway, iType);
  idlScope.putIdentifier(iOperation, true);
  return iOperation;
 }
 /** 
	 *  @param	identifier		Identifier
	 *  @param	oneway	
	 *  @param	idlType	
	 */
 public IdlOperation(IdlIdentifier identifier, boolean oneway,
  IdlType idlType)
 {
  super(identifier);
  this.oneway= oneway;
  this.idlType= idlType;
 }
 /** Read operation
	 *
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		IdlFile
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public void readIdl(TxtTokenRef tRef, TxtTokenReader idlRd)
  throws TxtReadException
 {
  // <parameter_dcls> [<raises_expr>] [<context_expr>]
  //
  TxtToken token= tRef.getOrReadToken(idlRd);
  // "(" <param_dcl> { "," <param_dcl> }* ")" | "(" ")"
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != '(')
  { throw new TxtReadException(token.getFilePos(),
    "\"(\" of parameter declaration expected");
  }
  token= idlRd.readToken();
  if(!(token instanceof TxtTokSepChar) ||
   ((TxtTokSepChar)token).getChar() != ')')
  { for(; ; )
   { IdlOpParameter iOpParameter= IdlOpParameter.readIdlHead(
     this, token, idlRd);
    addIdlOpParameter(iOpParameter);
    //iOpParameter.readIdl(idlRd);
    token= idlRd.readToken();
    if(!(token instanceof TxtTokSepChar)
     || ((TxtTokSepChar)token).getChar() != ',')
     break;
    token= idlRd.readToken();
   }
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ')')
   { throw new TxtReadException(token.getFilePos(),
     "\",\" or \")\" of parameter declaration expected");
   }
  }
  // [<raises_expr>]
  //
  token= idlRd.readToken();
  String keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.RAISES)
  { token= idlRd.readToken();
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != '(')
   { throw new TxtReadException(token.getFilePos(),
     "\"(\" of raises expression expected");
   }
   do
   { token= tRef.getOrReadToken(idlRd);
    tRef.ungetToken(token);
    // (12) <scoped_name>
    IdlIdentifier identifier;
    identifier= getSurScope().readScopedName(tRef, idlRd, false, false);
    if(!(identifier instanceof IdlException))
    { TxtReadException ex= new TxtReadException(token.getFilePos(),
      "Scoped name of an IDL exception expected");
     ex.setNextException(new TxtReadException(identifier.getFilePos(),
      "Position where the last identifier "
      + "of the given scoped name is defined"));
     throw ex;
    }
    addRaiseException((IdlException)identifier);
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ')')
   { throw new TxtReadException(token.getFilePos(),
     "\",\" or \")\" of raises expression expected");
   }
   token= idlRd.readToken();
  }
  // [<context_expr>]
  //
  keyword= IdlSpecification.readKeyword(token, true);
  if(keyword == IdlSpecification.CONTEXT)
  { token= idlRd.readToken();
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != '(')
   { throw new TxtReadException(token.getFilePos(),
     "\"(\" of context expression expected");
   }
   do
   { token= idlRd.readToken();
    // <string_literal>
    if(!(token instanceof TxtTokString))
    { throw new TxtReadException(token.getFilePos(),
      "String literal of context expression expected");
    }
    /*??? one or more context string literals "ghg" "hjh", ""
				*/
    String oneContextStr= IdlString.readStringValue(
     (TxtTokString)token, tRef, idlRd);
    token= tRef.getOrReadToken(idlRd);
   }while(token instanceof TxtTokSepChar
    && ((TxtTokSepChar)token).getChar() == ',');
   if(!(token instanceof TxtTokSepChar) ||
    ((TxtTokSepChar)token).getChar() != ')')
   { throw new TxtReadException(token.getFilePos(),
     "\",\" or \")\" of context expression expected");
   }
  }else
  { tRef.ungetToken(token);
  }
  // Register the operation as a new IDL definition
  //
  getIdlSpecification().registerIdlDef(this);
 }
 /** Add operation parameter
	 *
	 *  @param	iOpParameter
	 */
 protected void addIdlOpParameter(IdlOpParameter iOpParameter)
 {
  if(idlOpParameters == null)
  { idlOpParameters= iOpParameter;
  }else
  { idlOpParameters.addNext(iOpParameter);
  }
 }
 /** Add raises expression
	 *
	 *  @param	iException
	 */
 protected void addRaiseException(IdlException iException)
 {
  if(rExceptions == null)
  { rExceptions= new IdlException[1];
   rExceptions[0]= iException;
  }else
  { IdlException oldExs[]= rExceptions;
   rExceptions= new IdlException[oldExs.length + 1];
   System.arraycopy(oldExs, 0, rExceptions, 0, oldExs.length);
   rExceptions[oldExs.length]= iException;
  }
 }
 /** Get raises expressions
	 *
	 *  @return		rExceptions or null if no raises expression
	 */
 public IdlException[] getIdlExceptions()
 {
  return rExceptions;
 }
 /** Add context expression
	 *
	 *  @param	iOpContext
	 */
//???
//	private void addIdlOpContext(IdlOpContext iOpContext)
//	{
//		if(idlOpContexts == null)
//		{	idlOpContexts= iOpContext;
//		}else
//		{	idlOpContexts.addNext(iOpContext);
//		}
//	}
 /** (IdlDefinition)
	 *  Get an IdlName to identify the operation definition
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
 public void writeC(/* CWriter out*/) //throws java.io.IOException
 {
  String opName= getUnEscName();
  for(IdlOpParameter opPar= idlOpParameters; opPar != null;
   opPar= (IdlOpParameter)opPar.getNext())
  { System.out.print(", ");//out.write(", ");
   opPar.writeCPar(/*out*/);
  }
  System.out.println();
 }
}
