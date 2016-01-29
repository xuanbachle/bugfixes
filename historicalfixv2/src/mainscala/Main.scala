package mainscala

import java.io.File

import localizations.{LineIden, LineIdenGZoltar, JavaNodeIden, PrecomputedInfor}
import org.apache.log4j.Logger
import org.eclipse.jdt.core.dom.{CompilationUnit, ASTNode}
import parsers.javaparser.{ASTRewriteUtils, JavaParser}
import parsers.javatypeinfor.JavaGlobalTypeInfor
import repair.geneticprogramming._

import repair.handletests.NegativeTestCaseOnly
import repair.histdriven.HistEvolve
import repair.representation.GenProgIndividual
import repair.terminations.{SolutionProbability, TargetTestScoreWithProb}
import util.FileFolderUtils


/**
 * @author xuanbach32bit
 */
object Main {

  private def getElapsedTime(start: Long): Int = {
    return (System.currentTimeMillis - start).toInt / 1000
  }

  def math_280_settings() ={
    val dependenciespath: String = "/home/dxble/MyWorkSpace/astor/examples/Math-issue-280/lib/junit-4.4.jar"
    val folder: String = "Math-issue-280"
    val failing: String = "org.apache.commons.math.distribution.NormalDistributionTest"
    val location: String = "/home/dxble/MyWorkSpace/astor/examples/Math-issue-280/"
    RepairOptions.pkgInstrument = "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath="/home/dxble/MyWorkSpace/astor/examples/Math-issue-280"
    val sourceToRead="/src/java/org/"
    RepairOptions.homeFolder="/home/dxble/MyWorkSpace/astor/examples/Math-issue-280"
    RepairOptions.sourceFolder="/home/dxble/MyWorkSpace/astor/examples/Math-issue-280/src/java/"
    RepairOptions.testFolder="/home/dxble/MyWorkSpace/astor/examples/Math-issue-280/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.fixFile="/home/dxble/MyWorkSpace/astor/examples/fixfile_math280.txt"
    RepairOptions.faultFile="/home/dxble/MyWorkSpace/astor/examples/faultfile_math280.txt"
  }

  def math_309_settings() ={
    val dependenciespath: String = "/home/dxble/MyWorkSpace/astor/examples/Math-issue-280/lib/junit-4.4.jar"
    val folder: String = "Math-issue-309"
    val failing: String = "org.apache.commons.math.random.RandomDataTest"
    val location: String = "/home/dxble/MyWorkSpace/astor/examples/Math-issue-309"
    RepairOptions.pkgInstrument = "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath="/home/dxble/MyWorkSpace/astor/examples/Math-issue-309"
    val sourceToRead="/src/main/java/org/"
    RepairOptions.homeFolder="/home/dxble/MyWorkSpace/astor/examples/Math-issue-309"
    RepairOptions.sourceFolder="/home/dxble/MyWorkSpace/astor/examples/Math-issue-309/src/main/java/"
    RepairOptions.testFolder="/home/dxble/MyWorkSpace/astor/examples/Math-issue-309/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.faultFilterFile ="/home/dxble/MyWorkSpace/historicalFix/faultFilter/m390.txt"
    //RepairOptions.fixFile="/home/dxble/MyWorkSpace/astor/examples/fixfile_math280.txt"
    //RepairOptions.faultFile="/home/dxble/MyWorkSpace/astor/examples/faultfile_math280.txt"
  }

  def expMut_settings() ={
    val dependenciespath: String = "/home/dxble/MyWorkSpace/mygcd/lib/junit-4.4.jar"
    val folder: String = "arithmeticTestMutation"
    val failing: String = "E2Test"
    val location: String = "/home/dxble/MyWorkSpace/arithmeticTestMutation"
    RepairOptions.pkgInstrument = ""
    val thfl: Double = 0.5
    val dirPath="/home/dxble/MyWorkSpace/arithmeticTestMutation"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder="/home/dxble/MyWorkSpace/arithmeticTestMutation"
    RepairOptions.sourceFolder="/home/dxble/MyWorkSpace/arithmeticTestMutation/src/main/java/"
    RepairOptions.testFolder="/home/dxble/MyWorkSpace/arithmeticTestMutation/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.fixFile="/home/dxble/MyWorkSpace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile="/home/dxble/MyWorkSpace/historicalFix/faultFilter/expMut.txt"
  }

  private val logger = Logger.getLogger(Main.getClass)

  def main(args: Array[String]) {
    val argsl = args.toList
    val ops=ArgParser.parseArgs(argsl, ArgParser.pf)

    println("debug=" + RepairOptions.debug)
    println("showme=" + RepairOptions.showme)
    println("filename=" + RepairOptions.filename)
    println("remainingopts=" + ops)
    RepairOptions.bugName ="m22"
    ConfigFactory.process_bug(RepairOptions.bugName)

    if(RepairOptions.language == RepairOptions.JAVA_LANG){
      //println("Language is Java")
      val prec: PrecomputedInfor =
      try{
        val temp=PrecomputedInfor.computeFixFaultSpacesFromFiles()
        temp
        //throw new RuntimeException("XXXX")
      }catch{
        case e: Throwable => {
          e.printStackTrace()
          PrecomputedInfor.computeFixFaultSpacesByOchiai()
        }
      }
      JavaParser.setPrecomputedInfor(prec)
      logger.info("Parsing Phase...")
      //JavaParser.ParseFilesInDir(RepairOptions.sourceFolder)
      JavaParser.batchParsingInDir(RepairOptions.sourceFolder)
      logger.info("Ended Parsing Phase...")
      val startTime: Long = System.currentTimeMillis
      logger.debug("Fault space bef refine: "+prec.getFaultSpace())
      prec.refineFaultSpace(RepairOptions.faultFilterFile, true)
      logger.debug("Fault space: "+prec.getFaultSpace())
      prec.writeFault2File()
      val genprog=new HistEvolve(prec)
      val targetScore = RepairOptions.chosenTestInvokerInstance().getTargetScore()
      val termination = SolutionProbability.getInstance(targetScore,true,RepairOptions.maxSolution, 3600000L, startTime)// limit to 10 solutions
      logger.debug("Target Score: "+targetScore)
      //Testing--
      //JavaGlobalTypeInfor.buildLocalClassTypeInfor(new LineIden("com.google.javascript.jscomp.CodeConsumer", 241, 1.0))
      //val x=JavaGlobalTypeInfor.getTypeInforForClasses()
      //---------
      val result: GenProgIndividual = genprog.evolveGenProg(termination)
      //logger.info("Best Evolution result: " + result)
      logger.info("===============================Solutions Found: ")
      val found=termination.getSolutionsFound()
      import scala.collection.JavaConversions._
      found.map(solution => logger.info(solution))
      //logger.info("===============================Retesting all solutions found...")
      //GenProgEvaluator.retestAllSolutionsFound(found)
      val elapsed: Int = getElapsedTime(startTime)
      logger.info("Elapsed Time: "+elapsed+" seconds")
      FileFolderUtils.removeDir(RepairOptions.variantOutputDir)
    }
  }
}