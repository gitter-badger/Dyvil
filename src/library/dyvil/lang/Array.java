package dyvil.lang;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.array.BooleanArray;
import dyvil.array.wrapper.BooleanArrayWrapper;

@ArrayConvertible
public interface Array
{
	public static BooleanArray apply(boolean[] array)
	{
		return new BooleanArrayWrapper(array);
	}
	
	public int length();
}
