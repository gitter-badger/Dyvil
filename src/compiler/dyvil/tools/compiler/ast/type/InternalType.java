package dyvil.tools.compiler.ast.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class InternalType implements IType
{
	protected String	internalName;
	
	public InternalType(String internalName)
	{
		this.internalName = internalName;
	}
	
	@Override
	public int typeTag()
	{
		return INTERNAL;
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified(this.internalName.substring(this.internalName.lastIndexOf('/') + 1));
	}
	
	@Override
	public IClass getTheClass()
	{
		return null;
	}
	
	@Override
	public boolean isResolved()
	{
		return false;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		IClass iclass = Package.rootPackage.resolveInternalClass(this.internalName);
		return new ClassType(iclass);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		return this.internalName;
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.internalName).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('L').append(this.internalName).append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.internalName);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.internalName = in.readUTF();
	}
	
	@Override
	public String toString()
	{
		return ClassFormat.internalToPackage(this.internalName);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(ClassFormat.internalToPackage(this.internalName));
	}
	
	@Override
	public IType clone()
	{
		return new InternalType(this.internalName);
	}
}
