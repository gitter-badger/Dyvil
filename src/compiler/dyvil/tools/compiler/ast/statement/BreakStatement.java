package dyvil.tools.compiler.ast.statement;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class BreakStatement extends ASTNode implements IStatement
{
	public Label		label;
	public Name			name;
	
	private IStatement	parent;
	
	public BreakStatement(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return BREAK;
	}
	
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.parent == null)
		{
			markers.add(this.position, "break.invalid");
			return this;
		}
		
		if (this.name != null)
		{
			this.label = this.parent.resolveLabel(this.name);
			
			if (this.label == null)
			{
				markers.add(this.position, "resolve.label", this.name);
			}
			else if (this.label.value instanceof ILoop)
			{
				this.label = ((ILoop) this.label.value).getBreakLabel();
			}
			else
			{
				markers.add(this.position, "break.invalid.label", this.name);
			}
		}
		else
		{
			IStatement parent = this.parent;
			while (parent != null)
			{
				if (parent instanceof ILoop)
				{
					this.label = ((ILoop) parent).getBreakLabel();
					break;
				}
				parent = parent.getParent();
			}
			
			if (this.label == null)
			{
				markers.add(this.position, "break.invalid");
			}
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeJumpInsn(Opcodes.GOTO, this.label.target);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		writer.writeJumpInsn(Opcodes.GOTO, this.label.target);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("break");
		if (this.name != null)
		{
			buffer.append(' ').append(this.name);
		}
	}
}
