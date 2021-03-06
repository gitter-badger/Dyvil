package dyvil.tools.compiler.backend.visitor;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.backend.ClassFormat;

import org.objectweb.asm.AnnotationVisitor;

public class ValueAnnotationVisitor extends AnnotationVisitor
{
	private IValued	valued;
	
	public ValueAnnotationVisitor(IValued valued)
	{
		super(DyvilCompiler.asmVersion);
		this.valued = valued;
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.valued.setValue(IValue.fromObject(value));
	}
	
	static IValue getEnumValue(String enumClass, String name)
	{
		IType t = ClassFormat.internalToType(enumClass);
		t.resolve(null, Package.rootPackage, TypePosition.CLASS);
		return new EnumValue(t, Name.getQualified(name));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.valued.setValue(enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		Array valueList = new Array();
		this.valued.setValue(valueList);
		return new ArrayAnnotationVisitor(this.api, valueList);
	}
	
	@Override
	public void visitEnd()
	{
	}
}
