package repair.mutationoperators

import java.util.Random

import localizations.Identifier

/**
 * Created by larcuser on 12/11/15.
 */
trait AvailableMutActions {
  def chooseAppropriateExpMutAction(exp: Identifier[Any], parentExp: Identifier[Any], rng: Random): ExpressionMutActions
}
