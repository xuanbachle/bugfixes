package repair.histdriven

import java.util

import localizations.PrecomputedInfor
import mainscala.RepairOptions
import org.uncommons.maths.random.MersenneTwisterRNG
import org.uncommons.watchmaker.framework._
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline
import org.uncommons.watchmaker.framework.selection.RouletteWheelSelection
import org.uncommons.watchmaker.framework.termination.GenerationCount
import repair.geneticprogramming.GeneticProgrammingFactory
import repair.representation.{GenProgIndividual, MutGene}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
 * Created by xuanbach32bit on 4/22/15.
 */
class HistEvolve (prec:PrecomputedInfor){

  def evolveGenProg(term: TerminationCondition): GenProgIndividual = {
    import scala.collection.JavaConversions._

    val factory: GeneticProgrammingFactory = new GeneticProgrammingFactory(prec, RepairOptions.fixStrategy, new HistMutation(prec, RepairOptions.fixStrategy))
    val operators: ListBuffer[EvolutionaryOperator[GenProgIndividual]] = new ListBuffer[EvolutionaryOperator[GenProgIndividual]]

    operators.append(new HistMutation(prec,RepairOptions.fixStrategy))
    //val cross = new ListCrossover[GenProgIndividual]()
    //operators.append(cross.asInstanceOf[EvolutionaryOperator[GenProgIndividual]])

    val pipeline: EvolutionaryOperator[GenProgIndividual] = new EvolutionPipeline[GenProgIndividual](operators.toList)
    val eval=new HistEvaluator(term)
    val ran=new MersenneTwisterRNG()
    ran.setSeed(RepairOptions.seed)
    val engine: GenerationalEvolutionEngine[GenProgIndividual] = new GenerationalEvolutionEngine[GenProgIndividual](factory, pipeline,eval, new RouletteWheelSelection, ran)
    engine.addEvolutionObserver(new EvolutionLogger)
    engine.setSingleThreaded(true)
    val genLimit=new GenerationCount(10)
    var numberOfSeedCandidates = 0
    val seedCandidates = new util.ArrayList[GenProgIndividual]()// initialize seed candidate, for now we set to 3 seed candidates
    while(numberOfSeedCandidates < 3){
      val seedCand=new GenProgIndividual(new ArrayBuffer[MutGene](), true);
      seedCand.updateGenerScore()
      seedCandidates.add(seedCand)
      numberOfSeedCandidates += 1
    }

    return engine.evolve(40, 0, seedCandidates, term,genLimit)

  }

  /**
   * Trivial evolution observer for displaying information at the end
   * of each generation.
   */
  private class EvolutionLogger extends EvolutionObserver[GenProgIndividual] {
    def populationUpdate(data: PopulationData[_ <: GenProgIndividual]) {
      println("Generation "+ data.getGenerationNumber + " Best Candidate:" + data.getBestCandidate)
    }
  }

}

