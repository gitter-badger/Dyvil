package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;

import org.objectweb.asm.AnnotationVisitor;

public final class ArrayAnnotationVisitor extends AnnotationVisitor
{
	private IValueList	array;
	
	public ArrayAnnotationVisitor(int api, IValueList array)
	{
		super(api);
		this.array = array;
	}
	
	@Override
	public void visit(String key, Object obj)
	{
		this.array.addValue(IValue.fromObject(obj));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = AnnotationVisitorImpl.getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.array.addValue(enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		Array valueList = new Array(null);
		this.array.addValue(valueList);
		return new ArrayAnnotationVisitor(this.api, valueList);
	}
}
