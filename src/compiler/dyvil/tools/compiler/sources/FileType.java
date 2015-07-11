package dyvil.tools.compiler.sources;

import java.io.File;

import dyvil.lang.Map;

import dyvil.collection.mutable.HashMap;
import dyvil.tools.compiler.ast.header.JavaUnit;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.DyvilUnit;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.CodeFile;

public class FileType implements IFileType
{
	@FunctionalInterface
	interface HeaderSupplier
	{
		public DyvilHeader newHeader(Package pack, CodeFile inputFile, File outputFile);
	}
	
	public static final Map<String, IFileType>	fileTypes		= new HashMap();
	
	public static final IFileType				DYVIL_UNIT		= new FileType("dyv", DyvilUnit::new);
	public static final IFileType				JAVA_UNIT		= new FileType("java", JavaUnit::new);
	public static final IFileType				DYVIL_HEADER	= new FileType("dyh", DyvilHeader::new);
	
	static
	{
		fileTypes.put("dyv", DYVIL_UNIT);
		fileTypes.put("dyvil", DYVIL_UNIT);
		fileTypes.put("dyh", DYVIL_HEADER);
		fileTypes.put("dyvilh", DYVIL_HEADER);
		
		// Java units use the same AST classes, only the parser is different
		fileTypes.put("java", JAVA_UNIT);
	}
	
	protected String							extension;
	protected HeaderSupplier					headerSupplier;
	
	public FileType(String extension, HeaderSupplier headerSupplier)
	{
		this.extension = extension;
		this.headerSupplier = headerSupplier;
	}
	
	@Override
	public String getExtension()
	{
		return this.extension;
	}
	
	@Override
	public ICompilationUnit createUnit(Package pack, CodeFile inputFile, File outputFile)
	{
		DyvilHeader header = this.headerSupplier.newHeader(pack, inputFile, outputFile);
		pack.addHeader(header);
		return header;
	}
}
