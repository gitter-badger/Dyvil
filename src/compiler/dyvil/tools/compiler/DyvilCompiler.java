package dyvil.tools.compiler;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

import dyvil.io.AppendableOutputStream;
import dyvil.io.LoggerOutputStream;
import dyvil.tools.compiler.ast.dwt.DWTFile;
import dyvil.tools.compiler.ast.structure.DyvilFile;
import dyvil.tools.compiler.ast.structure.ICompilationUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.CompilerConfig;
import dyvil.tools.compiler.config.ConfigParser;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.Util;

public final class DyvilCompiler
{
	public static final String				VERSION			= "1.0.0";
	public static final String				DYVIL_VERSION	= "1.0.0";
	
	public static boolean					parseStack;
	public static boolean					logFile			= true;
	public static boolean					debug;
	public static int						constantFolding;
	
	public static Logger					logger			= Logger.getLogger("DYVILC");
	public static LoggerOutputStream		loggerOut		= new LoggerOutputStream(logger, "TEST-OUT");
	public static LoggerOutputStream		loggerErr		= new LoggerOutputStream(logger, "TEST-ERR");
	public static DateFormat				format			= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static CompilerConfig			config			= new CompilerConfig();
	public static Set<CompilerPhase>		states			= new TreeSet();
	
	public static ParserManager				configParser	= new ParserManager();
	public static List<File>				files			= new ArrayList();
	public static List<ICompilationUnit>	units			= new ArrayList();
	
	public static void main(String[] args)
	{
		// Sets up States from arguments
		for (int i = 1; i < args.length; i++)
		{
			addStates(args[i]);
		}
		
		long now = System.nanoTime();
		
		// Sets up the logger
		initLogger();
		
		// Loads the config
		configParser.parse(new CodeFile(args[0]), new ConfigParser(config));
		
		File sourceDir = config.sourceDir;
		File outputDir = config.outputDir;
		Package root = Package.rootPackage;
		int states = DyvilCompiler.states.size();
		int libs = config.libraries.size();
		
		logger.info("Dyvil Compiler " + VERSION + " for Dyvil " + DYVIL_VERSION);
		logger.info("");
		
		if (DyvilCompiler.states.contains(CompilerPhase.RESOLVE_TYPES))
		{
			// Loads libraries
			for (Library library : config.libraries)
			{
				library.loadLibrary();
			}
			
			// Inits primitive data types
			Package.init();
			Type.init();
			
			Util.logProfile(now, libs, "Loaded " + libs + (libs == 1 ? " Library " : " Libraries ") + "(%.1f ms, %.1f ms/L, %.2f L/s)");
		}
		
		now = System.nanoTime();
		logger.info("Compiling '" + sourceDir + "' to '" + outputDir + "'");
		if (debug)
		{
			logger.info("Applying " + states + (states == 1 ? " State: " : " States: ") + DyvilCompiler.states);
		}
		
		// Scan for Packages and Compilation Units
		for (String s : sourceDir.list())
		{
			findUnits(new CodeFile(sourceDir, s), new File(outputDir, s), Package.rootPackage);
		}
		
		int fileCount = files.size();
		int unitCount = units.size();
		int packages = root.subPackages.size();
		logger.info("Compiling " + packages + (packages == 1 ? " Package, " : " Packages, ") + fileCount + (fileCount == 1 ? " File (" : " Files (")
				+ unitCount + (unitCount == 1 ? " Compilation Unit)" : " Compilation Units)"));
		logger.info("");
		
		// Apply states
		if (debug)
		{
			for (CompilerPhase state : DyvilCompiler.states)
			{
				DyvilCompiler.logger.info("Applying State " + state.getName());
				long now1 = System.nanoTime();
				state.apply(units);
				Util.logProfile(now1, units.size(), "Finished State " + state.getName() + " (%.1f ms, %.1f ms/CU, %.2f CU/s)");
			}
		}
		else
		{
			for (CompilerPhase state : DyvilCompiler.states)
			{
				state.apply(units);
			}
		}
		
		logger.info("");
		Util.logProfile(now, unitCount, "Compilation finished (%.1f ms, %.1f ms/CU, %.2f CU/s)");
	}
	
