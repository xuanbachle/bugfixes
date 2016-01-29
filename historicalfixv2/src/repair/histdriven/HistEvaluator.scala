package repair.histdriven

import mainscala.RepairOptions
import org.apache.log4j.Logger
import org.uncommons.watchmaker.framework.{FitnessEvaluator, TerminationCondition}
import repair.handletests._
import repair.handletests.junithandler.JUnitTestInvoker
import repair.representation.GenProgIndividual
import repair.terminations.SolutionProbability

import scala.collection.mutable.ArrayBuffer

/**
 * Created by xuanbach32bit on 4/22/15.
 */
class HistEvaluator(termination: TerminationCondition) extends FitnessEvaluator[GenProgIndividual] {

  /*
    Implement from FitnessEvaluator interface
   */
  def getFitness(candidate: GenProgIndividual, population: java.util.List[_ <: GenProgIndividual] ): Double = {
    if(candidate.getIsSolution())// so that we never choose a solution to validate again
      return 0.0

    if(termination.isInstanceOf[SolutionProbability]) {
      if(candidate.isSeedCandidate()|| candidate.getGenome.size == 0){
        candidate.updateGenerScore()
        return candidate.getGenerScore()
      }else {
        println("Evaluating indv: " + candidate.getInd_id())
        println("Indv: " + candidate.toString)
        val generScore = calculateFitnessBasedOnProbability(candidate)
        println("End Evaluation indv: ID=" + candidate.getInd_id() + " score=" + generScore)
        candidate.setGenerScore(generScore)
        generScore
      }
    } else{
      sys.error("Unknown Termination Condition When Getting Fitness!")
    }
  }

  private def calculateFitnessBasedOnTestCase (candidate: GenProgIndividual): Double = {
    val tests=RepairOptions.chosenTestInvokerInstance().chooseTestsToRun()
    val (testScore,_,_)=HistEvaluator.runVariantOnTests(candidate,tests,RepairOptions.chosenTestInvokerInstance())
    candidate.setTestScore(testScore)
    return testScore
  }

  private def calculateFitnessBasedOnProbability(candidate: GenProgIndividual): Double = {
    val tests=RepairOptions.chosenTestInvokerInstance().chooseTestsToRun()
    val (testScore, compilationError,_)=HistEvaluator.runVariantOnTests(candidate,tests, RepairOptions.chosenTestInvokerInstance())
    candidate.setTestScore(testScore)
    if(!compilationError) {
      if (termination.isInstanceOf[SolutionProbability]) {
        val temporaryTerm = termination.asInstanceOf[SolutionProbability]
        candidate.setIncentiveForPassedTest(testScore/temporaryTerm.getTargetTestScore)
        if (testScore == temporaryTerm.getTargetTestScore) {
          temporaryTerm.addSolution(candidate)
        }
      }
      candidate.updateGenerScore()
      return candidate.getGenerScore() //probability
    }else{
      candidate.setTestScore(0.0)
      return 0.0
    }
  }


  //private def writeVariant_Deprecated(candidate:GenProgIndividual): Double={
    //val gen=candidate.getGenome
    //candidate.getRewriters.assertEmptyModifiedRewriter()// modified rewriters need be empty before writing a variant
    //candidate.assertNotEmptyBackingRewriters()// backing rewriter should not be empty because we use it to write the variant to disk
    //candidate.writeIndividualToDisk() // apply the edits of the variant using modified rewriters, clear the modified rewriter after this.
    //return candidate.getProbability()
  //}

  /**
   * {@inheritDoc}
   */
  def isNatural: Boolean = {
    return true
  }
}

object HistEvaluator {
  private val logger = Logger.getLogger(HistEvaluator.getClass)

  def retestAllSolutionsFound(solutions: java.util.List[GenProgIndividual]) ={
    //RepairOptions.resetChosenTestFilterStrategy(new TestAll)
    val testInvoker = new JUnitTestInvoker(new TestAll)
    testInvoker.calculateTargetScore(RepairOptions.testClassAbsoluteDir(),".class")
    import scala.collection.JavaConversions._
    solutions.map(
      sol => {
        val tests=testInvoker.chooseTestsToRun()
        val (_,_,pass)=runVariantOnTests(sol,tests,testInvoker)
        if(!pass)
          logger.info("Solution " + sol.getInd_id()+":"+"failed!")
        else
          logger.info("Solution " + sol.getInd_id()+":"+"passed!")
        logger.info("===========================")
      }
    )
  }

  def runVariantOnTests(candidate:GenProgIndividual,tests:ArrayBuffer[TestCase[Any]], testInvoker: AbstractTestInvoker): (Double,Boolean, Boolean) ={
    // work flow if this method: write variant to disk, test variant, restore initial state
    val compilable=testInvoker.writeVariantToDisk(candidate) // after write, we test.
    if(!compilable)
      return (0.0, true,false)

    var compilationError = false
    var passAllGivenTests = true

    val testScore=tests.foldLeft(0.0)(
      (score,testParam) => {
        if(!compilationError) {
          val (res, compilationErr) = testInvoker.invokeTest(testParam, candidate) // after test, we need to restore
          logger.info("Test res: " + res + " Compilation error? " + compilationErr)
          if(res) {
            logger.info("Test passed: "+testParam)
          }else{
            logger.info("Test failed: "+testParam)
            passAllGivenTests = false
          }
          compilationError = compilationErr
          score + testInvoker.testScore(testParam, res)
        }else{
          score
        }
      }
    )
    if(passAllGivenTests)
      candidate.setIsSolution(true)

    testInvoker.restoreInitialState(candidate) // restore
    (testScore, compilationError, passAllGivenTests)
  }
}