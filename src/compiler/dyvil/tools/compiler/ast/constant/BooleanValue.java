package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.BoxedValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public final class BooleanValue extends ASTNode implements IConstantValue
{
	public static final IClass			BOOLEAN_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("BooleanConvertible");
	
	public static final BooleanValue	TRUE				= new BooleanValue(true);
	public static final BooleanValue	FALSE				= new BooleanValue(false);
	
	public boolean						value;
	
	public BooleanValue(boolean value)
	{
		this.value = value;
	}
	
	public BooleanValue(ICodePosition position, boolean value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int valueTag()
	{
		return BOOLEAN;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return Types.BOOLEAN;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.BOOLEAN)
		{
			return this;
		}
		if (type.isSuperTypeOf(Types.BOOLEAN))
		{
			return new BoxedValue(this, Types.BOOLEAN.boxMethod);
		}
		if (type.getTheClass().getAnnotation(BOOLEAN_CONVERTIBLE) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.BOOLEAN || type.isSuperTypeOf(Types.BOOLEAN) || type.getTheClass().getAnnotation(BOOLEAN_CONVERTIBLE) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(BOOLEAN_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		return type.getSubTypeDistance(Types.BOOLEAN);
	}
	
	@Override
	public Boolean toObject()
	{
		return Boolean.valueOf(this.value);
	}
	
	@Override
	public int stringSize()
	{
		return this.value ? 4 : 5;
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
		if (this.value)
		{
			writer.writeLDC(1);
		}
		else
		{
			writer.writeLDC(0);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.IRETURN);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		if (this.value)
		{
			writer.writeJumpInsn(Opcodes.GOTO, dest);
		}
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest) throws BytecodeException
	{
		if (!this.value)
		{
			writer.writeJumpInsn(Opcodes.GOTO, dest);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}
