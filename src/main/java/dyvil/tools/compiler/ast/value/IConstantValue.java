package dyvil.tools.compiler.ast.value;

import java.util.List;

import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IConstantValue extends IValue
{
	@Override
	public default boolean isConstant()
	{
		return true;
	}
	
	@Override
	public default void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public default IValue resolve(List<Marker> markers, IContext context)
	{
		return this;
	}
	
	@Override
	public default void check(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public default IValue foldConstants()
	{
		return this;
	}
}
