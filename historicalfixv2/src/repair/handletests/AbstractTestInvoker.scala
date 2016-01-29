package repair.handletests

import java.io.File

import mainscala.RepairOptions
import myLib.Lib
import parsers.javaparser.ASTRewriteFactory
import repair.representation.GenProgIndividual
import util.FileFolderUtils

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 8/2/15.
 */
abstract class AbstractTestInvoker {
  def invokeTest(testName: TestCase[Any], onVariant: GenProgIndividual): (Boolean, Boolean) = invokeTest(testName)
  def invokeTest(testName: TestCase[Any]): (Boolean, Boolean)
  protected def getTestFilteringStrategy():  TestCaseFilter[Any]
  private val allTests= new ArrayBuffer[TestCase[Any]]()//new ArrayBuffer[TestCase[Any]]
  private val postiveTests= new ArrayBuffer[TestCase[Any]]()
  private val negativeTests = new ArrayBuffer[TestCase[Any]]()
  private var targetScore: Double = -1.0

  // by default, maven test invoker rewriter the original source file to write variant on disk
  // junit invoker does not rewrite the original source file, instead it generate class file for variant on disk
  def modifiedSourceFileOnDisk(): Boolean = true


  def writeVariantToDisk(candidate:GenProgIndividual): Boolean= {
    val gen = candidate.getGenome
    val transformSuccess=candidate.transferGeneToStaticWriterFactory()
    //Remember: graph probability (frequency) has already been computed during mutation operation
    //val probability = SolutionProbability.computeProbabilityForVariant();
    // compute probability using the diff between modified rewriters and original rewriters

    if(transformSuccess) {
      // apply the edits of the variant using modified rewriters, clear the modified rewriter after this
      val compilable = ASTRewriteFactory.applyAllEditsOnModifiedRewriters(candidate)
      return compilable
    }else{
      return false
    }
  }

  def restoreInitialState(candidate: GenProgIndividual) = {
    candidate.cleanVariantClassFolder()
    ASTRewriteFactory.writeBackOriginalStateOfModifiedFiles() // restore
  }

  def getPossitiveTests(): ArrayBuffer[TestCase[Any]] = postiveTests
  def getNegativeTests(): ArrayBuffer[TestCase[Any]] = negativeTests
  def getTargetScore(): Double = targetScore

  def testScore(test:TestCase[Any],res:Boolean): Double ={
    val score = test match {
      case PositiveTest(_,_) => if(res) return 1.0 else return 0.0
      case NegativeTest(_,_) => if(res) return 10.0 else return 0.0
    }
    return score
  }

  def chooseTestsToRun(): ArrayBuffer[TestCase[Any]] ={
    //assert(allTests.length>0)
    //allTests
    assert(this.postiveTests.length>0)
    getTestFilteringStrategy() match {
      case NegativeTestCaseOnly() => {
        return negativeTests
      }
      case TestAll() =>{
        return postiveTests ++: negativeTests
      }
    }
  }

  def findAllTestsInFolder(folder: String, extension: String = ".java"): ArrayBuffer[String] ={
    import scala.collection.JavaConversions._
    val allTestsName=Lib.search4FilesContainName(new File(folder), "Test")
    val testFullNames=allTestsName.foldLeft(new ArrayBuffer[String]()) {
      case (testFullName, test) => {
        if (!TestCase.isAbstractTestFile(test,extension) && TestCase.isTestFile(test, extension)) {
          //testName.append(test.getName.split("\\.")(0)) // strip the .java
          testFullName.append(test.getCanonicalPath)
        }
        testFullName
      }
    }
    //all.map(test => println(test.getCanonicalPath))
    //testFullNames.map(test => println(test))
    testFullNames
  }

  def positiveTestsName(folder: String, negTests: ArrayBuffer[NegativeTest], ext: String = ".java"): ArrayBuffer[PositiveTest]={
    val allTestFullNames = findAllTestsInFolder(folder, ext)
    allTestFullNames.foldLeft(new ArrayBuffer[PositiveTest]) { (res, testFullPath) => {
      val relativeTestPath = FileFolderUtils.relativePath(folder,testFullPath).split("\\.")(0) // strip the extension such as .java
      val testName = FileFolderUtils.path2Package(relativeTestPath)
      if (!negTests.exists{neg: NegativeTest => neg.sameTestName(testName)}) {
        val index=(res.size+1)
        res.append(new PositiveTest(testName,"p"+index))
      }
      res
    }
    }
  }

  def negativeTestName(): ArrayBuffer[NegativeTest] ={
    val failingTests=RepairOptions.failingTests.split(";")
    for(failTest <-failingTests)
      println("Failing Test: "+failTest)
    failingTests.foldLeft(new ArrayBuffer[NegativeTest]){
      (res, test) => {
        val index=(res.size+1)
        res.append(new NegativeTest(test,"n"+index));
        res
      }
    }
  }

  def calculateTargetScore(folder: String, testFileExt: String = ".java"): Double ={
    if(targetScore > -1.0)
      return targetScore

    val negTests=negativeTestName()
    val posTests = positiveTestsName(folder, negTests, testFileExt)
    val all=posTests ++: negTests
    println("Test size:"+ all.size)
    println("Negative size: "+negTests.size)
    this.postiveTests.appendAll(posTests)
    this.negativeTests.appendAll(negTests)
    targetScore=getTestFilteringStrategy().calculateTestScore(this.postiveTests, this.negativeTests, testScore)
    return targetScore
  }
}
