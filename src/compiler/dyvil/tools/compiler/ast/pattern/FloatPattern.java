package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public final class FloatPattern extends ASTNode implements IPattern
{
	private float	value;
	
	public FloatPattern(ICodePosition position, float value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public int getPatternType()
	{
		return FLOAT;
	}
	
	@Override
	public IType getType()
	{
		return Types.FLOAT;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		return IPattern.primitiveWithType(this, type, Types.FLOAT);
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.FLOAT || type.isSuperTypeOf(Types.FLOAT);
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.FLOAD, varIndex);
		}
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_FCMPEQ, elseLabel);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex >= 0)
		{
			writer.writeVarInsn(Opcodes.FLOAD, varIndex);
		}
		writer.writeLDC(this.value);
		writer.writeJumpInsn(Opcodes.IF_FCMPNE, elseLabel);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}
