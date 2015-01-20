package dyvil.tools.compiler.config;

import java.io.File;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.Tokens;

public class ConfigParser extends Parser
{
	public static int			KEY		= 0;
	public static int			VALUE	= 1;
	public static int			ARRAY	= 2;
	
	protected CompilerConfig	config;
	
	private String				key;
	
	public ConfigParser(CompilerConfig config)
	{
		this.config = config;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == KEY)
		{
			if ("=".equals(value))
			{
				this.mode = VALUE;
				return true;
			}
			else if (token.isType(Tokens.TYPE_IDENTIFIER))
			{
				this.key = value;
				return true;
			}
		}
		else if (this.mode == VALUE)
		{
			if ("[".equals(value))
			{
				this.mode = ARRAY;
				return true;
			}
			else
			{
				this.setProperty(this.key, token.object());
				this.mode = KEY;
				this.key = null;
				return true;
			}
		}
		else if (this.mode == ARRAY)
		{
			if ("]".equals(value))
			{
				this.mode = KEY;
				this.key = null;
				return true;
			}
			else
			{
				this.setProperty(this.key, token.object());
				return true;
			}
		}
		return false;
	}
	
	private void setProperty(String name, Object property)
	{
		switch (name)
		{
		case "jar_name":
			this.config.jarName = (String) property;
			break;
		case "jar_vendor":
			this.config.jarVendor = (String) property;
			break;
		case "jar_version":
			this.config.jarVersion = (String) property;
			break;
		case "jar_format":
			this.config.jarNameFormat = (String) property;
		case "source_dir":
			this.config.sourceDir = new File((String) property);
			break;
		case "output_dir":
			this.config.outputDir = new File((String) property);
			break;
		case "main_type":
			this.config.mainType = (String) property;
			break;
		case "include":
			this.config.includeFile((String) property);
			break;
		case "exclude":
			this.config.excludeFile((String) property);
			break;
		}
	}
}