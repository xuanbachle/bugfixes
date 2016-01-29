package repair.geneticprogramming

import java.util.Random
import localizations.PrecomputedInfor
import localizations.fixlocalization.methodsimilarity.FindFixStrategies
import FindFixStrategies.FixStrat.FixStrat
import localizations.PrecomputedInfor
import org.uncommons.watchmaker.framework.EvolutionaryOperator
import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory
import repair.histdriven.HistMutation
import repair.par.PARMutation
import repair.representation.{GenProgIndividual, MutGene}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by xuanbach32bit on 4/22/15.
 */
class GeneticProgrammingFactory(prec: PrecomputedInfor, strat: FixStrat, mut: EvolutionaryOperator[GenProgIndividual]) extends AbstractCandidateFactory[GenProgIndividual] {

/**
 * Generates a random string of a pre-configured length.  Each character
 * is randomly selected from the pre-configured alphabet.  The same
 * character may appear multiple times and some characters may not appear
 * at all.
 * @param rng A source of randomness used to select characters to make up
 *            the string.
 * @return A randomly generated string.
 */
  def generateRandomCandidate (rng: Random): GenProgIndividual = {
    var candidate: GenProgIndividual = new GenProgIndividual(new ArrayBuffer[MutGene]())
    /*if(mut.isInstanceOf[HistMutation])
        candidate=mut.asInstanceOf[HistMutation].mutateIndividual(candidate,rng,true)
    else if(mut.isInstanceOf[PARMutation])
      candidate= mut.asInstanceOf[PARMutation].mutateIndividual(candidate, rng,true)
    else
        throw new RuntimeException("No appropriate Mutation given!")
    */
    candidate=mut.asInstanceOf[HistMutation].mutateIndividual(candidate,rng,true)
    println("Created indv:"+candidate.toString)
    return candidate
  }
}
