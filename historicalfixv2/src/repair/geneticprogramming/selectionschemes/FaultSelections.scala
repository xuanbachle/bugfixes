package repair.geneticprogramming.selectionschemes

import java.util.Random

import localizations.Identifier
import mainscala.RepairOptions
import org.apache.log4j.Logger

import scala.collection.mutable.HashMap
import scala.collection.mutable.ArrayBuffer

/**
 * Created by larcuser on 24/10/15.
 */
object FaultSelections {
  val logger = Logger.getLogger(FaultSelections.getClass)

  def faultLocSelection(faultSpace: Seq[Identifier[Any]], rng: Random): Seq[Identifier[Any]] ={
    val nTimesTournamentRuns = faultSpace.size * RepairOptions.poolSize
    def compareWeight(w1: Identifier[Any], w2: Identifier[Any]): Int = {
      if(w1.getProb() > w2.getProb())
        return 1
      else if(w1.getProb() < w2.getProb()) return -1
      else
        return 0
    }
    /*
      This is to make sure tournament size is not too large compared to the fault size
     */
    val tournamentSize = if(faultSpace.size/RepairOptions.tournamentSize >= 2) RepairOptions.tournamentSize
    else if(faultSpace.size/3 > 0) faultSpace.size/3
    else 1
    var selectedFaultLocs=SelectionScheme.tournamentSelectionNTimes[Identifier[Any]](nTimesTournamentRuns,faultSpace.toBuffer, tournamentSize,rng,true,compareWeight)
    //logger.debug(selectedFaultLocs)
    logger.debug("========Selected Fault Locations First Round========")
    selectedFaultLocs.map(fault => logger.debug(fault.getLine()+" "+fault.getProb()))
    logger.debug("Unique location size: "+selectedFaultLocs.toSet.size)
    logger.debug("========End Selected Fault Locations First Round========")
    /*selectedFaultLocs.foldLeft(new mutable.HashMap[Int, Int]()){
      (res, fault) =>{
        if(res.contains(fault.getLine())){
          val count=res.get(fault.getLine()).getOrElse(0)
          res.put(fault.getLine(), count+1)
        }else{
          res.put(fault.getLine(),1)
        }
        res
      }
    }*/
    val groupedByLine=selectedFaultLocs.groupBy(fault => fault.getLine())
    val maxSize = groupedByLine.maxBy(_._2.size)._2.size // to avoid some line dominates others
    if(tournamentSize == RepairOptions.tournamentSize && maxSize < selectedFaultLocs.size/5) {// second round if the size of the faultSpace is not too small
    val secondTournamentRuns = faultSpace.size * selectedFaultLocs.toSet.size
      selectedFaultLocs = SelectionScheme.tournamentSelectionNTimes[Identifier[Any]](secondTournamentRuns, selectedFaultLocs, tournamentSize, rng, true, compareWeight)
      logger.debug("========Selected Fault Locations Second Round========")
      selectedFaultLocs.map(fault => logger.debug(fault.getLine() + " " + fault.getProb()))
      logger.debug("Unique location size: " + selectedFaultLocs.toSet.size)
      logger.debug("========End Selected Fault Locations Second Round========")
    }

    return selectedFaultLocs
  }

  def faultLocSelection2(faultSpace: Seq[Identifier[Any]], rng: Random): Seq[Identifier[Any]] ={
    def sameLine (id1: Identifier[Any], id2: Identifier[Any]) : Boolean = return id1.getLine() == id2.getLine()
    if(faultSpace.size <= 10)
      return copyXTimesFaultLocs(5, faultSpace)
    else {
      val selectedLines=SelectionScheme.randomUtilSizeX(faultSpace,rng,10,sameLine)//sizeX = 10
      logger.debug("======FaultLoc2: selected faults:")
      selectedLines.map(fl => logger.debug(fl))
      logger.debug("======FaultLoc2: end selected faults:")
      return copyXTimesFaultLocs(5, selectedLines)
    }
  }

  def copyXTimesFaultLocs (x: Int, faults: Seq[Identifier[Any]]): Seq[Identifier[Any]] = {
    val selectedFaultLocs = new ArrayBuffer[Identifier[Any]]()
    for (i <- 1 to x) {
      selectedFaultLocs.appendAll(faults)
    }
    assert(selectedFaultLocs.size == x * faults.size)
    return selectedFaultLocs
  }

  def faultLocSelectionByGroup(faultSpace: Seq[Identifier[Any]], rng: Random): Seq[Identifier[Any]] ={
    val faultprobGroups=faultSpace.groupBy(fl => fl.getProb())
    val selectedFaultLocs = new ArrayBuffer[Identifier[Any]]()
    while(selectedFaultLocs.size < 10) {
      faultprobGroups.map {
        case (prob, arr) => {
          if (prob >= rng.nextDouble()) {
            val selectedFromGroup = SelectionScheme.random(arr, rng)
            selectedFaultLocs.append(selectedFromGroup)
          }
        }
      }
    }

    logger.debug("======FaultLoc2: selected faults:")
    selectedFaultLocs.map(fl => logger.debug(fl))
    logger.debug("======FaultLoc2: end selected faults:")
    return copyXTimesFaultLocs(5, selectedFaultLocs)

  }

  def faultLocSelControlMajority(faultSpace: Seq[Identifier[Any]], rng: Random, sizeX: Int, majoritySizeUpperBound: Int): Seq[Identifier[Any]] ={
    val faultprobGroups=faultSpace.groupBy(fl => fl.getProb())
    val selectedFaultLocs = new HashMap[Double,ArrayBuffer[Identifier[Any]]]
    var countAdded = 0
    while(countAdded < sizeX) {
      faultprobGroups.map {
        case (prob, arr) => {
          if (prob >= rng.nextDouble()) {
            countAdded += 1
            val selectedFromGroup = SelectionScheme.random(arr, rng)
            selectedFaultLocs.get(selectedFromGroup.getProb()) match {
              case None => {
                val selectedArr = new ArrayBuffer[Identifier[Any]]
                selectedArr.append(selectedFromGroup)
                selectedFaultLocs.put(selectedFromGroup.getProb(), selectedArr)
              }
              case Some(v) => if(v.size<majoritySizeUpperBound){v.append(selectedFromGroup)}
            }
          }
        }
      }
    }
    val selectedLines = selectedFaultLocs.values.foldLeft(new ArrayBuffer[Identifier[Any]]){
      case (res, fl) => {res.appendAll(fl); res}
    }
    logger.debug("======FaultLoc2: selected faults:")
    selectedLines.map(fl => logger.debug(fl))
    logger.debug("======FaultLoc2: end selected faults:")
    return copyXTimesFaultLocs(5, selectedLines)

  }
}
