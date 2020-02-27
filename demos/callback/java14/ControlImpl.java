// Part of server example (compile together with CBClient.java).

import Client.*;				// The package containing our skeleton.
import Server.Counter;

public class ControlImpl extends ControlPOA
{
	public int iClId;
	public Counter cntRef;
	
	/** (ControlOperations)
	 */
	public void setId(int id)
	{
		System.out.println("setId " + id);
		iClId= id;
	}

	/** (ControlOperations)
	 */
	public int getId()
	{
		System.out.println("getId");
		return iClId;
	}

	/** (ControlOperations)
	 */
	public void start()
	{
		System.out.println("start");
	}

	/** (ControlOperations)
	 */
	public String stop()
	{
		System.out.println("stop");
		return "sum= " + cntRef.sum();
	}

	/** (ControlOperations)
	 */
	public void sendBigArrayToClient(byte[] buffer)
	{
		System.out.println("sendBigArrayToClient");
	}

	/** (ControlOperations)
	 */
	public void sendWStringToClient(String wstr)
	{
		System.out.println("sendWStringToClient " + wstr);
	}
}
