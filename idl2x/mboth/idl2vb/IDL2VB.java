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
import gnu.getopt.*;
import java.io.IOException;
import mboth.util.*;
/**
 * @author  Martin Both
 */
public class IDL2VB
{
 /** 
	 */
 public static final String pgmname= "IDL2VB";
 public static final String clsname= "mboth.idl2vb." + pgmname;
 public static final String sVersion= "123";
 /**
	 */
 public static boolean bVbClsExposed;
 /**
	 */
 public static boolean bSimpleValues;
 /**
	 */
 public static boolean bNoValueFactories;
 /**
	 */
 public static int iGenIncludeLvl;
 /**
	 */
 private static String sVbClsPrefixDefault= "c_";
 /** 
	 *  @return				sVbClsPrefixDefault
	 */
 static public String getVbClsPrefixDefault()
 { return sVbClsPrefixDefault;
 }
 /**
	 */
 private static String sVbModPrefixDefault= "m_";
 /** 
	 *  @return				sVbModPrefixDefault
	 */
 static public String getVbModPrefixDefault()
 { return sVbModPrefixDefault;
 }
 /**
	 */
 private static String sVbOrbDLL;
 private static String sVbOrbDLLDefault= "VBOrb";
 /** 
	 *
	 *  @return				sVbOrbDLL
	 */
 static public String getVbOrbDLL()
 {
  if(sVbOrbDLL == null)
   sVbOrbDLL= sVbOrbDLLDefault;
  return sVbOrbDLL;
 }
 /** 
	 */
 static public void main(String args[])
 {
  MainOptions mainOpts= new MainOptions();
  int chno;
  LongOpt[] longopts= new LongOpt[]
  { new LongOpt("simplevalues", LongOpt.NO_ARGUMENT, null, 'U'),
   new LongOpt("novalfacts", LongOpt.NO_ARGUMENT, null, 'f'),
   new LongOpt("skeleton", LongOpt.NO_ARGUMENT, null, 'S'),
   new LongOpt("version", LongOpt.NO_ARGUMENT, null, 'V'),
   new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
  };
  // The first character is a colon to return a ':' instead of a '?'
  // when getopt() encounters an option with a missing required argument.
  Getopt go= new Getopt(pgmname, args,
   ":L:B:C:XM:SO:P:G:NAED:I:Vh", longopts);
  while((chno= go.getopt()) != -1)
  { switch(chno)
   {
   case 'L':
    MainOptions.iLogLvl= Integer.parseInt(go.getOptarg());
    break;
   case 'B':
    mainOpts.vbPath= go.getOptarg();
    break;
   case 'C':
    sVbClsPrefixDefault= go.getOptarg();
    break;
   case 'X':
    bVbClsExposed= true;
    break;
   case 'M':
    sVbModPrefixDefault= go.getOptarg();
    break;
   case 'U':
    bSimpleValues= true;
    break;
   case 'f':
    bNoValueFactories= true;
    break;
   case 'S':
    mainOpts.srvout= true;
    break;
   case 'O':
    sVbOrbDLLDefault= go.getOptarg();
    break;
   case 'P':
    VbWriter.vbProject= new VbProject(go.getOptarg());
    break;
   case 'G':
    iGenIncludeLvl= Integer.parseInt(go.getOptarg());
    break;
   case 'N':
    mainOpts.noidl= true;
    break;
   case 'A':
    IdlSpecification.bShowAnonymousTypes= true;
    break;
   case 'E':
    mainOpts.pponly= true;
    break;
   case 'D':
    try
    { TxtPreProcessor.addglDefine(go.getOptarg());
    }catch(ChainException ex)
    { System.out.println("E " + ex.toString());
     System.exit(1);
    }
    break;
   case 'I':
    try
    { TxtPreProcessor.addIncludePaths(go.getOptarg());
    }catch(ChainException ex)
    { System.out.println("E " + ex.toString());
     System.exit(1);
    }
    break;
   case 'h':
    printUsage();
    break;
   case 'V':
    printVersion();
    break;
   case ':':
   //	System.out.println("E You need an argument for option " +
   //		(char)go.getOptopt());
    System.exit(1);
   case '?':
   //	System.out.println("E The option '" + (char)go.getOptopt() + 
   //		"' is not valid");
    System.exit(1);
   default:
    System.out.println("E The option '" + chno +
     "' is not implemented");
    System.exit(1);
   }
  }
  if(args.length == 0)
  { printRevision();
   printUsage();
   System.exit(1);
  }
  try
  { TxtPreProcessor.addglDefine("__" + pgmname + "__=" + sVersion);
  }catch(ChainException ex)
  { System.out.println("E " + ex.toString());
   System.exit(1);
  }
  for(int i= go.getOptind(); i < args.length; i++)
  {
   try
   {
    if(mainOpts.pponly)
    {
     TxtTokenReader tr= new TxtPreProcessor(args[i]);
     TxtToken tt= tr.readToken();
     while(!(tt instanceof TxtTokEndOfFile))
     {
      System.out.print(tt.toString());
      tt= tr.readToken();
     }
     if(tt.isAfterNewLine())
      System.out.println();
    }else
    {
     TxtPreProcessor tpp= new TxtPreProcessor(args[i]);
     IdlSpecification idlSpec= new IdlSpecification(tpp);
     idlSpec.checkMappingOfIdlDefs();
     if(!mainOpts.noidl)
     { try
      { idlSpec.writeVbFiles(mainOpts);
      }catch(IOException ex)
      { throw new ChainException(ex.toString());
      }
     }
    }
   }catch(ChainException ex)
   { System.out.println();
    do
    { System.out.println("E " + ex.toString());
     ex= ex.getNextException();
    }while(ex != null);
    System.exit(1);
    break;
   }
  }
  if(VbWriter.vbProject != null)
  { try
   {
    VbWriter.vbProject.writeFiles(mainOpts.vbPath);
   }catch(IOException ex)
   {
    System.out.println("E " + ex.toString());
    System.exit(1);
   }
  }
 }
 /** 
	 */
 static public void printRevision()
 {
  System.out.println("I Revision: " + pgmname + " v" + sVersion + ", "
   + "2020-02-26" + " " + "21:30:11");
 }
 /** 
	 */
 static public void printVersion()
 {
  System.out.println("I " + sVersion);
 }
 /** 
	 */
 static public void printUsage()
 { System.out.println("usage: java " + clsname + " [-LBCXMSOGNAEDIVh] [file ...]");
  System.out.println(" -L<level>      Log level (0, 1, 2, 4, 8)");
  System.out.println(" -B<path>       Path of VB output");
  System.out.println(" -C<prefix>     Default vbClsPrefix (rather than c_)");
  System.out.println(" -X             Create exposed VB classes suitable to ActiveX");
  System.out.println(" -M<prefix>     Default vbModPrefix (rather than m_)");
  System.out.println(" --simplevalues Write direct use values and no examples");
  System.out.println(" --novalfacts   Write no value factories");
  System.out.println(" -S, --skeleton Write additional server skeleton examples");
  System.out.println(" -O<name>       Default name of the ORB library (rather than VBOrb)");
  System.out.println(" -P<name>       Write prototype project files to <name>.vbp and <name>.vbw");
  System.out.println(" -G<level>      Generate code for included files");
  System.out.println(" -N             Preprocess and compile but no VB output");
  System.out.println(" -A             Show anonymous types");
  System.out.println(" -E             Preprocess file(s) to standard output, no compile, no VB output");
  System.out.println(" -D<macro...>   Define a preprocessor macro");
  System.out.println(" -I<path(s)>    Add search path(s) for include files");
  System.out.println(" -V, --version  Display version number");
  System.out.println(" -h, --help     Display usage message");
 }
}
