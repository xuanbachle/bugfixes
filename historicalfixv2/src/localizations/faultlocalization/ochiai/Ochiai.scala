package localizations.faultlocalization.ochiai

/**
 * Created by xuanbach32bit on 5/17/15.
 */
import java.io.{IOException, File}
import java.util
import _root_.util.FileFolderUtils
import com.gzoltar.core.components.Statement
import mainscala.RepairOptions
import myLib.{MyjavaLib, Lib}
import org.apache.log4j.Logger
import repair.handletests._
import repair.handletests.mavenhandler.MavenTestInvoker
import scala.collection.JavaConversions._
import com.gzoltar.core.instr.testing.TestResult

import scala.collection.mutable.ArrayBuffer
import localizations.gzoltarwrapper.GZoltar
object Ochiai extends App{
  /*def main(args: Array[String]) {
    val ochi = new Ochiai("")
    //ochi.faultLocalizationGZoltar()
  }*/
  override def main(args: Array[String]): Unit = {
    testV6()
    //testGCD2()
  }
  def testV6() ={
    //val ochi = new Ochiai("/home/xuanbach32bit/workspace/historicalFix/outputMutation/MainjGenProg-v6-lang-pre-fix/bin/default")
    val ochi = new Ochiai("/home/xuanbach32bit/workspace/historicalFix/tempOutput/temp1")
    val testFolder = "/home/xuanbach32bit/workspace/v6/pre-fix/target/test-classes" // "/home/xuanbach32bit/workspace/v6/pre-fix/src/test/java"
    val sourceFolder = "/home/xuanbach32bit/workspace/v6/pre-fix/target/classes" // "/home/xuanbach32bit/workspace/v6/pre-fix/src/main/java"
    val junit = "/home/xuanbach32bit/Desktop/Apps/idea-IC-141.1532.4/lib/junit-4.11.jar"
    val testInvk = new MavenTestInvoker(new NegativeTestCaseOnly)
    RepairOptions.failingTests="org.apache.commons.lang3.StringUtilsTest#testEscapeSurrogatePairs"
    val score=testInvk.calculateTargetScore(testFolder,".class")
    val pkg = Array[String]("org.apache.commons")
    val classPaths = new ArrayBuffer[String]()
    classPaths.append(sourceFolder)
    classPaths.append(testFolder)
    classPaths.append(junit)
    ochi.searchFaultyLocations(sourceFolder,pkg, classPaths, 0.005, testInvk, ".class")
    /*testInvk.getPossitiveTests().map(p => println(p.getName()))
    println("Neg: "+testInvk.getNegativeTests())
    println(score)
    println(testInvk.getPossitiveTests().exists(p => p.equals(testInvk.getNegativeTests()(0))))*/
    /*val neg = new PositiveTest("x1","1")
    val neg2 = new NegativeTest("x1#10","2")
    println(neg.equals(neg2))*/

  }
  def testGCD()={
    var cl="gcd.Gcd"
    var t1="gcd.Gcd1Test"
    var t2="gcd.Gcd2Test"
    var t3="gcd.Gcd3Test"
    var t4="gcd.Gcd4Test"
    var t5="gcd.Gcd5Test"

    var clarr= Array[String](cl)
    println("CL "+cl)
    var tarr= Array[String](t1,t2,t3,t4,t5)
    //var tarr= Array[File](t1)
    var pkg = Array[String]("gcd.gcdnested")
    val ochi = new Ochiai("/home/xuanbach32bit/workspace/historicalFix/src/gcd/")
    //ochi.faultLocalization(clarr,tarr, pkg, null, 0.0)
  }
  def testGCD2()={
    val ochi = new Ochiai("/home/xuanbach32bit/workspace/historicalFix/src/gcd")
    val testFolder = "/home/xuanbach32bit/workspace/historicalFix/src/gcd" // "/home/xuanbach32bit/workspace/v6/pre-fix/src/test/java"
    val sourceFolder = "/home/xuanbach32bit/workspace/historicalFix/src/gcd" // "/home/xuanbach32bit/workspace/v6/pre-fix/src/main/java"
    val junit = "/home/xuanbach32bit/Desktop/Apps/idea-IC-141.1532.4/lib/junit-4.11.jar"
    val testInvk = new MavenTestInvoker(new NegativeTestCaseOnly)
    RepairOptions.failingTests="gcd.Gcd5Test"
    val score=testInvk.calculateTargetScore(testFolder,".java")
    val pkg = Array[String]("org.apache.commons")
    val classPaths = new ArrayBuffer[String]()
    classPaths.append(sourceFolder)
    classPaths.append(junit)
    ochi.searchFaultyLocations(sourceFolder,pkg, classPaths, 0.0, testInvk, ".java")
  }
}

