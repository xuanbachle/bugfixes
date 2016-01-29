package mainscala

import java.io.File
import java.util
import localizations.fixlocalization.methodsimilarity.FindFixStrategies
import FindFixStrategies.FixStrat
import FindFixStrategies.FixStrat.FixStrat
import FindFixStrategies.FixStrat.FixStrat
import localizations.fixlocalization.methodsimilarity.FindFixStrategies
import myLib.MyjavaLib
import repair.handletests._
import repair.handletests.bashhandler.BashTestInvoker
import repair.handletests.junithandler.JUnitTestInvoker
import repair.handletests.mavenhandler.MavenTestInvoker

import scala.collection.mutable.ArrayBuffer

object RepairOptions {

  var fixStrategy: FixStrat = FixStrat.BY_CURNODE

  var seed: Long = 200

  val overfitProbability = 0.8

  var faultFile: String = ""

  var fixFile: String = "" // "/home/dxble/MyWorkSpace/bugfixingtools/src/test/math-fix.txt" //

  var faultFilterFile: String =""
  var failingTests: String = ""//"/home/dxble/MyWorkSpace/bugfixingtools/src/test/failingtest.txt"

  var homeFolder: String = ""
  var pkgInstrument: String = ""
  var sourceFolder: String = ""
  var filterFaultScore: Double = 0.5
  var root: String =  "/home/xuanbach/" //"/Users/larcuser" //"/home/xledinh" //  //
  var dependencies: String =root+"/workspace/historicalfixv2/src;"+root+"/workspace/historicalfixv2/allLibs/junit-4.11.jar;./" //;allLibs/hamcrest-core-1.3.jar
  var localLibs: String = ""
  var testFolder: String = ""
  var maxSolution: Int = 10//5
  //var nameFaultFile: String = ""
  //var nameFaultMethod: String = ""
  //var lowerBound: Int = 0
  //var upperBound: Int = 0
  var timeout: Int = 25
  var bugName: String ="m22"
  var libs = Array("/usr/lib/jvm/java-8-oracle/jre/lib/rt.jar") // /usr/java/jdk1.8.0_51/jre/lib/rt.jar

  var minedPatternFile = root+"/workspace/historicalfixv2/allLibs/workingdata/test.lg"
  var totalGraphs = 1
  val usage = """
  Usage: parser [-v] [-f file] [-s sopt] ...
  Where: -v   Run verbosely
       -f F Set input file to F
       -s S Set Show option to S
  """
  val JAVA_LANG: String = "java"

  var testScript: String = ""
  var filename: String = ""
  var projectdir: String = ""
  var showme: String = ""
  var debug: Boolean = true
  val unknown = "(^-[^\\s])".r
  var language: String = JAVA_LANG

  var posTests: Int=4
  var negTests: Int=1

  var appMutProb: Double = 0.3
  var delMutProb: Double = 0.3
  var repMutProb: Double = 0.3
  var swapMutProb: Double = 0.0
  var globalWmut: Double = 0.3 // 0.03 previously

  val poolSize = 5 //10 // this is to multiply with the fault size. E.g., there are 5 buggy statements => size of pool = poolSize * 5 = 10 * 5 = 50
  val tournamentSize = 2 //3
  var variantOutputDir: String = root+"/workspace/historicalfixv2/tempOutput"
  var defaultVariantNumber = "default" //temp1
  var appClassDir = "target/classes" //(*math, closure*) "build/classes" //
  var testClassDir = "target/tests" //"target/test-classes" //"build/tests" //
  var thresholdFL = 0.005

  var testInvoker = "junit" //"maven" // bash, ant
  var testFilterStrat = "neg" // all
  private var testInvokerInstance: AbstractTestInvoker = null
  private var testFilterStratInstance: TestCaseFilter[Any] = null
  private var dependenciesList: java.util.ArrayList[String] = null

  def chosenTestInvokerInstance(): AbstractTestInvoker = {
    if(testInvokerInstance != null)
      return testInvokerInstance

    val testFilter = chosenTestFilterStrategy()
    if(testInvoker.toLowerCase.equals("maven")){
      testInvokerInstance = new MavenTestInvoker(testFilter)
    }else if(testInvoker.toLowerCase.equals("maven")) {
      testInvokerInstance = new BashTestInvoker(testScript, testFilter)
    }else if(testInvoker.toLowerCase.equals("junit")){
      testInvokerInstance = new JUnitTestInvoker(testFilter)
    }else
      throw new RuntimeException("!!! Unknown Test Invoker: "+ testInvoker)

    return testInvokerInstance
  }


  def chosenTestFilterStrategy(): TestCaseFilter[Any] = {
    if(testFilterStratInstance != null)
      return testFilterStratInstance

    if(testFilterStrat.toLowerCase.equals("neg")) {
      testFilterStratInstance = new NegativeTestCaseOnly
      return testFilterStratInstance
    }
    else if(testFilterStrat.toLowerCase.equals("all")) {
      testFilterStratInstance = new TestAll()
      return  testFilterStratInstance
    }
    else
      throw new RuntimeException("!!! Unknown test filter strategy: "+ testFilterStrat)
  }

  def testClassAbsoluteDir(): String = homeFolder+ File.separator + testClassDir
  def appClassAbsoluteDir(): String = homeFolder+ File.separator + appClassDir

  def getDependenciesList(): java.util.ArrayList[String] = {
    if(dependenciesList != null)
      return dependenciesList

    dependenciesList = dependencies.split(";").foldLeft(new java.util.ArrayList[String]()) {
      (res, dp) => {res.add(dp); res}
    }

    val depFromLocalLibs=localLibs.split(";").foldLeft(new java.util.ArrayList[String]()){
      (res, dp) =>{
        val foundJars = new util.ArrayList[File]()
        MyjavaLib.walk(dp,".jar",foundJars)
        import scala.collection.JavaConversions._
        for(aJar <- foundJars){
          res.add(aJar.getAbsolutePath)
        }
        res
      }
    }

    dependenciesList.addAll(depFromLocalLibs)
    dependenciesList.addAll(libs.foldLeft(new util.ArrayList[String]()){(res, path)=>{res.add(path);res}})
    println(dependenciesList)
    return dependenciesList
  }
}