	public static void initLogger()
	{
		try
		{
			logger.setUseParentHandlers(false);
			logger.setLevel(Level.ALL);
			
			Formatter formatter = new Formatter()
			{
				@Override
				public String format(LogRecord record)
				{
					String message = record.getMessage();
					if (message == null || message.isEmpty())
					{
						return "\n";
					}
					
					Throwable thrown = record.getThrown();
					StringBuilder builder = new StringBuilder();
					
					if (DyvilCompiler.debug)
					{
						builder.append('[').append(format.format(new Date(record.getMillis()))).append("] [");
						builder.append(record.getLevel()).append("]: ");
					}
					builder.append(message).append('\n');
					
					if (thrown != null)
					{
						thrown.printStackTrace(new AppendableOutputStream(builder));
					}
					return builder.toString();
				}
			};
			StreamHandler ch = new StreamHandler(System.out, formatter);
			ch.setLevel(Level.ALL);
			logger.addHandler(ch);
			
			if (logFile)
			{
				String path = new File("dyvilc.log").getAbsolutePath();
				FileHandler fh = new FileHandler(path, true);
				fh.setLevel(Level.ALL);
				fh.setFormatter(formatter);
				logger.addHandler(fh);
			}
		}
		catch (Exception ex)
		{
		}
	}
	
	public static void addStates(String s)
	{
		switch (s)
		{
		case "compile":
			states.add(CompilerPhase.TOKENIZE);
			states.add(CompilerPhase.PARSE);
			states.add(CompilerPhase.RESOLVE_TYPES);
			states.add(CompilerPhase.RESOLVE);
			states.add(CompilerPhase.CHECK);
			states.add(CompilerPhase.COMPILE);
			return;
			// case "obfuscate":
			// case "doc":
			// case "decompile":
		case "optimize":
			states.add(CompilerPhase.FOLD_CONSTANTS);
			constantFolding = 1;
			return;
		case "jar":
			states.add(CompilerPhase.JAR);
			return;
		case "format":
			states.add(CompilerPhase.TOKENIZE);
			states.add(CompilerPhase.PARSE);
			states.add(CompilerPhase.FORMAT);
			return;
		case "print":
			states.add(CompilerPhase.PRINT);
			return;
		case "test":
			states.add(CompilerPhase.TEST);
			return;
		case "--debug":
			states.add(CompilerPhase.PRINT);
			states.add(CompilerPhase.TEST);
			debug = true;
			return;
		case "--pstack":
			parseStack = true;
			return;
		case "--nolog":
			logFile = false;
			return;
		}
		
		if (s.startsWith("-o"))
		{
			try
			{
				states.add(CompilerPhase.FOLD_CONSTANTS);
				constantFolding = Integer.parseInt(s.substring(2));
				return;
			}
			catch (Exception ex)
			{
			}
		}
		
		System.out.println("Invalid Argument '" + s + "'. Ignoring.");
	}
	
	public static void findUnits(File source, File output, Package pack)
	{
		if (source.isDirectory())
		{
			String name = source.getName();
			for (String s : source.list())
			{
				findUnits(new CodeFile(source, s), new File(output, s), pack.createSubPackage(name));
			}
			return;
		}
		
		String fileName = source.getPath();
		if (!config.compileFile(fileName))
		{
			return;
		}
		
		if (fileName.endsWith("Thumbs.db") || fileName.endsWith(".DS_Store"))
		{
			return;
		}
		else if (fileName.endsWith(".dwt"))
		{
			DWTFile dwt = new DWTFile(pack, (CodeFile) source, output);
			output = dwt.outputFile;
			units.add(dwt);
		}
		else if (fileName.endsWith(".dyvil"))
		{
			DyvilFile unit = new DyvilFile(pack, (CodeFile) source, output);
			output = unit.outputFile;
			pack.addCompilationUnit(unit);
			units.add(unit);
		}
		files.add(output);
	}
}
