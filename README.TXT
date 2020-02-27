VBOrb v1.23
===========

This directory contains the version 1.23 of the VBOrb.

For the latest version of VBOrb and the IDL2VB compiler see
http://home.t-online.de/home/Martin.Both/vborb.html

The VBOrb is free software. See the file COPYING.LIB for copying
conditions.


Directory structure
-------------------

vb/                     Root directory

  COPYING.LIB           Copyright

  LIESMICH.TXT          This file in my native german language

  README.TXT            This file

  NEWS.TXT              Release notes

  INSTALL.TXT           How to install and test VBOrb

  bin/                  Programs
    IDL2VB.jar          Java program to compile CORBA IDL files

  idl2x/                Java sources of IDL2VB compiler

  orb/                  VBOrb classes
    VBOrb.vbp           VB project to build the VBOrb.dll
    VBOrb1xx.dll        This DLL is part of every VBOrb application.
                        1xx is the version of VBOrb.

  include/              Include IDL files for your IDL files
    generated/          ORB module definitions for your VB projects
  
  doc/                  Documents
    index.html            Start page of html documents

  naming/               VB examples for the COS Naming Service
    NameServ            Simple Naming Service written in VB
    NamingTest          Client to test a COS Naming Service

  demos/                VB examples and VB test applications


Please send BUG Reports to
Martin.Both@t-online.de
