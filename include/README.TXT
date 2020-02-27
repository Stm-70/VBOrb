Include files for your IDL files
================================


Directory structure
-------------------

vb/include/             This directory

  generated/            Generated VB module(s) for your VB application(s)
  
  README.TXT            This file

  orb.idl               Include this file in your IDL if you need
                        all standard definition(s).

  CORBA.idl             Include this file in your IDL if you need
                        module CORBA definition(s).
  
  TypeCode.idl          Include this file in your IDL if you need
                        the TypeCode definition
  
  IOP.idl               Include this file in your IDL if you need
                        module IOP definition(s).



Compiling standard IDL files
----------------------------

To recompile this standard IDL files for VBOrb.DLL call:
 ./bin/idl2vb -XA -OmVBOrb.VBOrb -Borb/generated include/CORBA.idl
 ./bin/idl2vb -XSA -OmVBOrb.VBOrb --novalfacts -Borb/generated include/IOP.idl
 ./bin/idl2vb -XA -OmVBOrb.VBOrb --novalfacts --simplevalues \
      -Borb/generated -D_TypeCodeImpl include/TypeCode.idl

To recompile this standard IDL files for your application(s) call:
 ./bin/idl2vb -XA -Binclude/generated include/CORBA.idl
