package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.generic.type.GenericType;
import dyvil.tools.compiler.ast.generic.type.NamedGenericType;
import dyvil.tools.compiler.ast.type.ArrayType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.NamedType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.header.JavaUnitParser;
import dyvil.tools.compiler.parser.type.TypeListParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public class JavaTypeParser extends Parser
{
	protected static final int	NAME		= 1;
	protected static final int	ARRAY_END	= 2;
	protected static final int	GENERIC_END	= 4;
	
	protected ITypeConsumer		typeConsumer;
	private IType				type;
	
	public JavaTypeParser(ITypeConsumer typeConsumer)
	{
		this.typeConsumer = typeConsumer;
		this.mode = NAME;
	}
	
	@Override
	public void reset()
	{
		this.type = null;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		switch (this.mode)
		{
		case NAME:
			if (type != Tokens.LETTER_IDENTIFIER)
			{
				pm.popParser(true);
				throw new SyntaxError(token, "Invalid Type - Name expected");
			}
			IToken next = token.next();
			if (JavaUnitParser.isOpenAngle(next))
			{
				GenericType gt = new NamedGenericType(token.raw(), token.nameValue());
				this.type = gt;
				pm.skip();
				pm.pushParser(new TypeListParser(gt));
				this.mode = GENERIC_END;
				return;
			}
			this.type = new NamedType(token.raw(), token.nameValue());
			if (next.type() == Symbols.OPEN_SQUARE_BRACKET)
			{
				pm.skip();
				this.mode = ARRAY_END;
				return;
			}
			
			this.typeConsumer.setType(this.type);
			pm.popParser();
			return;
		case ARRAY_END:
			this.type = new ArrayType(this.type);
			if (token.next().type() == Symbols.OPEN_SQUARE_BRACKET)
			{
				pm.skip();
			}
			else
			{
				pm.popParser();
				this.typeConsumer.setType(this.type);
			}
			if (type == Symbols.CLOSE_SQUARE_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Array Type - ']' expected");
		case GENERIC_END:
			if (JavaUnitParser.isClosingAngle(token))
			{
				this.typeConsumer.setType(this.type);
				pm.popParser();
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type - '>' expected");
		}
	}
}
