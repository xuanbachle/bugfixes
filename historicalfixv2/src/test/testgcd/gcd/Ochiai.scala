package test.testgcd.gcd

/**
 * Created by xuanbach32bit on 5/17/15.
 */
import java.io.IOException
import java.util

import com.gzoltar.core.GZoltar
import com.gzoltar.core.components.Statement
import com.gzoltar.core.instr.testing.TestResult

import scala.collection.JavaConversions._

object Ochiai extends App{//projLoc: String

  override def main(args: Array[String]): Unit = {
    var cl="test.gcd.Gcd"
    var t1="test.gcd.Gcd1Test"
    var t2="test.gcd.Gcd2Test"
    var t3="test.gcd.Gcd3Test"
    var t4="test.gcd.Gcd4Test"
    var t5="test.gcd.Gcd5Test"

    var clarr= Array[String](cl)
    println("CL "+cl)
    var tarr= Array[String](t1,t2,t3,t4,t5)
    //var tarr= Array[File](t1)
    faultLocalization(clarr,tarr)
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  def faultLocalization(classFiles: Array[String], testFiles: Array[String]) = {
    var projLoc="/home/xuanbach32bit/workspace/watchmaker_gp/src/test/"
    val gz: GZoltar = new GZoltar(projLoc)
   /* for( file <- classFiles) {
      println(file)
      gz.addClassToInstrument(file)
    }
    for( test <- testFiles) {
      println(test)
      gz.addTestToExecute(test)
    }*/
    println(gz.getWorkingDirectory)
    gz.addPackageToInstrument("test.gcd")
    gz.addTestPackageToExecute("test.gcd")
    gz.run
    import scala.collection.JavaConversions._
    val res: util.List[TestResult] = gz.getTestResults

    for ( r <- res) {
      println(r.getName + " : " + r.wasSuccessful)
    }
    import scala.collection.JavaConversions._
    for (s <- sortByScore(gz.getSuspiciousStatements)) {
      println(s.toString)
    }
  }

  private def sortByScore(res: util.List[Statement]):util.List[Statement]={
    res.sortWith((s1, s2) => s1.getSuspiciousness > s2.getSuspiciousness)
  }
}
