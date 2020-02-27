@echo off
REM call JDK12 interpreter with appropriate options

SET IDLVB=C:/vb/bin/IDL2VB.jar
SET IDLVB=H:/work/vb/bin/IDL2VB.jar
echo java -classpath %IDLVB% mboth.idl2vb.IDL2VB %1 %2 %3 %4 %5 %6
java -classpath %IDLVB% mboth.idl2vb.IDL2VB %1 %2 %3 %4 %5 %6
