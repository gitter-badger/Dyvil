package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IClassBodyConsumer;
import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.*;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.annotation.AnnotationParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.header.JavaUnitParser;
import dyvil.tools.compiler.parser.method.ExceptionListParser;
import dyvil.tools.compiler.parser.method.ParameterListParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.parser.type.JavaTypeParser;
import dyvil.tools.compiler.parser.type.TypeVariableListParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ModifierTypes;
import dyvil.tools.compiler.util.ParserUtil;

public final class JavaClassBodyParser extends Parser implements ITypeConsumer
{
	public static final int			GENERICS_END	= 4;
	public static final int			TYPE			= 1;
	public static final int			NAME			= 2;
	public static final int			PARAMETERS		= 8;
	public static final int			PARAMETERS_END	= 16;
	public static final int			FIELD_END		= 32;
	public static final int			PROPERTY_END	= 64;
	public static final int			METHOD_VALUE	= 128;
	public static final int			METHOD_END		= 256;
	
	protected IClass				theClass;
	protected IClassBodyConsumer	consumer;
	
	private IType					type;
	private int						modifiers;
	private Annotation[]			annotations		= new Annotation[2];
	private int						annotationCount;
	
	private IMember					member;
	
	public JavaClassBodyParser(IClass theClass, IClassBodyConsumer consumer)
	{
		this.theClass = theClass;
		this.consumer = consumer;
		this.mode = TYPE;
	}
	
	public JavaClassBodyParser(IClassBodyConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = TYPE;
	}
	
	@Override
	public void reset()
	{
		this.mode = TYPE;
		this.modifiers = 0;
		this.annotationCount = 0;
		this.type = null;
		this.member = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		
		switch (this.mode)
		{
		case TYPE:
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser(true);
				return;
			}
			
			// Type Variables
			if (JavaUnitParser.isOpenAngle(token))
			{
				Method m = new Method(this.theClass);
				this.member = m;
				pm.pushParser(new TypeVariableListParser(m));
				this.mode = GENERICS_END;
				return;
			}
			
			int i = 0;
			if ((i = ModifierTypes.MEMBER.parse(type)) != -1)
			{
				this.modifiers |= i;
				return;
			}
			if ((i = ModifierTypes.CLASS_TYPE.parse(type)) != -1)
			{
				IToken next = token.next();
				if (!ParserUtil.isIdentifier(next.type()))
				{
					this.reset();
					throw new SyntaxError(next, "Invalid Class Declaration - Name expected");
				}
				
				if (this.theClass == null)
				{
					this.reset();
					throw new SyntaxError(token, "Cannot define a class in this context");
				}
				
				Name name = next.nameValue();
				
				CodeClass codeClass = new CodeClass(next.raw(), this.theClass.getUnit(), this.modifiers | i);
				codeClass.setName(name);
				codeClass.setAnnotations(this.getAnnotations(), this.annotationCount);
				codeClass.setOuterClass(this.theClass);
				
				JavaClassParser parser = new JavaClassParser(this.theClass.getBody(), codeClass);
				pm.skip();
				pm.pushParser(parser);
				this.reset();
				return;
			}
			Name name = token.nameValue();
			if (name == Name.at)
			{
				Annotation annotation = new Annotation(token.raw());
				this.addAnnotation(annotation);
				pm.pushParser(new AnnotationParser(annotation));
				return;
			}
			// Constructors
			if (token.next().type() == Symbols.OPEN_PARENTHESIS && name == this.theClass.getName())
			{
				this.member = new Constructor(this.theClass);
				this.mode = PARAMETERS;
				return;
			}
			
			pm.pushParser(new JavaTypeParser(this), true);
			this.mode = NAME;
			return;
		case NAME:
			if (!ParserUtil.isIdentifier(type))
			{
				this.reset();
				throw new SyntaxError(token, "Invalid Member Declaration - Name expected", true);
			}
			IToken next = token.next();
			type = next.type();
			if (type == Symbols.SEMICOLON || type == Symbols.CLOSE_CURLY_BRACKET)
			{
				Field f = new Field(this.theClass, token.nameValue(), this.type);
				f.position = token.raw();
				f.modifiers = this.modifiers;
				f.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.consumer.addField(f);
				
				if (type == Symbols.CLOSE_CURLY_BRACKET)
				{
					pm.popParser(true);
					return;
				}
				
				pm.skip();
				this.reset();
				return;
			}
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				this.mode = PARAMETERS;
				
				Method m;
				if (this.member != null)
				{
					// Already constructed when type variables were parsed
					m = (Method) this.member;
					m.type = this.type;
					m.name = token.nameValue();
				}
				else
				{
					m = new Method(this.theClass, token.nameValue(), this.type);
				}
				
				m.modifiers = this.modifiers;
				m.position = token.raw();
				m.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.member = m;
				return;
			}
			if (type == Symbols.EQUALS)
			{
				Field f = new Field(this.theClass, token.nameValue(), this.type);
				f.position = token.raw();
				f.modifiers = this.modifiers;
				f.setAnnotations(this.getAnnotations(), this.annotationCount);
				this.member = f;
				this.mode = FIELD_END;
				
				pm.skip();
				pm.pushParser(new ExpressionParser(f));
				return;
			}
			
			this.mode = TYPE;
			throw new SyntaxError(token, "Invalid Declaration - ';', '=' or '(' expected");
		case GENERICS_END:
			this.mode = TYPE;
			if (JavaUnitParser.isClosingAngle(token))
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Generic Type Parameter List - '>' expected", true);
		case PARAMETERS:
			this.mode = PARAMETERS_END;
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ParameterListParser((IParameterList) this.member));
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter List - '(' expected", true);
		case PARAMETERS_END:
			this.mode = METHOD_VALUE;
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Parameter List - ')' expected", true);
		case METHOD_VALUE:
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				this.consumer.addMethod((IMethod) this.member);
				pm.popParser(true);
				return;
			}
			if (type == Symbols.SEMICOLON)
			{
				this.consumer.addMethod((IMethod) this.member);
				this.reset();
				return;
			}
			if (type == Symbols.OPEN_CURLY_BRACKET)
			{
				this.mode = METHOD_END;
				StatementList sl = new StatementList();
				((Method) this.member).value = sl;
				pm.pushParser(new StatementListParser(sl));
				return;
			}
			if (type == Keywords.THROWS)
			{
				pm.pushParser(new ExceptionListParser((IExceptionList) this.member));
				return;
			}
			
			this.mode = TYPE;
			throw new SyntaxError(token, "Invalid Method Declaration - ';', '{' or 'throws' expected");
		case METHOD_END:
			if (this.member instanceof IMethod)
			{
				this.consumer.addMethod((IMethod) this.member);
			}
			else
			{
				this.consumer.addConstructor((IConstructor) this.member);
			}
			this.reset();
			if (type == Symbols.CLOSE_CURLY_BRACKET)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Method Body - '}' expected", true);
		case FIELD_END:
			this.consumer.addField((IField) this.member);
			pm.reparse();
			this.reset();
			return;
		case PROPERTY_END:
			this.consumer.addProperty((IProperty) this.member);
			pm.reparse();
			this.reset();
			return;
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
