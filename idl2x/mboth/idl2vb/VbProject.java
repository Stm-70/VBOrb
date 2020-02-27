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
   Copyright (c) 2002 Ajay.Mohindra

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
import java.util.Enumeration;
import java.io.*;
import mboth.util.HashSet;
public class VbProject
{
 static String vbp_template1[]=
 { "Type=OleDll"
 };
 static String vbp_template2[]=
 { "Startup=\"(None)\"",
  "HelpFile=\"\"",
  "Command32=\"\""
 };
 static String vbp_template3[]=
 { "HelpContextID=\"0\"",
  "CompatibleMode=\"1\"",
  "MajorVer=1",
  "MinorVer=0",
  "RevisionVer=0",
  "AutoIncrementVer=0",
  "ServerSupportFiles=0",
  "VersionCompanyName=\"\"",
  "CompilationType=0",
  "OptimizationType=0",
  "FavorPentiumPro(tm)=0",
  "CodeViewDebugInfo=0",
  "NoAliasing=0",
  "BoundsCheck=0",
  "OverflowCheck=0",
  "FlPointCheck=0",
  "FDIVCheck=0",
  "UnroundedFP=0",
  "StartMode=1",
  "Unattended=0",
//		"Retained=0",
  "ThreadPerObject=0",
  "MaxNumberOfThreads=1",
  "ThreadingModel=1"
 };
 private String projfile_;
 private HashSet clspaths_= new HashSet();
 private HashSet modpaths_= new HashSet();
 VbProject(String projfile)
 {
  projfile_= projfile;
 }
 public void addClass(String path, String clsfile)
 {
  clspaths_.add(clsfile);
 }
 public void addModule(String path, String modfile)
 {
  modpaths_.add(modfile);
 }
 void writeFiles(String projpath) throws IOException
 {
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing " + projfile_ + ".vbp");
  BufferedWriter vp;
  vp= new BufferedWriter(new FileWriter(
   new File(projpath, projfile_+".vbp")));
  writelns(vp, vbp_template1);
  for(Enumeration i= clspaths_.elements(); i.hasMoreElements();)
  {
   String n= (String)i.nextElement();
   writeln(vp, "Class=" + n + "; " + n + ".cls");
  }
  for(Enumeration i= modpaths_.elements(); i.hasMoreElements();)
  {
   String n= (String)i.nextElement();
   writeln(vp, "Module=" + n + "; " + n + ".bas");
  }
  writelns(vp, vbp_template2);
  writeln(vp, "Name=\"" + projfile_ + "\"");
  writeln(vp, "Description=\"" + projfile_ + " VBOrb stubs\"");
  writelns(vp, vbp_template3);
  vp.close();
  if(MainOptions.iLogLvl >= 4)
   System.out.println("I Writing " + projfile_ + ".vbw");
  vp= new BufferedWriter(new FileWriter(
   new File(projpath, projfile_+".vbw")));
  for(Enumeration i= clspaths_.elements(); i.hasMoreElements();)
  {
   String n= (String)i.nextElement();
   writeln(vp, n + " = 0, 0, 0, 0, C");
  }
  for(Enumeration i= modpaths_.elements(); i.hasMoreElements();)
  {
   String n= (String)i.nextElement();
   writeln(vp, n + " = 0, 0, 0, 0, C");
  }
  vp.close();
 }
 private void writeln(BufferedWriter vp, String s)
  throws IOException
 {
  vp.write(s);
  vp.write("\r\n");
 }
 private void writelns(BufferedWriter vp, String s[])
  throws IOException
 {
  for(int i= 0; i < s.length; i++)
   writeln(vp, s[i]);
 }
};
