package dyvil.collection.iterator;

import java.util.Iterator;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.array.ObjectArray;

@ArrayConvertible
public class ArrayIterator<E> implements Iterator<E>
{
	private int			index;
	private final E[]	array;
	private final int	size;
	
	public static <E> ArrayIterator<E> apply(E... array)
	{
		return new ArrayIterator(array);
	}
	
	public ArrayIterator(E[] array)
	{
		this.array = array;
		this.size = array.length;
	}
	
	public ArrayIterator(E[] array, int size)
	{
		this.array = array;
		this.size = size;
	}
	
	public ArrayIterator(E[] array, int index, int size)
	{
		this.array = array;
		this.index = index;
		this.size = size;
	}
	
	@Override
	public E next()
	{
		return this.array[this.index++];
	}
	
	@Override
	public boolean hasNext()
	{
		return this.index < this.size;
	}
	
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("ArrayIterator(array: ");
		ObjectArray.deepToString(this.array);
		return builder.append(", index: ").append(this.index).append(", end: ").append(this.size).append(')').toString();
	}
}
