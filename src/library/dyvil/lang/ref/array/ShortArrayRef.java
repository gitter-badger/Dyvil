package dyvil.lang.ref.array;

import dyvil.lang.ref.ShortRef;

public class ShortArrayRef implements ShortRef
{
	protected final short[]	array;
	protected final int		index;
	
	public ShortArrayRef(short[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public short get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(short value)
	{
		this.array[this.index] = value;
	}
}
