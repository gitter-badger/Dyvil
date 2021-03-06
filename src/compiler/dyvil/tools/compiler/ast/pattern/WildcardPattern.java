package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public final class WildcardPattern extends ASTNode implements IPattern
{
	public WildcardPattern(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getPatternType()
	{
		return WILDCARD;
	}
	
	@Override
	public boolean isExhaustive()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return Types.ANY;
	}
	
	@Override
	public IPattern withType(IType type)
	{
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return true;
	}
	
	@Override
	public void writeJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex < 0)
		{
			writer.writeInsn(Opcodes.AUTO_POP);
		}
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, Label elseLabel) throws BytecodeException
	{
		if (varIndex < 0)
		{
			writer.writeInsn(Opcodes.AUTO_POP);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('_');
	}
}
