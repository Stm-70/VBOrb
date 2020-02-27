
SET JAVAHOME=c:\j2sdk1.4.1_01

%JAVAHOME%\bin\java -classpath . -Djava.naming.factory.initial=com.sun.jndi.cosnaming.CNCtxFactory -Djava.naming.provider.url=iiop://localhost:2809 HelloApp.HelloClient

pause
