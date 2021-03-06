package dyvil.tools.compiler.parser.method;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.ClassParameter;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.type.ArrayType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;

public final class ParameterListParser extends Parser implements ITypeConsumer
{
	public static final int		TYPE		= 1;
	public static final int		NAME		= 2;
	public static final int		SEPERATOR	= 4;
	
	protected IParameterList	paramList;
	
	private int					modifiers;
	private Annotation[]		annotations	= new Annotation[2];
	private int					annotationCount;
	
	private IType				type;
	private IParameter			parameter;
	private boolean				varargs;
	
	public ParameterListParser(IParameterList paramList)
	{
		this.paramList = paramList;
		this.mode = TYPE;
	}
	
	@Override
	public void reset()
	{
		this.mode = TYPE;
		this.modifiers = 0;
		this.annotationCount = 0;
		this.type = null;
		this.parameter = null;
		this.varargs = false;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (this.mode == TYPE)
		{
			if (type == Symbols.SEMICOLON && token.isInferred())
			{
				return;
			}
			
			int i = 0;
			if ((i = ModifierTypes.PARAMETER.parse(type)) != -1)
			{
				this.modifiers |= i;
				return;
			}
			if (token.nameValue() == Name.at)
			{
				Annotation annotation = new Annotation(token.raw());
				this.addAnnotation(annotation);
				pm.pushParser(pm.newAnnotationParser(annotation));
				return;
			}
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			
			this.mode = NAME;
			pm.pushParser(pm.newTypeParser(this), true);
			return;
		}
		if (this.mode == NAME)
		{
			if (type == Symbols.ELLIPSIS)
			{
				this.varargs = true;
				return;
			}
			this.mode = SEPERATOR;
			if (ParserUtil.isIdentifier(type))
			{
				if (this.varargs)
				{
					this.paramList.setVarargs();
					this.type = new ArrayType(this.type);
				}
				
				this.parameter = this.paramList instanceof IClass ? new ClassParameter(token.nameValue(), this.type) : new MethodParameter(token.nameValue(),
						this.type);
				this.parameter.setPosition(token.raw());
				this.parameter.setModifiers(this.modifiers);
				this.parameter.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.parameter.setVarargs(this.varargs);
				this.paramList.addParameter(this.parameter);
				
				this.varargs = false;
				
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter Declaration - Name expected");
		}
		if (this.mode == SEPERATOR)
		{
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			if (type == Symbols.EQUALS)
			{
				pm.pushParser(pm.newExpressionParser(this.parameter));
				return;
			}
			this.reset();
			if (type == Symbols.COMMA || type == Symbols.SEMICOLON)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter Declaration - ',' expected");
		}
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	public int annotationCount()
	{
		return this.annotationCount;
	}
	
	public Annotation[] getAnnotations()
	{
		Annotation[] a = new Annotation[this.annotationCount];
		System.arraycopy(this.annotations, 0, a, 0, this.annotationCount);
		return a;
	}
	
	public void addAnnotation(Annotation annotation)
	{
		int index = this.annotationCount++;
		if (this.annotationCount > this.annotations.length)
		{
			Annotation[] temp = new Annotation[this.annotationCount];
			System.arraycopy(this.annotations, 0, temp, 0, index);
			this.annotations = temp;
		}
		this.annotations[index] = annotation;
	}
}
