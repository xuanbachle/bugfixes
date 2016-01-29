package repair.handletests.junithandler

import java.io.File
import java.net.{MalformedURLException, URL}
import mainscala.RepairOptions
import parsers.javaparser.ASTRewriteFactory
import repair.handletests._
import repair.representation.GenProgIndividual
import util.FileFolderUtils


/**
 * Created by xuanbach32bit on 8/27/15.
 */
object JUnitTestInvoker{
  def main(args: Array[String]) {
    val ink = new JUnitTestInvoker(new NegativeTestCaseOnly)
    val onVariant = new GenProgIndividual(null, false)
    onVariant.setHasValidGenome(true)
    onVariant.setInd_id(1)
    val negTest = new NegativeTest("org.apache.commons.lang3.AnnotationUtilsTest","n1")
    //val posTest = new PositiveTest("","p1")
    println(ink.writeVariantToDisk(onVariant))
    println(ink.invokeTest(negTest, onVariant))
    //println(invokeTest(posTest, onVariant))
  }
}
class JUnitTestInvoker(testFilterStrat: TestCaseFilter[Any]) extends AbstractTestInvoker{


  def invokeTest(test: TestCase[Any]): (Boolean, Boolean) ={
    //return (true, true)
    throw new RuntimeException("Do not call this inside JUnitTestInvoker!")
  }

  override def modifiedSourceFileOnDisk(): Boolean = return false

  override def restoreInitialState(candidate: GenProgIndividual) = {
    candidate.cleanVariantClassFolder()
  }

  override def writeVariantToDisk(candidate: GenProgIndividual): Boolean ={
    val variantByteCodeFolder = FileFolderUtils.getOutDirWithPrefix(candidate.getInd_id().toString)
    println("VAR FOLDER: "+variantByteCodeFolder)
    if(new File(variantByteCodeFolder).exists())
      return true
    else
      return false
  }

  override def invokeTest(test: TestCase[Any], onVariant: GenProgIndividual): (Boolean, Boolean) ={
    val testRes = validate(test, onVariant)
    if(testRes != null)
    {
      //println(testRes)
      (testRes.wasSuccessful(), false) // compilation error false because a variant gone through validation step is a compilable one already
    }else
      (false,false)
  }

  private def validate (test: TestCase[Any],onVariant: GenProgIndividual): TestResult = {
    try {
      val p: JUnitExecutorProcess = new JUnitExecutorProcess
      //log.debug("-Running first validation")
      val t1: Long = System.currentTimeMillis
      val trfailing: TestResult = p.execute(onVariant.getClassPaths(),test.getName(), "repair.handletests.junithandler.SingleJUnitTestRunner"/*classOf[SingleJUnitTestRunner].getCanonicalName*/, RepairOptions.timeout)
      return trfailing
    }
    catch {
      case e: MalformedURLException => {
        e.printStackTrace
        throw e
      }
    }
  }
  
  def getTestFilteringStrategy() : TestCaseFilter[Any] = testFilterStrat

}
