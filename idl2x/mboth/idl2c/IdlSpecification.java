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
import java.util.Stack;
import java.util.Enumeration;
import java.io.IOException;
import mboth.util.*;
/**
 * @author  Martin Both
 */
public class IdlSpecification extends IdlModule implements TxtPreProPragma
{
 /** Keywords
	 */
 public static final String ABSTRACT= "abstract";
 public static final String ANY= "any";
 public static final String ATTRIBUTE= "attribute";
 public static final String BOOLEAN= "boolean";
 public static final String CASE= "case";
 public static final String CHAR= "char";
 public static final String COMPONENT= "component";
 public static final String CONST= "const";
 public static final String CONSUMES= "consumes";
 public static final String CONTEXT= "context";
 public static final String CUSTOM= "custom";
 public static final String DEFAULT= "default";
 public static final String DOUBLE= "double";
 public static final String ENUM= "enum";
 public static final String EXCEPTION= "exception";
 public static final String EMITS= "emits";
 public static final String EVENTTYPE= "eventtype";
 public static final String FACTORY= "factory";
 public static final String FALSE= "FALSE";
 public static final String FINDER= "finder";
 public static final String FIXED= "fixed";
 public static final String FLOAT= "float";
 public static final String GETRAISES= "getraises";
 public static final String HOME= "home";
 public static final String IMPORT= "import";
 public static final String IN= "in";
 public static final String INOUT= "inout";
 public static final String INTERFACE= "interface";
 public static final String LOCAL= "local";
 public static final String LONG= "long";
 public static final String MODULE= "module";
 public static final String MULTIPLE= "multiple";
 public static final String NATIVE= "native";
 public static final String OBJECT= "Object";
 public static final String OCTET= "octet";
 public static final String ONEWAY= "oneway";
 public static final String OUT= "out";
 public static final String PRIMARYKEY= "primarykey";
 public static final String PRIVATE= "private";
 public static final String PROVIDES= "provides";
 public static final String PUBLIC= "public";
 public static final String PUBLISHES= "publishes";
 public static final String RAISES= "raises";
 public static final String READONLY= "readonly";
 public static final String SETRAISES= "setraises";
 public static final String SEQUENCE= "sequence";
 public static final String SHORT= "short";
 public static final String STRING= "string";
 public static final String STRUCT= "struct";
 public static final String SUPPORTS= "supports";
 public static final String SWITCH= "switch";
 public static final String TRUE= "TRUE";
 public static final String TRUNCATABLE= "truncatable";
 public static final String TYPEDEF= "typedef";
 public static final String TYPEID= "typeid";
 public static final String TYPEPREFIX= "typeprefix";
 public static final String UNSIGNED= "unsigned";
 public static final String UNION= "union";
 public static final String USES= "uses";
 public static final String VALUEBASE= "ValueBase";
 public static final String VALUETYPE= "valuetype";
 public static final String VOID= "void";
 public static final String WCHAR= "wchar";
 public static final String WSTRING= "wstring";
 /** CORBA keyword table
	 */
 private static Hashtable keywords;
 static
 { String key2strs[]=
  { ABSTRACT, ANY, ATTRIBUTE, BOOLEAN, CASE,
   CHAR, CONST, CONTEXT, CUSTOM, DEFAULT,
   DOUBLE, ENUM, EXCEPTION, FACTORY, FALSE,
   FIXED, FLOAT, IN, INOUT, INTERFACE,
   LOCAL, LONG, MODULE, NATIVE, OBJECT, OCTET,
   ONEWAY, OUT, PRIVATE, PUBLIC, RAISES,
   READONLY, SEQUENCE, SHORT, STRING, STRUCT,
   SUPPORTS, SWITCH, TRUE, TRUNCATABLE, TYPEDEF,
   UNSIGNED, UNION, VALUEBASE, VALUETYPE, VOID,
   WCHAR, WSTRING
  };
  String key3strs[]=
  { COMPONENT, CONSUMES, EMITS, EVENTTYPE,
   FINDER, GETRAISES, HOME, IMPORT, MULTIPLE,
   PRIMARYKEY, PROVIDES, PUBLISHES, SETRAISES,
   TYPEID, TYPEPREFIX, USES
  };
  keywords= new Hashtable(50);
  for(int i= 0; i < key2strs.length; i++)
   keywords.put(key2strs[i].toLowerCase(), key2strs[i]);
  for(int i= 0; i < key3strs.length; i++)
   keywords.put(key3strs[i].toLowerCase(), key3strs[i]);
 }
 /** Hash an IdlKeyword
	 *
	 *  @param	word		Word (with inconsistent capitalization)
	 *	@return				Possible keyword or null
	 */
 public static String hashKeyword(String word)
 { return (String)keywords.get(word.toLowerCase());
 }
 /** Read an IdlKeyword
	 *
	 *  @param	token		Last token
	 *  @param	test		Return keyword/null/exception or keyword/exception
	 *	@return				Keyword or null
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static String readKeyword(TxtToken token, boolean test)
  throws TxtReadException
 {
  if(!(token instanceof TxtTokWord))
  { if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "IDL keyword expected");
  }
  String word= ((TxtTokWord)token).getWord();
  String keyword= hashKeyword(word);
  if(keyword == null)
  { if(test)
    return null;
   throw new TxtReadException(token.getFilePos(),
    "IDL keyword expected");
  }
  // Not the same case?
  if(!keyword.equals(word))
  { throw new TxtReadException(token.getFilePos(),
    "Word `" + word + "' collides with the keyword `"
    + keyword + "'");
  }
  return keyword;
 }
 /**
	 */
 public static boolean bShowAnonymousTypes;
 /**
	 */
 public static void showAnonymousType(TxtFilePos filePos, IdlType iType)
 {
  if(!IdlSpecification.bShowAnonymousTypes)
   return;
  String typeName= iType.getUnEscName();
  if(typeName.indexOf('[') < 0 && typeName.indexOf('<') < 0)
   typeName += "<>";
  System.out.println(filePos.toString()
   + ": Using of anonymous type `" + typeName + "' is deprecated here");
  /*if(!filePos.equals(iType.getFilePos()))
			System.out.println(iType.getFilePos().toString()
			+ ": Position of the anonymous type definition");
		*/
 }
 /** IDL types and other definitions
	 */
 private Hashtable idlDefs;
 /** Include level
	 */
 private int iIncludeLevel;
 /** PragmaPrefixStack
	 */
 private Stack typePrefixStack;
 /** Pragma IdlScope
	 */
 private IdlScope pragmaScope;
 /** 
	 *  @param	idlRd		IdlFile
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public IdlSpecification(TxtPreProcessor idlRd) throws TxtReadException
 {
  super(new IdlIdentifier((IdlScope)null, " ", idlRd.getFilePos()));
  this.idlDefs= new Hashtable();
  this.iIncludeLevel= 0;
  // Set defaults before reading an IDL file
  this.typePrefixStack= new Stack();
  this.typePrefixStack.push("");
  this.setPragmaScope(this);
  readIdl(idlRd);
 }
 /** 
	 */
 private void readIdl(TxtPreProcessor idlRd) throws TxtReadException
 {
  idlRd.setPragmaReader(this);
  TxtToken token;
  // <definition>+
  if((token= readIdlDefinition(idlRd)) != null)
  { throw new TxtReadException(token.getFilePos(),
    (token instanceof TxtTokEndOfFile)?
    "Missing an IDL definition": "Unknown IDL definition");
  }
  while((token= readIdlDefinition(idlRd)) == null)
   ;
  if(!(token instanceof TxtTokEndOfFile))
  { throw new TxtReadException(token.getFilePos(),
    "IDL definition expected");
  }
 }
 /** Get the current setting of pragma prefix
	 *
	 *  @return	The current pragma prefix
	 */
 public String getCurTypePrefix()
 {
  return (String)typePrefixStack.peek();
 }
 /** 
	 *  @return	iIncludeLevel
	 */
 public int getIncludeLevel()
 {
  return iIncludeLevel;
 }
 /** 
	 *  @iScope		Current IdlScope
	 */
 public void setPragmaScope(IdlScope iScope)
 {
  pragmaScope= iScope;
 }
 /** (TxtPreProPragma)
	 *  @param	filename
	 */
 public void openIncludeFile(String filename)
 {
  iIncludeLevel++;
  // System.out.println("*** Pragma: open include file: " + filename);
  typePrefixStack.push("");
 }
 /** (TxtPreProPragma)
	 */
 public void closeIncludeFile()
 {
  iIncludeLevel--;
  // System.out.println("*** Pragma: close include file");
  typePrefixStack.pop();
 }
 /** (TxtPreProPragma)
	 *  Read pragma prefix, version, ID
	 *
	 *  @param	ttRd
	 *
	 *  @exception	TxtReadException
	 */
 public void readPragma(TxtTokenReader tr) throws TxtReadException
 {
  TxtToken tt= tr.readToken();
  if(tt instanceof TxtTokWord)
  { TxtTokWord word= (TxtTokWord)tt;
   if(word.getWord().equals("prefix"))
   { // #pragma prefix "prefix"
    tt= tr.readToken();
    if(!(tt instanceof TxtTokString))
    { throw new TxtReadException(tt.getFilePos(),
      "A string constant after `prefix' expected");
    }
    // Set current type prefix
    typePrefixStack.pop();
    typePrefixStack.push(((TxtTokString)tt).getOriginalStr());
   }else if(word.getWord().equals("version"))
   { TxtTokenRef tRef= new TxtTokenRef();
    IdlIdentifier ii= pragmaScope.readScopedName(tRef, tr, false, false);
    tt= tRef.getOrReadToken(tr);
    if(!(tt instanceof TxtTokDigits))
    { throw new TxtReadException(tt.getFilePos(),
      "A decimal constant expected");
    }
    String major= ((TxtTokDigits)tt).getDigits();
    tt= tr.readToken();
    if(!(tt instanceof TxtTokSepChar) ||
     ((TxtTokSepChar)tt).getChar() != '.')
    { throw new TxtReadException(tt.getFilePos(),
      "A `.' expected");
    }
    tt= tr.readToken();
    if(!(tt instanceof TxtTokDigits))
    { throw new TxtReadException(tt.getFilePos(),
      "A decimal constant expected");
    }
    String minor= ((TxtTokDigits)tt).getDigits();
    ii.setPragmaVersion(tt.getFilePos(), major + "." + minor);
   }else if(word.getWord().equals("ID"))
   { TxtTokenRef tRef= new TxtTokenRef();
    IdlIdentifier ii= pragmaScope.readScopedName(tRef, tr, false, false);
    tt= tRef.getOrReadToken(tr);
    if(!(tt instanceof TxtTokString))
    { throw new TxtReadException(tt.getFilePos(),
      "A string constant expected");
    }
    ii.setPragmaTypeId(tt.getFilePos(),
     ((TxtTokString)tt).getUnEscString());
   }else
   { // Ignore unknown pragma directive
    do
    { tt= tr.readToken();
    }while(!(tt instanceof TxtTokEndOfFile));
    return;
   }
   tt= tr.readToken();
   if(!(tt instanceof TxtTokEndOfFile))
   { throw new TxtReadException(tt.getFilePos(),
     "A new line expected");
   }
  }else
  { // Ignore unknown pragma directive
   while(!(tt instanceof TxtTokEndOfFile))
   { tt= tr.readToken();
   }
  }
 }
 /**
	 *  @param	iDef		All kind of IdlType and other definitions
	 */
 public Object getPrevIdlDef(IdlDefinition iDef)
 {
  String sUniqueName= iDef.getIdlName();
  //if(idlDefs.get(sUniqueName) == null)
  //	System.out.println("REG-Check: " + sUniqueName + " is new");
  //else
  //	System.out.println("REG-Check: " + sUniqueName + " is already "
  //		+ ((IdlDefinition)idlDefs.get(sUniqueName)).getIdlName());
  return idlDefs.get(sUniqueName);
 }
 /**
	 *  @param	iDef		All kind of IdlType and other definitions
	 */
 public void registerIdlDef(IdlDefinition iDef)
 {
  // Same IDL types like sequences can be defined more than onces.
  // They have to use getPrevIdlDef() to find a previous definition.
  String sUniqueName= iDef.getIdlName();
  idlDefs.put(sUniqueName, iDef);
  // System.out.println("REG: " + sUniqueName + " as "
  //	+ iDef.getClass().getName());
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
  { this.sCName= "";
   //???if(CWriter.hashResWord(sVbName) != null)
  }
  return this.sCName;
 }
 /**
	 *  @param	cPath		Prefix
	 *  @param	srvout		Write additional server skeleton examples
	 *
	 *	@exception	IOException	
	 */
 public void writeCFiles(MainOptions opts) throws java.io.IOException
 {
  if(MainOptions.iLogLvl >= 2)
   System.out.println("W Output directory is `" + opts.out_path + "'");
  /* Write contained IdlDefinitions
		*/
  super.writeCFiles(opts);
  /* Write anonymous defined (IdlDefinition)iDefs
		*/
  for(Enumeration iDefEnum= idlDefs.elements();
   iDefEnum.hasMoreElements(); )
  {
   IdlDefinition iDef= (IdlDefinition)iDefEnum.nextElement();
   if(iDef instanceof IdlSequence)
   { IdlSequence iSeq= (IdlSequence)iDef;
    iSeq.writeC(opts.out_path);//???
   }else if(iDef instanceof IdlArray)
   { IdlArray iArr= (IdlArray)iDef;
    //???iArr.writeCFiles(opts);
   }
  }
 }
}
