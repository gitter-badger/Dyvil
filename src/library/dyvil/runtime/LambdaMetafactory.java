package dyvil.runtime;

import java.lang.invoke.*;

public class LambdaMetafactory
{
	private static final Class<?>[]		EMPTY_CLASS_ARRAY	= new Class<?>[0];
	private static final MethodType[]	EMPTY_MT_ARRAY		= new MethodType[0];
	
	public static CallSite metafactory(MethodHandles.Lookup caller, String invokedName, MethodType invokedType, MethodType samMethodType,
			MethodHandle implMethod, MethodType instantiatedMethodType) throws LambdaConversionException
	{
		String type = '<' + invokedType.returnType().getName() + '>';
		return metafactory(caller, invokedName, invokedType, samMethodType, implMethod, instantiatedMethodType, type);
	}
	
	public static CallSite metafactory(MethodHandles.Lookup caller, String invokedName, MethodType invokedType, MethodType samMethodType,
			MethodHandle implMethod, MethodType instantiatedMethodType, String toString) throws LambdaConversionException
	{
		
		AbstractLMF mf = new AnonymousClassLMF(caller, invokedType, invokedName, samMethodType, implMethod, instantiatedMethodType, toString);
		mf.validateMetafactoryArgs();
		return mf.buildCallSite();
	}
}
