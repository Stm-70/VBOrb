SET JAVAHOME=c:\j2sdk1.4.1_01

echo "Type Control + C to stop the Name Service"

rem start %JAVAHOME%\bin\orbd -ORBInitialPort 2809

%JAVAHOME%\bin\tnameserv -ORBInitialPort 2809

pause
