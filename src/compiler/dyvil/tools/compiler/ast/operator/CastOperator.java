package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import static dyvil.reflect.Opcodes.*;

public final class CastOperator extends ASTNode implements IValue
{
	public IValue	value;
	public IType	type;
	public boolean	typeHint;
	
	public CastOperator(ICodePosition position, IValue value)
	{
		this.position = position;
		this.value = value;
	}
	
	public CastOperator(IValue value, IType type)
	{
		this.value = value;
		this.type = type;
	}
	
	@Override
	public int valueTag()
	{
		return CAST_OPERATOR;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.type.isPrimitive();
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type.isSuperTypeOf(this.type) ? this : null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context, TypePosition.TYPE);
		this.value.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		if (this.type == Types.VOID)
		{
			markers.add(this.position, "cast.void");
			
			this.value.checkTypes(markers, context);
			return this;
		}
		
		if (!this.type.isResolved())
		{
			return this;
		}
		
		IValue value1 = this.value.withType(this.type, null, markers, context);
		if (value1 != null && value1 != this.value)
		{
			this.value = value1;
			this.typeHint = true;
			this.value.checkTypes(markers, context);
			this.type = value1.getType();
			return this;
		}
		
		if (!this.typeHint && this.type.equals(this.value.getType()))
		{
			markers.add(this.position, "cast.unnecessary");
			this.typeHint = true;
		}
		
		this.value.checkTypes(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
		
		if (this.typeHint)
		{
			return;
		}
		
		boolean primitiveType = this.type.isPrimitive();
		boolean primitiveValue = this.value.isPrimitive();
		if (primitiveType)
		{
			if (!primitiveValue)
			{
				markers.add(this.position, "cast.reference");
			}
		}
		else if (primitiveValue)
		{
			markers.add(this.position, "cast.primitive");
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.typeHint)
		{
			return this.value.cleanup(context, compilableList);
		}
		
		this.value = this.value.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.value.writeExpression(writer);
		if (this.typeHint)
		{
			return;
		}
		
		if (this.type.isPrimitive())
		{
			writePrimitiveCast(this.value.getType(), (PrimitiveType) this.type, writer);
		}
		else
		{
			writer.writeTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(this.type.getReturnOpcode());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append(" as ");
		this.type.toString(prefix, buffer);
	}
	
	public static void writePrimitiveCast(IType value, PrimitiveType cast, MethodWriter writer) throws BytecodeException
	{
		IClass iclass = value.getTheClass();
		if (iclass == Types.BYTE_CLASS || iclass == Types.SHORT_CLASS || iclass == Types.CHAR_CLASS || iclass == Types.INT_CLASS)
		{
			writeIntCast(cast, writer);
			return;
		}
		if (iclass == Types.LONG_CLASS)
		{
			writeLongCast(cast, writer);
			return;
		}
		if (iclass == Types.FLOAT_CLASS)
		{
			writeFloatCast(cast, writer);
			return;
		}
		if (iclass == Types.DOUBLE_CLASS)
		{
			writeDoubleCast(cast, writer);
			return;
		}
	}
	
	private static void writeIntCast(PrimitiveType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.typecode)
		{
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			break;
		case ClassFormat.T_LONG:
			writer.writeInsn(I2L);
			break;
		case ClassFormat.T_FLOAT:
			writer.writeInsn(I2F);
			break;
		case ClassFormat.T_DOUBLE:
			writer.writeInsn(I2D);
			break;
		}
	}
	
	private static void writeLongCast(PrimitiveType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			writer.writeInsn(L2I);
			break;
		case ClassFormat.T_BYTE:
			writer.writeInsn(L2B);
			break;
		case ClassFormat.T_SHORT:
			writer.writeInsn(L2S);
			break;
		case ClassFormat.T_CHAR:
			writer.writeInsn(L2C);
			break;
		case ClassFormat.T_INT:
			writer.writeInsn(L2I);
			break;
		case ClassFormat.T_LONG:
			break;
		case ClassFormat.T_FLOAT:
			writer.writeInsn(L2F);
			break;
		case ClassFormat.T_DOUBLE:
			writer.writeInsn(L2D);
			break;
		}
	}
	
	private static void writeFloatCast(PrimitiveType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			writer.writeInsn(F2I);
			break;
		case ClassFormat.T_BYTE:
			writer.writeInsn(F2B);
			break;
		case ClassFormat.T_SHORT:
			writer.writeInsn(F2S);
			break;
		case ClassFormat.T_CHAR:
			writer.writeInsn(F2C);
			break;
		case ClassFormat.T_INT:
			writer.writeInsn(F2I);
			break;
		case ClassFormat.T_LONG:
			writer.writeInsn(F2L);
			break;
		case ClassFormat.T_FLOAT:
			break;
		case ClassFormat.T_DOUBLE:
			writer.writeInsn(F2D);
			break;
		}
	}
	
	private static void writeDoubleCast(PrimitiveType cast, MethodWriter writer) throws BytecodeException
	{
		switch (cast.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			writer.writeInsn(D2I);
			break;
		case ClassFormat.T_BYTE:
			writer.writeInsn(D2B);
			break;
		case ClassFormat.T_SHORT:
			writer.writeInsn(D2S);
			break;
		case ClassFormat.T_CHAR:
			writer.writeInsn(D2C);
			break;
		case ClassFormat.T_INT:
			writer.writeInsn(D2I);
			break;
		case ClassFormat.T_LONG:
			writer.writeInsn(D2L);
			break;
		case ClassFormat.T_FLOAT:
			writer.writeInsn(D2F);
			break;
		case ClassFormat.T_DOUBLE:
			break;
		}
	}
}
