package dyvil.tools.compiler.ast.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.*;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class PrimitiveType implements IType
{
	public Name		name;
	public IClass	theClass;
	public int		typecode;
	public IMethod	boxMethod;
	public IMethod	unboxMethod;
	
	public PrimitiveType(Name name, int typecode)
	{
		this.name = name;
		this.typecode = typecode;
	}
	
	public static IType getPrimitiveType(IType type)
	{
		if (type.isArrayType())
		{
			return type;
		}
		IClass iclass = type.getTheClass();
		if (iclass == Types.VOID_CLASS)
		{
			return Types.VOID;
		}
		if (iclass == Types.BOOLEAN_CLASS)
		{
			return Types.BOOLEAN;
		}
		if (iclass == Types.BYTE_CLASS)
		{
			return Types.BYTE;
		}
		if (iclass == Types.SHORT_CLASS)
		{
			return Types.SHORT;
		}
		if (iclass == Types.CHAR_CLASS)
		{
			return Types.CHAR;
		}
		if (iclass == Types.INT_CLASS)
		{
			return Types.INT;
		}
		if (iclass == Types.LONG_CLASS)
		{
			return Types.LONG;
		}
		if (iclass == Types.FLOAT_CLASS)
		{
			return Types.FLOAT;
		}
		if (iclass == Types.DOUBLE_CLASS)
		{
			return Types.DOUBLE;
		}
		return type;
	}
	
	public static PrimitiveType fromTypecode(int typecode)
	{
		switch (typecode)
		{
		case ClassFormat.T_BOOLEAN:
			return Types.BOOLEAN;
		case ClassFormat.T_BYTE:
			return Types.BYTE;
		case ClassFormat.T_SHORT:
			return Types.SHORT;
		case ClassFormat.T_CHAR:
			return Types.CHAR;
		case ClassFormat.T_INT:
			return Types.INT;
		case ClassFormat.T_LONG:
			return Types.LONG;
		case ClassFormat.T_FLOAT:
			return Types.FLOAT;
		case ClassFormat.T_DOUBLE:
			return Types.DOUBLE;
		default:
			return Types.VOID;
		}
	}
	
	@Override
	public int typeTag()
	{
		return PRIMITIVE;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return true;
	}
	
	@Override
	public final IType getReferenceType()
	{
		return new ClassType(this.theClass);
	}
	
	@Override
	public IMethod getBoxMethod()
	{
		return this.boxMethod;
	}
	
	@Override
	public IMethod getUnboxMethod()
	{
		return this.unboxMethod;
	}
	
	@Override
	public IClass getArrayClass()
	{
		return Types.getPrimitiveArray(this);
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public boolean isSuperClassOf(IType that)
	{
		return this.theClass == that.getTheClass();
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		if (this == type)
		{
			return true;
		}
		if (type.getName() == this.name)
		{
			return true;
		}
		return IType.super.classEquals(type);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		// TODO Position and report errors related to the position
		
		return this;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		return this;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
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
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			return "Z";
		case ClassFormat.T_BYTE:
			return "B";
		case ClassFormat.T_SHORT:
			return "S";
		case ClassFormat.T_CHAR:
			return "C";
		case ClassFormat.T_INT:
			return "I";
		case ClassFormat.T_LONG:
			return "J";
		case ClassFormat.T_FLOAT:
			return "F";
		case ClassFormat.T_DOUBLE:
			return "D";
		default:
			return "V";
		}
	}
	
	@Override
	public void appendExtendedName(StringBuilder buf)
	{
		buf.append(this.getInternalName());
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		buf.append(this.getInternalName());
	}
	
	@Override
	public int getLoadOpcode()
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return Opcodes.ILOAD;
		case ClassFormat.T_LONG:
			return Opcodes.LLOAD;
		case ClassFormat.T_FLOAT:
			return Opcodes.FLOAD;
		case ClassFormat.T_DOUBLE:
			return Opcodes.DLOAD;
		default:
			return 0;
		}
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
			return Opcodes.BALOAD;
		case ClassFormat.T_SHORT:
			return Opcodes.SALOAD;
		case ClassFormat.T_CHAR:
			return Opcodes.CALOAD;
		case ClassFormat.T_INT:
			return Opcodes.IALOAD;
		case ClassFormat.T_LONG:
			return Opcodes.LALOAD;
		case ClassFormat.T_FLOAT:
			return Opcodes.FALOAD;
		case ClassFormat.T_DOUBLE:
			return Opcodes.DALOAD;
		default:
			return 0;
		}
	}
	
	@Override
	public int getStoreOpcode()
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return Opcodes.ISTORE;
		case ClassFormat.T_LONG:
			return Opcodes.LSTORE;
		case ClassFormat.T_FLOAT:
			return Opcodes.FSTORE;
		case ClassFormat.T_DOUBLE:
			return Opcodes.DSTORE;
		default:
			return 0;
		}
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
			return Opcodes.BASTORE;
		case ClassFormat.T_SHORT:
			return Opcodes.SASTORE;
		case ClassFormat.T_CHAR:
			return Opcodes.CASTORE;
		case ClassFormat.T_INT:
			return Opcodes.IASTORE;
		case ClassFormat.T_LONG:
			return Opcodes.LASTORE;
		case ClassFormat.T_FLOAT:
			return Opcodes.FASTORE;
		case ClassFormat.T_DOUBLE:
			return Opcodes.DASTORE;
		default:
			return 0;
		}
	}
	
	@Override
	public int getReturnOpcode()
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return Opcodes.IRETURN;
		case ClassFormat.T_LONG:
			return Opcodes.LRETURN;
		case ClassFormat.T_FLOAT:
			return Opcodes.FRETURN;
		case ClassFormat.T_DOUBLE:
			return Opcodes.DRETURN;
		default:
			return Opcodes.RETURN;
		}
	}
	
	@Override
	public Object getFrameType()
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			return ClassFormat.BOOLEAN;
		case ClassFormat.T_BYTE:
			return ClassFormat.BYTE;
		case ClassFormat.T_SHORT:
			return ClassFormat.SHORT;
		case ClassFormat.T_CHAR:
			return ClassFormat.CHAR;
		case ClassFormat.T_INT:
			return ClassFormat.INT;
		case ClassFormat.T_LONG:
			return ClassFormat.LONG;
		case ClassFormat.T_FLOAT:
			return ClassFormat.FLOAT;
		case ClassFormat.T_DOUBLE:
			return ClassFormat.DOUBLE;
		}
		return null;
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		int i;
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			i = dyvil.reflect.type.PrimitiveType.BOOLEAN;
			break;
		case ClassFormat.T_BYTE:
			i = dyvil.reflect.type.PrimitiveType.BYTE;
			break;
		case ClassFormat.T_SHORT:
			i = dyvil.reflect.type.PrimitiveType.SHORT;
			break;
		case ClassFormat.T_CHAR:
			i = dyvil.reflect.type.PrimitiveType.CHAR;
			break;
		case ClassFormat.T_INT:
			i = dyvil.reflect.type.PrimitiveType.INT;
			break;
		case ClassFormat.T_LONG:
			i = dyvil.reflect.type.PrimitiveType.LONG;
			break;
		case ClassFormat.T_FLOAT:
			i = dyvil.reflect.type.PrimitiveType.FLOAT;
			break;
		case ClassFormat.T_DOUBLE:
			i = dyvil.reflect.type.PrimitiveType.DOUBLE;
			break;
		default:
			i = 0;
		}
		
		writer.writeLDC(i);
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/PrimitiveType", "apply", "(I)Ldyvil/reflect/type/PrimitiveType;", false);
	}
	
	@Override
	public void writeDefaultValue(MethodWriter writer)
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			writer.writeLDC(0);
			break;
		case ClassFormat.T_LONG:
			writer.writeLDC(0L);
			break;
		case ClassFormat.T_FLOAT:
			writer.writeLDC(0F);
			break;
		case ClassFormat.T_DOUBLE:
			writer.writeLDC(0D);
			break;
		}
	}
	
	@Override
	public IConstantValue getDefaultValue()
	{
		switch (this.typecode)
		{
		case ClassFormat.T_BOOLEAN:
			return BooleanValue.TRUE;
		case ClassFormat.T_BYTE:
		case ClassFormat.T_SHORT:
		case ClassFormat.T_CHAR:
		case ClassFormat.T_INT:
			return IntValue.getNull();
		case ClassFormat.T_LONG:
			return LongValue.getNull();
		case ClassFormat.T_FLOAT:
			return FloatValue.getNull();
		case ClassFormat.T_DOUBLE:
			return DoubleValue.getNull();
		}
		return null;
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeByte(this.typecode);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
	}
	
	@Override
	public String toString()
	{
		return this.name.qualified;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
	}
	
	@Override
	public PrimitiveType clone()
	{
		PrimitiveType t = new PrimitiveType(this.name, this.typecode);
		t.theClass = this.theClass;
		return t;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}
	
	@Override
	public int hashCode()
	{
		return this.typecode;
	}
}
