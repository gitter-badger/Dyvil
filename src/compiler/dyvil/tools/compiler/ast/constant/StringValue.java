package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class StringValue extends ASTNode implements IConstantValue
{
	public static final IClass	STRING_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("StringConvertible");
	
	public String				value;
	
	public StringValue(String value)
	{
		this.value = value;
	}
	
	public StringValue(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int valueTag()
	{
		return STRING;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public ClassType getType()
	{
		return Types.STRING;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.isSuperTypeOf(Types.STRING))
		{
			return this;
		}
		if (type.getTheClass().getAnnotation(STRING_CONVERTIBLE) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(Types.STRING) || type.getTheClass().getAnnotation(STRING_CONVERTIBLE) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(STRING_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.STRING);
	}
	
	@Override
	public String toObject()
	{
		return this.value;
	}
	
	@Override
	public int stringSize()
	{
		return this.value.length();
	}
	
	@Override
	public boolean toStringBuilder(StringBuilder builder)
	{
		builder.append(this.value);
		return true;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.value);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.value);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.value.length();
		buffer.ensureCapacity(buffer.length() + len + 3);
		buffer.append('"');
		append(this.value, len, buffer);
		buffer.append('"');
	}
	
	public static void append(String value, int len, StringBuilder buffer)
	{
		for (int i = 0; i < len; i++)
		{
			char c = value.charAt(i);
			switch (c)
			{
			case '"':
				buffer.append("\\\"");
				continue;
			case '\\':
				buffer.append("\\\\");
				continue;
			case '\n':
				buffer.append("\\n");
				continue;
			case '\t':
				buffer.append("\\t");
				continue;
			case '\r':
				buffer.append("\\r");
				continue;
			case '\b':
				buffer.append("\\b");
				continue;
			case '\f':
				buffer.append("\\f");
				continue;
			}
			buffer.append(c);
		}
	}
}
