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
package mboth.idl2c;
import java.io.IOException;
import mboth.util.*;
/**
 * @author  Martin Both
 */
public class IDL2C
{
 /**
	 */
 public static final String version= "50";
 /** 
	 */
 static public void main(String args[])
 {
  MainOptions mainOpts= new MainOptions();
  if(args.length == 0)
  { printVersion();
   printUsage();
   return;
  }
  try
  { TxtPreProcessor.addglDefine("__IDL2C__=" + version);
  }catch(ChainException ex)
  { System.out.println(ex.toString());
   return;
  }
  boolean pponly= false;
  for(int i= 0; i < args.length; i++)
  {
   try
   { if(args[i].startsWith("-B"))
    { mainOpts.out_path= args[i].substring(2);
    }else if(args[i].equals("-E"))
    { pponly= true;
    }else if(args[i].equals("-N"))
    { mainOpts.no_out= true;
    }else if(args[i].equals("-S"))
    { mainOpts.srv_out= true;
    }else if(args[i].equals("-V"))
    { printVersion();
    }else if(args[i].startsWith("-I"))
    { TxtPreProcessor.addIncludePaths(args[i].substring(2));
    }else if(args[i].startsWith("-D"))
    { TxtPreProcessor.addglDefine(args[i].substring(2));
    }else if(pponly)
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
     if(!mainOpts.no_out)
     { try
      { idlSpec.writeCFiles(mainOpts);
      }catch(IOException ex)
      { throw new ChainException(ex.toString());
      }
     }
    }
   }catch(ChainException ex)
   { System.out.println();
    do
    { System.out.println(ex.toString());
     ex= ex.getNextException();
    }while(ex != null);
    break;
   }
  }
 }
 /** 
	 */
 static public void printVersion()
 { System.out.println("IDL2C " + version);
 }
 /** 
	 */
 static public void printUsage()
 { System.out.println("usage: java mboth.idl2c.IDL2C [-VDIENBS] [file ...]");
  System.out.println(" -V           Display version number");
  System.out.println(" -D<macro...> Define a preprocessor macro");
  System.out.println(" -I<path(s)>  Add search path(s) for include files");
  System.out.println(" -E           Preprocess file(s) to standard output, no compile, no C output");
  System.out.println(" -N           Preprocess and compile but no C output");
  System.out.println(" -B<path>     Path of C output");
  System.out.println(" -S           Write additional server skeleton examples");
 }
}
