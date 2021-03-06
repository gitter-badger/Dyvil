package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.Symbols;

public class InferredSemicolon implements IToken
{
	public IToken		prev;
	public IToken		next;
	
	public final int	lineNumber;
	public final int	start;
	
	public InferredSemicolon(int lineNumber, int start)
	{
		this.lineNumber = lineNumber;
		this.start = start;
	}
	
	@Override
	public boolean isInferred()
	{
		return true;
	}
	
	@Override
	public int type()
	{
		return Symbols.SEMICOLON;
	}
	
	@Override
	public int startIndex()
	{
		return this.start;
	}
	
	@Override
	public int endIndex()
	{
		return this.start + 1;
	}
	
	@Override
	public int startLine()
	{
		return this.lineNumber;
	}
	
	@Override
	public int endLine()
	{
		return this.lineNumber;
	}
	
	@Override
	public void setPrev(IToken prev)
	{
		this.prev = prev;
	}
	
	@Override
	public void setNext(IToken next)
	{
		this.next = next;
	}
	
	@Override
	public IToken prev() throws SyntaxError
	{
		if (this.prev == null)
		{
			throw new SyntaxError(this, "Unexpected End of Input");
		}
		return this.prev;
	}
	
	@Override
	public IToken next() throws SyntaxError
	{
		if (this.next == null)
		{
			throw new SyntaxError(this, "Unexpected End of Input");
		}
		return this.next;
	}
	
	@Override
	public IToken getPrev()
	{
		return this.prev;
	}
	
	@Override
	public IToken getNext()
	{
		return this.next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next != null;
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev != null;
	}
	
	@Override
	public ICodePosition raw()
	{
		return new CodePosition(this.lineNumber, this.start, this.start + 1);
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "Inferred Semicolon (line " + this.lineNumber + ")";
	}
}
