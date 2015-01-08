package dyvil.tools.compiler.ast.api;

public interface IProperty extends IField
{
	public void setGetter(IValue get);
	
	public IValue getGetter();
	
	public void setSetter(IValue set);
	
	public IValue getSetter();
	
	public void setGetterMethod(IMethod method);
	
	public void setSetterMethod(IMethod method);
}
