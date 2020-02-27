/* HelloInterface.java
*/

package HelloApp;

import java.util.HashMap;

public interface HelloInterface extends java.rmi.Remote
{
	public void sayHello() throws java.rmi.RemoteException;
	
	public Integer getIntegerObj() throws java.rmi.RemoteException;
	public void setIntegerObj(Integer intobj) throws java.rmi.RemoteException;

	public char getChar() throws java.rmi.RemoteException;
	public void setChar(char ch) throws java.rmi.RemoteException;

	public byte[] getBytes() throws java.rmi.RemoteException;
	public void setBytes(byte[] barr) throws java.rmi.RemoteException;

	public String getString() throws java.rmi.RemoteException;
	public void setString(String str) throws java.rmi.RemoteException;

	public HashMap getHashMap(int typ) throws java.rmi.RemoteException;
	public void setHashMap(HashMap hm) throws java.rmi.RemoteException;
}
