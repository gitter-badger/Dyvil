package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class PackageParser extends Parser
{
	protected PackageDeclaration	packageDeclaration;
	private StringBuilder			buffer	= new StringBuilder();
	
	public PackageParser(PackageDeclaration pack)
	{
		this.packageDeclaration = pack;
	}
	
	@Override
	public void reset()
	{
		this.packageDeclaration = null;
		this.buffer.delete(0, this.buffer.length());
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Symbols.SEMICOLON)
		{
			this.packageDeclaration.setPackage(this.buffer.toString());
			
			pm.popParser();
			return;
		}
		if (type == Keywords.TYPE)
		{
			this.buffer.append("type");
			return;
		}
		if (type == Keywords.ANNOTATION)
		{
			this.buffer.append("annotation");
			return;
		}
		if (type == Symbols.DOT)
		{
			this.buffer.append('.');
			return;
		}
		if (ParserUtil.isIdentifier(type))
		{
			this.buffer.append(token.nameValue().qualified);
			return;
		}
		throw new SyntaxError(token, "Invalid Package Declaration - Invalid " + token);
	}
}
