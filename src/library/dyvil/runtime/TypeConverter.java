package dyvil.runtime;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;

import org.objectweb.asm.MethodVisitor;

import sun.invoke.util.BytecodeDescriptor;

import static dyvil.runtime.Wrapper.*;

public class TypeConverter
{
	private static final String		DYVIL_LANG_NUMBER	= "dyvil/lang/Number";
	
	private static final int		NUM_WRAPPERS		= Wrapper.values().length;
	
	private static final String		NAME_OBJECT			= "java/lang/Object";
	private static final String		WRAPPER_PREFIX		= "Ldyvil/lang/";
	
	// Same for all primitives; name of the boxing method
	private static final String		NAME_BOX_METHOD		= "apply";
	
	// Table of opcodes for widening primitive conversions
	private static final int[][]	wideningOpcodes		= new int[NUM_WRAPPERS][NUM_WRAPPERS];
	
	private static final Wrapper[]	FROM_WRAPPER_NAME	= new Wrapper[16];
	
	// Table of wrappers for primitives, indexed by ASM type sorts
	private static final Wrapper[]	FROM_TYPE_SORT		= new Wrapper[16];
	
	static
	{
		for (Wrapper w : Wrapper.values())
		{
			if (w.basicTypeChar() != 'L')
			{
				int wi = hashWrapperName(w.wrapperSimpleName());
				assert FROM_WRAPPER_NAME[wi] == null;
				FROM_WRAPPER_NAME[wi] = w;
			}
		}
		
		initWidening(LONG, Opcodes.I2L, BYTE, SHORT, INT, CHAR);
		initWidening(LONG, Opcodes.F2L, FLOAT);
		initWidening(FLOAT, Opcodes.I2F, BYTE, SHORT, INT, CHAR);
		initWidening(FLOAT, Opcodes.L2F, LONG);
		initWidening(DOUBLE, Opcodes.I2D, BYTE, SHORT, INT, CHAR);
		initWidening(DOUBLE, Opcodes.F2D, FLOAT);
		initWidening(DOUBLE, Opcodes.L2D, LONG);
		
		FROM_TYPE_SORT[Type.BYTE] = Wrapper.BYTE;
		FROM_TYPE_SORT[Type.SHORT] = Wrapper.SHORT;
		FROM_TYPE_SORT[Type.INT] = Wrapper.INT;
		FROM_TYPE_SORT[Type.LONG] = Wrapper.LONG;
		FROM_TYPE_SORT[Type.CHAR] = Wrapper.CHAR;
		FROM_TYPE_SORT[Type.FLOAT] = Wrapper.FLOAT;
		FROM_TYPE_SORT[Type.DOUBLE] = Wrapper.DOUBLE;
		FROM_TYPE_SORT[Type.BOOLEAN] = Wrapper.BOOLEAN;
	}
	
	private static void initWidening(Wrapper to, int opcode, Wrapper... from)
	{
		for (Wrapper f : from)
		{
			wideningOpcodes[f.ordinal()][to.ordinal()] = opcode;
		}
	}
	
	private static int hashWrapperName(String xn)
	{
		if (xn.length() < 3)
		{
			return 0;
		}
		return (3 * xn.charAt(1) + xn.charAt(2)) % 16;
	}
	
	private static Wrapper wrapperOrNullFromDescriptor(String desc)
	{
		if (!desc.startsWith(WRAPPER_PREFIX))
		{
			// Not a class type (array or method), so not a boxed type
			// or not in the right package
			return null;
		}
		// Pare it down to the simple class name
		String cname = desc.substring(WRAPPER_PREFIX.length(), desc.length() - 1);
		// Hash to a Wrapper
		Wrapper w = FROM_WRAPPER_NAME[hashWrapperName(cname)];
		if (w == null || w.wrapperSimpleName().equals(cname))
		{
			return w;
		}
		return null;
	}
	
	private static String wrapperName(Wrapper w)
	{
		return "dyvil/lang/" + w.wrapperSimpleName();
	}
	
	private static String unboxMethod(Wrapper w)
	{
		return w.primitiveSimpleName() + "Value";
	}
	
	private static String boxingDescriptor(Wrapper w)
	{
		return String.format("(%s)L%s;", w.basicTypeChar(), wrapperName(w));
	}
	
	private static String unboxingDescriptor(Wrapper w)
	{
		return "()" + w.basicTypeChar();
	}
	
	static void boxIfTypePrimitive(MethodVisitor mv, Type t)
	{
		Wrapper w = FROM_TYPE_SORT[t.getSort()];
		if (w != null)
		{
			box(mv, w);
		}
	}
	
