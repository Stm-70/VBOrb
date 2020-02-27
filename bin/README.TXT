IDL2VB
======

This directory contains the wrapper scripts for IDL2VB.

For the latest version of VBOrb and the IDL2VB compiler see
http://home.t-online.de/home/Martin.Both/vborb.html

The VBOrb is free software. See the file vb/COPYING.LIB for copying
conditions. Remember if you like to redistribute IDL2VB you have
to download the sources of IDL2VB from my web site and redistribute
it too.


Directory structure
-------------------

vb/idl/                 Root directory

  LIESMICH.TXT          This file in my native german language

  README.TXT            This file

  IDL2VB.jar            Java archive containing the compiler

  idl2vb.bat            Example of a batch file used to start compiling (Windows)

  idl2vb                Example of a batch file used to start compiling (UNIX)


Start Compiling
---------------
If you are using JDK 1.2 and Windows you could do something
like following to start compiling.
Open the example batch file idl2vb.bat by an editor. Customize
contents of idl2vb.bat to your directory structure and call
	idl2vb
or include IDL2VB.jar in your CLASSPATH and call
	java mboth.idl2vb.IDL2VB
or call
	java -jar IDL2VB.jar

If you are using JDK 1.1 and UNIX-ksh you could write something
like that:
	# IDL2VB is the directory where IDL2VB.jar can be found
	IDL2VB=/home/martin/vb/idl
	export CLASSPATH=${IDL2VB}/IDL2VB.jar:${CLASSPATH}
After that call
	java mboth.idl2vb.IDL2VB
or write your own batch file and call it idl2vb.


Tip:
The easiest way to put all generated .cls and .bas files into your
VB project is to select the desired files in an explorer window
and drag and drop the files into the VB project window.


Use of IDL2VB (Very brief description)
======================================

usage: java mboth.idl2vb.IDL2VB [-BCXMSOGNAEDIVh] [file ...]
 -B<path>       Path of VB output
 -C<prefix>     Default vbClsPrefix (rather than c_)
 -X             Create exposed VB classes suitable to ActiveX
 -M<prefix>     Default vbModPrefix (rather than m_)
 --simplevalues Write direct use values and no examples
 --novalfacts   Write no value factories
 -S, --skeleton Write additional server skeleton examples
 -O<name>       Default name of the ORB DLL (rather than VBOrb)
 -G<level>      Generate code for included files
 -N             Preprocess and compile but no VB output
 -A             Show anonymous types
 -E             Preprocess file(s) to standard output, no compile, no VB output
 -D<macro...>   Define a preprocessor macro
 -I<path(s)>    Add search path(s) for include files
 -V, --version  Display version number
 -h, --help     Display usage message


Example to generate grid demo client VB classes
-----------------------------------------------
idl2vb -B../demos/grid ../demos/grid/grid.idl
or
java mboth.idl2vb.IDL2VB -B../demos/grid ../demos/grid/grid.idl


Example to generate grid demo server VB classes
-----------------------------------------------
idl2vb -S -B../demos/grid_server ../demos/grid/grid.idl

It is possible to use the c-preprocessor standalone
Example to precompile hello.c
-----------------------------
idl2vb -E -D__GNUC__=2 -D__STDC__=1 -D__GNUC_MINOR__=91 \
	-I/usr/include:/usr/lib/gcc-lib/i486-linux/egcs-2.91.66/include \
	hello.c
It is like gcc -E -P hello.c


Please send BUG Reports to
Martin.Both@t-online.de
