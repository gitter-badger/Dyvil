package dyvil.tools.compiler.parser.annotation;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ArgumentMap;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.type.NamedType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionListParser;
import dyvil.tools.compiler.parser.expression.ExpressionMapParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class AnnotationParser extends Parser
{
	public static final int	NAME				= 1;
	public static final int	PARAMETERS_START	= 2;
	public static final int	PARAMETERS_END		= 4;
	
	private Annotation		annotation;
	
	public AnnotationParser(Annotation annotation)
	{
		this.annotation = annotation;
		this.mode = NAME;
	}
	
	@Override
	public void reset()
	{
		this.mode = NAME;
		this.annotation = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == NAME)
		{
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				this.annotation.name = name;
				this.annotation.type = new NamedType(token.raw(), name);
				
				this.mode = PARAMETERS_START;
				return;
			}
			throw new SyntaxError(token, "Invalid Annotation - Name expected");
		}
		if (this.mode == PARAMETERS_START)
		{
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				IToken next = token.next();
				if (ParserUtil.isIdentifier(next.type()) && next.next().type() == Symbols.COLON)
				{
					ArgumentMap map = new ArgumentMap();
					this.annotation.arguments = map;
					pm.pushParser(new ExpressionMapParser(map));
				}
				else
				{
					ArgumentList list = new ArgumentList();
					this.annotation.arguments = list;
					pm.pushParser(new ExpressionListParser(list));
				}
				
				this.mode = PARAMETERS_END;
				return;
			}
			
			if (type == Symbols.OPEN_SQUARE_BRACKET)
			{
				SingleArgument arg = new SingleArgument();
				this.annotation.arguments = arg;
				pm.popParser();
				pm.pushParser(pm.newExpressionParser(arg), true);
				return;
			}
			
			pm.popParser(true);
			return;
		}
		if (this.mode == PARAMETERS_END)
		{
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				pm.popParser();
				return;
			}
			throw new SyntaxError(token, "Invalid Annotation - ')' expected");
		}
	}
}