	static void widen(MethodVisitor mv, Wrapper ws, Wrapper wt)
	{
		if (ws != wt)
		{
			int opcode = wideningOpcodes[ws.ordinal()][wt.ordinal()];
			if (opcode != Opcodes.NOP)
			{
				mv.visitInsn(opcode);
			}
		}
	}
	
	static void box(MethodVisitor mv, Wrapper w)
	{
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapperName(w), NAME_BOX_METHOD, boxingDescriptor(w), false);
	}
	
	static void unbox(MethodVisitor mv, String sname, Wrapper wt)
	{
		if (sname == DYVIL_LANG_NUMBER)
		{
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, sname, unboxMethod(wt), unboxingDescriptor(wt), true);
			return;
		}
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, sname, unboxMethod(wt), unboxingDescriptor(wt), false);
	}
	
	private static String descriptorToName(String desc)
	{
		int last = desc.length() - 1;
		if (desc.charAt(0) == 'L' && desc.charAt(last) == ';')
		{
			// In descriptor form
			return desc.substring(1, last);
		}
		// Already in internal name form
		return desc;
	}
	
	static void cast(MethodVisitor mv, String ds, String dt)
	{
		String ns = descriptorToName(ds);
		String nt = descriptorToName(dt);
		if (!nt.equals(ns) && !nt.equals(NAME_OBJECT))
		{
			mv.visitTypeInsn(Opcodes.CHECKCAST, nt);
		}
	}
	
	private boolean isPrimitive(Wrapper w)
	{
		return w != OBJECT;
	}
	
	private static Wrapper toWrapper(String desc)
	{
		char first = desc.charAt(0);
		if (first == '[' || first == '(')
		{
			first = 'L';
		}
		return Wrapper.forBasicType(first);
	}
	
	static void convertType(MethodVisitor mv, Class<?> arg, Class<?> target, Class<?> functional)
	{
		if (arg.equals(target) && arg.equals(functional))
		{
			return;
		}
		if (arg == Void.TYPE || target == Void.TYPE)
		{
			return;
		}
		if (arg.isPrimitive())
		{
			Wrapper wArg = Wrapper.forPrimitiveType(arg);
			if (target.isPrimitive())
			{
				// Both primitives: widening
				widen(mv, wArg, Wrapper.forPrimitiveType(target));
			}
			else
			{
				// Primitive argument to reference target
				String dTarget = BytecodeDescriptor.unparse(target);
				Wrapper wPrimTarget = wrapperOrNullFromDescriptor(dTarget);
				if (wPrimTarget != null)
				{
					// The target is a boxed primitive type, widen to get there
					// before boxing
					widen(mv, wArg, wPrimTarget);
					box(mv, wPrimTarget);
				}
				else
				{
					// Otherwise, box and cast
					box(mv, wArg);
					cast(mv, wrapperName(wArg), dTarget);
				}
			}
		}
		else
		{
			String dArg = BytecodeDescriptor.unparse(arg);
			String dSrc;
			if (functional.isPrimitive())
			{
				dSrc = dArg;
			}
			else
			{
				// Cast to convert to possibly more specific type, and generate
				// CCE for invalid arg
				dSrc = BytecodeDescriptor.unparse(functional);
				cast(mv, dArg, dSrc);
			}
			String dTarget = BytecodeDescriptor.unparse(target);
			if (target.isPrimitive())
			{
				Wrapper wTarget = toWrapper(dTarget);
				// Reference argument to primitive target
				Wrapper wps = wrapperOrNullFromDescriptor(dSrc);
				if (wps != null)
				{
					if (wps.isSigned() || wps.isFloating())
					{
						// Boxed number to primitive
						unbox(mv, wrapperName(wps), wTarget);
					}
					else
					{
						// Character or Boolean
						unbox(mv, wrapperName(wps), wps);
						widen(mv, wps, wTarget);
					}
				}
				else
				{
					// Source type is reference type, but not boxed type,
					// assume it is super type of target type
					String intermediate;
					if (wTarget.isSigned() || wTarget.isFloating())
					{
						// Boxed number to primitive
						intermediate = DYVIL_LANG_NUMBER;
					}
					else
					{
						// Character or Boolean
						intermediate = wrapperName(wTarget);
					}
					cast(mv, dSrc, intermediate);
					unbox(mv, intermediate, wTarget);
				}
			}
			else
			{
				// Both reference types: just case to target type
				cast(mv, dSrc, dTarget);
			}
		}
	}
}
