package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class FieldInitializer extends ASTNode implements IValue, IValued
{
	public Variable	variable;
	
	public FieldInitializer(ICodePosition position, Name name, IType type)
	{
		this.position = position;
		this.variable = new Variable(this.position, name, type);
	}
	
	@Override
	public int valueTag()
	{
		return VARIABLE;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.variable.type.isPrimitive();
	}
	
	@Override
	public void setType(IType type)
	{
		this.variable.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.variable.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.variable.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.variable.value;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.variable.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.variable.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.variable.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.variable.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.variable.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.variable.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.variable.writeInit(writer, this.variable.value);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}
