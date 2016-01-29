package localizations.fixlocalization.methodsimilarity

import localizations.fixlocalization.methodsimilarity.FindFixStrategies.FixStrat.FixStrat
import localizations.{Identifier, PrecomputedInfor}
import parsers.javaparser.JavaParser
import util.ast.visitor.StatementCollector

import scala.collection.mutable.ArrayBuffer

/**
 * Created by xuanbach32bit on 5/17/15.
 */
object FindFixStrategies {

  object FixStrat extends Enumeration{
    type FixStrat = Value
    val BY_FILES, BY_CURNODE, NO_RANK = Value
  }

  //fixSpace: HashMap[String, ArrayBuffer[Identifier[Any]]]
  def chooseAStrategy(prec: PrecomputedInfor, curNode: Identifier[Any], strat: FixStrat): ArrayBuffer[Identifier[Any]] ={
    println("FIX STRAT: "+strat)
    if(strat.equals(FixStrat.BY_FILES)){
      return rankFixSpaceByFiles(prec, curNode)
    }
    else if(strat.equals(FixStrat.BY_CURNODE)){
      return rankFixSpaceByCurrentNode(prec,curNode)
    }
    else if(strat.equals(FixStrat.NO_RANK)){
      val fixSpace=noRank(prec, curNode)
      println("Fix space:"+ fixSpace)
      return fixSpace
    }
    println("RETURNING NULL in Strategy!")
    null
  }

  def rankFixSpaceByFiles(prec: PrecomputedInfor, curNode: Identifier[Any]): ArrayBuffer[Identifier[Any]]={
    var res= new ArrayBuffer[Identifier[Any]]()
    val iter=prec.getFaultFiles().entrySet().iterator()
    while(iter.hasNext){
      val entry=iter.next()
      val value=prec.getFixSpace().get(entry.getKey)
      if(value !=null)
        res.appendAll(value)
    }
    res
  }

  //curNode: Identifier[Any]
  def rankFixSpaceByCurrentNode(prec: PrecomputedInfor, curNode: Identifier[Any]): ArrayBuffer[Identifier[Any]] = {
    //var scope: java.util.Set[String] = null
    println("Processing Fix space for: "+curNode)
    /*if(curNode.getJavaNode()==null) {
      //sys.error("HAVE NOT SET THE Java Node for node: " + curNode)
      val (_, javaNode, _) = ASTRewriteUtils.getIdenRefInfor(curNode)
      if(javaNode == null)
        return new ArrayBuffer[Identifier[Any]]()
      curNode.setJavaNode(javaNode)
      println("Setting java node for: " + curNode + " as "+javaNode)
    }*/

    /*if(curNode.getNames == null){// to set names for this curNode
      if(curNode.getJavaNode()!=null)
        curNode.setNames(ASTUtils.getNames(curNode.getJavaNode()))
    }*/

    /*if(prec.getFixSpace().isEmpty){
      val compilationUnitCurrentNode = JavaParser.globalASTs.get(curNode.getFileName())
      val compilationUnitStatements = new StatementCollector(curNode.getFileName(),prec.getFaultSpace() /*new ArrayBuffer[Identifier[Any]]()*/, curNode, checkScope = true)
      compilationUnitCurrentNode.accept(compilationUnitStatements)
      val fixSpace = new java.util.HashMap [String, ArrayBuffer[Identifier[Any]]]()
      fixSpace.put(curNode.getFileName(), compilationUnitStatements.statements)
      prec.fix = fixSpace
    }

    val fixspace=prec.getFixSpace().entrySet().iterator().next().getValue;// fix space has only one element

    //println("Fix space = "+fixspace)
    fixspace*/
    if(curNode.getFixSpace() == null){
      val compilationUnitCurrentNode = JavaParser.globalASTs.get(curNode.getFileName())
      val compilationUnitStatements = new StatementCollector(curNode.getFileName(),prec.getFaultSpace() /*new ArrayBuffer[Identifier[Any]]()*/, curNode, checkScope = true)
      compilationUnitCurrentNode.accept(compilationUnitStatements)
      curNode.setFixSpace(compilationUnitStatements.statements)
    }
    curNode.getFixSpace()
  }

  def noRank(prec: PrecomputedInfor, curNode: Identifier[Any]) : ArrayBuffer[Identifier[Any]]={
    //var scope: java.util.Set[String] = null
    println("Processing Fix space for: "+curNode)
    /*if(curNode.getJavaNode()==null) {
      //sys.error("HAVE NOT SET THE Java Node for node: " + curNode)
      val (_, javaNode, _) = ASTRewriteUtils.getIdenRefInfor(curNode)
      if(javaNode == null)
        return new ArrayBuffer[Identifier[Any]]()
      curNode.setJavaNode(javaNode)
      println("Setting java node for: " + curNode + " as "+javaNode)
    }*/

    //For now we do not consider scope yet
    /*if(curNode.getScope()==null){// to set scope for this curNode
      curNode.setScope(ASTUtils.getScope(curNode.getJavaNode()))
    }*/

    /*if(curNode.getNames == null){// to set names for this curNode
      if(curNode.getJavaNode()!=null)
        curNode.setNames(ASTUtils.getNames(curNode.getJavaNode()))
    }*/

    /*if(prec.getFixSpace().isEmpty){
      val compilationUnitCurrentNode = JavaParser.globalASTs.get(curNode.getFileName())
      val compilationUnitStatements = new StatementCollector(curNode.getFileName())
      compilationUnitCurrentNode.accept(compilationUnitStatements)
      var fixSpace = new java.util.HashMap [String, ArrayBuffer[Identifier[Any]]]()
      fixSpace.put(curNode.getFileName(), compilationUnitStatements.statements)
      prec.fix = fixSpace
    }*/
    val fixspace = new ArrayBuffer[Identifier[Any]]
    val iter=prec.getFixSpace().entrySet().iterator()
    while(iter.hasNext){
      val entry=iter.next()
      fixspace.appendAll(entry.getValue)
    }
    println("Fix space = "+fixspace)
    fixspace
  }

}
