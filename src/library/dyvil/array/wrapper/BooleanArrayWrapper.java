package dyvil.array.wrapper;

import dyvil.array.BooleanArray;

public class BooleanArrayWrapper implements BooleanArray
{
	private boolean[]	array;
	
	public BooleanArrayWrapper(boolean[] array)
	{
		this.array = array;
	}
	
	@Override
	public int length()
	{
		return this.array.length;
	}
	
	@Override
	public boolean subscript(int index)
	{
		return this.array[index];
	}
	
	@Override
	public void subscript_$eq(int index, boolean value)
	{
		this.array[index] = value;
	}
	
	@Override
	public boolean[] toArray()
	{
		int len = this.array.length;
		boolean[] newArray = new boolean[len];
		System.arraycopy(this.array, 0, newArray, 0, len);
		return newArray;
	}
	
	@Override
	public String toString()
	{
		return BooleanArray.toString(this.array);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (obj instanceof boolean[])
		{
			return BooleanArray.equals(this.array, (boolean[]) obj);
		}
		if (!(obj instanceof BooleanArray))
		{
			return false;
		}
		return BooleanArray.equals(this, (BooleanArray) obj);
	}
	
	@Override
	public int hashCode()
	{
		return BooleanArray.hashCode(this.array);
	}
}
