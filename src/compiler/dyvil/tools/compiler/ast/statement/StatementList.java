package dyvil.tools.compiler.ast.statement;

import java.util.Iterator;

import dyvil.collection.Entry;
import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValueList;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class StatementList extends ASTNode implements IStatement, IValueList, IContext
{
	private IValue[]			values	= new IValue[3];
	private int					valueCount;
	
	private Label[]				labels;
	private Map<Name, Variable>	variables;
	private IType				requiredType;
	
	private IContext			context;
	private IStatement			parent;
	
	public StatementList()
	{
	}
	
	public StatementList(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return STATEMENT_LIST;
	}
	
	@Override
	public void setParent(IStatement parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IStatement getParent()
	{
		return this.parent;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.requiredType.isPrimitive();
	}
	
	@Override
	public IType getType()
	{
		if (this.requiredType != null)
		{
			return this.requiredType;
		}
		if (this.valueCount == 0)
		{
			return this.requiredType = Types.VOID;
		}
		return this.requiredType = this.values[this.valueCount - 1].getType();
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == Types.VOID || type == Types.UNKNOWN)
		{
			this.requiredType = Types.VOID;
			return this;
		}
		
		if (this.valueCount > 0)
		{
			IValue v = this.values[this.valueCount - 1].withType(type, typeContext, markers, context);
			if (v != null)
			{
				this.values[this.valueCount - 1] = v;
				this.requiredType = type;
				return this;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID || type == Types.UNKNOWN)
		{
			return true;
		}
		
		return this.valueCount > 0 && this.values[this.valueCount - 1].isType(type);
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (this.valueCount > 0)
		{
			return this.values[this.valueCount - 1].getTypeMatch(type);
		}
		return 0;
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator(this.values, this.valueCount);
	}
	
	@Override
	public int valueCount()
	{
		return this.valueCount;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.valueCount == 0;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values[index] = value;
	}
	
	@Override
	public void addValue(IValue value)
	{
		int index = this.valueCount++;
		if (index >= this.values.length)
		{
			IValue[] temp = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}
	
	@Override
	public void addValue(IValue value, Label label)
	{
		int index = this.valueCount++;
		if (this.valueCount > this.values.length)
		{
			IValue[] temp = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
		
		if (this.labels == null)
		{
			this.labels = new Label[index + 1];
			this.labels[index] = label;
			return;
		}
		if (index >= this.labels.length)
		{
			Label[] temp = new Label[index + 1];
			System.arraycopy(this.labels, 0, temp, 0, this.labels.length);
			this.labels = temp;
		}
		this.labels[index] = label;
	}
	
	@Override
	public void addValue(int index, IValue value)
	{
		IValue[] temp = new IValue[++this.valueCount];
		System.arraycopy(this.values, 0, temp, 0, index);
		temp[index] = value;
		System.arraycopy(this.values, index, temp, index + 1, this.valueCount - index - 1);
		this.values = temp;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values[index];
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.context = context;
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue v = this.values[i];
			if (v.isStatement())
			{
				((IStatement) v).setParent(this);
			}
			
			v.resolveTypes(markers, this);
		}
		this.context = null;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.context = context;
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue v1 = this.values[i];
			IValue v2 = v1.resolve(markers, this);
			if (v1 != v2)
			{
				this.values[i] = v2;
			}
			
			if (v2.valueTag() == IValue.VARIABLE)
			{
				if (this.variables == null)
				{
					this.variables = new IdentityHashMap();
				}
				
				FieldInitializer fi = (FieldInitializer) v2;
				Variable var = fi.variable;
				this.variables.put(var.name, var);
			}
		}
		this.context = null;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.valueCount == 0)
		{
			return;
		}
		
		this.context = context;
		int len = this.valueCount - 1;
		for (int i = 0; i < len; i++)
		{
			IValue v = this.values[i];
			IValue v1 = v.withType(Types.VOID, null, markers, context);
			if (v1 == null)
			{
				Marker marker = markers.create(v.getPosition(), "statement.type");
				marker.addInfo("Returning Type: " + v.getType());
			}
			else
			{
				v = this.values[i] = v1;
			}
			
			v.checkTypes(markers, this);
		}
		
		IValue lastValue = this.values[len];
		if (this.requiredType != null)
		{
			if (!lastValue.isType(this.requiredType))
			{
				Marker marker = markers.create(lastValue.getPosition(), "block.type");
				marker.addInfo("Block Type: " + this.requiredType);
				marker.addInfo("Returning Type: " + lastValue.getType());
			}
		}
		else
		{
			this.requiredType = lastValue.getType();
		}
		lastValue.checkTypes(markers, this);
		
		this.context = null;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.context = context;
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.valueCount == 1 && this.requiredType != Types.VOID)
		{
			return this.values[0].foldConstants();
		}
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.valueCount == 1 && this.requiredType != Types.VOID)
		{
			return this.values[0].cleanup(context, compilableList);
		}
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public boolean isStatic()
	{
		return this.context.isStatic();
	}
	
	@Override
	public IDyvilHeader getHeader()
	{
		return this.context.getHeader();
	}
	
	@Override
	public IClass getThisClass()
	{
		return this.context.getThisClass();
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
	public IType resolveType(Name name)
	{
		return this.context.resolveType(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return this.context.resolveTypeVariable(name);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variables != null)
		{
			IDataMember field = this.variables.get(name);
			if (field != null)
			{
				return field;
			}
		}
		
		return this.context.resolveField(name);
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
	public boolean handleException(IType type)
	{
		return this.context.handleException(type);
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		return this.context.getVisibility(member);
	}
	
	@Override
	public Label resolveLabel(Name name)
	{
		if (this.labels != null)
		{
			for (Label label : this.labels)
			{
				if (label != null && name == label.name)
				{
					return label;
				}
			}
		}
		
		return this.parent == null ? null : this.parent.resolveLabel(name);
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.requiredType == Types.VOID)
		{
			this.writeStatement(writer);
			return;
		}
		
		org.objectweb.asm.Label start = new org.objectweb.asm.Label();
		org.objectweb.asm.Label end = new org.objectweb.asm.Label();
		
		writer.writeLabel(start);
		int count = writer.localCount();
		int len = this.valueCount - 1;
		
		if (this.labels == null)
		{
			for (int i = 0; i < len; i++)
			{
				this.values[i].writeStatement(writer);
			}
			this.values[len].writeExpression(writer);
		}
		else
		{
			for (int i = 0; i < len; i++)
			{
				Label l = this.labels[i];
				if (l != null)
				{
					writer.writeLabel(l.target);
				}
				
				this.values[i].writeStatement(writer);
			}
			
			Label l = this.labels[len];
			if (l != null)
			{
				writer.writeLabel(l.target);
			}
			
			this.values[len].writeExpression(writer);
		}
		
		writer.resetLocals(count);
		writer.writeLabel(end);
		
		if (this.variables == null)
		{
			return;
		}
		
		for (Entry<Name, Variable> entry : this.variables)
		{
			Variable var = entry.getValue();
			writer.writeLocal(var.index, var.name.qualified, var.type.getExtendedName(), var.type.getSignature(), start, end);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		org.objectweb.asm.Label start = new org.objectweb.asm.Label();
		org.objectweb.asm.Label end = new org.objectweb.asm.Label();
		
		writer.writeLabel(start);
		int count = writer.localCount();
		
		if (this.labels == null)
		{
			for (int i = 0; i < this.valueCount; i++)
			{
				this.values[i].writeStatement(writer);
			}
		}
		else
		{
			for (int i = 0; i < this.valueCount; i++)
			{
				Label l = this.labels[i];
				if (l != null)
				{
					writer.writeLabel(l.target);
				}
				
				this.values[i].writeStatement(writer);
			}
		}
		
		writer.resetLocals(count);
		writer.writeLabel(end);
		
		if (this.variables == null)
		{
			return;
		}
		
		for (Entry<Name, Variable> entry : this.variables)
		{
			entry.getValue().writeLocal(writer, start, end);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.valueCount == 0)
		{
			buffer.append(Formatting.Expression.emptyExpression);
		}
		else
		{
			buffer.append('\n').append(prefix).append('{').append('\n');
			String prefix1 = prefix + Formatting.Method.indent;
			ICodePosition prevPos = null;
			
			for (int i = 0; i < this.valueCount; i++)
			{
				IValue value = this.values[i];
				buffer.append(prefix1);
				
				if (prevPos != null)
				{
					ICodePosition pos = value.getPosition();
					if (pos != null && pos.startLine() - prevPos.endLine() > 1)
					{
						buffer.append('\n').append(prefix1);
					}
					prevPos = pos;
				}
				
				if (this.labels != null)
				{
					Label l = this.labels[i];
					if (l != null)
					{
						buffer.append(l.name).append(Formatting.Expression.labelSeperator);
					}
				}
				
				value.toString(prefix1, buffer);
				buffer.append(";\n");
			}
			buffer.append(prefix).append('}');
		}
	}
}
