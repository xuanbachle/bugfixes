package repair.mutationoperators

import repair.representation.GenProgIndividual

import scala.collection.mutable

/**
 * Created by dxble on 8/19/15.
 */
object ChosenIndividualsCache {
  private val chosenIndividuals = new mutable.HashSet[GenProgIndividual]()

  def alreadyChosenBefore(currentInidv: GenProgIndividual): Boolean ={
    val existed= chosenIndividuals.contains(currentInidv)
    //if(existed){
      //println(currentInidv)
    //}
    return existed
  }

  def addToChosenIndividual(currentInidv: GenProgIndividual): Boolean ={
    //if(!alreadyGenratedBefore(currentInidv)){
    val success = chosenIndividuals.add(currentInidv)
    return success
    //}
  }

  def getChosenIndividuals() = chosenIndividuals
}
