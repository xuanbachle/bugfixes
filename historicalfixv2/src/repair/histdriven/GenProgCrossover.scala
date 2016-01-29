package repair.histdriven

import java.util.Random

import org.uncommons.maths.number.NumberGenerator
import org.uncommons.maths.random.Probability
import repair.representation.GenProgIndividual

import scala.collection.mutable.ListBuffer

//import org.uncommons.watchmaker.framework.operators.AbstractCrossover

/**
 * Created by xuanbach32bit on 4/22/15.
 */

class GenProgCrossover (crossoverPointsVariable: NumberGenerator[Integer], crossoverProbabilityVariable: NumberGenerator[Probability]) extends GenProgAbstractCrossover[GenProgIndividual] (crossoverPointsVariable: NumberGenerator[Integer], crossoverProbabilityVariable: NumberGenerator[Probability]) {


//  override def mate(t: String, t1: String, i: Int, random: Random): List[String] = {
//    import scala.collection.JavaConversions._
//    val res: ListBuffer[String] = new ListBuffer[String]
//    res.toList
//  }

  override def mate(t: GenProgIndividual, t1: GenProgIndividual, i: Int, random: Random): List[GenProgIndividual] = {
    //import scala.collection.JavaConversions._
    val res = new ListBuffer[GenProgIndividual]
    t.asInstanceOf[GenProgIndividual]
    return res.toList
  }

}

