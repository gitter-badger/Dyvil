package dyvil.tools.compiler.ast.external;

import dyvil.tools.compiler.ast.annotation.Annotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ExternalMethod extends Method
{
	private boolean	annotationsResolved;
	private boolean	returnTypeResolved;
	private boolean	genericsResolved;
	private boolean	parametersResolved;
	private boolean	exceptionsResolved;
	
	public ExternalMethod(IClass iclass)
	{
		super(iclass);
	}
	
	private void resolveAnnotations()
	{
		this.annotationsResolved = true;
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveReturnType()
	{
		this.returnTypeResolved = true;
		this.type = this.type.resolve(null, Package.rootPackage);
	}
	
	private void resolveGenerics()
	{
		this.genericsResolved = true;
		for (int i = 0; i < this.genericCount; i++)
		{
			this.generics[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveParameters()
	{
		this.parametersResolved = true;
		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].resolveTypes(null, Package.rootPackage);
		}
	}
	
	private void resolveExceptions()
	{
		this.exceptionsResolved = true;
		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolve(null, Package.rootPackage);
		}
	}
	
	@Override
	public IType getType()
	{
		if (!this.returnTypeResolved)
		{
			this.resolveReturnType();
		}
		return this.type;
	}
	
	@Override
	public IType getType(ITypeContext context)
	{
		if (!this.returnTypeResolved)
		{
			this.resolveReturnType();
		}
		return this.type.getConcreteType(context);
	}
	
	@Override
	public ITypeVariable getTypeVariable(int index)
	{
		if (!this.genericsResolved)
		{
			this.resolveGenerics();
		}
		return this.generics[index];
	}
	
	@Override
	public IParameter getParameter(int index)
	{
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return this.parameters[index];
	}
	
	@Override
	public int getSignatureMatch(Name name, IValue instance, IArguments arguments)
	{
		if (name != this.name)
		{
			return 0;
		}
		
		if (!this.parametersResolved)
		{
			this.resolveParameters();
		}
		return super.getSignatureMatch(name, instance, arguments);
	}
	
	@Override
	public IType getException(int index)
	{
		if (!this.exceptionsResolved)
		{
			this.resolveExceptions();
		}
		return this.exceptions[index];
	}
	
	@Override
	public Annotation getAnnotation(int index)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		return this.annotations[index];
	}
	
	@Override
	public Annotation getAnnotation(IType type)
	{
		if (this.annotations == null)
		{
			return null;
		}
		
		if (!this.annotationsResolved)
		{
			this.resolveAnnotations();
		}
		for (int i = 0; i < this.annotationCount; i++)
		{
			Annotation a = this.annotations[i];
			if (a.type.equals(type))
			{
				return a;
			}
		}
		return null;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
	}
	
	@Override
	public void foldConstants()
	{
	}
}