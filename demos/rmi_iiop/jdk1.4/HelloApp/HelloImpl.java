/* HelloImpl.java
*/

package HelloApp;

import java.util.HashMap;
import javax.rmi.PortableRemoteObject;

/**
 */
public class HelloImpl extends PortableRemoteObject implements HelloInterface
{
	/**
	 */
	public HelloImpl() throws java.rmi.RemoteException
	{	super();	// invoke rmi linking and remote object initialization
	}

	/** (HelloInterface)
	 */
	public void sayHello() throws java.rmi.RemoteException
	{	System.out.println("It works!  Hello World!!");
	}

	/**
	 */
	private Integer intObj= new Integer(77);
	
	/** (HelloInterface)
	 */
	public Integer getIntegerObj() throws java.rmi.RemoteException
	{	return intObj;
	}

	/** (HelloInterface)
	 */
	public void setIntegerObj(Integer intobj) throws java.rmi.RemoteException
	{	System.out.println("Setting Integer to " +
			((intobj == null)? "null": intobj.toString()));
		this.intObj= intobj;
	}

	/**
	 */
	private char ch= '\u1234';

	/** (HelloInterface)
	 */
	public char getChar() throws java.rmi.RemoteException
	{	return ch;
	}

	/** (HelloInterface)
	 */
	public void setChar(char ch) throws java.rmi.RemoteException
	{	System.out.println("Setting Char to '" + ch + "' = \\u" +
			Integer.toString(ch, 16));
		this.ch= ch;
	}

	/** (HelloInterface)
	 */
	public byte[] getBytes() throws java.rmi.RemoteException
	{	return null;
	}

	/** (HelloInterface)
	 */
	public void setBytes(byte[] barr) throws java.rmi.RemoteException
	{	
	}

	/**
	 */
	private String strObj= "Example of a Java String";

	/** (HelloInterface)
	 */
	public String getString() throws java.rmi.RemoteException
	{	return strObj;
	}

	/** (HelloInterface)
	 */
	public void setString(String str) throws java.rmi.RemoteException
	{	System.out.println("Setting String to " +
			((str == null)? "null": "\"" + str + "\""));
		this.strObj= str;
	}

	/**
	 */
	private HashMap hmObj;

	/** (HelloInterface)
	 */
	public HashMap getHashMap(int typ) throws java.rmi.RemoteException
	{
		HashMap hm= new HashMap();
		if(typ == 1)
		{	hm.put(new Integer(8), new Integer(16));
		}else if(typ == 2)
		{	hm.put(new Integer(8), new Integer(16));
			hm.put(new Integer(16), new Integer(32));
		}else if(typ == 3)
		{	hm.put(new Integer(1), "one");
			// If VBOrb does not send codesets -> Exception in Java server
			//  -> Exception is send in ServiceContext 9 by JDK ORB.
			//    -> Exception is so big that JDK ORB is using IIOP fragments!
				hm.put(new Integer(4), "four");
		}else if(typ == 4)
		{	HashMap hm2= new HashMap();
			hm2.put(new Integer(8), new Integer(16));
			hm.put(new Integer(16), hm2);
		}else if(typ == 5)
		{	byte barr[]= new byte[1000000];
			for(int i= 0; i < barr.length; i++)
				barr[i]= (byte)i;
			hm.put(new Integer(5), barr);
		}else
		{	hm= hmObj;
		}
		return hm;
	}

	/** (HelloInterface)
	 */
	public void setHashMap(HashMap hm) throws java.rmi.RemoteException
	{	this.hmObj= hm;
	}
}
