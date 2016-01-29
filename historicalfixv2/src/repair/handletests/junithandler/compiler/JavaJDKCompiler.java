package repair.handletests.junithandler.compiler;

import java.io.File;
import java.io.FileFilter;
import java.io.StringWriter;
import java.util.*;

/**
 * From https://weblogs.java.net/blog/malenkov/archive/2008/12/how_to_compile.html
 */
public class JavaJDKCompiler {
	private MemoryClassLoader mcl;
	private Map<String, String> sourceFileContents;
	private String classDirectory;
	private ArrayList<String> classpath;
	private Queue<String> errors;
	private String[] copyIncludes;
	private String[] copyExcludes;
	
	public JavaJDKCompiler(String classDirectoryOfVariant, ArrayList<String> classpath,
						   Map<String, String> sourceFileContents,
						   String[] copyIncludes,
						   String[] copyExcludes
						){
		this.classDirectory = classDirectoryOfVariant;
		this.classpath = classpath;
		this.mcl = null;
		this.errors = new LinkedList<String>();
		this.sourceFileContents = sourceFileContents;
		this.copyIncludes = copyIncludes;
		this.copyExcludes = copyExcludes;
	}
	
	/**
	 * Compiles the Java source file and writes the resulting .class file to the build/classes
	 * directory. Returns the result of the compilation (COMPILED if ok or NOT_COMPILED if
	 * there was an error).
	 * @param packageName
	 * @param className
	 * @param document
	 * @return JRSRepair.TestStatus (NOT_COMPILED or COMPILED)
	 * @throws Exception
	 */
	public Status compile() throws Exception {
		StringWriter output = new StringWriter();
		
		/* Build the map of source files that the repair.handletests.junithandler.compiler will read from. The
		 * modified files will be returned by DocumentASTRewrite. */
		//Map<String, String> sourceMap = this.buildSourceMap();
		
		/* Compile the Java file. */
		//System.out.println(sourceMap.size());
	    this.mcl = new MemoryClassLoader(sourceFileContents, this.classpath, output);

	    /* Check the compilation went ok. */
		//System.out.println(output);
	    if(output.toString().matches("(?s).*\\d+ errors?.*")){
	    	this.errors.add(output.toString());
	    	return Status.NOT_COMPILED;
	    }
	    
	    /* Write the class files to disk. */
	    this.errors.add("Compiled");
	    this.storeCompiled(this.classDirectory);
	    
	    /* Copy data files to be packaged with class files. If copyIncludes is
	     * empty, don't copy anything. */
	    /*if(this.copyIncludes.length > 0){
            JavaSourceFilter javaSourceFilter = new JavaSourceFilter(this.copyIncludes, this.copyExcludes);
            
            for(String sourcePath : this.sourcePaths) {
                Utilities.copyDataFiles(new File(sourcePath), new File(this.classDirectory), javaSourceFilter);
            }
	    }*/
	    
        return Status.COMPILED;
	}
	
	/**
	 * Stores the class files in the given directory.
	 * @param directory Base directory for .class files.
	 */
	public void storeCompiled(String directory){
	    /* Write the class files to disk. */
	    List<Output> classFiles = mcl.getAllClasses();
	    
	    /* Write the class to disk. */
	    try{
            for(Output classFile : classFiles){
                File f = new File(directory, classFile.getName());
                f.getParentFile().mkdirs();
                Utilities.writeToFile(new File(directory, classFile.getName()), classFile.toByteArray());
            }
	    }catch (Exception e){
	    	System.out.println(e.getMessage());
	    }
	}
	
	/**
	 * Returns the error message at the head of the queue.
	 * 
	 * Recommended use is to dequeue after each compile, or
	 * wait until execution has finished and iterate through
	 * the queue.
	 * @return
	 */
	public String dequeueCompileError() {
		return this.errors.remove();
	}
	
	/**
	 * Build the map of source files that the repair.handletests.junithandler.compiler will read from.
	 * @throws Exception
	 */
	/*private Map<String, String> buildSourceMap() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		for(String sourcePath : this.sourceFileContents.keySet()){
			System.out.println("Source File: "+sourcePath);
			DocumentASTRewrite drwt = this.sourceFileContents.get(sourcePath);
			map.put(this.getRelativePath(sourcePath), drwt.modifiedDocument.get());
			drwt.untaintDocument();
		}
		return map;
	}*/

	
	  /**
	   * Checks, whether the child directory is a subdirectory of the base 
	   * directory.
	   * 
	   * http://www.java2s.com/Tutorial/Java/0180__File/Checkswhetherthechilddirectoryisasubdirectoryofthebasedirectory.htm
	   *
	   * @param base the base directory.
	   * @param child the suspected child directory.
	   * @return true, if the child is a subdirectory of the base directory.
	   * @throws IOException if an IOError occured during the test.
	   */

	/**
	 * The possible results from the compile method.
	 */
	public enum Status{
		NOT_COMPILED, COMPILED
	}

	private class JavaSourceFilter implements FileFilter {
		
		String[] includes;
		String[] excludes;
		
		public JavaSourceFilter(String[] includes, String[] excludes){
			this.includes = includes;
			this.excludes = excludes;
		}
		
		public boolean accept(File pathname){
			
			/* If includes is empty, nothing can match. */
			if(includes.length == 0) return false;

			/* Include condition. */
			for(String include : this.includes){
				if(!pathname.getName().matches(include)){
					return false;
				}
			}
			
			/* Exclude condition. */
			for(String exclude : this.excludes){
				if(pathname.getName().matches(exclude)){
					return false;
				}
			}

			return true;
		}
		
	}
	
}
