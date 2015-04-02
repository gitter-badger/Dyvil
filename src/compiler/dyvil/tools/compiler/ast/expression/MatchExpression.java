package dyvil.tools.compiler.ast.expression;

import org.objectweb.asm.Label;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.pattern.IPattern;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public final class MatchExpression extends ASTNode implements IValue
{
	private IValue			value;
	private CaseStatement[]	cases;
	private int				caseCount;
	private boolean			exhaustive;
	
	private IType			type;
	
	public MatchExpression(IValue value, CaseStatement[] cases)
	{
		this.value = value;
		this.cases = cases;
		this.caseCount = cases.length;
	}
	
	@Override
	public int getValueType()
	{
		return MATCH;
	}
	
	@Override
	public boolean isPrimitive()
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			IValue v = this.cases[i];
			if (v != null && v.isPrimitive())
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IType getType()
	{
		if (this.type != null)
		{
			return this.type;
		}
		
		int len = this.caseCount;
		if (len == 0)
		{
			this.type = Types.VOID;
			return this.type;
		}
		
		IType t = null;
		for (int i = 0; i < len; i++)
		{
			IValue v = this.cases[i].value;
			if (v == null)
			{
				continue;
			}
			IType t1 = this.cases[i].value.getType();
			if (t == null)
			{
				t = t1;
				continue;
			}
			
			t = Type.findCommonSuperType(t, t1);
			if (t == null)
			{
				return this.type = Types.VOID;
			}
		}
		
		if (t == null)
		{
			return this.type = Types.VOID;
		}
		return this.type = t;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Types.VOID ? this : IValue.super.withType(type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID)
		{
			return true;
		}
		
		for (int i = 0; i < this.caseCount; i++)
		{
			IValue v = this.cases[i].value;
			if (v != null && !v.isType(type))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		for (int i = 0; i < this.caseCount; i++)
		{
			IValue v = this.cases[i].value;
			if (v != null && !v.isType(type))
			{
				return 0;
			}
		}
		return 3;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.value.resolveTypes(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.value = this.value.resolve(markers, context);
		
		IType type = this.type = this.value.getType();
		for (int i = 0; i < this.caseCount; i++)
		{
			CaseStatement c = this.cases[i];
			if (this.exhaustive)
			{
				markers.add(c.getPosition(), "pattern.dead");
			}
			
			IPattern pattern = c.pattern;
			if (pattern.getPatternType() == IPattern.WILDCARD)
			{
				if (c.condition == null)
				{
					this.exhaustive = true;
				}
			}
			else
			{
				IPattern pattern1 = pattern.withType(type);
				if (pattern1 == null)
				{
					Marker marker = markers.create(pattern.getPosition(), "pattern.type");
					marker.addInfo("Pattern Type: " + pattern.getType());
					marker.addInfo("Value Type: " + type);
				}
				else
				{
					c.pattern = pattern1;
				}
			}
			
			this.cases[i].resolve(markers, context);
		}
		
		if (type == Types.BOOLEAN && this.caseCount >= 2)
		{
			this.exhaustive = true;
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.value.checkTypes(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.value.check(markers, context);
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.value = this.value.foldConstants();
		for (int i = 0; i < this.caseCount; i++)
		{
			this.cases[i].foldConstants();
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.write(writer, true);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.write(writer, false);
	}
	
	private void write(MethodWriter writer, boolean expr)
	{
		int varIndex = writer.registerLocal();
		
		IType type = this.value.getType();
		this.value.writeExpression(writer);
		writer.writeVarInsn(type.getStoreOpcode(), varIndex);
		
		int localCount = writer.registerLocal();
		
		Label elseLabel = new Label();
		Label endLabel = new Label();
		Label destLabel = null;
		for (int i = 0; i < this.caseCount;)
		{
			CaseStatement c = this.cases[i];
			IPattern pattern = c.pattern;
			IValue condition = c.condition;
			IValue value = c.value;
			
			if (value == null)
			{
				if (destLabel == null)
				{
					destLabel = new Label();
				}
				
				if (condition != null)
				{
					pattern.writeInvJump(writer, varIndex, elseLabel);
					condition.writeJump(writer, destLabel);
				}
				else
				{
					pattern.writeJump(writer, varIndex, destLabel);
				}
				
				writer.resetLocals(localCount);
				++i;
				continue;
			}
			
			pattern.writeInvJump(writer, varIndex, elseLabel);
			if (condition != null)
			{
				condition.writeInvJump(writer, elseLabel);
			}
			
			if (destLabel != null)
			{
				writer.writeLabel(destLabel);
				destLabel = null;
			}
			
			if (expr)
			{
				value.writeExpression(writer);
			}
			else
			{
				value.writeStatement(writer);
			}
			
			writer.resetLocals(localCount);
			writer.writeJumpInsn(Opcodes.GOTO, endLabel);
			writer.writeLabel(elseLabel);
			if (++i < this.caseCount)
			{
				elseLabel = new Label();
			}
		}
		
		// MatchError
		writer.writeLabel(elseLabel);
		if (!this.exhaustive)
		{
			writer.writeTypeInsn(Opcodes.NEW, "dyvil/lang/MatchError");
			writer.writeInsn(Opcodes.DUP);
			writer.writeVarInsn(type.getLoadOpcode(), varIndex);
			writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "dyvil/lang/MatchError", "<init>", "(" + type.getExtendedName() + ")V", false);
			writer.writeInsn(Opcodes.ATHROW);
			writer.resetLocals(varIndex);
		}
		writer.writeLabel(endLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		if (this.caseCount == 1)
		{
			buffer.append(" match ");
			this.cases[0].toString(prefix, buffer);
			return;
		}
		
		buffer.append(" match {\n");
		String prefix1 = prefix + Formatting.Method.indent;
		for (int i = 0; i < this.caseCount; i++)
		{
			buffer.append(prefix1);
			this.cases[i].toString(prefix1, buffer);
			buffer.append('\n');
		}
		buffer.append(prefix).append('}');
	}
}