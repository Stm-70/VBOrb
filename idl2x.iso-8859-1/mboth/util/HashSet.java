
package mboth.util;

import java.util.Hashtable;
import java.util.Enumeration;


public class HashSet
{
	/**
	 */
	private Hashtable htab;
	
	/**
	 */
	public HashSet()
	{
		this.htab= new Hashtable();
	}
	
	/**
	 */
	public boolean add(Object obj)
	{
		return this.htab.put(obj, obj) == null;
	}
	
	/**
	 */
	public boolean contains(Object obj)
	{
		return this.htab.get(obj) != null;
	}
	
	/**
	 */
	public Enumeration elements()
	{
		return this.htab.keys();
	}
}
