package gumdiff.difftemplates

import scala.collection.mutable.{ArrayBuffer, HashMap}

/**
 * Created by dxble on 7/15/15.
 */
object Statistics {
  //var tempCount = new ArrayBuffer[Int]()

  def statisticMatchTemplates(matched: HashMap[String, Array[String]],noMatched: ArrayBuffer[String]) ={
    val totalTrials = matched.size + noMatched.size
    println("Total Trials of matching: "+ totalTrials)

    val counts = matched.foldLeft(new Array[Int](10)){
      (counts,kv) =>
        kv match {
          case (countFolder, matchedTemps) => {
            val matchedTempsNodup=matchedTemps.distinct
            if (matchedTempsNodup.size != 1 || matchedTempsNodup.isEmpty) {
              println("Bachle: Warning matched two templates at one time! " + countFolder)
              counts
            }else{
              counts(matchedTempsNodup(0).toInt - 1) += 1
              counts
            }
          }
        }
    }

    printMatchedRes(counts)
    println("Unmatched: "+noMatched.size)
    counts
  }

  def printMatchedRes(counts: Array[Int]): Int ={
    counts.foldLeft(1){
      (temp,count) =>{
        println("Template "+temp+": "+count)
        temp+1
      }
    }
  }
}
