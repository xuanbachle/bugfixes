package gumdiff.difftemplates

import java.io.File
import java.util
import java.util.List

import com.github.gumtreediff.actions.model.Action
import com.github.gumtreediff.tree.{TreeUtils, ITree}
import org.eclipse.jdt.core.dom.ASTNode
import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
/**
 * Created by dxble on 7/18/15.
 */
object ActionHandling {

  private def actionName(ac: Action): String = ac.getClass.getSimpleName

  private def printActions(actions: List[Action]): Unit ={
    println("Action size: "+ actions.size())
    for(ac<-actions) {
      println(ac.toString)
      //println(nodeClassName(ac.getNode))
      //println(ac.getNode.areDescendantsMatched())
      //println(ac.getNode.toTreeString)
      //println(ASTNode.nodeClassForType(42))
      //println(nodeClassName(ac.getNode.getParent))
      //println(nodeClassName(ac.getNode.getParent.getParent))
    }
  }

  def getContext(actions: util.List[Action], ac: Action) = {
    val context = new ArrayBuffer[ITree]()
    if(actions.size()==1)
      context.append(ac.getNode.getParent.getParent)
    /*println(ac.getNode)
    println(ac.getNode.getEndPos - ac.getNode.getPos)
    var parent: ITree = ac.getNode.getParent
    do {
      println(ASTNode.nodeClassForType(parent.getType).getCanonicalName)
      context.append(parent)
      parent = parent.getParent
    }while(!ASTNode.nodeClassForType(parent.getType).getCanonicalName.contains("Block"))*/
    context.append(ac.getNode.getParent)
    context.append(ac.getNode)
    context
  }

  def encodeActionName(ac: Action): Int = {
    if(actionName(ac).equals("Update")){
      return 1
    }else if(actionName(ac).equals("Insert")){
      return 2
    }else if(actionName(ac).equals("Move")){
      return 3
    }else if(actionName(ac).equals("Delete")){
      return 4
    }else {
      return 5
    }
  }

  def code2ActionName(code: Int) ={
    if(code == 0)
      "Parent-Child"
    else if(code == 1)
      "Update"
    else if(code == 2)
      "Insert"
    else if(code == 3)
      "Move"
    else if(code == 4)
      "Delete"
    else
      "Unknown"
  }

  def simpleName2RealName(code: Int): String ={
    if(code == 103)
      return "SimpleName"
    else if(code == 100)
      return "VarName"
    else if(code == 101)
      return "MethodName"
    else if(code == 102)
      return "ArgumentName"
    else if(code == 105)
      return "ClassName"
    else
      return "UnKnown Name"
  }

  private def isSimpleNameNode(node: ITree, diff: DiffTemplates): Boolean ={
    if(diff.nodeClassName(node) =="SimpleName")
      true
    else
      false
  }

  def serializeDiff2GSpanGraphString(actions: java.util.List[Action], graphID: Int, folder: String, diff: DiffTemplates): String ={
    val buf=serializeDiff2GSpanGraph(actions, graphID, folder, diff);
    buf.foldLeft(""){
      (res, line) =>{
        res + line+"\n"
      }
    }
  }

  def serializeDiff2GSpanGraph(actions: java.util.List[Action], graphID: Int, folder: String, diff: DiffTemplates): ArrayBuffer[String] ={// to generate data for mining frequent patterns
    //println("Sel: "+folder)
    var verLables = new HashMap[Int, Int] // label = node type -> index Nth
    var verID = 0
    //printActions(actions)
    import scala.collection.JavaConversions._
    val vers=actions.foldLeft(new ArrayBuffer[String]){
      (buf, ac) =>{
        if(buf.isEmpty)
          buf.append("t"+" # "+graphID)

        def verHelper(node: ITree): String= {
          val v = verLables.get(node.getType)
          val ver=v match {
            case Some(index) => null
            case None => {
              val nodeType =if(!isSimpleNameNode(node,diff))
                node.getType
              else
                diff.typeOfRealSimpleName(diff.extractRealSimpleName(node.getShortLabel))
              if(nodeType==103)
                println("Why null: "+node.getShortLabel)
              verLables += (nodeType -> verID)
              verID += 1
              "v" + " " + (verID-1) + " " + nodeType
            }
          }
          return ver
        }
        //buf.append(ver)

        val context=getContext(actions, ac)
        context.map{
            ctx => {
            val ver = verHelper(ctx)
            if (ver != null)
              buf.append(ver)
          }
        }
        buf
      }
    }

    def getVerIndex(labelType: Int): Int ={
      val vIdx=verLables.get(labelType) match {
        case Some(v) => v
        case None => {println("not found "+labelType +  ASTNode.nodeClassForType(labelType).getSimpleName); -1}
      }
      return vIdx
    }

    val graphText=actions.foldLeft(vers){
      (buf, ac) =>{
        val context=getContext(actions, ac)
        context.foldLeft(0){
          (count, ctx) => {
            if(count<context.size-2){
              val edge = "e" + " " +getVerIndex(diff.extractTypeOfNode(ctx))+" "+getVerIndex(diff.extractTypeOfNode(context(count+1)))+" "+"0" // 0 means parent child relationship
              buf.append(edge)
            }else if(count==context.size-2){
              val edge = "e" + " " +getVerIndex(diff.extractTypeOfNode(ctx))+" "+getVerIndex(diff.extractTypeOfNode(context(count+1)))+" "+encodeActionName(ac)
              buf.append(edge)
            }
            count + 1
          }
        }
        buf
      }
    }
    graphText.append(folder)
    //println(graphText(graphText.size-1))
    return graphText
  }

  def serializeOneActionToOneGraph(action: Action): String ={
    val tree=TreeUtils.preOrder(action.getNode);
    tree.foldLeft(""){
      (str,node) =>{
        str + node.getType + " "
      }
    }
  }

  def needRootOnly(actions: List[Action]): Boolean ={
    println(actions.size())
    if(actions.size() == 1)
      return false

    actions.foldLeft(1){
      (i,ac) => {
        if (!actionName(ac).equals(actionName(actions.get(0)))) {
          println(actionName(ac)+ " "+ actionName(actions.get(0)))
          return false
        }
        try {
          val firstChild = ac.getNode.getChild(0)
          if (!firstChild.isSimilar(actions.get(i).getNode)) {
            println(firstChild)
            println(actions.get(i).getNode)
            return false
          }
        }catch{
          case e: Throwable => {
            //return false
          }
        }
        i+1
      }
    }
    true
  }
}
