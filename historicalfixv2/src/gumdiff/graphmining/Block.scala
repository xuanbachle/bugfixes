package gumdiff.graphmining

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 7/19/15.
 */
class Block {
  var id = 0
  var support = 0
  var verList = new ArrayBuffer[String]
  var edgeList = new ArrayBuffer[String]()
  var containers = new ArrayBuffer[String]()
  var folder = ""
  var fullyAssignedProps = false
  
  def toShortString(): String ={
    id + " " + support +"\n" + verList.foldLeft(""){(res,ver) => res+ver+"\n" } + edgeList.foldLeft(""){(res,ver) => res+ver+"\n" } + containers.foldLeft(""){(res,ver) => res+ver+" "} + "\n"
  }

  def isSubgraph(anotherBlock: Block) : Boolean={
    //println("Comparing: "+ id +" "+ anotherBlock.id)
    if(support != anotherBlock.support) {
      //println("1")
      return false
    }

    if(isSubVerList(anotherBlock) && isSubEdgeList(anotherBlock)) {
      //println("2")
      return true
    }
    else {
      //println(isSubVerList(anotherBlock))
      //println(isSubEdgeList(anotherBlock))
      return false
    }
  }

  private def isSubVerList(anotherBlock: Block): Boolean ={
    if(verList.size > anotherBlock.verList.size)
      return false

    verList.foldLeft(0)((i,ver) => {
      if (!ver.equals(anotherBlock.verList(i)))
        return false
      i + 1
    }
    )
    return true
  }

  private def isSubEdgeList(anotherBlock: Block): Boolean ={
    if(edgeList.size > anotherBlock.edgeList.size)
      return false

    edgeList.foldLeft(0)((i,edge) => {
      if (!edge.equals(anotherBlock.edgeList(i)))
        return false
      i + 1
      }
    )
    return true
  }

  def hasSmallerSize(anotherBlock: Block): Boolean={
    if(verList.size <= anotherBlock.verList.size && edgeList.size < anotherBlock.edgeList.size)
      return true
    else
      return false
  }
}
