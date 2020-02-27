# Copyright (c) 1999 Martin.Both
#
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Library General Public
# License as published by the Free Software Foundation; either
# version 2 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Library General Public License for more details.

.SUFFIXES:
.SUFFIXES: .jp .java .class

.jp.java:
	gcc -E -P -C -xc $*.jp > $*.java

.java.class:
	javac $*.java

SHELL   = /usr/bin/ksh

# Tokenizer and Preprocessor
UTIL_OBJS = mboth/util/TxtReader.class mboth/util/TxtFilePos.class \
	mboth/util/TxtToken.class mboth/util/TxtReadException.class \
	mboth/util/TxtTokEndOfFile.class mboth/util/TxtTokSepChar.class \
	mboth/util/TxtTokWord.class mboth/util/TxtTokDigits.class \
	mboth/util/TxtTokString.class mboth/util/TxtPreProcessor.class \
	mboth/util/TxtTokLineReader.class mboth/util/TxtPreProIfList.class \
	mboth/util/TxtPreProMacro.class mboth/util/TxtPreProConsts.class \
	mboth/util/TxtPreProPragma.class mboth/util/TxtTokenRef.class \
	mboth/util/ArrayList.class mboth/util/HashSet.class

# IDL parser
IDL2LANG_OBJS = mboth/idl2lang/MainOptions.class \
	mboth/idl2lang/IdlIdentifier.class \
	mboth/idl2lang/IdlDefinition.class mboth/idl2lang/IdlType.class \
	mboth/idl2lang/IdlScope.class mboth/idl2lang/IdlContainer.class \
	mboth/idl2lang/IdlModule.class mboth/idl2lang/IdlSpecification.class \
	mboth/idl2lang/IdlInterface.class mboth/idl2lang/IdlObjRefType.class \
	mboth/idl2lang/IdlAttribute.class mboth/idl2lang/IdlOperation.class \
	mboth/idl2lang/IdlOpParameter.class mboth/idl2lang/IdlException.class \
	mboth/idl2lang/IdlDeclarator.class \
	mboth/idl2lang/IdlVoid.class mboth/idl2lang/IdlBoolean.class \
	mboth/idl2lang/IdlOctet.class \
	mboth/idl2lang/IdlShort.class mboth/idl2lang/IdlLong.class \
	mboth/idl2lang/IdlLongLong.class \
	mboth/idl2lang/IdlChar.class mboth/idl2lang/IdlString.class \
	mboth/idl2lang/IdlDouble.class mboth/idl2lang/IdlFixed.class \
	mboth/idl2lang/IdlObject.class mboth/idl2lang/IdlValueBase.class \
	mboth/idl2lang/IdlValueBox.class mboth/idl2lang/IdlValueType.class \
	mboth/idl2lang/IdlValStateDcl.class mboth/idl2lang/IdlValInitOp.class \
	mboth/idl2lang/IdlAny.class mboth/idl2lang/IdlNative.class \
	mboth/idl2lang/IdlBaseType.class mboth/idl2lang/IdlSequence.class \
	mboth/idl2lang/IdlTypedef.class mboth/idl2lang/IdlStruct.class \
	mboth/idl2lang/IdlUnion.class mboth/idl2lang/IdlSwitchType.class \
	mboth/idl2lang/IdlEnum.class mboth/idl2lang/IdlArray.class \
	mboth/idl2lang/IdlConst.class mboth/idl2lang/IdlConstType.class \
	mboth/idl2lang/IdlConstValue.class \
	mboth/idl2lang/IdlPositiveInt.class
IDL2LANG_SRCS = $(IDL2LANG_OBJS:.class=.java)

# Visual Basic language generator
IDL2VB_OBJS = mboth/idl2vb/MainOptions.class \
	mboth/idl2vb/IdlIdentifier.class \
	mboth/idl2vb/IdlDefinition.class mboth/idl2vb/IdlType.class \
	mboth/idl2vb/IdlScope.class mboth/idl2vb/IdlContainer.class \
	mboth/idl2vb/IdlModule.class mboth/idl2vb/IdlSpecification.class \
	mboth/idl2vb/IdlInterface.class mboth/idl2vb/IdlObjRefType.class \
	mboth/idl2vb/IdlAttribute.class mboth/idl2vb/IdlOperation.class \
	mboth/idl2vb/IdlOpParameter.class mboth/idl2vb/IdlException.class \
	mboth/idl2vb/IdlDeclarator.class \
	mboth/idl2vb/IdlVoid.class mboth/idl2vb/IdlBoolean.class \
	mboth/idl2vb/IdlOctet.class \
	mboth/idl2vb/IdlShort.class mboth/idl2vb/IdlLong.class \
	mboth/idl2vb/IdlLongLong.class \
	mboth/idl2vb/IdlChar.class mboth/idl2vb/IdlString.class \
	mboth/idl2vb/IdlDouble.class mboth/idl2vb/IdlFixed.class \
	mboth/idl2vb/IdlObject.class mboth/idl2vb/IdlValueBase.class \
	mboth/idl2vb/IdlValueBox.class mboth/idl2vb/IdlValueType.class \
	mboth/idl2vb/IdlValStateDcl.class mboth/idl2vb/IdlValInitOp.class \
	mboth/idl2vb/IdlAny.class mboth/idl2vb/IdlNative.class \
	mboth/idl2vb/IdlBaseType.class mboth/idl2vb/IdlSequence.class \
	mboth/idl2vb/IdlTypedef.class mboth/idl2vb/IdlStruct.class \
	mboth/idl2vb/IdlUnion.class mboth/idl2vb/IdlSwitchType.class \
	mboth/idl2vb/IdlEnum.class mboth/idl2vb/IdlArray.class \
	mboth/idl2vb/IdlConst.class mboth/idl2vb/IdlConstType.class \
	mboth/idl2vb/IdlConstValue.class \
	mboth/idl2vb/IdlPositiveInt.class \
	mboth/idl2vb/VbWriter.class mboth/idl2vb/VbClsWriter.class \
	mboth/idl2vb/VbModWriter.class \
	mboth/idl2vb/VbProject.class
