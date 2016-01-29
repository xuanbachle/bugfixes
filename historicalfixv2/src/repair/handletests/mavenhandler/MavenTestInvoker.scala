package repair.handletests.mavenhandler

import java.io.File
import java.util.Collections

import mainscala.RepairOptions
import org.apache.maven.shared.invoker._
import repair.handletests._
import repair.handletests.timeouthandler.MyCustomExecutionContext

import scala.concurrent._
import scala.concurrent.duration._

/**
 * Created by dxble on 7/16/15.
 */
object MavenTestInvoker{
  def main(args: Array[String]) {
    val invk = new MavenTestInvoker(new NegativeTestCaseOnly)
    //val request=invk.sanityCheck("/home/dxble/MyWorkSpace/astor/examples/Math-issue-280")
    //invk.invokeTest(new NegativeTest("NormalDistributionTest","n1"))
    invk.invokeTest(new NegativeTest("RandomDataTest","n1"))
    println("Not died")
  }
}

class MavenTestInvoker(testFilteringStrategy: TestCaseFilter[Any]) extends AbstractTestInvoker{
  val MAVEN_HOME = new File("/home/dxble/usr/local/apache-maven/apache-maven-3.2.1")
  val JAVA_HOME = new File("/usr/java/jdk1.7.0_71/")
  private var PROJECT_HOME: String = RepairOptions.homeFolder //"/home/dxble/MyWorkSpace/astor/examples/Math-issue-309" // // //"/home/dxble/MyWorkSpace/astor/examples/Math-issue-280" //  //"/home/dxble/MyWorkSpace/astor/examples/Math-issue-280"

  def getProjectHome: String = PROJECT_HOME

  def setProjectHome(value: String): Unit = {
      PROJECT_HOME = value
  }

  def getTestFilteringStrategy() : TestCaseFilter[Any] = testFilteringStrategy

  private def setCommonRequest(projectFolder: String) ={
    val request = new DefaultInvocationRequest()
    request.setPomFile(getPomFile(projectFolder))
    request.setGoals( Collections.singletonList( "test" ) )
    request.setJavaHome(JAVA_HOME)
    request.setFailureBehavior(InvocationRequest.REACTOR_FAIL_NEVER)
    request
  }

  private def invoke(request: DefaultInvocationRequest) ={
    val invoker: Invoker = new DefaultInvoker()
    invoker.setMavenHome(MAVEN_HOME)

    implicit val exec = new MyCustomExecutionContext()
    val f = Future[Int] {
      val result=invoker.execute(request); result.getExitCode
    }
    try {
      //println(i)
      Await.result(f, RepairOptions.timeout seconds) // 100% cpu here

    } catch {
      case e: TimeoutException =>
        println("Stopping Test...")
        exec.lastThread.getOrElse(throw new RuntimeException("Not started"))
          .stop()// 0% cpu here
    }

    /*val f = Future(blocking(invoker.execute(request).getExitCode)) // wrap in Future
    val res = try {
        Await.result(f, duration.Duration(RepairOptions.timeout, "sec"))
      } catch {
        case _: TimeoutException =>
          println("TIMEOUT!")

          //p.destroy()
          //p.exitValue()
      }*/
  }

  private def sanityCheck(projectFolder: String) ={
    val request = setCommonRequest(projectFolder)
    invoke(request)
  }

  private def mavenClean(projectFolder: String) ={
    val request = new DefaultInvocationRequest()
    request.setPomFile(getPomFile(projectFolder))
    request.setGoals( Collections.singletonList( "clean" ) )
    request.setJavaHome(JAVA_HOME)
    invoke(request)
  }

  def invokeTest(testName: TestCase[Any]): (Boolean, Boolean) ={
    //mavenClean(PROJECT_HOME)
    println("Running Test: "+testName + " in "+PROJECT_HOME)
    val request = setCommonRequest(PROJECT_HOME)
    request.setMavenOpts("-Dtest="+testName.getName())
    //request.setMavenOpts("-Dsurefire.printSummary=false")
    val testResultHandler = new HandleMavenTestOutput
    request.setOutputHandler(testResultHandler)
    invoke(request)
    println("Compilation error? "+testResultHandler.compilationError)
    println("Build success? "+testResultHandler.buildSuccess)
    return (testResultHandler.buildSuccess, testResultHandler.compilationError)
  }

  private def getPomFile(projectFolder: String) = {
    new File(projectFolder+ File.separator +"pom.xml" )
  }
}
