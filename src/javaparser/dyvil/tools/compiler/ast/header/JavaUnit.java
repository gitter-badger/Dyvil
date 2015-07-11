package dyvil.tools.compiler.ast.header;

import java.io.File;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.DyvilUnit;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.IdentifierToken;
import dyvil.tools.compiler.parser.JavaParserManager;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.header.JavaUnitParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Tokens;

public class JavaUnit extends DyvilUnit
{

	public JavaUnit(Package pack, CodeFile input, File output)
	{
		super(pack, input, output);
	}
	
	@Override
	public void tokenize()
	{
		this.tokens = Dlex.tokenIterator(this.inputFile.getCode());
	}
	
	@Override
	public void parse()
	{
		ParserManager manager = new JavaParserManager(new JavaUnitParser(this));
		manager.setOperatorMap(this);
		manager.parse(this.markers, this.tokens);
		this.tokens = null;
	}
	
	protected void processJavaFile()
	{
		while (this.tokens.hasNext())
		{
			IToken token = this.tokens.next();
			int type = token.type();
			
			switch (type)
			{
			case Keywords.ANNOTATION:
			case Keywords.AS:
			case Keywords.FUNCTIONAL:
			case Keywords.IMPLICIT:
			case Keywords.INCLUDE:
			case Keywords.INFIX:
			case Keywords.INLINE:
			case Keywords.IS:
			case Keywords.LAZY:
			case Keywords.MACRO:
			case Keywords.NIL:
			case Keywords.OBJECT:
			case Keywords.OPERATOR:
			case Keywords.OVERRIDE:
			case Keywords.POSTFIX:
			case Keywords.PREFIX:
			case Keywords.SEALED:
			case Keywords.TYPE:
			case Keywords.USING:
			case Keywords.VAR:
				String keyword = Keywords.keywordToString(type);
				int startIndex = token.startIndex();
				this.tokens.set(new IdentifierToken(Name.getQualified(keyword), Tokens.LETTER_IDENTIFIER, token.startLine(), startIndex, startIndex
						+ keyword.length()));
				break;
			}
		}
		
	}
	
	@Override
	protected boolean printMarkers()
	{
		return ICompilationUnit.printMarkers(this.markers, "Java Unit", this.name, this.inputFile);
	}
}
