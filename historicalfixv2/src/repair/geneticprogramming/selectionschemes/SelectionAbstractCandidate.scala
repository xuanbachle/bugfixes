package repair.geneticprogramming.selectionschemes

/**
 * Created by larcuser on 25/9/15.
 */
class SelectionAbstractCandidate[T] (candidate: T, fitness: Double) {
  def getFitness() = fitness
  def getCandidate() = candidate

  override def equals(another: Any): Boolean ={
    if(!another.isInstanceOf[SelectionAbstractCandidate[T]]){
      return false
    } else{
      val anotherAbsCandidate = another.asInstanceOf[SelectionAbstractCandidate[T]]
      return (anotherAbsCandidate.getCandidate().equals(candidate) || anotherAbsCandidate == candidate) && anotherAbsCandidate.getFitness() == fitness
    }
  }

  override def hashCode(): Int ={
    candidate.hashCode() + fitness.hashCode()
  }

  override def toString(): String={
    return fitness.toString
  }
}