IDL2VB_SRCS = $(IDL2VB_OBJS:.class=.java)

# C language generator
IDL2C_OBJS    =  mboth/idl2c/MainOptions.class \
	mboth/idl2c/IdlIdentifier.class \
	mboth/idl2c/IdlDefinition.class mboth/idl2c/IdlType.class \
	mboth/idl2c/IdlScope.class mboth/idl2c/IdlContainer.class \
	mboth/idl2c/IdlModule.class mboth/idl2c/IdlSpecification.class \
	mboth/idl2c/IdlInterface.class mboth/idl2c/IdlObjRefType.class \
	mboth/idl2c/IdlAttribute.class mboth/idl2c/IdlOperation.class \
	mboth/idl2c/IdlOpParameter.class mboth/idl2c/IdlException.class \
	mboth/idl2c/IdlDeclarator.class \
	mboth/idl2c/IdlVoid.class mboth/idl2c/IdlBoolean.class \
	mboth/idl2c/IdlOctet.class \
	mboth/idl2c/IdlShort.class mboth/idl2c/IdlLong.class \
	mboth/idl2c/IdlLongLong.class \
	mboth/idl2c/IdlChar.class mboth/idl2c/IdlString.class \
	mboth/idl2c/IdlDouble.class mboth/idl2c/IdlFixed.class \
	mboth/idl2c/IdlObject.class mboth/idl2c/IdlValueBase.class \
	mboth/idl2c/IdlValueBox.class mboth/idl2c/IdlValueType.class \
	mboth/idl2c/IdlValStateDcl.class mboth/idl2c/IdlValInitOp.class \
	mboth/idl2c/IdlAny.class mboth/idl2c/IdlNative.class \
	mboth/idl2c/IdlBaseType.class mboth/idl2c/IdlSequence.class \
	mboth/idl2c/IdlTypedef.class mboth/idl2c/IdlStruct.class \
	mboth/idl2c/IdlUnion.class mboth/idl2c/IdlSwitchType.class \
	mboth/idl2c/IdlEnum.class mboth/idl2c/IdlArray.class \
	mboth/idl2c/IdlConst.class mboth/idl2c/IdlConstType.class \
	mboth/idl2c/IdlConstValue.class \
	mboth/idl2c/IdlPositiveInt.class \
	mboth/idl2c/CWriter.class
IDL2C_SRCS = $(IDL2C_OBJS:.class=.java)


# Build IDL2VB compiler, use target IDL2VB.jar to build jar archive
IDL2VB:		mboth/idl2vb/IDL2VB.class

# Build stand alone parser
IDL2LANG:	mboth/idl2lang/IDL2LANG.class

# Build IDL2C compiler
IDL2C:		mboth/idl2c/IDL2C.class

mboth/idl2vb/IDL2VB.class:		mboth/idl2vb/IDL2VB.java\
	${UTIL_OBJS} ${IDL2VB_SRCS} ${IDL2VB_OBJS}

mboth/idl2lang/IDL2LANG.class:	mboth/idl2lang/IDL2LANG.java\
	${UTIL_OBJS} ${IDL2LANG_SRCS} ${IDL2LANG_OBJS}

mboth/idl2c/IDL2C.class:		mboth/idl2c/IDL2C.java\
	${UTIL_OBJS} ${IDL2C_SRCS} ${IDL2C_OBJS}

mboth/idl2vb/IDL2VB.java:
	gcc -E -P -C -xc -DVERSIONNO=\"123\" \
		-DREVISIONDATE=\"$$(date "+%Y-%m-%d")\" $*.jp > $*.java

mboth/idl2lang/IDL2LANG.java:
	gcc -E -P -C -xc -DVERSIONNO=\"100\" \
		-DREVISIONDATE=\"$$(date "+%Y-%m-%d")\" $*.jp > $*.java

