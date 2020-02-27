// Part of server example (compile together with CBServer.java).

import Server.*;				// The package containing our skeleton.

public class CounterImpl extends CounterPOA
{
	private int iSum;
	
	/** (CounterOperations)
	 */
	public int sum()
	{
		System.out.println("CounterImpl.sum= " + iSum);
		return iSum;
	}
	
	/** (CounterOperations)
	 */
	public void sum(int newSum)
	{	
		System.out.println("CounterImpl.sum(" + newSum + ")");
		iSum= newSum;
	}

	/** (CounterOperations)
	 */
	public int increment()
	{
		//System.out.println("CounterImpl.increment");
		return ++iSum;
	}
}
