package repair.handletests.junithandler.compiler

import java.util

import _root_.util.FileFolderUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.SuffixFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import org.eclipse.jface.text.Document
import org.eclipse.jface.text.IDocument
import java.io.File
import java.util.Collection
import java.util.HashMap
import java.util.LinkedList

import repair.representation.GenProgIndividual

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 8/3/15.
 */
object TestCompiler {
  /**
   * Generates a list of java source files given a directory, or returns the
   * file specified in an array.
   * @param sourcePaths The path to the file/directory.
   * @return An array of paths to Java source files.
   * @throws Exception
   */
  @throws(classOf[Exception])
  private def getSourceFiles(sourcePaths: Array[String], ext: String = ".java"): Array[String] = {
    val sourceFiles: Collection[File] = new LinkedList[File]
    var sourceFilesArray: Array[String] = null
    for (sourcePath <- sourcePaths) {
      val sourceFile: File = new File(sourcePath)
      if (sourceFile.isDirectory) {
        sourceFiles.addAll(FileUtils.listFiles(sourceFile, new SuffixFileFilter(ext), TrueFileFilter.INSTANCE))
      }
      else {
        sourceFiles.add(sourceFile)
      }
      sourceFilesArray = new Array[String](sourceFiles.size)
      var i: Int = 0
      import scala.collection.JavaConversions._
      for (file <- sourceFiles) {
        sourceFilesArray(i) = file.getCanonicalPath
        i += 1
      }
    }
    return sourceFilesArray
  }

  /**
   * Converts a serialized array in the format "{string1,string2,...,stringN}"
   * to a String array.
   * @param packed The seralized array.
   * @return The String[] array
   */
  @throws(classOf[Exception])
  private def unpackArray(packed: String): Array[String] = {
    var unpacked: Array[String] = null
    if ((packed.substring(0, 1) == "{") && (packed.substring(packed.length - 1, packed.length) == "}")) {
      val packedtemp = packed.substring(1, packed.length - 1)
      unpacked = packedtemp.split(",")
    }
    else throw new Exception("Array not enclosed in parenthesis '{ }', cannot unpack.")
    return unpacked
  }

  /**
   * Builds a HashMap with Java file paths as keys and Java file text contents as values.
   * @param sourceFilesArray
   * @return A HashMap containing the text of the source Java files.
   */
  @throws(classOf[Exception])
  private def buildSourceDocumentMap(sourceFilesArray: Array[String], sourcePath: String): HashMap[String, String] = {
    val map: HashMap[String, String] = new HashMap[String, String]
    for (sourceFile <- sourceFilesArray) {
      val backingFile: File = new File(sourceFile)
      val encoded: Array[Byte] = Utilities.readFromFile(backingFile)
      val contents: String = new String(encoded)
      val temp=FileFolderUtils.relativePath(sourcePath, sourceFile).split("\\.")(0)
      map.put(temp, contents)
    }
    return map
  }

  def test1() ={
    /*val classDirectory: String = "/home/dxble/MyWorkSpace/arithmeticTestMutation/bach"//"/home/dxble/MyWorkSpace/arithmeticTestMutation/target/classes"
val classpaths: Array[String] = Array[String]("/home/dxble/MyWorkSpace/arithmeticTestMutation/lib/junit-4.4.jar","/home/dxble/MyWorkSpace/arithmeticTestMutation/lib/hamcrest-core-1.3.jar")
val sourcepaths: Array[String] = Array[String]("/home/dxble/MyWorkSpace/arithmeticTestMutation/src/main")
val copyIncludes: Array[String] = Array[String]{""}
val copyExcludes: Array[String] = Array[String]{""}
val sourceFilesArray: Array[String] = getSourceFiles(sourcepaths)
val sourceFileContents: HashMap[String, DocumentASTRewrite] = buildSourceDocumentMap(sourceFilesArray)
val repair.handletests.junithandler.compiler: JavaJDKCompiler = new JavaJDKCompiler(classDirectory, classpaths, sourceFileContents, sourcepaths, copyIncludes, copyExcludes)
*/
    val classDirectory: String = "/home/xuanbach32bit/workspace/v6/pre-fix/target/classes" //"/home/dxble/MyWorkSpace/arithmeticTestMutation/target/classes"
    val classpaths: Array[String] = Array[String]("/home/xuanbach32bit/Downloads/hamcrest-core-1.3.jar",
        "/home/xuanbach32bit/Downloads/junit-4.12.jar",classDirectory)
    val sourcepaths: Array[String] = Array[String]("/home/xuanbach32bit/workspace/v6/pre-fix/src/main/java")
    val copyIncludes: Array[String] = Array[String]{""}
    val copyExcludes: Array[String] = Array[String]{""}
    val sourceFilesArray: Array[String] = getSourceFiles(sourcepaths)
    val sourceFileContents: HashMap[String, String] = buildSourceDocumentMap(sourceFilesArray, sourcepaths(0))
    val toCompile: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()
    val ent = sourceFileContents.entrySet().iterator().next()
    toCompile.put(ent.getKey,ent.getValue)
    //val repair.handletests.junithandler.compiler: JavaJDKCompiler = new JavaJDKCompiler(classDirectory, classpaths, toCompile, sourcepaths, copyIncludes, copyExcludes)
    //println(repair.handletests.junithandler.compiler.compile())
    //repair.handletests.junithandler.compiler.storeCompiled("./xum")
    //println(repair.handletests.junithandler.compiler.compile())
  }

  def test2() ={
    val classDirectory: String = "/home/xuanbach32bit/workspace/historicalFix/tempOutput/s1" //"/home/dxble/MyWorkSpace/arithmeticTestMutation/target/classes"
    //val classpaths: Array[String] = Array[String]("/home/xuanbach32bit/Downloads/hamcrest-core-1.3.jar",
    //    "/home/xuanbach32bit/Downloads/junit-4.12.jar",classDirectory)
    val sourcepaths = new java.util.ArrayList[String]()
    sourcepaths.add("/home/xuanbach32bit/workspace/historicalFix/tempOutput/s1")
    val copyIncludes: Array[String] = Array[String]()
    val copyExcludes: Array[String] = Array[String]()
    val sourceFilesArray: Array[String] = getSourceFiles(sourcepaths.toArray(new Array(sourcepaths.size())))
    val sourceFileContents: HashMap[String, String] = buildSourceDocumentMap(sourceFilesArray, sourcepaths.get(0))
    val toCompile: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()
    val ent = sourceFileContents.entrySet().iterator().next()
    toCompile.put(ent.getKey,ent.getValue)
    //val toCompile: java.util.HashMap[String, String] = new java.util.HashMap[String, String]()

    val onVariant = new GenProgIndividual(null, false)
    onVariant.setHasValidGenome(true)
    onVariant.setInd_id(1)

    val compiler: JavaJDKCompiler = new JavaJDKCompiler(classDirectory,onVariant.getClassPathsString(), sourceFileContents, copyIncludes, copyExcludes)
    println(compiler.compile())
  }
  @throws(classOf[Exception])
  def main(args: Array[String]) {
    test2()
  }
}