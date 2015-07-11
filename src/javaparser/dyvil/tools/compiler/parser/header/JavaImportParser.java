package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.consumer.IImportConsumer;
import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.imports.PackageImport;
import dyvil.tools.compiler.ast.imports.SimpleImport;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public class JavaImportParser extends Parser
{
	protected static final int	NAME	= 1;
	protected static final int	DOT		= 2;
	
	protected IImportConsumer	consumer;
	private Import				currentImport;
	
	public JavaImportParser(IImportConsumer consumer)
	{
		this.mode = NAME;
	}
	
	@Override
	public void reset()
	{
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		switch (this.mode)
		{
		case NAME:
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				Import parent = this.currentImport;
				this.currentImport = new SimpleImport(token.raw(), token.nameValue());
				this.currentImport.setParent(parent);
				this.mode = DOT;
				return;
			}
			if (type == Tokens.SYMBOL_IDENTIFIER)
			{
				Name name = token.nameValue();
				if (name == Name.times)
				{
					Import parent = this.currentImport;
					PackageImport pi = new PackageImport(token.raw());
					pi.setParent(parent);
					this.consumer.setImport(pi);
					pm.popParser();
					return;
				}
			}
			throw new SyntaxError(token, "Invalid " + token + " - Delete this token");
		case DOT:
			if (type == Symbols.SEMICOLON)
			{
				this.consumer.setImport(this.currentImport);
				pm.popParser(true);
				return;
			}
			this.mode = NAME;
			if (type == Symbols.DOT)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Import Declaration - Identifier expected");
		}
	}
}
