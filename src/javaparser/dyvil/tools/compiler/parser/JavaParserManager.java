package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.type.JavaTypeParser;

public class JavaParserManager extends ParserManager
{
	public JavaParserManager(Parser parser)
	{
		this.parser = parser;
	}
	
	@Override
	public Parser newTypeParser(ITypeConsumer typeConsumer)
	{
		return new JavaTypeParser(typeConsumer);
	}
}
