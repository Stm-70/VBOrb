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
import mboth.util.*;
import java.math.*; // BigDecimal
/** Is a union for const values. The union can read from IDL file or set
 *  by user.
 *
 * @author  Martin Both
 */
public class IdlConstValue
{
 /**
	 */
 public final static int PRIOR_EX= 0; // To execute all operators
 private final static int PRIOR_OR=4; // ´|´
 private final static int PRIOR_XOR=5; // ´^´
 private final static int PRIOR_AND=6; // ´&´
 private final static int PRIOR_SHIFT=9; // ´>>´ and ´<<´
 private final static int PRIOR_AS=10; // ´+´ and ´-´
 private final static int PRIOR_MD=11; // ´*´, ´/´, ´%´
 private final static int PRIOR_UN=12; // ´+´, ´-´, ´~´
 /** Read an expression until finding an operator with less or equal
	 *  priority of prevPrior or unexpected token. If there is no value token
	 *  an exception is thrown.
	 *
	 *	@param	subExRd		IdlConstSubExReader to read subexpressions
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	tRef		Next TxtToken, unread() and unget() buffer
	 *                      are used to unread long operators (´>>´)!
	 *  @param	idlRd		TxtTokenReader
	 *	@param	prevPrior
	 *  @return				Result value and maybe two unread and unget tokens
	 */
 public static IdlConstValue readConstExpression(IdlConstType subExRd,
  IdlScope idlScope, TxtTokenRef tRef, TxtTokenReader idlRd,
  int prevPrior) throws TxtReadException
 {
  IdlConstValue result;
  TxtToken token= tRef.getOrReadToken(idlRd);
  /* Zuerst wird eine Variable, eine Funktion
		   oder ein Wert erwartet.
		*/
  if(token instanceof TxtTokSepChar)
  { char sepCh= ((TxtTokSepChar)token).getChar();
   if(sepCh == ':')
   { tRef.ungetToken(token);
    result= readScopedNameValue(idlScope, tRef, idlRd);
   }else if(sepCh == '+')
   { result= subExRd.readSubExpression(idlScope, tRef, idlRd,
     PRIOR_UN);
    result.op_plus(token);
   }else if(sepCh == '-')
   { result= subExRd.readSubExpression(idlScope, tRef, idlRd,
     PRIOR_UN);
    result.op_minus(token);
   }else if(sepCh == '~')
   { result= subExRd.readSubExpression(idlScope, tRef, idlRd,
     PRIOR_UN);
    result.op_not(token);
   }else if(sepCh == '(')
   { result= subExRd.readSubExpression(idlScope, tRef, idlRd,
     PRIOR_EX);
    token= tRef.getOrReadToken(idlRd);
    if(!(token instanceof TxtTokSepChar) ||
     ((TxtTokSepChar)token).getChar() != ')')
    { throw new TxtReadException(token.getFilePos(),
      "`)' expected");
    }
   }else if(sepCh == '.')
   { TxtFilePos dPos= token.getFilePos();
    TxtToken token3= idlRd.readToken();
    String fracDigits;
    TxtToken token2;
    if(!token3.isAfterWhiteSpaces() &&
     token3 instanceof TxtTokDigits)
    { fracDigits= ((TxtTokDigits)token3).getDigits();
     token2= idlRd.readToken();
    }else
    { throw new TxtReadException(token3.getFilePos(),
      "Digits expected");
    }
    if(!token2.isAfterWhiteSpaces() &&
     token2 instanceof TxtTokWord)
    { String suffix= ((TxtTokWord)token2).getWord();
     if(suffix.length() > 0 && (suffix.charAt(0) == 'e'
      || suffix.charAt(0) == 'E'))
      result= readFloatingLiteral(dPos, null, fracDigits,
       token2.getFilePos(), suffix, idlRd);
     else if(suffix.length() > 0 && (suffix.charAt(0) == 'd'
      || suffix.charAt(0) == 'D'))
      result= readFixedLiteral(dPos, null, fracDigits,
       token2.getFilePos(), suffix);
     else
     { idlRd.unreadToken();
      result= readFloatingLiteral(dPos, null, fracDigits,
       null, null, null);
     }
    }else
    { idlRd.unreadToken();
     result= readFloatingLiteral(dPos, null, fracDigits, null,
      null, null);
    }
   }else
   { throw new TxtReadException(token.getFilePos(),
     "Const expression expected");
   }
  }else if(token instanceof TxtTokDigits)
  { // An Integer, Floating-point or Fixed-Point literal
   TxtFilePos dPos= token.getFilePos();
   String digits= ((TxtTokDigits)token).getDigits();
   // Read Suffix
   TxtToken token2= idlRd.readToken();
   if(!token2.isAfterWhiteSpaces() &&
    token2 instanceof TxtTokWord)
   { String suffix= ((TxtTokWord)token2).getWord();
    if(suffix.length() > 0 && (suffix.charAt(0) == 'e'
     || suffix.charAt(0) == 'E'))
     result= readFloatingLiteral(dPos, digits, null,
      token2.getFilePos(), suffix, idlRd);
    else if(suffix.length() > 0 && (suffix.charAt(0) == 'd'
     || suffix.charAt(0) == 'D'))
     result= readFixedLiteral(dPos, digits, null,
      token2.getFilePos(), suffix);
    else
     result= readIntegerLiteral(dPos, digits,
      token2.getFilePos(), suffix);
   }else if(!token2.isAfterWhiteSpaces() &&
    token2 instanceof TxtTokSepChar &&
    ((TxtTokSepChar)token2).getChar() == '.')
   { TxtToken token3= idlRd.readToken();
    String fracDigits;
    if(!token3.isAfterWhiteSpaces() &&
     token3 instanceof TxtTokDigits)
    { fracDigits= ((TxtTokDigits)token3).getDigits();
     token2= idlRd.readToken();
    }else
    { fracDigits= null;
     token2= token3;
    }
    if(!token2.isAfterWhiteSpaces() &&
     token2 instanceof TxtTokWord)
    { String suffix= ((TxtTokWord)token2).getWord();
     if(suffix.length() > 0 && (suffix.charAt(0) == 'e'
      || suffix.charAt(0) == 'E'))
      result= readFloatingLiteral(dPos, digits, fracDigits,
       token2.getFilePos(), suffix, idlRd);
     else if(suffix.length() > 0 && (suffix.charAt(0) == 'd'
      || suffix.charAt(0) == 'D'))
      result= readFixedLiteral(dPos, digits, fracDigits,
       token2.getFilePos(), suffix);
     else
     { idlRd.unreadToken();
      result= readFloatingLiteral(dPos, digits, fracDigits,
       null, null, null);
     }
    }else
    { idlRd.unreadToken();
     result= readFloatingLiteral(dPos, digits, fracDigits, null,
      null, null);
    }
   }else
   { idlRd.unreadToken();
    result= readIntegerLiteral(dPos, digits, null, null);
   }
  }else if(token instanceof TxtTokWord)
  { String keyword= IdlSpecification.readKeyword(token, true);
   if(keyword == IdlSpecification.FALSE)
   { result= new IdlConstValue(false);
   }else if(keyword == IdlSpecification.TRUE)
   { result= new IdlConstValue(true);
   }else if(((TxtTokWord)token).getWord().equals("L"))
   { TxtToken token2= idlRd.readToken();
    if(!token2.isAfterWhiteSpaces() &&
     token2 instanceof TxtTokString)
    { // wchar or wstring
     TxtTokString strToken= (TxtTokString)token2;
     if(strToken.getBoundary() == '\'')
     { result= new IdlConstValue(IdlChar.readCharValue(
       strToken));
     }else
     { result= new IdlConstValue(IdlString.readStringValue(
       strToken, tRef, idlRd));
     }
    }else
    { idlRd.unreadToken();
     tRef.ungetToken(token);
     result= readScopedNameValue(idlScope, tRef, idlRd);
    }
   }else
   { tRef.ungetToken(token);
    result= readScopedNameValue(idlScope, tRef, idlRd);
   }
  }else if(token instanceof TxtTokString)
  { // char or string
   TxtTokString strToken= (TxtTokString)token;
   if(strToken.getBoundary() == '\'')
   { result= new IdlConstValue(IdlChar.readCharValue(strToken));
   }else
   { result= new IdlConstValue(IdlString.readStringValue(strToken,
     tRef, idlRd));
   }
  }else if(token instanceof TxtTokEndOfFile)
  { throw new TxtReadException(token.getFilePos(),
    "Missing a const expression");
  }else
  { throw new TxtReadException(token.getFilePos(),
    "Const expression expected");
  }
  /* Nachdem der erste Wert oder die erste Funktion eingelesen wurde...
		*/
  for(; ; )
  { token= tRef.getOrReadToken(idlRd);
   // System.out.println("readOp: " + result + " " + token);
   if(token instanceof TxtTokSepChar)
   { char sepCh= ((TxtTokSepChar)token).getChar();
    if(sepCh == '+')
    { if(prevPrior >= PRIOR_AS)
      break;
     result.op_add(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_AS));
    }else if(sepCh == '-')
    { if(prevPrior >= PRIOR_AS)
      break;
     result.op_sub(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_AS));
    }else if(sepCh == '*')
    { if(prevPrior >= PRIOR_MD)
      break;
     result.op_mul(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_MD));
    }else if(sepCh == '/')
    { if(prevPrior >= PRIOR_MD)
      break;
     result.op_div(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_MD));
    }else if(sepCh == '%')
    { if(prevPrior >= PRIOR_MD)
      break;
     result.op_mod(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_MD));
    }else if(sepCh == '^')
    { if(prevPrior >= PRIOR_XOR)
      break;
     result.op_xor(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_XOR));
    }else if(sepCh == '&')
    { if(prevPrior >= PRIOR_AND)
      break;
     result.op_and(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_AND));
    }else if(sepCh == '|')
    { if(prevPrior >= PRIOR_OR)
      break;
     result.op_or(token, subExRd.readSubExpression(idlScope,
      tRef, idlRd, PRIOR_OR));
    }else if(sepCh == '>')
    { TxtToken token2= idlRd.readToken();
     if(!token2.isAfterWhiteSpaces() &&
      (token2 instanceof TxtTokSepChar) &&
      ((TxtTokSepChar)token2).getChar() == '>')
     { if(prevPrior >= PRIOR_SHIFT)
      { idlRd.unreadToken();
       break;
      }
      result.op_rightshift(token2, subExRd.readSubExpression(
       idlScope, tRef, idlRd, PRIOR_SHIFT));
     }else
     { idlRd.unreadToken();
      break;
     }
    }else if(sepCh == '<')
    { TxtToken token2= idlRd.readToken();
     if(!token2.isAfterWhiteSpaces() &&
      (token2 instanceof TxtTokSepChar) &&
      ((TxtTokSepChar)token2).getChar() == '<')
     { if(prevPrior >= PRIOR_SHIFT)
      { idlRd.unreadToken();
       break;
      }
      result.op_leftshift(token2, subExRd.readSubExpression(
       idlScope, tRef, idlRd, PRIOR_SHIFT));
     }else
     { idlRd.unreadToken();
      break;
     }
    }else
    { break;
    }
   }else
   { break;
   }
  }
  // Unget unknown operator
  // System.out.println("Result= " + result + ", next= " + token);
  tRef.ungetToken(token);
  return result;
 }
 /** Read an integer literal
	 *
	 *  @param	dPos		FilePosition of digits
	 *  @param	digits		Start digits
	 *  @param	sPos		FilePosition of suffix or null
	 *  @param	suffix		Suffix (characters and digits) or null
	 *  @return				Result value
	 */
 private static IdlConstValue readIntegerLiteral(TxtFilePos dPos,
  String digits, TxtFilePos sPos, String suffix) throws TxtReadException
 {
  long longVal;
  // System.out.println("Reading Integer literal, Digits= " + digits);
  // Octal or Hex?
  if(digits.charAt(0) == '0')
  { // Hex?
   int digit;
   if(digits.length() == 1 && suffix != null && suffix.length() > 1
    && (suffix.charAt(0) == 'x' || suffix.charAt(0) == 'X')
    && (digit= Character.digit(suffix.charAt(1), 16)) >= 0)
   { // 0xf...
    longVal= digit;
    int pos= 2;
    while(pos < suffix.length()
     && (digit= Character.digit(suffix.charAt(pos), 16)) >= 0)
    { if(pos > 16)
      throw new TxtReadException(dPos,
       "Integer literal out of range");
     longVal *= 16;
     longVal += digit;
     pos++;
    }
    suffix= suffix.substring(pos);
   }else
   { // Octal
    longVal= 0;
    for(int pos= 1; pos < digits.length(); pos++)
    { digit= Character.digit(digits.charAt(pos), 8);
     if(digit < 0)
     { throw new TxtReadException(dPos,
       "Octal digits expected");
     }
     if(longVal >= 0200000000000000000000l
      || pos > 22)
      throw new TxtReadException(dPos,
       "Integer literal out of range");
     longVal *= 8;
     longVal += digit;
    }
   }
  }else
  { // Decimal
   longVal= 0;
   for(int pos= 0; pos < digits.length(); pos++)
   { int digit= Character.digit(digits.charAt(pos), 10);
    if(longVal > 1844674407370955161l
     || (longVal == 1844674407370955161l && digit > 5)
     /* || pos >= 20 */)
     throw new TxtReadException(dPos,
      "Integer literal out of range");
    longVal *= 10;
    longVal += digit;
   }
  }
  // Integer Suffix: u, U, l, L
  if(suffix != null && suffix.length() > 0)
  { // System.out.println("Integer Suffix= " + suffix);
   if(suffix.length() > 2 ||
    !(((suffix.charAt(0) == 'u' || suffix.charAt(0) == 'U')
     && (suffix.length() == 1 ||
     suffix.charAt(1) == 'l' || suffix.charAt(1) == 'L'))
    || ((suffix.charAt(0) == 'l' || suffix.charAt(0) == 'L')
     && (suffix.length() == 1 ||
     suffix.charAt(1) == 'u' || suffix.charAt(1) == 'U'))))
   {
    throw new TxtReadException(sPos,
     "Invalid integer suffix `" + suffix + "'");
   }
  }
  return new IdlConstValue(longVal);
 }
 /** Read a floating-point literal
	 *
	 *  @param	dPos		FilePosition of digits
	 *  @param	intDigits	Integer part or null
	 *  @param	fracDigits	Fraction part or null
	 *  @param	sPos		FilePosition of suffix or null
	 *  @param	suffix		Suffix (characters and digits) or null
	 *  @param	idlRd		TxtTokenReader or null if no suffix
	 *  @return				Result value
	 */
 private static IdlConstValue readFloatingLiteral(TxtFilePos dPos,
  String intDigits, String fracDigits, TxtFilePos sPos, String suffix,
  TxtTokenReader idlRd) throws TxtReadException
 {
  double doubleVal;
  // System.out.println("Reading floating-point literal");
  if(suffix != null)
  { if(suffix.length() <= 0 || (suffix.charAt(0) != 'e'
    && suffix.charAt(0) != 'E'))
    throw new TxtReadException(sPos,
     "Invalid floating-point literal suffix `" + suffix + "'");
   if(suffix.length() == 1)
   { char signChar;
    TxtToken eToken= idlRd.readToken();
    if(!eToken.isAfterWhiteSpaces() &&
     eToken instanceof TxtTokSepChar)
     signChar= ((TxtTokSepChar)eToken).getChar();
    else
     signChar= ' ';
    if(signChar == '+' || signChar == '-')
    { suffix += signChar;
     eToken= idlRd.readToken();
     if(!eToken.isAfterWhiteSpaces() &&
      eToken instanceof TxtTokDigits)
      suffix += ((TxtTokDigits)eToken).getDigits();
     else
      throw new TxtReadException(eToken.getFilePos(),
       "Invalid floating-point literal exponent `"
       + eToken.toString() + "'");
    }else
    { throw new TxtReadException(sPos,
      "Invalid floating-point literal exponent `"
      + eToken.toString() + "'");
    }
   }else
   { for(int pos= 1; pos < suffix.length(); pos++)
     if(Character.digit(suffix.charAt(pos), 10) < 0)
      throw new TxtReadException(sPos,
       "Invalid floating-point literal exponent `"
       + suffix + "'");
   }
  }else
   suffix= "";
  // In JDK 1.2 it is better to use Double.parseDouble()
  // instead of Double.valueOf().doubleValue()
  if(intDigits == null)
   doubleVal= Double.valueOf("0." + fracDigits + suffix).doubleValue();
  else if(fracDigits == null)
   doubleVal= Double.valueOf(intDigits + suffix).doubleValue();
  else
   doubleVal= Double.valueOf(intDigits + "." + fracDigits + suffix)
    .doubleValue();
  return new IdlConstValue(doubleVal);
 }
 /** Read a fixed-point literal
	 *
	 *  @param	dPos		FilePosition of digits
	 *  @param	intDigits	Integer part or null
	 *  @param	fracDigits	Fraction part or null
	 *  @param	sPos		FilePosition of suffix or null
	 *  @param	suffix		Suffix (characters and digits) or null
	 *  @return				Result value
	 */
 private static IdlConstValue readFixedLiteral(TxtFilePos dPos,
  String intDigits, String fracDigits, TxtFilePos sPos, String suffix)
  throws TxtReadException
 {
  BigDecimal decimalVal;
  // System.out.println("Reading fixed-point literal");
  if(suffix != null)
  { if(suffix.length() != 1 || (suffix.charAt(0) != 'd'
    && suffix.charAt(0) != 'D'))
    throw new TxtReadException(sPos,
     "Invalid fixed-point literal suffix `" + suffix + "'");
  }
  if(intDigits == null)
   decimalVal= new BigDecimal("0." + fracDigits);
  else if(fracDigits == null)
   decimalVal= new BigDecimal(intDigits);
  else
   decimalVal= new BigDecimal(intDigits + "." + fracDigits);
  return new IdlConstValue(decimalVal);
 }
 /** Read an scoped name value. If there is no value identifier
	 *  an exception is thrown.
	 *
	 *  @param	idlScope	Information about the surrounding scope
	 *  @param	tRef		Maybe next token, unread() is not allowed
	 *  @param	idlRd		TxtTokenReader
	 *  @return				Result value
	 *
	 *	@exception	TxtReadException	With fromFilePos
	 */
 public static IdlConstValue readScopedNameValue(IdlScope idlScope,
  TxtTokenRef tRef, TxtTokenReader idlRd) throws TxtReadException
 {
  TxtToken token= tRef.getOrReadToken(idlRd);
  tRef.ungetToken(token);
  IdlIdentifier identifier= idlScope.readScopedName(tRef, idlRd, true, false);
  if(!(identifier instanceof IdlConst))
  { TxtReadException ex= new TxtReadException(token.getFilePos(),
    "Scoped name of an IDL constant expected");
   ex.setNextException(new TxtReadException(identifier.getFilePos(),
    "Position where the last identifier "
    + "of the given scoped name is defined"));
   throw ex;
  }
  IdlConstValue result= ((IdlConst)identifier).getConstValue();
  if(result == null)
   throw new TxtReadException(token.getFilePos(),
    "Constant is not yet defined");
  return result;
 }
 /** All possible kind of (Java) values
	 */
 private Boolean booleanVal; // Literal
 private Character charVal; // Literal
 private Long longVal; // Literal
 private Double doubleVal; // Literal
 private String stringVal; // Literal
 private BigDecimal decimalVal; // Literal
 /** IDL type of an predefined IDL constant
	 */
 private IdlConstType iConstType;
 /** Create a boolean value
	 */
 public IdlConstValue(boolean value)
 { booleanVal= new Boolean(value);
 }
 /** Create a character value
	 */
 public IdlConstValue(char value)
 { charVal= new Character(value);
 }
 /** Create a long integer value
	 */
 public IdlConstValue(long value)
 { longVal= new Long(value);
 }
 /** Create a double value
	 */
 public IdlConstValue(double value)
 { doubleVal= new Double(value);
 }
 /** Create a string value
	 */
 public IdlConstValue(String value)
 { stringVal= value;
 }
 /** Create a decimal value
	 */
 public IdlConstValue(BigDecimal value)
 { decimalVal= value;
 }
 /** @return		null, if other type
	 */
 public Boolean getBoolean()
 { return booleanVal;
 }
 /** @return		null, if other type
	 */
 public Character getCharacter()
 { return charVal;
 }
 /** @return		null, if other type
	 */
 public Long getLong()
 { return longVal;
 }
 /** @return		null, if other type
	 */
 public Double getDouble()
 { return doubleVal;
 }
 /** @return		null, if other type
	 */
 public String getString()
 { return stringVal;
 }
 /** @return		null, if other type
	 */
 public BigDecimal getDecimal()
 { return decimalVal;
 }
 /** Set IDL type of literal to be an IdlConst
	 */
 public void setConstType(IdlConstType iType)
 { this.iConstType= iType;
 }
 /** Get the const type of scoped name value
	 *
	 * @return		null, if literal
	 */
 public IdlConstType getConstType()
 { return iConstType;
 }
 /**
	 */
 public void op_or(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     | val2.getLong().longValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(booleanVal != null)
  { if(val2.getBoolean() != null)
   { booleanVal= new Boolean(booleanVal.booleanValue()
     || val2.getBoolean().booleanValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_xor(TxtToken opToken, IdlConstValue val2) throws
  TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     ^ val2.getLong().longValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(booleanVal != null)
  { if(val2.getBoolean() != null)
   { booleanVal= new Boolean(booleanVal.booleanValue()
     ^ val2.getBoolean().booleanValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_and(TxtToken opToken, IdlConstValue val2) throws
  TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     & val2.getLong().longValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(booleanVal != null)
  { if(val2.getBoolean() != null)
   { booleanVal= new Boolean(booleanVal.booleanValue()
     && val2.getBoolean().booleanValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_leftshift(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     << val2.getLong().longValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_rightshift(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     >> val2.getLong().longValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_add(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     + val2.getLong().longValue());
    iConstType= null;
   }else if(val2.getDouble() != null)
   { doubleVal= new Double((double)longVal.longValue()
     + val2.getDouble().doubleValue());
    longVal= null;
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(doubleVal != null)
  { if(val2.getLong() != null)
   { doubleVal= new Double(doubleVal.doubleValue()
     + (double)val2.getLong().longValue());
    iConstType= null;
   }else if(val2.getDouble() != null)
   { doubleVal= new Double(doubleVal.doubleValue()
     + val2.getDouble().doubleValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(stringVal != null)
  { if(val2.getString() != null)
   { stringVal= new String(stringVal + val2.getString());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(decimalVal != null)
  { if(val2.getDecimal() != null)
   { decimalVal= decimalVal.add(val2.getDecimal());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_sub(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     - val2.getLong().longValue());
    iConstType= null;
   }else if(val2.getDouble() != null)
   { doubleVal= new Double((double)longVal.longValue()
     - val2.getDouble().doubleValue());
    longVal= null;
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(doubleVal != null)
  { if(val2.getLong() != null)
   { doubleVal= new Double(doubleVal.doubleValue()
     - (double)val2.getLong().longValue());
    iConstType= null;
   }else if(val2.getDouble() != null)
   { doubleVal= new Double(doubleVal.doubleValue()
     - val2.getDouble().doubleValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(decimalVal != null)
  { if(val2.getDecimal() != null)
   { decimalVal= decimalVal.subtract(val2.getDecimal());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_mul(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 { if(longVal != null)
  { if(val2.getLong() != null)
   { longVal= new Long(longVal.longValue()
     * val2.getLong().longValue());
    iConstType= null;
   }else if(val2.getDouble() != null)
   { doubleVal= new Double((double)longVal.longValue()
     * val2.getDouble().doubleValue());
    longVal= null;
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(doubleVal != null)
  { if(val2.getLong() != null)
   { doubleVal= new Double(doubleVal.doubleValue()
     * (double)val2.getLong().longValue());
    iConstType= null;
   }else if(val2.getDouble() != null)
   { doubleVal= new Double(doubleVal.doubleValue()
     * val2.getDouble().doubleValue());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else if(decimalVal != null)
  { if(val2.getDecimal() != null)
   { decimalVal= decimalVal.multiply(val2.getDecimal());
    iConstType= null;
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_div(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 {
  try
  { if(longVal != null)
   { if(val2.getLong() != null)
    { longVal= new Long(longVal.longValue()
      / val2.getLong().longValue());
     iConstType= null;
    }else if(val2.getDouble() != null)
    { doubleVal= new Double((double)longVal.longValue()
      / val2.getDouble().doubleValue());
     longVal= null;
     iConstType= null;
    }else
    { throw new TxtReadException(opToken.getFilePos(),
      "Kind of operation is not suitable.");
    }
   }else if(doubleVal != null)
   { if(val2.getLong() != null)
    { doubleVal= new Double(doubleVal.doubleValue()
      / (double)val2.getLong().longValue());
     iConstType= null;
    }else if(val2.getDouble() != null)
    { doubleVal= new Double(doubleVal.doubleValue()
      / val2.getDouble().doubleValue());
     iConstType= null;
    }else
    { throw new TxtReadException(opToken.getFilePos(),
      "Kind of operation is not suitable.");
    }
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }catch(java.lang.ArithmeticException ex)
  { throw new TxtReadException(opToken.getFilePos(), ex.getMessage());
  }
 }
 /**
	 */
 public void op_mod(TxtToken opToken, IdlConstValue val2)
  throws TxtReadException
 {
  try
  { if(longVal != null)
   { if(val2.getLong() != null)
    { longVal= new Long(longVal.longValue()
      % val2.getLong().longValue());
     iConstType= null;
    }else
    { throw new TxtReadException(opToken.getFilePos(),
      "Kind of operation is not suitable.");
    }
   }else
   { throw new TxtReadException(opToken.getFilePos(),
     "Kind of operation is not suitable.");
   }
  }catch(java.lang.ArithmeticException ex)
  { throw new TxtReadException(opToken.getFilePos(), ex.getMessage());
  }
 }
 /**
	 */
 public void op_minus(TxtToken opToken) throws TxtReadException
 { if(longVal != null)
  { longVal= new Long(-longVal.longValue());
   iConstType= null;
  }else if(doubleVal != null)
  { doubleVal= new Double(-doubleVal.doubleValue());
   iConstType= null;
  }else if(decimalVal != null)
  { decimalVal= decimalVal.negate();
   iConstType= null;
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 */
 public void op_plus(TxtToken opToken) throws TxtReadException
 { if(longVal == null && doubleVal == null && decimalVal == null)
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /** Arithmetical not (not equal logical not)
	 */
 public void op_not(TxtToken opToken) throws TxtReadException
 { if(booleanVal != null)
  { booleanVal= new Boolean(!booleanVal.booleanValue());
   iConstType= null;
  }else if(longVal != null)
  { longVal= new Long(~longVal.longValue());
   iConstType= null;
  }else
  { throw new TxtReadException(opToken.getFilePos(),
    "Kind of operation is not suitable.");
  }
 }
 /**
	 *  If iConstType == null then constant expression or literal
	 *  If iConstType != null then value of a IDL constant definition
	 *
	 */
 public String toString()
 {
  StringBuffer sBuf;
  if(iConstType == null)
   sBuf= new StringBuffer("ConstExp."); // Literal
  else
   sBuf= new StringBuffer("ConstDef.");
  if(booleanVal != null)
   sBuf.append(booleanVal.booleanValue());
  else if(charVal != null)
   sBuf.append(charVal.charValue());
  else if(longVal != null)
   sBuf.append(longVal.longValue());
  else if(doubleVal != null)
   sBuf.append(doubleVal.doubleValue());
  else if(stringVal != null)
   sBuf.append(stringVal);
  else if(decimalVal != null)
   sBuf.append(decimalVal);
  else
   sBuf.append('?');
  return sBuf.toString();
 }
 /**
	 *  @param	out		
	 *
	 *	@exception	java.io.IOException	
	 */
 public String getVbValueStr()
 {
  if(booleanVal != null)
  { if(booleanVal.booleanValue())
    return VbWriter.TRUE;
   else
    return VbWriter.FALSE;
  }else if(charVal != null)
   return String.valueOf((int)charVal.charValue());
  else if(longVal != null)
   return longVal.toString();
  else if(doubleVal != null)
   return doubleVal.toString();
  else if(stringVal != null)
  { StringBuffer sBuf= new StringBuffer(stringVal);
   for(int i= sBuf.length(); i > 0; )
   { i--;
    if(sBuf.charAt(i) == '"')
     sBuf.insert(i, '"');
   }
   String str= sBuf.toString();
   sBuf= new StringBuffer();
   int pos;
   boolean literal= false;
   int litstart= 0;
   for(pos= 0; pos < str.length(); pos++)
   { if((int)str.charAt(pos) < 32)
    { if(literal)
     { literal= false;
      if(litstart != 0)
       sBuf.append(" & ");
      sBuf.append("\"" + str.substring(litstart, pos) + "\"");
     }
     if(pos != 0)
      sBuf.append(" & ");
     sBuf.append("CHR$(" + ((int)str.charAt(pos)) + ")");
    }else if(!literal)
    { literal= true;
     litstart= pos;
    }
   }
   if(pos == 0)
    sBuf.append("\"\"");
   else if(literal)
   { literal= false;
    if(litstart != 0)
     sBuf.append(" & ");
    sBuf.append("\"" + str.substring(litstart, pos) + "\"");
   }
   return sBuf.toString();
  }else if(decimalVal != null)
   // (If writing as property it is not allowed to declare
   // more constants after that in Visual Basic.)
   // CDec() does not work in VB constant definition.
   //return "CDec(\"" + decimalVal.toString() + "\")";
   return "\"" + decimalVal.toString() + "\"";
  else
   return "???";
 }
 /**
	 *  @param	out		
	 *
	 *	@exception	java.io.IOException	
	 */
 public void writeVbValue(VbWriter out)
   throws java.io.IOException
 {
  if(stringVal == null)
  { out.write(this.getVbValueStr());
  }else
  { StringBuffer sBuf= new StringBuffer(stringVal);
   for(int i= sBuf.length(); i > 0; )
   { i--;
    if(sBuf.charAt(i) == '"')
     sBuf.insert(i, '"');
   }
   String str= sBuf.toString();
   int pos;
   boolean literal= false;
   int litstart= 0;
   for(pos= 0; pos < str.length(); pos++)
   { if((int)str.charAt(pos) < 32)
    { if(literal)
     { literal= false;
      if(litstart != 0)
       out.write(" & ");
      out.write("\"" + str.substring(litstart, pos) + "\"");
     }
     if(pos != 0)
      out.write(" & ");
     switch(str.charAt(pos))
     {
     case 8:
      out.write("vbBack");
      break;
     case 9:
      out.write("vbTab");
      break;
     case 10:
      out.write("vbLf");
      break;
     case 11:
      out.write("vbVerticalTab");
      break;
     case 12:
      out.write("vbFormFeed");
      break;
     case 13:
      if(pos + 1 < str.length() && str.charAt(pos + 1) == 10)
      { pos++;
       out.write("vbCrLf");
      }else
      { out.write("vbCr");
      }
      break;
     default:
      out.write("CHR$(" + ((int)str.charAt(pos)) + ")");
     }
    }else if(!literal)
    { literal= true;
     litstart= pos;
    }
   }
   if(pos == 0)
    out.write("\"\"");
   else if(literal)
   { literal= false;
    if(litstart != 0)
     out.write(" & ");
    out.write("\"" + str.substring(litstart, pos) + "\"");
   }
  }
 }
}
