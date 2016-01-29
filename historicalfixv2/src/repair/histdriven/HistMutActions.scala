package repair.histdriven

import java.util.Random

import localizations.Identifier
import org.apache.log4j.Logger
import repair.mutationoperators._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by larcuser on 12/11/15.
 */
class HistMutActions extends AvailableMutActions{
  private val logger = Logger.getLogger(classOf[HistMutActions])
  def random[T](pick: Seq[T], rng: Random): T = return pick(rng.nextInt(pick.size))

  def chooseAppropriateExpMutAction(exp: Identifier[Any], parentExp: Identifier[Any], rng: Random): ExpressionMutActions = {
    val possibleExpMutActions = Array[ExpressionMutActions](new ExpInfixDeletion,new ExpClassCastDeletion, new ExpClassCastInsertion(rng), new ExpBooleanNegation,
      new ExpOperatorMut(rng), new ExpMethodCallInvokerReplacement(rng), new ExpMethodCallParameterReplacement(rng),
      new ExpMethodCallNameReplacement(rng), new ExpAdder(parentExp,rng), new ExpReplacer(parentExp, rng))

    if(exp.transformToJavaNode()) {
      val jvNode = exp.getJavaNode()
      val possibleActions = new ArrayBuffer[ExpressionMutActions]()
      for(action <- possibleExpMutActions){
        if(action.isAppropriateAction(exp)){
          possibleActions.append(action)
        }
      }
      logger.debug("Possible ExpMut Actions: "+possibleActions)
      return random(possibleActions, rng)
    }else{
      throw new RuntimeException("Transform to java node not successful: " + exp)
    }
  }
}
