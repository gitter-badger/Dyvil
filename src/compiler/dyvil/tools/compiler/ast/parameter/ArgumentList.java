package dyvil.tools.compiler.ast.parameter;

import java.util.Iterator;
import java.util.List;

import dyvil.collections.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;

public final class ArgumentList implements IArguments, IValueList
{
	private IValue[]	values;
	private int			size;
	
	private boolean		varargs;
	
	public ArgumentList()
	{
		this.values = new IValue[3];
	}
	
	protected ArgumentList(IValue[] values, int size)
	{
		this.values = values;
		this.size = size;
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator(this.values, this.size);
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public int valueCount()
	{
		return this.size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.size > 0;
	}
	
	@Override
	public IArguments dropFirstValue()
	{
		int len1 = this.size - 1;
		IValue[] values = new IValue[len1];
		System.arraycopy(this.values, 1, values, 0, len1);
		return new ArgumentList(values, len1);
	}
	
	@Override
	public IArguments addLastValue(IValue value)
	{
		IValue[] values = new IValue[this.size + 1];
		System.arraycopy(this.values, 0, values, 0, this.size);
		values[this.size] = value;
		return new ArgumentList(values, this.size + 1);
	}
	
	@Override
	public IValue getFirstValue()
	{
		return this.values[0];
	}
	
	@Override
	public void setFirstValue(IValue value)
	{
		this.values[0] = value;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		if (index > 0 && index < this.size)
		{
			this.values[index] = value;
		}
	}
	
	@Override
	public void addValue(IValue value)
	{
		int index = this.size++;
		if (this.size > this.values.length)
		{
			IValue[] temp = new IValue[this.size];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}
	
	@Override
	public void addValue(int index, IValue value)
	{
		int i = this.size++;
		if (this.size > this.values.length)
		{
			int j = index + 1;
			IValue[] temp = new IValue[this.size];
			System.arraycopy(this.values, 0, temp, 0, index);
			temp[index] = value;
			System.arraycopy(this.values, j, temp, j, i - j);
			this.values = temp;
		}
		else
		{
			System.arraycopy(this.values, index, this.values, index + 1, i - index + 1);
			this.values[index] = value;
		}
	}
	
	@Override
	public IValue getValue(int index)
	{
		if (index >= this.size)
		{
			return null;
		}
		return this.values[index];
	}
	
	@Override
	public IValue getValue(int index, Parameter param)
	{
		return this.values[index];
	}
	
	@Override
	public IType getType(int index, Parameter param)
	{
		return this.values[index].getType();
	}
	
	@Override
	public int getTypeMatch(int index, Parameter param)
	{
		if (index >= this.size)
		{
			return param.defaultValue != null ? 3 : 0;
		}
		
		return this.values[index].getTypeMatch(param.type);
	}
	
	@Override
	public int getVarargsTypeMatch(int index, Parameter param)
	{
		if (index >= this.size)
		{
			return 0;
		}
		
		IValue argument = this.values[index];
		IType type = param.getType();
		int m = argument.getTypeMatch(type);
		if (m != 0)
		{
			return m;
		}
		return argument.getTypeMatch(type.getElementType());
	}
	
	@Override
	public void checkValue(int index, Parameter param, List<Marker> markers, ITypeContext context)
	{
		if (index >= this.size)
		{
			return;
		}
		
		IType type = param.type.getConcreteType(context);
		IValue value = this.values[index];
		IValue value1 = value.withType(type);
		if (value1 == null)
		{
			Marker marker = Markers.create(value.getPosition(), "access.method.argument_type", param.name);
			marker.addInfo("Required Type: " + type);
			marker.addInfo("Value Type: " + value.getType());
			markers.add(marker);
		}
		else
		{
			this.values[index] = value;
		}
	}
	
	@Override
	public void checkVarargsValue(int index, Parameter param, List<Marker> markers, ITypeContext context)
	{
		IType varParamType = param.getType(context);
		
		IValue value = this.values[index];
		IValue value1 = value.withType(varParamType);
		if (value1 != null)
		{
			this.values[index] = value1;
			this.varargs = true;
			return;
		}
		
		IType elementType = varParamType.getElementType();
		
		for (; index < this.size; index++)
		{
			value = this.values[index];
			value1 = value.withType(elementType);
			if (value1 == null)
			{
				Marker marker = Markers.create(value.getPosition(), "access.method.argument_type", param.name);
				marker.addInfo("Required Type: " + elementType);
				marker.addInfo("Value Type: " + value.getType());
				markers.add(marker);
			}
			this.values[index] = value;
		}
	}
	
	@Override
	public void writeValue(int index, String name, IValue defaultValue, MethodWriter writer)
	{
		if (index < this.size)
		{
			this.values[index].writeExpression(writer);
			return;
		}
		defaultValue.writeExpression(writer);
	}
	
	@Override
	public void writeVarargsValue(int index, String name, IType type, MethodWriter writer)
	{
		if (this.varargs)
		{
			this.values[index].writeExpression(writer);
			return;
		}
		
		type = type.getElementType();
		int len = this.size - index;
		if (len < 0)
		{
			writer.writeLDC(0);
			writer.writeTypeInsn(Opcodes.ANEWARRAY, type);
			return;
		}
		
		int opcode = type.getArrayStoreOpcode();
		
		writer.writeLDC(len);
		writer.writeTypeInsn(Opcodes.ANEWARRAY, type);
		
		for (int i = 0; i < len; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			IValue value = this.values[index + i];
			writer.writeLDC(i);
			value.writeExpression(writer);
			writer.writeInsn(opcode);
		}
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(List<Marker> markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i].check(markers, context);
		}
	}
	
	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		this.toString("", buf);
		return buf.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('(');
		int len = this.size;
		for (int i = 0; i < len; i++)
		{
			IValue v = this.values[i];
			if (v == null)
			{
				buffer.append("[null-value]");
			}
			else
			{
				v.toString(prefix, buffer);
			}
			if (i + 1 == len)
			{
				break;
			}
			buffer.append(", ");
		}
		buffer.append(')');
	}
}