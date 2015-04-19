package dyvil.random;

import dyvil.annotation.sealed;
import dyvil.lang.Random;

public final @sealed class JavaBasedRandom extends java.util.Random implements Random
{
	private static final long	serialVersionUID	= -6648049590269700311L;
	
	public JavaBasedRandom()
	{
		super();
	}
	
	public JavaBasedRandom(long seed)
	{
		super(seed);
	}
	
	@Override
	public int next(int bits)
	{
		return super.next(bits);
	}
}
