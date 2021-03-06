package dyvil.tools.repl;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.operator.Operators;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.InferredSemicolon;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

public class REPLParser implements IParserManager
{
	private TokenIterator	tokens;
	private Parser			parser;
	private int				skip;
	private boolean			reparse;
	
	public boolean parse(TokenIterator tokens, Parser parser)
	{
		this.tokens = tokens;
		this.parser = parser;
		this.skip = 0;
		this.reparse = false;
		
		IToken token = null, prev = null;
		tokens.reset();
		while (tokens.hasNext())
		{
			token = tokens.next();
			token.setPrev(prev);
			prev = token;
		}
		
		if (prev == null)
		{
			return false;
		}
		
		int type = prev.type();
		if (!ParserUtil.isSeperator(type) && type != (Tokens.IDENTIFIER | Tokens.MOD_SYMBOL))
		{
			IToken semicolon = new InferredSemicolon(prev.endLine(), prev.endIndex());
			semicolon.setPrev(prev);
			prev.setNext(semicolon);
		}
		
		tokens.reset();
		while (true)
		{
			if (this.reparse)
			{
				this.reparse = false;
			}
			else
			{
				token = tokens.next();
				
				if (token == null)
				{
					break;
				}
			}
			
			if (this.skip > 0)
			{
				this.skip--;
				continue;
			}
			
			try
			{
				this.parser.parse(this, token);
			}
			catch (SyntaxError ex)
			{
				return false;
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				return false;
			}
			
			if (this.parser == null)
			{
				break;
			}
			
			if (DyvilCompiler.parseStack)
			{
				System.out.println(token + ":\t\t" + this.parser.getName() + " @ " + this.parser.getMode());
			}
		}
		
		return true;
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		Operator op = DyvilREPL.context.getOperator(name);
		if (op != null)
		{
			return op;
		}
		return Operators.map.get(name);
	}
	
	protected boolean isRoot()
	{
		return this.parser == Parser.rootParser;
	}
	
	@Override
	public void skip()
	{
		this.skip++;
	}
	
	@Override
	public void skip(int tokens)
	{
		this.skip += tokens;
	}
	
	@Override
	public void reparse()
	{
		this.reparse = true;
	}
	
	@Override
	public void jump(IToken token)
	{
		this.tokens.jump(token);
	}
	
	@Override
	public void setParser(Parser parser)
	{
		this.parser = parser;
	}
	
	@Override
	public Parser getParser()
	{
		return this.parser;
	}
	
	@Override
	public void pushParser(Parser parser)
	{
		parser.setParent(this.parser);
		this.parser = parser;
	}
	
	@Override
	public void pushParser(Parser parser, boolean reparse)
	{
		parser.setParent(this.parser);
		this.parser = parser;
		this.reparse = reparse;
	}
	
	@Override
	public void popParser()
	{
		this.parser = this.parser.getParent();
	}
	
	@Override
	public void popParser(boolean reparse)
	{
		this.parser = this.parser.getParent();
		this.reparse = reparse;
	}
}