class Ochiai (projLoc: String){//
  val logger = Logger.getLogger(Ochiai.getClass);

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def faultLocalization(classNames: ArrayBuffer[String], testNames: ArrayBuffer[TestCase[Any]], pkgInstrument: Array[String],
                        classPaths: ArrayBuffer[String], threshold: Double) : ArrayBuffer[SuspiciousCode]= {
    val gz: GZoltar = new GZoltar(projLoc)
    println("Gzoltar Project Loc: "+ projLoc)
    assert(classNames != null && testNames != null)
    assert(!classNames.isEmpty && !testNames.isEmpty)
    classNames.map(file => {
       //println(file)
      if(isSource(file))
        gz.addClassToInstrument(file)
     }
    )
    testNames.map( test => {
      //println(test)
      val testClass = test.getName().split("#")(0)
      gz.addTestToExecute(testClass)
      gz.addClassNotToInstrument(testClass)
     }
    )
    //println(gz.getWorkingDirectory)
    pkgInstrument.map( pkg => gz.addPackageToInstrument(pkg))
    if(classPaths != null)
      classPaths.map(cp => gz.getClasspaths.add(cp))
    gz.run()
    import scala.collection.JavaConversions._
    val res: util.List[TestResult] = gz.getTestResults

    for ( r <- res) {
      println(r.getName + " : " + r.wasSuccessful)
    }
    import scala.collection.JavaConversions._
    val filteredSusLoc = gz.getSuspiciousStatements.foldLeft(new ArrayBuffer[SuspiciousCode]()){
      (res, s) =>{
        val compName = s.getMethod().getParent().getLabel();
        if(s.getSuspiciousness >= threshold && s.getSuspiciousness > 0.0 && isSource(compName)){
          val c = new SuspiciousCode(compName, s.getMethod().toString(), s.getLineNumber(), s.getSuspiciousness())
          res.append(c)
        }
        res
      }
    }
    val ret=sortSuspicousCodeByScore(filteredSusLoc)
    for (s <- ret) {
      println(s.toString)
    }
    return ret
  }

  private def isSource(compName: String): Boolean = {
    val clRoot = compName.split("\\$")(0)
    val segmentationName = clRoot.split("\\.")
    val simpleClassName = segmentationName(segmentationName.length - 1)
    return !compName.toLowerCase().endsWith("test") && !compName.toLowerCase().endsWith("tests") && !simpleClassName.toLowerCase().startsWith("test") && !simpleClassName.toLowerCase().startsWith("validate");
  }

  private def allClassNames(sourceFolder: String, ext: String = ".class") ={
    //println(sourceFolder)
    val files: java.util.List[File] = MyjavaLib.walk(sourceFolder,ext,new java.util.ArrayList[File])
    import scala.collection.JavaConversions._
    files.foldLeft(new ArrayBuffer[String]){
      (res, pathFile) =>{
        if(isSource(pathFile.getCanonicalPath)) {
          val relativeSrcFilePath = FileFolderUtils.relativePath(sourceFolder, pathFile.getCanonicalPath)
          val nameSrcFile = FileFolderUtils.path2Package(relativeSrcFilePath)
          res.append(nameSrcFile)
        }
        res
      }
    }
  }

  def searchFaultyLocations(sourceFolder: String, pkgInstrument: Array[String],
                            classPaths: ArrayBuffer[String], threshold: Double,
                            testInvk: AbstractTestInvoker, ext: String = ".class") : ArrayBuffer[SuspiciousCode]= {
    val allSourceFileNames = allClassNames(sourceFolder, ext)
    //println(allSourceFileNames)
    val allTests = new ArrayBuffer[TestCase[Any]]()
    allTests.appendAll(testInvk.getPossitiveTests())
    allTests.appendAll(testInvk.getNegativeTests())
    faultLocalization(allSourceFileNames,allTests,pkgInstrument, classPaths, threshold)
  }

  private def sortSuspicousCodeByScore(res: ArrayBuffer[SuspiciousCode]):ArrayBuffer[SuspiciousCode]={
    res.sortWith((s1, s2) => s1.getSuspiciousValue > s2.getSuspiciousValue)
  }

  private def sortByScore(res: util.List[Statement]):util.List[Statement]={
    res.sortWith((s1, s2) => s1.getSuspiciousness > s2.getSuspiciousness)
  }
}
