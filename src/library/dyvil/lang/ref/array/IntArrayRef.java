package dyvil.lang.ref.array;

import dyvil.lang.ref.IntRef;

public class IntArrayRef implements IntRef
{
	protected final int[]	array;
	protected final int		index;
	
	public IntArrayRef(int[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public int get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(int value)
	{
		this.array[this.index] = value;
	}
}
