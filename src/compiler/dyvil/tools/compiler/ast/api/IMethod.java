package dyvil.tools.compiler.ast.api;

import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IMethod extends IASTNode, IMember, IGeneric, IValued, IThrower, IVariableList, IParameterized, IContext
{
	public void checkArguments(List<Marker> markers, IValue instance, List<IValue> arguments);
	
	public int getSignatureMatch(String name, ITyped instance, List<? extends ITyped> arguments);
	
	// @Bytecode
	
	public default void writePrefixBytecode(MethodWriter writer)
	{
	}
	
	public default void writeInfixBytecode(MethodWriter writer)
	{
	}
	
	public default boolean writePostfixBytecode(MethodWriter writer)
	{
		return false;
	}
	
	// Compilation
	
	public String getDescriptor();
	
	public String getSignature();
	
	public String[] getExceptions();
	
	public void write(ClassWriter writer);
	
	public static IType[] getArgumentTypes(List<IValue> arguments)
	{
		int len = arguments.size();
		IType[] types = new Type[len];
		for (int i = 0; i < len; i++)
		{
			IValue arg = arguments.get(i);
			if (arg == null)
			{
				return null;
			}
			
			IType t = arg.getType();
			if (t == null)
			{
				return null;
			}
			
			types[i] = t;
		}
		return types;
	}
}