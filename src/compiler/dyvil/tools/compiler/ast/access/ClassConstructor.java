package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.AnonymousClassMetadata;
import dyvil.tools.compiler.ast.classes.IClassBody;
import dyvil.tools.compiler.ast.classes.NestedClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassConstructor extends ConstructorCall
{
	private NestedClass				nestedClass;
	private AnonymousClassMetadata	metadata;
	
	public ClassConstructor(ICodePosition position)
	{
		super(position);
		this.nestedClass = new NestedClass();
	}
	
	public NestedClass getNestedClass()
	{
		return this.nestedClass;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context, TypePosition.TYPE);
		this.arguments.resolveTypes(markers, context);
		
		this.nestedClass.setSuperType(this.type);
		
		this.nestedClass.context = context;
		this.nestedClass.resolveTypes(markers, context);
		this.nestedClass.context = null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		this.metadata = new AnonymousClassMetadata(this.nestedClass, this.constructor);
		this.nestedClass.setMetadata(this.metadata);
		
		this.nestedClass.context = context;
		this.nestedClass.resolve(markers, context);
		this.nestedClass.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		this.nestedClass.context = context;
		this.nestedClass.checkTypes(markers, context);
		this.nestedClass.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
		
		this.nestedClass.context = context;
		this.nestedClass.check(markers, context);
		this.nestedClass.context = null;
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		
		this.nestedClass.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.arguments.cleanup(context, compilableList);
		this.nestedClass.cleanup(context, compilableList);
		
		IDyvilHeader header = context.getHeader();
		this.nestedClass.setUnit(header);
		header.addInnerClass(this.nestedClass);
		
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.metadata.writeConstructorCall(writer, this.arguments);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.metadata.writeConstructorCall(writer, this.arguments);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		
		IClassBody body = this.nestedClass.getBody();
		if (body != null)
		{
			buffer.append(' ');
			body.toString(prefix, buffer);
		}
		else
		{
			buffer.append(" {}");
		}
	}
}
