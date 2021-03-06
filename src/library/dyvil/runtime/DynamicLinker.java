package dyvil.runtime;

import java.lang.invoke.*;

public class DynamicLinker
{
	public static CallSite linkMethod(MethodHandles.Lookup callerClass, String name, MethodType type) throws Throwable
	{
		Class clazz = type.parameterType(0);
		type = type.dropParameterTypes(0, 1);
		MethodHandle handle = callerClass.findStatic(clazz, name, type);
		if (!type.equals(handle.type()))
		{
			handle = handle.asType(type);
		}
		return new ConstantCallSite(handle);
	}
	
	public static CallSite linkGetter(MethodHandles.Lookup callerClass, String name, Class type) throws Throwable
	{
		MethodHandle handle = callerClass.findGetter(DynamicLinker.class, name, type);
		return new ConstantCallSite(handle);
	}
	
	public static CallSite linkSetter(MethodHandles.Lookup callerClass, String name, Class type) throws Throwable
	{
		MethodHandle handle = callerClass.findSetter(DynamicLinker.class, name, type);
		return new ConstantCallSite(handle);
	}
}
