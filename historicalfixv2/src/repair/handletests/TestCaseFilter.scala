package repair.handletests

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 8/3/15.
 */
abstract class TestCaseFilter[+T] {
  def calculateTestScore(positiveTests: ArrayBuffer[TestCase[Any]], negativeTests: ArrayBuffer[TestCase[Any]], func: (TestCase[Any], Boolean) => Double): Double = {
    def helper(tests: ArrayBuffer[TestCase[Any]])= {
      tests.foldLeft(0.0) {
        (res, test) => {
          res + func(test, true)
        }
      }
    }
    return helper(positiveTests) + helper(negativeTests)
  }
}
case class NegativeTestCaseOnly() extends TestCaseFilter[Any]{
  override def calculateTestScore(positiveTests: ArrayBuffer[TestCase[Any]], negativeTests: ArrayBuffer[TestCase[Any]], func: (TestCase[Any], Boolean) => Double): Double = {
    negativeTests.foldLeft(0.0) {
      (res, test) => {
        res + func(test, true)
      }
    }
  }
}
case class TestAll() extends TestCaseFilter[Any]{

}
