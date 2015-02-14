package dyvil.tools.compiler.parser.method;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.member.IAnnotated;
import dyvil.tools.compiler.ast.method.IParameterized;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.type.TypeParser;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ParameterListParser extends Parser implements IAnnotated, ITyped
{
	public static final int		TYPE		= 1;
	public static final int		NAME		= 2;
	public static final int		SEPERATOR	= 4;
	
	protected IParameterized	parameterized;
	
	private int					modifiers;
	private List<Annotation>	annotations;
	private IType				type;
	private Parameter			parameter;
	private boolean				varargs;
	
	public ParameterListParser(IParameterized parameterized)
	{
		this.parameterized = parameterized;
		this.reset();
	}
	
	private void reset()
	{
		this.mode = TYPE;
		this.modifiers = 0;
		this.annotations = new ArrayList();
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == TYPE)
		{
			int i = 0;
			String value = token.value();
			if ((i = Modifiers.PARAMETER.parse(value)) != -1)
			{
				this.modifiers |= i;
				return true;
			}
			if (value.charAt(0) == '@')
			{
				pm.pushParser(new AnnotationParser(this), true);
				return true;
			}
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return true;
			}
			
			this.mode = NAME;
			pm.pushParser(new TypeParser(this), true);
			return true;
		}
		if (this.mode == NAME)
		{
			if (token.equals("..."))
			{
				this.varargs = true;
				return true;
			}
			if (ParserUtil.isIdentifier(type))
			{
				this.parameter = new Parameter(0, token.value(), this.type, this.modifiers, this.annotations);
				this.parameterized.addParameter(this.parameter);
				this.mode = SEPERATOR;
				
				if (this.varargs)
				{
					this.parameter.setVarargs();
					this.parameterized.setVarargs();
					this.varargs = false;
				}
				return true;
			}
			return false;
		}
		if (this.mode == SEPERATOR)
		{
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return true;
			}
			if (type == Tokens.COMMA)
			{
				this.reset();
				return true;
			}
			if (type == Tokens.SEMICOLON)
			{
				this.parameter.seperator = ';';
				this.reset();
				return true;
			}
			if (type == Tokens.EQUALS)
			{
				pm.pushParser(new ExpressionParser(this.parameter));
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setAnnotations(List<Annotation> annotations)
	{
	}
	
	@Override
	public List<Annotation> getAnnotations()
	{
		return null;
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		return null;
	}
	
	@Override
	public void addAnnotation(Annotation annotation)
	{
		this.annotations.add(annotation);
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return null;
	}
}
