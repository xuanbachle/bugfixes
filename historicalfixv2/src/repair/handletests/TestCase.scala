package repair.handletests

import java.io.File

import mainscala.{RepairOptions}

/**
 * Created by xuanbach32bit on 4/25/15.
 */
abstract class TestCase[+T]{
  def getName():String
  def getId(): String

  protected def sameTestName(name: String): Boolean ={
    val currentNoNumberSign = getName().split("#")(0)
    val objNoNumberSign = name.split("#")(0)
    return currentNoNumberSign.equals(objNoNumberSign)
  }

  override def equals(obj: Any): Boolean ={
    if(obj.isInstanceOf[TestCase[Any]]){
      val objTest = obj.asInstanceOf[TestCase[Any]]
      val currentNoNumberSign = getName().split("#")(0)
      val objNoNumberSign = objTest.getName().split("#")(0)
      if(currentNoNumberSign.equals(objNoNumberSign))
        return true
      else
        return false
    }else
      return false
  }
}
case class PositiveTest(name:String, id: String) extends TestCase[Any]{
  def getName():String=name
  def getId(): String=id
  override def sameTestName(name: String) = super.sameTestName(name)
}
case class NegativeTest(name:String, id: String) extends  TestCase[Any]{
  def getName():String=name
  def getId(): String=id
  override def sameTestName(name: String) = super.sameTestName(name)
}

object TestCase{
  def isNotTestFile(f: File, ext: String =".java"): Boolean ={
    return !f.getName.endsWith("TestPermutations"+ext) &&
      !f.getName.endsWith("Test"+ext) && !f.getName.endsWith("TestCase"+ext) &&
      !f.getName.endsWith("AbstractTest"+ext) && f.getName.endsWith(ext) && !f.getName.endsWith("~") &&
      !f.getName.endsWith("Tests"+ext) && !f.getName.startsWith("Test")
  }

  def isTestFile(f: File, ext: String =".java"): Boolean={
    return f.getName.endsWith("TestPermutations"+ext) ||
      f.getName.endsWith("Test"+ext) || f.getName.endsWith("TestCase"+ext) ||
      f.getName.endsWith("AbstractTest"+ext) || f.getName.endsWith("Tests"+ext) ||
      (f.getName.startsWith("Test") && f.getName.endsWith(ext))
  }

  def isAbstractTestFile(f: File, ext : String = ".java"): Boolean={
    return f.getName.contains("AbstractTest"+ext)
  }
}
/*object HandleTestCases {

  private val allTests= new ArrayBuffer[TestCase[Any]]()//new ArrayBuffer[TestCase[Any]]
  private val postiveTests= new ArrayBuffer[TestCase[Any]]()
  private val negativeTests = new ArrayBuffer[TestCase[Any]]()

  /*def calculateTargetScore():Double={
    assert(allTests.length==0)
    var i=1
    while(i<=RepairOptions.posTests){
      allTests.append(new PositiveTest("p"+i))
      i=i+1
    }
    i=1
    while(i <= RepairOptions.negTests){
      allTests.append(new NegativeTest("n"+i))
      i=i+1
    }
    assert(allTests.length==RepairOptions.posTests+RepairOptions.negTests)

    val target= allTests.foldLeft(0.0)((score,test)=> {
       val add = testScore(test,true)
        score + add
      })
    return target
  }*/

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
    //postiveTests ++: negativeTests
    negativeTests
  }

  def findAllTestsInFolder(folder: String): (ArrayBuffer[File], ArrayBuffer[String]) ={
    import scala.collection.JavaConversions._
    val allTestsName=Lib.search4FilesContainName(new File(folder), "Test.java")
    val (all, names)=allTestsName.foldLeft(new ArrayBuffer[File](), new ArrayBuffer[String]()) {
      case ((res,testName), test) => {
        if (!test.getName.contains("AbstractTest.java")) {
          res.append(test)
          testName.append(test.getName.split("\\.")(0)) // strip the .java
        }
        (res,testName)
      }
    }
    //all.map(test => println(test.getCanonicalPath))
    //names.map(test => println(test))
    (all,names)
  }

  def positiveTestsName(folder: String, negTests: ArrayBuffer[NegativeTest]): ArrayBuffer[PositiveTest]={
    val (_,allTestsName) = findAllTestsInFolder(folder)
    allTestsName.foldLeft(new ArrayBuffer[PositiveTest]) { (res, test) => {
        if (!negTests.exists(neg => neg.name==test)) {
          val index=(res.size+1)
          res.append(new PositiveTest(test,"p"+index))
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

  def calculateTargetScore(folder: String): Double ={
    val negTests=negativeTestName()
    val posTests = positiveTestsName(folder, negTests)
    val all=posTests ++: negTests
    println("Test size:"+ all.size +" "+posTests)
    println("Negative size: "+negTests.size)
    this.postiveTests.appendAll(posTests)
    this.negativeTests.appendAll(negTests)
    all.foldLeft(0.0)((score,test)=> {
      val add = testScore(test,true)
      score + add
    })
  }

  def main(args: Array[String]): Unit = {
    //testRunShell()
    //runScript("/home/xuanbach32bit/workspace/watchmaker_gp/src/test/scriptTest.sh",ArrayBuffer[String]("n1","a2"))
    /*println("target Score: "+calculateTargetScore())
    val tests=HandleTestCases.chooseTestsToRun()
    val s=tests.foldLeft(0.0)(
      (score,testParam) => {
        val (wtest, res) = HandleTestCases.runScript(GenOptions.testScript, testParam)
        score+HandleTestCases.testScore(wtest,res)
      }
    )
    println("score evaluate tests: "+s)
    */
    //findAllTestsInFolder("/home/dxble/MyWorkSpace/astor/examples/Math-issue-280")
    println(calculateTargetScore("/home/dxble/MyWorkSpace/astor/examples/Math-issue-280"))
  }
}
*/