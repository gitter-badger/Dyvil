package dyvil.reflect.type;

import dyvil.lang.Type;
import dyvil.lang.literal.NilConvertible;

import dyvil.annotation.object;

@NilConvertible
public @object class AnyType implements Type<Object>
{
	public static final AnyType	instance	= new AnyType();
	
	public static AnyType apply()
	{
		return instance;
	}
	
	@Override
	public Class<Object> getTheClass()
	{
		return null;
	}
	
	@Override
	public String getName()
	{
		return "any";
	}
	
	@Override
	public String toString()
	{
		return "any";
	}
	
	@Override
	public String getQualifiedName()
	{
		return "dyvil/lang/Any";
	}
	
	@Override
	public void appendSignature(StringBuilder builder)
	{
		builder.append("Ljava/lang/Object;");
	}
}
