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
   Copyright (c) 1999 Martin.Both

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
import java.util.Hashtable;
import java.io.*;
/**
 * @author  Martin Both
 */
public abstract class VbWriter
{
 /** VB keywords
	 */
 public final static String ABS= "Abs";
 public final static String ADDRESSOF= "AddressOf";
 public final static String AND= "And";
 public final static String ANY= "Any";
 public final static String ARRAY= "Array";
 public final static String AS= "As";
 public final static String ATTRIBUTE= "Attribute";
 public final static String BOOLEAN= "Boolean";
 public final static String BYVAL= "ByVal";
 public final static String BYREF= "ByRef";
 public final static String BYTE= "Byte";
 public final static String CALL= "Call";
 public final static String CASE= "Case";
 public final static String CDBL= "CDbl";
 public final static String CINT= "CInt";
 public final static String CLNG= "CLng";
 public final static String CLOSE= "Close";
 public final static String CONST= "Const";
 public final static String CSTR= "CStr";
 public final static String CURRENCY= "Currency";
 public final static String DATE= "Date";
 public final static String DEBUG= "Debug";
 public final static String DECIMAL= "Decimal";
 public final static String DECLARE= "Declare";
 public final static String DIM= "Dim"; // In VB Type Decl. not a keyword
 public final static String DO= "Do";
 public final static String DOEVENTS= "DoEvents";
 public final static String DOUBLE= "Double";
 public final static String EMPTY= "Empty";
 public final static String END= "End";
 public final static String ENDIF= "EndIf";
 public final static String ENUM= "Enum";
 public final static String ELSE= "Else";
 public final static String ELSEIF= "ElseIf";
 public final static String ERASE= "Erase";
 public final static String EVENT= "Event";
 public final static String EXIT= "Exit";
 public final static String FALSE= "False";
 public final static String FOR= "For";
 public final static String FRIEND= "Friend";
 public final static String FUNCTION= "Function";
 public final static String GET= "Get";
 public final static String GOTO= "GoTo";
 public final static String IF= "If";
 public final static String IMPLEMENTS= "Implements";
 public final static String INPUT= "Input";
 public final static String INTEGER= "Integer";
 public final static String IS= "Is";
 public final static String LBOUND= "LBound";
 public final static String LEN= "Len";
 public final static String LENB= "LenB";
 public final static String LET= "Let";
 public final static String LONG= "Long";
 public final static String LOOP= "Loop";
 public final static String LSET= "LSet";
 public final static String ME= "Me";
 public final static String MOD= "Mod";
 public final static String NEW= "New";
 public final static String NEXT= "Next";
 public final static String NOT= "Not";
 public final static String NOTHING= "Nothing";
 public final static String ON= "On";
 public final static String OPEN= "Open";
 public final static String OPTION= "Option";
 public final static String OPTIONAL= "Optional";
 public final static String OR= "Or";
 public final static String PARAMARRAY= "ParamArray";
 public final static String PRESERVE= "Preserve";
 public final static String PRINT= "Print";
 public final static String PRIVATE= "Private";
 public final static String PUBLIC= "Public";
 public final static String REDIM= "ReDim";
 public final static String RESUME= "Resume";
 public final static String RSET= "RSet";
 public final static String SCALE= "Scale";
 public final static String SELECT= "Select";
 public final static String SET= "Set";
 public final static String SINGLE= "Single";
 public final static String STATIC= "Static";
 public final static String STOP= "Stop";
 public final static String STRING= "String";
 public final static String SUB= "Sub";
 public final static String THEN= "Then";
 public final static String TO= "To";
 public final static String TRUE= "True";
 public final static String TYPE= "Type";
 public final static String UBOUND= "UBound";
 public final static String UNTIL= "Until";
 public final static String VARIANT= "Variant";
 public final static String WEND= "Wend";
 public final static String WHILE= "While";
 public final static String WITH= "With";
 public final static String XOR= "Xor";
 /** VB second keywords
	 */
 public final static String ALIAS= "Alias";
 public final static String ERR= "Err";
 public final static String ERROR= "Error";
 public final static String EXPLICIT= "Explicit";
 public final static String IIF= "IIf";
 public final static String INVOKE= "invoke"; // VB class method
 public final static String LIB= "Lib";
 public final static String OBJECT= "Object";
 public final static String PROPERTY= "Property";
 public final static String RELEASE= "release"; // VB class method
 public final static String STEP= "Step";
 public final static String TYPENAME= "TypeName";
 /** Reserved words
	 */
 public final static String CORBABSTRACTBASE= "cOrbAbstractBase";
 public final static String CORBANY= "cOrbAny";
 public final static String CORBEXCEPTION= "cOrbException";
 public final static String CORBLONGDOUBLE= "cOrbLongdouble";
 public final static String CORBOBJECT= "cOrbObject";
 public final static String CORBOBJREF= "cOrbObjRef";
 public final static String CORBREQUEST= "cOrbRequest";
 public final static String CORBSKELETON= "cOrbSkeleton";
 public final static String CORBSTREAM= "cOrbStream";
 public final static String CORBTYPECODE= "cOrbTypeCode";
 public final static String CORBVALUEBASE= "cOrbValueBase";
 public final static String CORBVALUEFACTORY= "cOrbValueFactory";
 public final static String DESCRIPTION= "Description";
 public final static String ERRHANDLER= "ErrHandler";
 public final static String EXECUTE= "execute";
 public final static String EXTRACT= "extract"; // Helper for Any
 public final static String INITBYANY= "initByAny"; // Helper for Any
 public final static String INITBYREAD= "initByRead";
 public final static String INSERT= "insert"; // Helper for Any
 public final static String INSERTINTOANY= "insertIntoAny"; // Helper for Any
 public final static String NARROW= "narrow";
 public final static String OANY= "oAny";
 public final static String OBJREF= "ObjRef";
 public final static String OEX= "oEx";
 public final static String OIN= "oIn";
 public final static String OOBJREF= "oObjRef";
 public final static String OORB= "oOrb"; // as TypeCode factory
 public final static String OOUT= "oOut";
 public final static String OREQUEST= "oRequest";
 public final static String OUSEREX= "oUserEx";
 public final static String OSYSTEMEX= "oSystemEx";
 public final static String SOPERATION= "sOperation";
 public final static String SP= " ";
 public final static String STYPEID= "sTypeId";
 public final static String TYPECODE= "TypeCode";
 public final static String TYPEID= "TypeId";
 public final static String UNCHECKEDNARROW= "uncheckedNarrow";
 public final static String USEREXWRITER= "UserExWriter";
 public final static String WRITEME= "writeMe";
 /** Keyword table
	 */
 private static Hashtable keywords;
 static
 { String keystrs[]=
  { ABS, ADDRESSOF, ALIAS, AND, ANY, ARRAY, AS, ATTRIBUTE,
   BOOLEAN, BYVAL, BYREF, BYTE,
   CALL, CASE, CDBL, CINT, CLNG, CLOSE, CONST, CSTR, CURRENCY,
   DATE,
   DEBUG, DECIMAL, DECLARE, DESCRIPTION, DIM, DO, DOEVENTS, DOUBLE,
   EMPTY, END, ENDIF, ELSE, ELSEIF, ENUM, ERASE, ERR, ERRHANDLER,
   EVENT, EXIT, EXPLICIT, EXTRACT,
   FALSE, FOR, FRIEND, FUNCTION, GET, GOTO,
   IF, IIF, IMPLEMENTS, INPUT, INSERT, INTEGER, INVOKE, IS,
   LBOUND, LEN, LENB, LET, LIB, LONG, LOOP, LSET, ME, MOD,
   NARROW, NEW, NEXT, NOT, NOTHING,
   OBJECT, OBJREF, OEX, OIN, ON, OOUT, OREQUEST,
   OPEN, OPTION, OPTIONAL, OR, OUSEREX, OSYSTEMEX,
   PARAMARRAY, PRESERVE, PRINT, PRIVATE, PROPERTY, PUBLIC,
   REDIM, RELEASE, RESUME, RSET,
   SCALE, SELECT, SET, SINGLE, STATIC, STOP, STEP, STRING, SUB,
   THEN, TO, TRUE, TYPE, TYPEID, TYPENAME,
   UBOUND, UNCHECKEDNARROW, UNTIL, VARIANT,
   WEND, WHILE, WITH, XOR
  };
  keywords= new Hashtable(50);
  for(int i= 0; i < keystrs.length; i++)
   keywords.put(keystrs[i].toLowerCase(), keystrs[i]);
 }
 /** Hash a VB reserved word
	 *
	 *  @param	word		Word (with inconsistent capitalization)
	 *	@return				Possible keyword or null
	 */
 public static String hashResWord(String word)
 { return (String)keywords.get(word.toLowerCase());
 }
 /** Static variable used to store the project definition (if any)
	 */
 protected static VbProject vbProject;
 /**
	 */
 private BufferedWriter out;
 /**
	 */
 private int indents;
 /**
	 */
 private int column;
 /** Without '\r', '\n', '\t'
	 */
 private char lastChar;
 /** 
	 *  @param	path		
	 *  @param	fileName		
	 *
	 *	@exception	IOException
	 */
 public VbWriter(String path, String fileName) throws IOException
 {
  out= new BufferedWriter(new FileWriter(new File(path, fileName)));
 }
 /**
	 */
 public void close() throws IOException
 {
  out.close();
  if(column > 0)
   throw new InternalError("column > 0");
  if(indents > 0)
   throw new InternalError("indents > 0");
 }
 /** 
	 *	@exception	IOException
	 */
 public void indent(boolean indent)
 {
  if(indent)
  { indents += 1;
  }else
  { indents -= 1;
   if(indents < 0)
    throw new InternalError("indents < 0");
  }
 }
 /** 
	 *	@exception	IOException
	 */
 public void writeLine() throws IOException
 {
  out.write('\r');
  out.write('\n');
  column= 0;
 }
 /** 
	 *  @param	str		
	 *
	 *	@exception	IOException
	 */
 public void write(String str) throws IOException
 {
  int len= str.length();
  if(len < 0)
   return;
  int wrIndents= indents;
  // ")", ", " --> len <= 2
  if(column > 40 && column + len > 80 && len > 2)
  { if(lastChar != ' ')
    out.write(' ');
   out.write('_');
   writeLine(); // Is setting column to 0
   wrIndents++;
  }
  if(column == 0)
  { if(wrIndents > 0)
   { for(int i= 0; i < wrIndents; i++)
    { column += 4;
     out.write("    ", 0, 4);
    }
   }
  }
  lastChar= str.charAt(len - 1);
  column += len;
  if(wrIndents > indents && str.charAt(0) == ' ')
   out.write(str, 1, len - 1);
  else
   out.write(str, 0, len);
 }
 /** 
	 *  @param	str		
	 *
	 *	@exception	IOException
	 */
 public void writeLine(String str) throws IOException
 {
  write(str);
  writeLine();
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeIf(String condition) throws IOException
 {
  write(IF + SP);
  write(condition);
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeThen() throws IOException
 {
  writeLine(SP + THEN);
  indent(true);
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeElse() throws IOException
 {
  indent(false);
  writeLine(ELSE);
  indent(true);
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeElseIf(String condition) throws IOException
 {
  indent(false);
  write(ELSEIF + SP);
  write(condition);
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeEndIf() throws IOException
 {
  indent(false);
  writeLine(END + SP + IF);
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeVarLine(String vbAttr, String vbVar, String vbType)
  throws java.io.IOException
 {
  writeLine(vbAttr + SP + vbVar + SP + AS + SP + vbType);
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeDimLine(String vbVar, String vbType) throws IOException
 {
  writeVarLine(DIM, vbVar, vbType);
 }
 /**
	 *  @param	isVbSet			Set or Let
	 *
	 *	@exception	IOException
	 */
 public void writeAssign(String vbVar, boolean isVbSet) throws IOException
 {
  if(isVbSet)
   write(SET + SP);
  write(vbVar + SP + "=" + SP);
 }
 /**
	 *
	 *	@exception	IOException
	 */
 public void writeConstLine(String vbAttr, String vbConst, String vbType,
  String vbValue) throws java.io.IOException
 {
  if(vbAttr != null)
   write(vbAttr + SP);
  writeLine(CONST + SP + vbConst + SP + AS + SP + vbType + " = "
   + vbValue);
 }
 /**
	 *	@exception	IOException
	 */
 public void writeOnErrorLine(String vbLabel) throws IOException
 {
  if(vbLabel == null)
   writeLine(ON + SP + ERROR + SP + RESUME + SP + NEXT);
  else
   writeLine(ON + SP + ERROR + SP + GOTO + SP + vbLabel);
 }
 /**
	 *	@exception	IOException
	 */
 public void writeLabelLine(String vbLabel) throws IOException
 {
  indent(false);
  write(vbLabel); writeLine(":");
  indent(true);
 }
 /**
	 *	@exception	IOException
	 */
 public void writeErrReraiseLine(String vbErrSource) throws IOException
 {
  writeLine(CALL + SP + IDL2VB.getVbOrbDLL() + ".ErrReraise(Err, \""
   + vbErrSource + "\")");
 }
 /**
	 *  @param	byVal			BYVAL, BYREF, PARAMARRAY
	 *
	 *	@exception	IOException
	 */
 public void writeArg(String byVal, String vbName, String vbType,
  String vbDefault) throws IOException
 {
  if(lastChar != '(')
   write(", ");
  StringBuffer sBuf= new StringBuffer();
  if(vbDefault != null)
  { sBuf.append(OPTIONAL).append(SP);
  }
  if(byVal != null)
  { sBuf.append(byVal).append(SP);
  }
  sBuf.append(vbName);
  if(vbType != null)
  { sBuf.append(SP).append(AS).append(SP).append(vbType);
  }
  if(vbDefault != null)
  { sBuf.append(" = ").append(vbDefault);
  }
  write(sBuf.toString());
 }
 /**
	 *  @param	opType			SUB, FUNCTION or PROPERTY
	 *  @param	vbErrSource		Source Name if error handling or null
	 *  @param	setNothing		Name or null
	 *
	 *	@exception	IOException
	 */
 public void writeExitEnd(String opType, String vbErrSource,
  String setNothing) throws IOException
 {
  if(vbErrSource != null)
  { writeLine(EXIT + SP + opType);
   writeLabelLine(ERRHANDLER);
   if(setNothing != null)
    writeLine(SET + SP + setNothing + " = " + NOTHING);
   writeErrReraiseLine(vbErrSource);
  }
  indent(false);
  writeLine(END + SP + opType);
 }
 /**
	 *  @param	opType			SUB, FUNCTION or PROPERTY
	 *  @param	vbErrSource		Source Name if error handling or null
	 *  @param	setNothing		Name or null
	 *
	 *	@exception	IOException
	 */
 public void writeExitEndNoUserEx(String opType, String vbErrSource,
  String setNothing) throws IOException
 {
  if(vbErrSource != null)
  { writeLine(EXIT + SP + opType);
   writeLabelLine(ERRHANDLER);
   writeLine(IF + SP + IDL2VB.getVbOrbDLL() + ".ErrIsUserEx() "
    + THEN + SP + RESUME + SP + USEREXWRITER);
   if(setNothing != null)
    writeLine(SET + SP + setNothing + " = " + NOTHING);
   writeErrReraiseLine(vbErrSource);
  }
  indent(false);
  writeLine(END + SP + opType);
 }
 /**
	 *  @param	modifiers		Public, Private, Friend, Static
	 *  @param	name			Property name
	 *
	 *	@exception	IOException
	 */
 public void writeSubHead(String modifiers, String name) throws IOException
 {
  write(modifiers + SP + SUB + SP + name + "(");
 }
 /**
	 *  @param	byVal			BYVAL, BYREF, PARAMARRAY
	 *
	 *	@exception	IOException
	 */
 public void writeSubArg(String byVal, String vbName, String vbType,
  String vbDefault) throws IOException
 {
  writeArg(byVal, vbName, vbType, vbDefault);
 }
 /**
	 *  @param	vbOnErrGo		On Error Goto ErrHandler
	 *
	 *	@exception	IOException
	 */
 public void writeSubBody(boolean vbOnErrGo) throws IOException
 {
  if(lastChar != ')')
   write(")");
  writeLine();
  indent(true);
  if(vbOnErrGo)
   writeOnErrorLine(ERRHANDLER);
 }
 /**
	 *  @param	vbErrSource		Source Name if error handling or null
	 *
	 *	@exception	IOException
	 */
 public void writeSubTail(String vbErrSource)
  throws IOException
 {
  writeExitEnd(SUB, vbErrSource, /*setNothing*/null);
 }
 /**
	 *  @param	modifiers		Public, Private, Friend, Static
	 *  @param	name			Property name
	 *
	 *	@exception	IOException
	 */
 public void writeFuncHead(String modifiers, String name) throws IOException
 {
  write(modifiers + SP + FUNCTION + SP + name + "(");
 }
 /**
	 *  @param	byVal			BYVAL, BYREF, PARAMARRAY
	 *
	 *	@exception	IOException
	 */
 public void writeFuncArg(String byVal, String vbName, String vbType,
  String vbDefault) throws IOException
 {
  writeArg(byVal, vbName, vbType, vbDefault);
 }
 /**
	 *  @param	vbType			String, Integer, Long, Boolean, ... or null
	 *  @param	vbOnErrGo		On Error Goto ErrHandler
	 *
	 *	@exception	IOException
	 */
 public void writeFuncBody(String vbType, boolean vbOnErrGo) throws IOException
 {
  if(lastChar != ')')
   write(")");
  if(vbType != null)
   writeLine(SP + AS + SP + vbType);
  else
   writeLine();
  indent(true);
  if(vbOnErrGo)
   writeOnErrorLine(ERRHANDLER);
 }
 /**
	 *  @param	vbErrSource		Source Name if error handling or null
	 *  @param	setNothing		Name or null
	 *
	 *	@exception	IOException
	 */
 public void writeFuncTail(String vbErrSource, String setNothing)
  throws IOException
 {
  writeExitEnd(FUNCTION, vbErrSource, setNothing);
 }
 /**
	 *  @param	modifiers		Public, Private, Friend, Static
	 *  @param	kind			Get, Let, Set
	 *  @param	name			Property name
	 *
	 *	@exception	IOException
	 */
 public void writePropertyHead(String modifiers, String kind, String name)
  throws IOException
 {
  write(modifiers + SP + PROPERTY + SP + kind + SP + name
   + "(");
 }
 /**
	 *  @param	vbType			String, Integer, Long, Boolean, ... or null
	 *  @param	vbGet			Get or Let/Set
	 *  @param	vbOnErrGo		On Error Goto ErrHandler
	 *
	 *	@exception	IOException
	 */
 public void writePropertyBody(String vbType, boolean vbGet,
  boolean vbOnErrGo) throws IOException
 {
  if(vbGet)
  { if(lastChar != ')')
    write(")");
   writeLine(SP + AS + SP + vbType);
  }else
  { writeLine(SP + AS + SP + vbType + ")");
  }
  indent(true);
  if(vbOnErrGo)
   writeOnErrorLine(ERRHANDLER);
 }
 /**
	 *  @param	vbErrSource		Source Name if error handling or null
	 *  @param	setNothing		Name or null
	 *
	 *	@exception	IOException
	 */
 public void writePropertyTail(String vbErrSource, String setNothing)
  throws IOException
 {
  writeExitEnd(PROPERTY, vbErrSource, setNothing);
 }
 /** 
	 *	@exception	IOException
	 */
 public void writeConstTypeId(boolean isPublic, String sTypeId)
  throws java.io.IOException
 {
  if(isPublic)
   writeConstLine(PUBLIC, TYPEID, STRING, "\"" + sTypeId + "\"");
  else
   writeConstLine(null, STYPEID, STRING, "\"" + sTypeId + "\"");
 }
}
