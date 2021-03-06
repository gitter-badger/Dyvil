package dyvil.tools.compiler.util;

import dyvil.string.CharUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class Util
{
	public static void propertySignatureToString(IProperty property, StringBuilder buf)
	{
		buf.append(ModifierTypes.FIELD.toString(property.getModifiers()));
		property.getType().toString("", buf);
		buf.append(' ').append(property.getName());
	}
	
	public static void methodSignatureToString(IMethod method, StringBuilder buf)
	{
		buf.append(ModifierTypes.METHOD.toString(method.getModifiers()));
		method.getType().toString("", buf);
		buf.append(' ').append(method.getName()).append('(');
		
		int params = method.parameterCount();
		if (params > 0)
		{
			method.getParameter(0).getType().toString("", buf);
			for (int i = 1; i < params; i++)
			{
				buf.append(", ");
				method.getParameter(i).getType().toString("", buf);
			}
		}
		
		buf.append(')');
	}
	
	public static void astToString(String prefix, IASTNode[] array, int size, String separator, StringBuilder buffer)
	{
		if (size <= 0)
		{
			return;
		}
		
		array[0].toString(prefix, buffer);
		for (int i = 1; i < size; i++)
		{
			buffer.append(separator);
			array[i].toString(prefix, buffer);
		}
	}
	
	public static String getAdder(String methodName)
	{
		StringBuilder builder = new StringBuilder("add");
		int len = methodName.length() - 1;
		builder.append(CharUtils.toUpperCase(methodName.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(methodName.charAt(i));
		}
		return builder.toString();
	}
	
	public static String getSetter(String methodName)
	{
		StringBuilder builder = new StringBuilder("set");
		int len = methodName.length();
		builder.append(CharUtils.toUpperCase(methodName.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(methodName.charAt(i));
		}
		return builder.toString();
	}
	
	public static String getGetter(String methodName)
	{
		StringBuilder builder = new StringBuilder("get");
		int len = methodName.length();
		builder.append(CharUtils.toUpperCase(methodName.charAt(0)));
		for (int i = 1; i < len; i++)
		{
			builder.append(methodName.charAt(i));
		}
		return builder.toString();
	}
	
	public static IValue constant(IValue value, MarkerList markers)
	{
		int depth = DyvilCompiler.maxConstantDepth;
		while (!value.isConstant())
		{
			if (--depth < 0)
			{
				markers.add(value.getPosition(), "value.constant", value.toString(), DyvilCompiler.maxConstantDepth);
				return value.getType().getDefaultValue();
			}
			value = value.foldConstants();
		}
		return value;
	}
	
	public static void prependValue(IMethod method, IValue value)
	{
		IValue value1 = method.getValue();
		if (value1 instanceof IValueList)
		{
			((IValueList) value1).addValue(0, value);
		}
		else if (value1 != null)
		{
			StatementList list = new StatementList(null);
			list.addValue(value1);
			list.addValue(value);
			method.setValue(list);
		}
		else
		{
			method.setValue(value);
		}
	}
	
	public static String toTime(long nanos)
	{
		if (nanos < 1000L)
		{
			return nanos + " ns";
		}
		if (nanos < 1000000L)
		{
			return nanos / 1000000D + " ms";
		}
		
		long l = 0L;
		StringBuilder builder = new StringBuilder();
		if (nanos >= 60_000_000_000L) // minutes
		{
			l = nanos / 60_000_000_000L;
			builder.append(l).append(" min ");
			nanos -= l;
		}
		if (nanos >= 1_000_000_000L) // seconds
		{
			l = nanos / 1_000_000_000L;
			builder.append(l).append(" s ");
			nanos -= l;
		}
		if (nanos >= 1_000_000L)
		{
			l = nanos / 1_000_000L;
			builder.append(l).append(" ms ");
			nanos -= l;
		}
		return builder.deleteCharAt(builder.length() - 1).toString();
	}
}
