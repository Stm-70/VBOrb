VBOrb.DLL
---------

This directory contains the VBOrb itself. You need this classes
to build the VBOrb.DLL which is used by every VBOrb application.

(It is also possible to include the sources into one VB project
without using a DLL.)

If you are using VBOrb.DLL and you like to run and debug the
application in your VB development environment then you should
use the OrbRunLoopOutsideOfDLL() method as shown in the callback
example (See vb/demos/callback/fServer.frm).


Directory structure
-------------------

vb/orb                  This directory

  README.TXT            This file

  VBOrb.vbp             VB project to build VBOrb.DLL.

  cOrb....cls           Classes of VBOrb
  cVBOrb.cls            GlobalMultiUse object
  mVBOrb.bas            Global module inside of VBOrb.DLL

  include/              IDL files that can be included in your IDL file(s)
  
  /orb.idl              Include this file in your IDL file(s)
                        if you need standard CORBA definitions

  /TypeCode.idl         Include this file in your IDL file(s)
                        if you need the standard definition of TypeCode only
  
  generated/            Generated source files
