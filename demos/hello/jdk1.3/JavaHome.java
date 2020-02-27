// Show Java Home
// JDK 1.3.1: Compile by c:\jdk1.3.1\bin\javac JavaHome.java
//            Calling by c:\jdk1.3.1\bin\java -classpath . JavaHome

public class JavaHome
{
	public static void main(String [] args)
	{
		System.out.println("You have to put your 'ORB.properties' in : ");
		System.out.println( System.getProperty("java.home") + java.io.File.separator + "lib" );
	}
}
