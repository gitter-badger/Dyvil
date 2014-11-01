package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IAccess extends INamed, IValue, IValued, IValueList
{
	public boolean resolve(IContext context);
	
	public IAccess resolve2(IContext context);
	
	public Marker getResolveError();
	
	@Override
	public IValue getValue();
	
	@Override
	public List<IValue> getValues();
}