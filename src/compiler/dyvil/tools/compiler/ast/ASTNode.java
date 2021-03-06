package dyvil.tools.compiler.ast;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class ASTNode implements IASTNode
{
	public ICodePosition	position;
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public int getLineNumber()
	{
		return this.position == null ? 0 : this.position.startLine();
	}
	
	@Override
	public void expandPosition(ICodePosition position)
	{
		if (this.position != null)
		{
			this.position = this.position.to(position);
		}
		else
		{
			this.position = position.raw();
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		this.toString("", buffer);
		return buffer.toString();
	}
	
	@Override
	public abstract void toString(String prefix, StringBuilder buffer);
}
