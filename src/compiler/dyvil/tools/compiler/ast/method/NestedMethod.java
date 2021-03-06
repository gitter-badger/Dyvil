package dyvil.tools.compiler.ast.method;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.CaptureVariable;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;

import org.objectweb.asm.Label;

public class NestedMethod extends Method
{
	private IClass				thisClass;
	private CaptureVariable[]	capturedFields;
	private int					capturedFieldCount;
	
	public transient IContext	context;
	
	public NestedMethod(IClass iclass)
	{
		super(iclass);
	}
	
	public NestedMethod(IClass iclass, Name name)
	{
		super(iclass, name);
	}
	
	public NestedMethod(IClass iclass, Name name, IType type)
	{
		super(iclass, name, type);
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.thisClass = this.context.getThisClass();
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.context.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.context.resolveClass(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		for (int i = 0; i < this.genericCount; i++)
		{
			ITypeVariable var = this.generics[i];
			if (var.getName() == name)
			{
				return var;
			}
		}
		
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			if (param.getName() == name)
			{
				return param;
			}
		}
		
		IDataMember match = this.context.resolveField(name);
		if (match == null)
		{
			return null;
		}
		
		if (!match.isVariable())
		{
			return match;
		}
		if (this.capturedFields == null)
		{
			this.capturedFields = new CaptureVariable[2];
			this.capturedFieldCount = 1;
			return this.capturedFields[0] = new CaptureVariable((IVariable) match);
		}
		
		// Check if the variable is already in the array
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			if (this.capturedFields[i].variable == match)
			{
				// If yes, return the match and skip adding the variable
				// again.
				return match;
			}
		}
		
		int index = this.capturedFieldCount++;
		if (this.capturedFieldCount > this.capturedFields.length)
		{
			CaptureVariable[] temp = new CaptureVariable[this.capturedFieldCount];
			System.arraycopy(this.capturedFields, 0, temp, 0, index);
			this.capturedFields = temp;
		}
		return this.capturedFields[index] = new CaptureVariable((IVariable) match);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		return this.context.getVisibility(member);
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		int modifiers = this.modifiers & 0xFFFF;
		if (this.value == null)
		{
			modifiers |= Modifiers.ABSTRACT;
		}
		
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, this.name.qualified, this.getDescriptor(), this.getSignature(),
				this.getExceptions()));
		
		if (this.thisClass != null)
		{
			mw.setThisType(this.theClass.getInternalName());
		}
		
		for (int i = 0; i < this.annotationCount; i++)
		{
			this.annotations[i].write(mw);
		}
		
		if ((this.modifiers & Modifiers.INLINE) == Modifiers.INLINE)
		{
			mw.addAnnotation("Ldyvil/annotation/inline;", false);
		}
		if ((this.modifiers & Modifiers.INFIX) == Modifiers.INFIX)
		{
			mw.addAnnotation("Ldyvil/annotation/infix;", false);
		}
		if ((this.modifiers & Modifiers.PREFIX) == Modifiers.PREFIX)
		{
			mw.addAnnotation("Ldyvil/annotation/prefix;", false);
		}
		if ((this.modifiers & Modifiers.DEPRECATED) == Modifiers.DEPRECATED)
		{
			mw.addAnnotation("Ljava/lang/Deprecated;", true);
		}
		
		int index = 0;
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable capture = this.capturedFields[i];
			capture.index = index;
			index = mw.registerParameter(index, capture.variable.getName().qualified, capture.getActualType(), 0);
		}
		
		index = 0;
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			index = mw.registerParameter(index, param.getName().qualified, param.getType(), 0);
			param.setIndex(index);
		}
		
		Label start = new Label();
		Label end = new Label();
		
		if (this.value != null)
		{
			mw.begin();
			mw.writeLabel(start);
			this.value.writeExpression(mw);
			mw.writeLabel(end);
			mw.end(this.type);
		}
		
		if (this.thisClass != null)
		{
			mw.writeLocal(0, "this", this.theClass.getInternalName(), null, start, end);
		}
		
		for (int i = 0; i < this.parameterCount; i++)
		{
			IParameter param = this.parameters[i];
			mw.writeLocal(param.getIndex(), param.getName().qualified, param.getDescription(), param.getSignature(), start, end);
		}
	}
	
	private void writeCaptures(MethodWriter writer) throws BytecodeException
	{
		for (int i = 0; i < this.capturedFieldCount; i++)
		{
			CaptureVariable var = this.capturedFields[i];
			writer.writeVarInsn(var.getActualType().getLoadOpcode(), var.variable.getIndex());
		}
	}
	
	@Override
	public void writeCall(MethodWriter writer, IValue instance, IArguments arguments, IType type, int lineNumber) throws BytecodeException
	{
		this.writeCaptures(writer);
		super.writeCall(writer, instance, arguments, type, lineNumber);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		this.writeCaptures(writer);
		super.writeJump(writer, dest, instance, arguments, lineNumber);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest, IValue instance, IArguments arguments, int lineNumber) throws BytecodeException
	{
		this.writeCaptures(writer);
		super.writeInvJump(writer, dest, instance, arguments, lineNumber);
	}
}
