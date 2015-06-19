package dyvil.tools.repl;

import java.util.HashMap;
import java.util.Map;

import dyvil.lang.List;

import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class REPLContext extends DyvilHeader implements IValued
{
	private static final CodePosition	CODE_POSITION	= new CodePosition(1, 0, 1);
	
	protected static int				resultIndex;
	
	private Map<Name, REPLVariable>		variables		= new HashMap();
	
	private String currentClassName;
	private String						currentInternalName;
	private IValue						value;
	private ImportDeclaration			importDeclaration;
	private IncludeDeclaration			includeDeclaration;
	private IClass						tempClass;
	private List<IClassCompilable>		compilableList	= new ArrayList();
	private List<IClassCompilable>		innerClassList	= new ArrayList();
	
	public REPLContext()
	{
		super("REPL");
	}
	
	private boolean reportErrors(MarkerList markers)
	{
		if (!markers.isEmpty())
		{
			StringBuilder buf = new StringBuilder();
			String code = DyvilREPL.currentCode;
			markers.sort();
			for (Marker m : markers)
			{
				m.log(code, buf);
			}
			
			System.err.println(buf.toString());
			
			if (markers.getErrors() > 0)
			{
				return true;
			}
		}
		return false;
	}
	
	protected void processHeader()
	{
		MarkerList markers = new MarkerList();
		
		if (this.tempClass != null)
		{
			this.tempClass.resolveTypes(markers, this);
			this.tempClass.resolve(markers, this);
			this.tempClass.checkTypes(markers, this);
			this.tempClass.check(markers, this);
			this.tempClass.foldConstants();
			
			super.addClass(this.tempClass);
			System.out.println("Defined class " + this.tempClass.getName());
			this.tempClass = null;
		}
		
		if (this.includeDeclaration != null)
		{
			IncludeDeclaration inc = this.includeDeclaration;
			this.includeDeclaration = null;
			inc.resolve(markers);
			
			if (this.reportErrors(markers))
			{
				return;
			}
			
			this.addIncludeToArray(inc);
			System.out.println("Included " + inc.getHeader().getFullName());
			return;
		}
		
		if (this.importDeclaration == null || this.importDeclaration.theImport == null)
		{
			return;
		}
		this.importDeclaration.resolveTypes(markers, this, false);
		
		if (this.reportErrors(markers))
		{
			return;
		}
		
		boolean isStatic = this.importDeclaration.isStatic;
		
		if (isStatic)
		{
			super.addUsing(this.importDeclaration);
			System.out.println("Using " + this.importDeclaration.theImport);
		}
		else
		{
			super.addImport(this.importDeclaration);
			System.out.println("Imported " + this.importDeclaration.theImport);
		}
		
		this.importDeclaration = null;
	}
	
	protected void processValue()
	{
		if (this.value == null)
		{
			return;
		}
		
		MarkerList markers = new MarkerList();
		this.currentClassName = "REPL" + resultIndex;
		this.currentInternalName = "repl/".concat(this.currentClassName);
		Name name = Name.getQualified("res" + resultIndex);
		IValue value = this.value;
		IType type = Types.UNKNOWN;
		ICodePosition position = CODE_POSITION;
		
		this.value = null;
		
		REPLVariable field = new REPLVariable(position, name, type, value);
		field.modifiers = Modifiers.FINAL;
		field.resolveTypes(markers, this);
		field.resolve(markers, this);
		field.checkTypes(markers, this);
		field.check(markers, this);
		
		if (this.reportErrors(markers))
		{
			return;
		}
		
		field.foldConstants();
		
		for (IClassCompilable icc : this.innerClassList)
		{
			try
			{
				String fileName = icc.getFileName();
				byte[] bytes = ClassWriter.compile(icc);
				ReflectUtils.unsafe.defineClass(fileName, bytes, 0, bytes.length, null, null);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		
		field.compute(this.currentInternalName, this.compilableList);
		
		this.compilableList.clear();
		this.innerClassList.clear();
		
		if (field.getType() != Types.VOID)
		{
			this.variables.put(name, field);
			System.out.println(field.toString());
			resultIndex++;
		}
	}
	
	@Override
	public void addOperator(Operator op)
	{
		this.operators.put(op.name, op);
		
		System.out.println("Defined " + op);
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		return this.operators.get(name);
	}
	
	@Override
	public void addImport(ImportDeclaration component)
	{
		this.importDeclaration = component;
	}
	
	@Override
	public void addUsing(ImportDeclaration component)
	{
		this.importDeclaration = component;
	}
	
	@Override
	public void addInclude(IncludeDeclaration component)
	{
		this.includeDeclaration = component;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		this.tempClass = iclass;
	}
	
	@Override
	public void addInnerClass(IClassCompilable iclass)
	{
		if (iclass.hasSeparateFile())
		{
			iclass.setInnerIndex(this.currentInternalName, this.innerClassList.size());
			this.innerClassList.add(iclass);
		}
		else
		{
			iclass.setInnerIndex(this.currentInternalName, this.compilableList.size());
			this.compilableList.add(iclass);
		}
	}
	
	@Override
	public IField resolveField(Name name)
	{
		IField f = this.variables.get(name);
		if (f != null)
		{
			return f;
		}
		return null;
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return true;
	}
	
	@Override
	public byte getVisibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == this || iclass == null)
		{
			return VISIBLE;
		}
		
		int level = member.getAccessLevel();
		if ((level & Modifiers.SEALED) != 0)
		{
			if (iclass instanceof ExternalClass)
			{
				return SEALED;
			}
			// Clear the SEALED bit by ANDing with 0b1111
			level &= 0b1111;
		}
		if (level == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}
		
		return INVISIBLE;
	}
	
	@Override
	public String getName()
	{
		return this.currentClassName;
	}
	
	@Override
	public String getFullName()
	{
		return this.currentInternalName;
	}
	
	@Override
	public String getFullName(String name)
	{
		return this.currentInternalName + '$' + name;
	}
	
	@Override
	public String getInternalName()
	{
		return this.currentInternalName;
	}
	
	@Override
	public String getInternalName(String name)
	{
		return this.currentInternalName + '$' + name;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
}
