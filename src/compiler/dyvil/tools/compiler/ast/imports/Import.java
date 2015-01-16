package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class Import extends ASTNode implements IContext, IImportContainer
{
	public IImport	theImport;
	public IImport	last;
	public boolean	isStatic;
	
	public Import(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		this.theImport = iimport;
	}
	
	public void resolveTypes(List<Marker> markers)
	{
		this.theImport.resolveTypes(markers, Package.rootPackage, this.isStatic);
		
		IImport iimport = this.theImport;
		while (iimport instanceof SimpleImport)
		{
			IImport child = ((SimpleImport) iimport).child;
			if (child == null)
			{
				break;
			}
			iimport = child;
		}
		
		this.last = iimport;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.isStatic;
	}
	
	@Override
	public IType getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.last.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.last.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		return this.last.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(ITyped instance, String name, List<? extends ITyped> arguments)
	{
		return this.last.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, ITyped instance, String name, List<? extends ITyped> arguments)
	{
		this.last.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return READ_WRITE_ACCESS;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isStatic)
		{
			buffer.append("using ");
		}
		else
		{
			buffer.append("import ");
		}
		
		this.theImport.toString(prefix, buffer);
	}
}