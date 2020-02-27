IDL2VB
======

This directory contains the sources of IDL2VB compiler for VBOrb.

For the latest version of VBOrb and the IDL2VB compiler see
http://home.t-online.de/home/Martin.Both/vborb.html

The VBOrb is free software. See the file COPYING.LIB for copying
conditions.


Directory structure
-------------------

vb/idl2x/               Root directory

  COPYING.LIB           Copyright

  LIESMICH.TXT          This file in my native german language

  README.TXT            This file

  Makefile              Makefile

  gnu/getopt/           Java-Source to parse compiler options

  mboth/util/           Java-Source of C preprocessor

  mboth/idl2lang/       Java-Source of CORBA-IDL parser

  mboth/idl2vb/         Java-Source of CORBA-IDL to VisualBasic compiler


Requirements
------------
IDL2VB is developed in a UNIX environment, therefore i recommend
to use also a UNIX environment if you plan to develop on IDL2VB.

Following tools are required:
 - nedit (Or any other editor)
 - make
 - gcc -E (Fast preprocessor of the GNU compiler)
 - JDK 1.1 or JDK 1.2 (Java Development Kit)


What about .jp files?
---------------------
The IDL parser and the VB code generator are separated to support more than
one programming language. Since Java does not support multiple inheritance
which would be a good solution here I decide to use a c-preprocessor to
imitate that.

Don't edit .java files in the directories of mboth/idl2lang and mboth/idl2vb.
Edit .jp files instead.

Examples:
A)
You change a .jp file in mboth/idl2lang. You call "make IDL2LANG" to build
the .java file and the IDL2LANG.
You can test IDL2LANG independently of IDL2VB. After that you have to call
"make clear" and "make IDL2VB" to build a new VB code generator.

B)
You change a .jp file in mboth/idl2vb. You call "make IDL2VB" to build
the .java file and the IDL2VB.


make targets
------------
"make clear"          Is deleting all generated .java and .class files.
"make IDL2LANG"       Build a stand alone IDL-Parser
"make IDL2VB"         Build an IDL to VB compiler
"make runCB"          Build files for the callback example
"make runDB"          Build files for the database example
"make IDL2VB.jar"     Build a Java archive of IDL2VB.



Please send BUG Reports to
Martin.Both@t-online.de
