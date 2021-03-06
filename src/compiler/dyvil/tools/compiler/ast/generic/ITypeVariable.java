package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;

import org.objectweb.asm.ClassWriter;

public interface ITypeVariable extends IASTNode, INamed
{
	public IGeneric getGeneric();
	
	public void setIndex(int index);
	
	public int getIndex();
	
	// Variance
	
	public void setVariance(Variance variance);
	
	public Variance getVariance();
	
	// Upper Bounds
	
	public int upperBoundCount();
	
	public void setUpperBound(int index, IType bound);
	
	public void addUpperBound(IType bound);
	
	public IType getUpperBound(int index);
	
	public IType[] getUpperBounds();
	
	// Lower Bounds
	
	public void setLowerBound(IType bound);
	
	public IType getLowerBound();
	
	// Super Types
	
	public IClass getTheClass();
	
	public boolean isSuperTypeOf(IType type);
	
	// Resolve Types
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	// Compilation
	
	public void appendSignature(StringBuilder buffer);
	
	public void write(ClassWriter writer);
}
