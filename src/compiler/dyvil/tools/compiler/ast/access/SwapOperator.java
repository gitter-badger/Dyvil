package dyvil.tools.compiler.ast.access;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SwapOperator extends ASTNode implements IValue
{
	public FieldAccess	left;
	public FieldAccess	right;
	
	public SwapOperator(ICodePosition position, FieldAccess left, FieldAccess right)
	{
		this.position = position;
		this.left = left;
		this.right = right;
	}
	
	@Override
	public int getValueType()
	{
		return SWAP_OPERATOR;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.left.resolveTypes(markers, context);
		this.right.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.left.resolve(markers, context);
		this.right.resolve(markers, context);
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		this.left.check(markers, context);
		this.right.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		IValue leftInstance = this.left.instance;
		IField leftField = this.left.field;
		IValue rightInstance = this.right.instance;
		IField rightField = this.right.field;
		
		if (leftInstance != null)
		{
			leftInstance.writeExpression(writer);
		}
		leftField.writeGet(writer);
		
		if (rightInstance != null)
		{
			rightInstance.writeExpression(writer);
		}
		rightField.writeGet(writer);
		
		if (leftInstance != null)
		{
			leftInstance.writeExpression(writer);
		}
		leftField.writeSet(writer);
		
		if (rightInstance != null)
		{
			rightInstance.writeExpression(writer);
		}
		rightField.writeSet(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.left.toString(prefix, buffer);
		buffer.append(" :=: ");
		this.right.toString(prefix, buffer);
	}
}