mboth/idl2c/IDL2C.java:
	gcc -E -P -C -xc -DVERSIONNO=\"100\" \
		-DREVISIONDATE=\"$$(date "+%Y-%m-%d")\" $*.jp > $*.java


runGRID:
	java mboth.idl2vb.IDL2VB -L 4 -B../demos/grid/generated \
		../demos/grid/grid.idl
	java mboth.idl2vb.IDL2VB -L 4 -S -B../demos/grid_server/generated \
		../demos/grid/grid.idl

runTST:
	rm -f ../demos/test/generated/c*.cls
	rm -f ../demos/test/generated/m*.bas
	java mboth.idl2vb.IDL2VB -L 4 -S -B../demos/test/generated ../demos/test/test.idl

runDB:
	rm -f ../demos/db_query/generated/c*.cls
	rm -f ../demos/db_query/generated/m*.bas
	java mboth.idl2vb.IDL2VB -B../demos/db_query/generated \
		../demos/db_query/database.idl
	rm -f ../demos/db_server/generated/c*.cls
	rm -f ../demos/db_server/generated/m*.bas
	java mboth.idl2vb.IDL2VB -S -B../demos/db_server/generated \
		../demos/db_query/database.idl

runCB:
	rm -f ../demos/callback/generated/c*.cls
	rm -f ../demos/callback/generated/m*.bas
	java mboth.idl2vb.IDL2VB -S -PCBClient -B../demos/callback/generated \
		../demos/callback/client.idl
	java mboth.idl2vb.IDL2VB -S -B../demos/callback/generated \
		../demos/callback/server.idl

run2:
	java mboth.idl2vb.IDL2VB -E -D__STDC__=1 -I/usr/include ../../c/hello.c

run2b:
	java mboth.idl2vb.IDL2VB -E -D_GNU_SOURCE -D__STDC__ \
	-I/usr/include:/usr/include/g++-2.7:/usr/include/g++-2.7/std \
	~/pc2/home/martin/src/geo140c/function.c

runb:
	java mboth.idl2vb.IDL2VB -E -D__GNUC__=2 -D__STDC__=1 -D__GNUC_MINOR__=91 \
	-I/usr/include:/usr/lib/gcc-lib/i486-linux/egcs-2.91.66/include \
	hello.c

run4:
	java mboth.idl2vb.IDL2VB -B../demo CosNaming.idl

runCN:
	rm -f ../naming/generated/c*.cls
	rm -f ../naming/generated/m*.bas
	java mboth.idl2vb.IDL2VB -S -B../naming/generated \
		../naming/CosNaming.idl
	java mboth.idl2vb.IDL2VB -S -B../naming/generated \
		../naming/tnameserv.idl

runOBV:
	rm -f ../demos/obv/valuebox/generated/c*.cls
	rm -f ../demos/obv/valuebox/generated/m*.bas
	java mboth.idl2vb.IDL2VB -SA -B../demos/obv/valuebox/generated \
		../demos/obv/valuebox/box.idl
	rm -f ../demos/obv/valuetype/generated/c*.cls
	rm -f ../demos/obv/valuetype/generated/m*.bas
	java mboth.idl2vb.IDL2VB -SA -B../demos/obv/valuetype/generated \
		../demos/obv/valuetype/valuetype.idl

runORB: IDL2VB.jar
	rm -f ../orb/generated/c*.cls
	rm -f ../orb/generated/m*.bas
	cd ../orb && \
	../bin/idl2vb -XA  -OmVBOrb.VBOrb \
		-B./generated ../include/CORBA.idl && \
	../bin/idl2vb -XA  -OmVBOrb.VBOrb --novalfacts --simplevalues \
		-B./generated -D_TypeCodeImpl ../include/TypeCode.idl && \
	../bin/idl2vb -XSA -OmVBOrb.VBOrb --novalfacts \
		-B./generated ../include/IOP.idl
	cd ../include && \
	../bin/idl2vb -XA  \
		-B./generated ../include/CORBA.idl

runTMP:
	java mboth.idl2vb.IDL2VB -S -Btmp tmp/test2.idl

clean:
	rm -f `find ./mboth -name '*.class'`
	rm -f `find ./mboth/idl2lang -name '*.java'`
	rm -f `find ./mboth/idl2vb -name '*.java'`
	rm -f `find ./mboth/idl2c -name '*.java'`

classes:	mboth/idl2vb/IDL2VB.class
	rm -rf ./classes
	mkdir ./classes
	cp -R ./free ./classes

IDL2VB.jar:
	find gnu -name "*?.?*" | \
		xargs jar -cvmf ./mboth/idl2vb/manifest.txt ../bin/IDL2VB.jar
	find mboth/idl2vb mboth/util -name "*.class" | \
		xargs jar -uvf ../bin/IDL2VB.jar
