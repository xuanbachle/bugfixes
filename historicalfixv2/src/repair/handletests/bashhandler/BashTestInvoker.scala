package repair.handletests.bashhandler

import repair.handletests.{AbstractTestInvoker, TestCase, TestCaseFilter}

import scala.sys.process._
/**
 * Created by dxble on 8/2/15.
 */
class BashTestInvoker(pathToScript: String, var testFilteringStrategy: TestCaseFilter[Any]) extends AbstractTestInvoker{

  def getTestFilteringStrategy() : TestCaseFilter[Any] = testFilteringStrategy
  def invokeTest(test: TestCase[Any]) : (Boolean,Boolean) ={
    (runScript(pathToScript, test.getId()),false) // TODO: need to handle compilation error later
  }

  def runScript(scriptPath:String,testID: String) ={
    try {
      print("Running test: "+testID)
      val cmd = Seq("bash", scriptPath, testID)
      for(line <- cmd.lineStream)
        println(line)
      val output = cmd.!!
      processOutput(output)
    }catch {
      case _:Throwable =>{
        println(" Test Fail")
        false
      }
    }
  }

  def processOutput(out:String): Boolean ={
    if(out.contains("FAILURES")) {
      println(" Test Fail")
      return false
    }
    else if(out.contains("OK")) {
      println(" Test OK")
      return true
    }
    else {
      println(" Test Fail")
      false
    }
  }

  def testRunShell() = "ls -al" !


}
