
SET IDLDIR=jdk1.4/idl
SET IDL2VB=java -jar ..\..\bin\IDL2VB.jar -S --simplevalues -Bgenerated -I..\..\include;%IDLDIR%

%IDL2VB% %IDLDIR%/HelloApp/HelloInterface.idl
%IDL2VB% %IDLDIR%/java/lang/Number.idl
%IDL2VB% %IDLDIR%/java/lang/Comparable.idl
%IDL2VB% %IDLDIR%/java/lang/Cloneable.idl
%IDL2VB% %IDLDIR%/java/util/AbstractMap.idl
%IDL2VB% %IDLDIR%/java/util/Map.idl

pause
