package repair.geneticprogramming.selectionschemes

import java.util.Random

import mainscala.RepairOptions

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import org.apache.log4j.Logger
/**
 * Created by larcuser on 25/9/15.
 */
object SelectionScheme {
  private val logger = Logger.getLogger(SelectionScheme.getClass)

  def randomWithCondition[T](pick: Seq[T], satisfyingCondition:(T, Random) => Boolean, rng: Random, trialLimit : Int = 50): (T, Boolean) = {
    var choosen = pick(rng.nextInt(pick.size))
    var limit = 0
    while(!satisfyingCondition(choosen, rng)){
      if(limit > trialLimit){// to avoid lopp forever with limit
        return (choosen, false)// false because the one we are forced to choose does not satisfy the condition
      }
      choosen = pick(rng.nextInt(pick.size))
      limit += 1
    }
    return (choosen, true)
  }

  //def tournamentSelection()
  def random[T](pick: Seq[T], rng: Random): T = return pick(rng.nextInt(pick.size))

  def randomUtilSizeX[T](pick: Seq[T], rng: Random, sizeX: Int, sameT:  (T,T) => Boolean): Seq[T] ={
    assert(pick.size > sizeX)
    val pool = new ArrayBuffer[T]()
    while(pool.size < sizeX){
      val picked = random(pick, rng)
      if(!pool.exists(p => sameT (picked,p))){
        pool.append(picked)
      }
    }
    assert(pool.size == sizeX)
    return pool
  }

  def tournamentSelection[T >: Null](population: mutable.Seq[T], selectionSize: Int, rng: Random, withoutReplacement: Boolean = true, compareFitness: (T,T) => Int)
  : T ={
    /*
      This assertion is to guarantee that selectionSize is not too large compared to population size.
     */
    assert(population.size >0)

    if(population.size == 1)
      return population(0)

    if(population.size >= 2)
      assert(population.size/selectionSize >= 2)

    val selection: ArrayBuffer[T] = new ArrayBuffer[T](selectionSize);
    val selectedIndices = new mutable.HashSet[Int]()

    def selectSmallerSet() ={
      val chosenIndex=rng.nextInt(population.size)
      //logger.debug("Index: "+chosenIndex)
      val chosenCandidate = population(chosenIndex);
      if(withoutReplacement) {
        if(!selectedIndices.contains(chosenIndex)) {
          //logger.debug("Adding candidate at index: "+chosenIndex)
          selection.append(chosenCandidate);
          selectedIndices.add(chosenIndex)
        }
      }else{
        selection.append(chosenCandidate);
      }
    }
    //val actualSelectionSize = if(population.size/selectionSize >= 2) selectionSize else population.size/3
    while(selectedIndices.size <= selectionSize){
      selectSmallerSet()
    }
    //println(selection)
    logger.debug(selectedIndices)
    logger.debug(selection)
    var selectedCandidate: T = null
    selection.map(candidate => {
      if (selectedCandidate == null)
        selectedCandidate = candidate
      else if (compareFitness(candidate, selectedCandidate) > 0) {
        selectedCandidate = candidate
      }
    }
    )
    logger.debug(selectedCandidate)
    return selectedCandidate;
  }

  def tournamentSelectionNTimes[T >: Null](nTimes: Int, population: mutable.Seq[T],
                                   selectionSize: Int, rng: Random, withoutReplacement: Boolean = true, compareFitness: (T,T) => Int): ArrayBuffer[T] ={
    val selectedCandidates = new ArrayBuffer[T](nTimes)
    while(selectedCandidates.size <= nTimes){
      selectedCandidates.append(tournamentSelection(population,selectionSize,rng,withoutReplacement,compareFitness))
    }
    return selectedCandidates
  }

  def stochasticUniversalSampling[T](population: mutable.Seq[T],
    selectionSize: Int,
    rng: Random, getFitness: T => Double, compilableIndividual: T => Boolean): ArrayBuffer[T]=
    {
      val selection = new ArrayBuffer[T](selectionSize);
      if (population.size == 1){
        selection.append(population(0))
        return selection
      }

      // Calculate the sum of all fitness values.
      var aggregateFitness: Double = 0.0;
      for (candidate <- population) {
        aggregateFitness += getFitness(candidate)
      }
      assert(aggregateFitness>0)
      // Pick a random offset between 0 and 1 as the starting point for selection.
      var pop = population
      while(selection.size < selectionSize) {
        val startOffset = rng.nextDouble()
        var cumulativeExpectation = 0.0
        var index = 0
        var toCont = true
        for (candidate <- pop) {
          // Calculate the number of times this candidate is expected to
          // be selected on average and add it to the cumulative total
          // of expected frequencies.
          toCont = if (getFitness(candidate)>= RepairOptions.overfitProbability) {rng.nextBoolean()} else true // to somehow avoid overfit candidate, which has very high fitness
          if(toCont) {
            cumulativeExpectation += getFitness(candidate) / aggregateFitness * selectionSize
            logger.debug(cumulativeExpectation + " " + startOffset + " " + index + " " + aggregateFitness)
            // If f is the expected frequency, the candidate will be selected at
            // least as often as floor(f) and at most as often as ceil(f). The
            // actual count depends on the random starting offset.
            while (cumulativeExpectation > startOffset + index && selection.size < selectionSize) {
              if (compilableIndividual(candidate)) {
                selection.append(candidate);
              }
              index += 1
            }
          }
        }
        //pop=scala.util.Random.shuffle(population)
      }
      return selection;
  }

  def main(args: Array[String]) {
    val arr = new ArrayBuffer[SelectionAbstractCandidate[Double]]()
    val x1=new SelectionAbstractCandidate[Double](0.2, 0.2)
    val x2=new SelectionAbstractCandidate[Double](0.3, 0.3)
    arr.append(x1)
    arr.append(x1)
    arr.append(x1)
    arr.append(x2)
    arr.append(x2)
    arr.append(x2)
    arr.append(new SelectionAbstractCandidate[Double](0.1, 0.1))
    arr.append(new SelectionAbstractCandidate[Double](0.7, 0.7))
    arr.append(new SelectionAbstractCandidate[Double](0.8, 0.8))
    arr.append(x2)
    arr.append(x2)
    arr.append(new SelectionAbstractCandidate[Double](0.8, 0.8))
    arr.append(new SelectionAbstractCandidate[Double](0.8, 0.8))
    arr.append(new SelectionAbstractCandidate[Double](0.8, 0.8))
    arr.append(new SelectionAbstractCandidate[Double](0.8, 0.8))
    arr.append(new SelectionAbstractCandidate[Double](0.6, 0.6))
    val rng = new Random(1002345)
    def compareDouble(w1: SelectionAbstractCandidate[Double], w2: SelectionAbstractCandidate[Double]) : Int ={
      if(w1.getFitness() > w2.getFitness())
        return 1
      else
        return -1;
    }
    def getFitness(w1: SelectionAbstractCandidate[Double]) = w1.getFitness()
    def compilabile(w: SelectionAbstractCandidate[Double]) = true
    println(tournamentSelection(arr, 3, rng, true, compareDouble))
    for(i <- 0 until 15)
      println(stochasticUniversalSampling(arr,2,rng,getFitness, compilabile))

    //println(x1.equals(x2))
  }
}
