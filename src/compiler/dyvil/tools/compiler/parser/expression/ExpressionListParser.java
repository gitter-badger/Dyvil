package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public final class ExpressionListParser extends Parser implements IValued
{
	protected IValueList	valueList;
	
	private String			label;
	
	public ExpressionListParser(IValueList valueList)
	{
		this.valueList = valueList;
	}
	
	@Override
	public void reset()
	{
		this.mode = 0;
		this.label = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (ParserUtil.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}
		
		if (this.mode == 0)
		{
			if (token.next().type() == Tokens.COLON)
			{
				this.label = token.value();
				pm.skip();
				return;
			}
			
			this.mode = 1;
			pm.pushParser(new ExpressionParser(this), true);
			return;
		}
		if (this.mode == 1)
		{
			if (type == Tokens.COMMA)
			{
				this.valueList.setArray(true);
				this.mode = 0;
				return;
			}
			if (type == Tokens.SEMICOLON)
			{
				this.mode = 0;
				return;
			}
			if (token.prev().type() == Tokens.CLOSE_CURLY_BRACKET)
			{
				pm.pushParser(new ExpressionParser(this), true);
				return;
			}
			throw new SyntaxError(token, "Invalid Token '" + token.value() + "'");
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.valueList.addValue(value);
		
		if (this.label != null)
		{
			this.valueList.addLabel(this.label, value);
			this.label = null;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}
