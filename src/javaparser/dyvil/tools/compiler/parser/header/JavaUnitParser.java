package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.classes.JavaClassParser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Tokens;

public class JavaUnitParser extends Parser
{
	protected static final int	PACKAGE	= 1;
	protected static final int	IMPORT	= 2;
	protected static final int	CLASS	= 4;
	
	protected IDyvilHeader		unit;
	
	public JavaUnitParser(IDyvilHeader unit)
	{
		this.unit = unit;
		this.mode = PACKAGE;
	}
	
	@Override
	public void reset()
	{
		this.mode = PACKAGE;
	}
	
	public static boolean isOpenAngle(IToken token)
	{
		return token.type() == Tokens.SYMBOL_IDENTIFIER && token.nameValue() == Name.lt;
	}
	
	public static boolean isClosingAngle(IToken token)
	{
		return token.type() == Tokens.SYMBOL_IDENTIFIER && token.nameValue() == Name.gt;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		switch (this.mode)
		{
		case PACKAGE:
			if (type == Keywords.PACKAGE)
			{
				PackageDeclaration pack = new PackageDeclaration(token.raw());
				this.unit.setPackageDeclaration(pack);
				pm.pushParser(new PackageParser(pack));
				return;
			}
		case IMPORT:
			if (type == Keywords.IMPORT)
			{
				if (token.next().type() == Keywords.STATIC)
				{
					pm.skip();
					ImportDeclaration i = new ImportDeclaration(token.raw(), true);
					pm.pushParser(new ImportParser(im -> {
						i.setImport(im);
						this.unit.addUsing(i);
					}));
					return;
				}
				
				ImportDeclaration i = new ImportDeclaration(token.raw());
				pm.pushParser(new ImportParser(im -> {
					i.setImport(im);
					this.unit.addImport(i);
				}));
				return;
			}
		case CLASS:
			this.mode = CLASS;
			pm.pushParser(new JavaClassParser(this.unit), true);
			return;
		}
		throw new SyntaxError(token, "Invalid " + token + " - Delete this token");
	}
}
