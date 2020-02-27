
SET JAVAHOME=c:\j2sdk1.4.1_01

%JAVAHOME%\bin\javac -d . -classpath . HelloApp/HelloImpl.java

%JAVAHOME%\bin\rmic -iiop -poa -classpath . HelloApp.HelloImpl
%JAVAHOME%\bin\rmic -d ./idl -idl -noValueMethods -classpath . HelloApp.HelloImpl

%JAVAHOME%\bin\javac -d . -classpath . HelloApp/HelloInterface.java
%JAVAHOME%\bin\javac -d . -classpath . HelloApp/HelloServer.java
%JAVAHOME%\bin\javac -d . -classpath . HelloApp/HelloClient.java

pause
