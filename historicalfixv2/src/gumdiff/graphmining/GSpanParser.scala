package gumdiff.graphmining

import gumdiff.difftemplates.ActionHandling
import org.eclipse.jdt.core.dom.ASTNode

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.collection.JavaConversions._

/**
 * Created by dxble on 7/18/15.
 */
object GSpanParser{
  def main(args: Array[String]) {
    val gSpan = new GSpanParser
    /*val finalClusters=gSpan.getClosedFrequentGraph("/home/dxble/MyWorkSpace/fixtemplate/src/graphmining/Test.txt")
    finalClusters.map(blk=> {
      println("*****BLK****")
      println(blk.toShortString())
    })*/
    val lines = gSpan.convert2ASTNodeName("/home/xuanbach/test.lg")//gInput.txt.fp
    lines.map{
      l => println(l)
    }
    //gSpan.matchParentGraphFolder("/home/dxble/MyWorkSpace/fixtemplate/src/output/res2/minedge5/gInput.lg","/home/dxble/MyWorkSpace/fixtemplate/src/output/res2/minedge5/result10f.template")
  }
}

class GSpanParser {

  def getClosedFrequentGraph(file: String) = {
    val blocks=parse(file)
    val clustersBySup = traverseAllBlocks(blocks)
    val allPatterns = traverseClusters(clustersBySup)
    allPatterns
  }

  def convert2ASTNodeName(file: String) ={
    val lines=Source.fromFile(file).getLines()
    lines.foldLeft(new ArrayBuffer[String]())((buf, line) =>{
      if(line.startsWith("v")){
        val sp = line.split(" ")
        //println(sp(2))
        val nodeName = try{
          ASTNode.nodeClassForType(sp(2).toInt).getSimpleName
        } catch {
          case e: Throwable => ActionHandling.simpleName2RealName(sp(2).toInt)
        }

        buf.append(sp(0)+" "+sp(1)+" "+nodeName)
      } else if(line.startsWith("e")){
        val sp = line.split(" ")
        val actionName = ActionHandling.code2ActionName(sp(3).toInt)
        buf.append(sp(0)+" "+sp(1)+" "+sp(2)+ " "+actionName)
      }else{
        buf.append(line)
      }
      buf
    })
  }

  def matchParentGraphFolder(gInput: String, gOutputWithFreq: String) ={
    import scala.io.Source
    val lines=Source.fromFile(gOutputWithFreq).getLines()

    val (tempBlocks,_)=lines.foldLeft(new ArrayBuffer[Block](), new Block){case ((data, block), line) => {
      if(line.startsWith("freq")) {
        block.support = line.split(" ")(1).toInt
        (data, block)
      } else if(line.startsWith("t")){
        block.id = line.split(" ")(2).toInt
        (data, block)
      }
      else if(line.startsWith("x")){
        block.containers = line.split(" ").foldLeft(new ArrayBuffer[String]()){(res, str) => {
          if (!str.equals("x"))
            res.append(str)
          res
        }}
        data.append(block)
        (data, new Block)
      }else{
        (data, block)
      }
    }}

    val inLines=Source.fromFile(gInput).getLines()
    val (inputBlocks,_)=inLines.foldLeft(new ArrayBuffer[Block](), new Block){
      case ((data, block), line) => {
        if(line.startsWith("t")){
          block.id = line.split(" ")(2).toInt
          (data, block)
        } else if(line.startsWith("/home/")){
          block.folder = line
          data.append(block)
          (data, new Block)
        } else{
          (data, block)
        }
      }
    }
  }

  def parse2(file: String) ={
    import scala.io.Source
    val lines=Source.fromFile(file).getLines()
    val (allBlocks, size)= lines.foldLeft((new ArrayBuffer[ArrayBuffer[String]],0)){
      (bufi,line) => {
        bufi match {
          case (buf, i)=>{
            if(!line.startsWith("freq")){
              try {
                buf(i)
              }catch {
                case e: Throwable =>{
                  buf.append(new ArrayBuffer[String]())
                }
              }
              buf(i).append(line)
              (buf,i)
            }else{
              (buf,i+1)
            }
          }
        }
      }
    }
    allBlocks
  }


  def parse(file: String) ={
    import scala.io.Source
    val lines=Source.fromFile(file).getLines()
    val (allBlocks, size)= lines.foldLeft((new ArrayBuffer[ArrayBuffer[String]],0)){
      (bufi,line) => {
        bufi match {
          case (buf, i)=>{
            if(!line.isEmpty){
              try {
                buf(i)
              }catch {
                case e: Throwable =>{
                  buf.append(new ArrayBuffer[String]())
                }
              }
              buf(i).append(line)
              (buf,i)
            }else{
              (buf,i+1)
            }
          }
        }
      }
    }
    allBlocks
  }

  def traverseAllBlocks(allBlocks: ArrayBuffer[ArrayBuffer[String]]) ={
    allBlocks.foldLeft(new HashMap[Int, ArrayBuffer[Block]]){
      (clusters,block) => {
        //println("****Block****")
        //block.map(b => println(b))
        val bl = getInforFromBlock(block)
        //println(bl.toShortString())
        clusters.get(bl.support) match {
          case Some(v) => {
            v.append(bl)
            clusters
          }
          case None => {
            val v = new ArrayBuffer[Block]()
            v.append(bl)
            clusters + (bl.support -> v)
          }
        }
      }
    }
  }

  def traverseClusters(clusters: HashMap[Int, ArrayBuffer[Block]])={
    clusters.foldLeft(new ArrayBuffer[Block]) { (cls,kv) => kv match {
      case (k,v) => {
          //println("*****BLOCK****" + k.toString)
          val sortedV = v.sortWith((b1, b2) => b1.hasSmallerSize(b2))
          //v.map { bl => println(bl.toShortString()) }
          def helper(sortedV:ArrayBuffer[Block]): Unit = {
            if(sortedV.size > 1) {
              val toCompare = sortedV.takeRight(sortedV.size - 1)
              if (!toCompare.exists(cp => sortedV(0).isSubgraph(cp))) {
                cls.append(sortedV(0))
              }
              helper(toCompare)
            }else
              cls.append(sortedV(0))
          }
          helper(sortedV)
          cls
      }
    }}
  }

  def getInforFromBlock(block: ArrayBuffer[String]) ={ // vertex, edge sets and graph id, support, list of graph containing the pattern
    block.foldLeft(new Block){
      (b,str) =>{
        if(str.startsWith("t")){
          val sp=str.split(" ")
          //println(sp(1)+sp(2))
          b.id = sp(2).toInt
          b.support = sp(4).toInt
        }
        if(str.startsWith("v")){
          b.verList.append(str)
        }
        if(str.startsWith("e")){
          b.edgeList.append(str)
        }
        if(str.startsWith("x")){
          val sp = str.split(" ")
          b.containers.++=(sp)
          //b.containers.++=(sp.takeRight(sp.size -1))
        }
        b
      }
    }
  }

}
