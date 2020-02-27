/*
   Copyright (c) 1999 Martin.Both
*/

package mboth.util;

/**
 * @author  Martin Both
 */

public class ArrayList
{
	/** The array
	 */
	private Object objs[];
	
	/** Size
	 */
	private int size;
	
	/** 
	 */
	public ArrayList()
	{
		this.objs= new Object[16];
		this.size= 0;
	}

	/**
	 */
	public int size()
	{
		return this.size;
	}
	
	/**
	 */
	public void add(Object obj)
	{
		if(this.size >= this.objs.length)
		{
			Object newObjs[]= new Object[newsize(this.size + 1)];
			System.arraycopy(this.objs, 0, newObjs, 0, this.size);
			this.objs= newObjs;
		}
		this.objs[this.size++]= obj;
	}
	
	/**
	 */
	public void insert(int index, Object obj)
	{
		if(this.size >= this.objs.length)
		{
			Object newObjs[]= new Object[newsize(this.size + 1)];
			System.arraycopy(this.objs, 0, newObjs, 0, index);
			System.arraycopy(this.objs, index, newObjs, index + 1,
				this.size - index);
			this.objs= newObjs;
		}else
		{	System.arraycopy(this.objs, index, this.objs, index + 1,
				this.size - index);
		}
		this.size++;
		this.objs[index]= obj;
	}

	/**
	 */
	private int newsize(int minCapacity)
	{
		int newsize;
		if(this.size < 8)
			newsize= 16;
		else if(this.size > 65536)
			newsize= this.size + this.size / 2;
		else
			newsize= this.size + this.size;
		if(newsize < minCapacity)
			newsize= minCapacity;
		return newsize;
	}
	
	/**
	 */
	public void remove(int index)
	{
		this.objs[index]= null;
		this.size--;
		System.arraycopy(this.objs, index + 1, this.objs, index,
			this.size - index);
	}

	/**
	 */
	public Object get(int index)
	{
		if(index >= this.size)
			throw new IndexOutOfBoundsException(index + " >= " + this.size);

		return this.objs[index];
	}

	/**
	 */
	public void clear()
	{	while(this.size > 0)
		{	this.objs[--this.size]= null;
		}
	}
	
	/**
	 */
	public void copyInto(Object[] anArray)
	{
		System.arraycopy(this.objs, 0, anArray, 0, this.size);
	}

	/**
	 */
	public Object[] toArray()
	{
		Object newObjs[]= new Object[this.size];
		System.arraycopy(this.objs, 0, newObjs, 0, this.size);
		return newObjs;
	}
}
