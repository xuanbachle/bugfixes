package mainscala

import localizations.PrecomputedInfor
import org.apache.log4j.Logger
import parsers.javaparser.JavaParser
import repair.par.PAREvolve
import repair.representation.GenProgIndividual
import repair.terminations.SolutionProbability
import util.FileFolderUtils

/**
 * Created by larcuser on 12/11/15.
 */
object MainPAR {
  private def getElapsedTime(start: Long): Int = {
    return (System.currentTimeMillis - start).toInt / 1000
  }

  private val logger = Logger.getLogger(MainPAR.this.getClass)

  def main(args: Array[String]) {
    val argsl = args.toList
    RepairOptions.testFilterStrat="all"
    val ops=ArgParser.parseArgs(argsl, ArgParser.pf)

    println("debug=" + RepairOptions.debug)
    println("showme=" + RepairOptions.showme)
    println("filename=" + RepairOptions.filename)
    println("remainingopts=" + ops)
    //RepairOptions.bugName ="m34"
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
      val parEvolution=new PAREvolve(prec)
      val targetScore = RepairOptions.chosenTestInvokerInstance().getTargetScore()
      val termination = SolutionProbability.getInstance(targetScore,true,RepairOptions.maxSolution, 3600000L, startTime)// limit to 10 solutions
      logger.debug("Target Score: "+targetScore)
      //Testing--
      //JavaGlobalTypeInfor.buildLocalClassTypeInfor(new LineIden("com.google.javascript.jscomp.CodeConsumer", 241, 1.0))
      //val x=JavaGlobalTypeInfor.getTypeInforForClasses()
      //---------
      val result: GenProgIndividual = parEvolution.evolveGenProg(termination)
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
