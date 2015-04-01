package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class NilValue implements IConstantValue
{
	public static final IType	NIL_CONVERTIBLE	= new Type(Package.dyvilLangLiteral.resolveClass("NilConvertible"));
	
	private ICodePosition		position;
	private IType				requiredType;
	private IMethod				method;
	
	public NilValue()
	{
	}
	
	public NilValue(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return IValue.NULL;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.requiredType == null ? Types.UNKNOWN : this.requiredType;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (this.isType(type))
		{
			this.requiredType = type;
			return this;
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isArrayType() || NIL_CONVERTIBLE.isSuperTypeOf(type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 3 : 0;
	}
	
	@Override
	public Object toObject()
	{
		return null;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.requiredType == null)
		{
			markers.add(this.position, "nil.type");
			return;
		}
		
		if (this.requiredType.isArrayType())
		{
			return;
		}
		
		MethodMatch match = this.requiredType.resolveMethod(null, Name.apply, EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(this.position, "nil.method", this.requiredType.toString());
		}
		else
		{
			this.method = match.method;
		}
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		// Write an array type
		int dims = this.requiredType.getArrayDimensions();
		if (dims > 0)
		{
			for (int i = 0; i < dims; i++)
			{
				writer.writeLDC(0);
			}
			writer.writeNewArray(this.requiredType, dims);
			return;
		}
		
		this.method.writeCall(writer, null, EmptyArguments.INSTANCE, this.requiredType);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public String toString()
	{
		return "nil";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("nil");
	}
}