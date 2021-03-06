package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class ArrayForStatement extends ForEachStatement
{
	public static final Name	$index	= Name.getQualified("$index");
	public static final Name	$length	= Name.getQualified("$length");
	public static final Name	$array	= Name.getQualified("$array");
	
	protected Variable			indexVar;
	protected Variable			lengthVar;
	protected Variable			arrayVar;
	
	protected IMethod			boxMethod;
	
	public ArrayForStatement(Variable var, IValue action)
	{
		this(var, action, var.value.getType());
	}
	
	public ArrayForStatement(Variable var, IValue action, IType arrayType)
	{
		super(var, action);
		
		Variable temp = new Variable();
		temp.type = Types.INT;
		temp.name = $index;
		this.indexVar = temp;
		
		temp = new Variable();
		temp.type = Types.INT;
		temp.name = $length;
		this.lengthVar = temp;
		
		temp = new Variable();
		temp.type = arrayType;
		temp.name = $array;
		this.arrayVar = temp;
		
		IType elementType = arrayType.getElementType();
		IType varType = var.type;
		boolean primitive = varType.isPrimitive();
		if (primitive != elementType.isPrimitive())
		{
			if (primitive)
			{
				this.boxMethod = varType.getUnboxMethod();
			}
			else
			{
				this.boxMethod = elementType.getBoxMethod();
			}
		}
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == this.variable.name)
		{
			return this.variable;
		}
		
		if (name == $index)
		{
			return this.indexVar;
		}
		if (name == $length)
		{
			return this.lengthVar;
		}
		if (name == $array)
		{
			return this.arrayVar;
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		org.objectweb.asm.Label startLabel = this.startLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label updateLabel = this.updateLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = this.endLabel.target = new org.objectweb.asm.Label();
		
		Variable var = this.variable;
		Variable arrayVar = this.arrayVar;
		Variable indexVar = this.indexVar;
		Variable lengthVar = this.lengthVar;
		int lineNumber = this.getLineNumber();
		
		org.objectweb.asm.Label scopeLabel = new org.objectweb.asm.Label();
		writer.writeLabel(scopeLabel);
		
		// Load the array
		var.value.writeExpression(writer);
		
		// Local Variables
		int locals = writer.localCount();
		writer.writeInsn(Opcodes.DUP);
		arrayVar.writeInit(writer, null);
		// Load the length
		writer.writeLineNumber(lineNumber);
		writer.writeInsn(Opcodes.ARRAYLENGTH);
		writer.writeInsn(Opcodes.DUP);
		lengthVar.writeInit(writer, null);
		// Set index to 0
		writer.writeLDC(0);
		indexVar.writeInit(writer, null);
		
		// Initial Boundary Check - if the length is 0, skip the loop
		writer.writeJumpInsn(Opcodes.IFEQ, endLabel);
		writer.writeTargetLabel(startLabel);
		
		// Load the element
		arrayVar.writeGet(writer, null, lineNumber);
		indexVar.writeGet(writer, null, lineNumber);
		writer.writeLineNumber(lineNumber);
		writer.writeInsn(arrayVar.type.getElementType().getArrayLoadOpcode());
		// Auto(un)boxing
		if (this.boxMethod != null)
		{
			this.boxMethod.writeCall(writer, null, EmptyArguments.INSTANCE, null, lineNumber);
		}
		// Store variable
		var.writeInit(writer, null);
		
		// Action
		if (this.action != null)
		{
			this.action.writeStatement(writer);
		}
		
		writer.writeLabel(updateLabel);
		// Increase index
		writer.writeIINC(indexVar.index, 1);
		// Boundary Check
		indexVar.writeGet(writer, null, lineNumber);
		lengthVar.writeGet(writer, null, lineNumber);
		writer.writeJumpInsn(Opcodes.IF_ICMPLT, startLabel);
		
		// Local Variables
		writer.resetLocals(locals);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
		indexVar.writeLocal(writer, scopeLabel, endLabel);
		lengthVar.writeLocal(writer, scopeLabel, endLabel);
		arrayVar.writeLocal(writer, scopeLabel, endLabel);
	}
}
