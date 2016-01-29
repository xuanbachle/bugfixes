package parsers

import java.io.{FileReader, BufferedReader, File, IOException}
import java.util
import java.util.Hashtable

import _root_.util.FileFolderUtils
import com.sun.corba.se.spi.ior.IdentifiableFactory
import localizations.{LineIdenGZoltar, Identifier, PrecomputedInfor}
import mainscala.RepairOptions
import org.eclipse.jdt.core.dom.CompilationUnit
import parsers.javaparser.ASTRewriteUtils
import parsers.javaparser.JavaParser._
import localizations.Identifier
import repair.handletests.TestCase

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.sys.process.FileProcessLogger


/**
 * Created by xuanbach32bit on 5/17/15.
 */
abstract class AbstractParser[T] {
  //keep ASTs of all files being parsed
  val globalASTs: util.Hashtable[String, T] = new Hashtable[String, T]

  //keep original contents of files being edited for repair
  val modifyingFiles: util.Hashtable[String, String] = new Hashtable[String,String]

  //information about fix space and fault space
  var prec:PrecomputedInfor=null

  def parse[B >: T](fileContent:String, filePath: String): T
  def batchParse(filePath: Seq[String], faultFiles: util.HashMap[String, Boolean]): Unit

  def getPackageName[B >: T](unitContainingFile: T, path: String): String

  // this is to avoid keeping all files' contents
  protected def addModifyingFiles(fileName: String, content: String) = {
    assert(prec != null)
    println(fileName + " "+ prec.isFaultFile(fileName))
    println(prec.getFaultFiles())
    if(prec.isFaultFile(fileName))
      modifyingFiles.put(fileName, content)
  }

  protected def convertFilePath2FilePackageName(filePath: String): String ={
    /*val fileName=filePath.substring(RepairOptions.sourceFolder.length,filePath.length)
    val fileNameWithoutdotJava=fileName.split("\\.")(0)
    if(fileNameWithoutdotJava.startsWith("/"))
      fileNameWithoutdotJava.substring(1).replace(File.separator,".")
    else
      fileNameWithoutdotJava.replace(File.separator,".")*/
    val fileRelativePath=FileFolderUtils.relativePath(RepairOptions.sourceFolder,filePath)
    val fileName = FileFolderUtils.path2Package(fileRelativePath)
    return fileName
  }

  @throws(classOf[IOException])
  protected def readFileToString(filePath: String, faultFiles: util.HashMap[String,Boolean]): String = {
    val fileData: StringBuilder = new StringBuilder(1000)
    val reader: BufferedReader = new BufferedReader(new FileReader(filePath))
    var buf: Array[Char] = new Array[Char](10)
    var numRead: Int = 0
    while ((({
      numRead = reader.read(buf); numRead
    })) != -1) {
      //System.out.println(numRead)
      val readData: String = String.valueOf(buf, 0, numRead)
      fileData.append(readData)
      buf = new Array[Char](1024)
    }
    reader.close()
    val fileName=convertFilePath2FilePackageName(filePath)
    // if file being read is a fault file, keep its content
    // println("Processed FileName"+fileName)
    if(faultFiles.containsKey(fileName)) {
      addModifyingFiles(fileName, fileData.toString())
      faultFiles.remove(fileName)
    }

    return fileData.toString
  }

  @throws(classOf[IOException])
  def ParseFiles(files: util.List[File]) = {
    import scala.collection.JavaConversions._
    //println("Files tp parse:"+files)
    assert(this.prec!=null)
    var faultFiles=prec.getFaultFiles()
    var filePath: String = null
    for(f <- files){
      val filePath = f.getAbsolutePath
      if (f.isFile) {
        if(TestCase.isNotTestFile(f)) {
          //System.out.println("Parsing: " + filePath)
          val cu = parse(readFileToString(filePath, faultFiles), filePath)
          globalASTs.put(getPackageName(cu,filePath), cu)
        }
      }
    }
  }

  @throws(classOf[IOException])
  def batchParseFiles(files: util.List[File]) = {
    import scala.collection.JavaConversions._
    //println("Files tp parse:"+files)
    assert(this.prec!=null)
    var faultFiles=prec.getFaultFiles()
    val actualFiles=files.foldLeft(new ArrayBuffer[String]()){
      case (res,f) => if(f.isFile && TestCase.isNotTestFile(f)){res.append(f.getAbsolutePath)}; res
    }
    batchParse(actualFiles, faultFiles)
  }
}
