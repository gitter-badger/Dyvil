package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.type.Type;

public class LongValue extends ASTObject implements IValue
{
	public long	value;
	
	public LongValue(String value)
	{
		this.value = Long.parseLong(value);
	}
	
	public LongValue(String value, int radix)
	{
		this.value = Long.parseLong(value);
	}
	
	public LongValue(long value)
	{
		this.value = value;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return Type.LONG;
	}
	
	@Override
	public void applyState(CompilerState state)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}
