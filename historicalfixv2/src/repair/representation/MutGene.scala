package repair.representation

import java.util.Random

import localizations.{JavaNodeIden, Identifier}
import mainscala.RepairOptions
import org.eclipse.jdt.core.dom._
import repair.mutationoperators.{BooleanConditionSynthesizer, AvailableMutActions, ExpressionMutActions}
import util.ast.ASTUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class MutGene {
  protected var probMut: Double = 0.33
  var Wmut: Double = RepairOptions.globalWmut
  def getProbMut(): Double = probMut
  def resetProbMut(reset: Double): Unit = probMut = reset
  def getMutatedLineProb() = 1.0
  protected def random[T](pick: Seq[T], rng: Random): T = return pick(rng.nextInt(pick.size))
  def isAppropriateMut(currentNode: Identifier[Any]): Boolean = true

}
case class AppMut(des: Identifier[Any], to_app: Identifier[Any]) extends MutGene{
  probMut = RepairOptions.appMutProb
  def getDes() = des
  def getToApp() = to_app
  override def getMutatedLineProb() = des.getProb()
  override def toString() = {
    val sim = if(to_app!=null) " sim="+to_app.getFixProb() else ""
    "App: "+des +" with "+to_app +" prob="+ probMut+sim
  }
  override def hashCode(): Int ={
    return 1
  }

  override def equals(obj: Any): Boolean ={
    if(!obj.isInstanceOf[AppMut]){
      return false
    }

    val objAppMut = obj.asInstanceOf[AppMut]
    if(objAppMut.getDes().getLine() == des.getLine() && objAppMut.getToApp().getLine() == to_app.getLine())
      return true
    else
      return false
  }
}
case class PrepMut(des: Identifier[Any], to_prepend: Identifier[Any]) extends MutGene{
  //probMut = RepairOptions.appMutProb
  def getDes() = des
  def getToPrep() = to_prepend
  override def getMutatedLineProb() = des.getProb()
  override def toString() = {
    val sim = if(to_prepend!=null) " sim="+to_prepend.getFixProb() else ""
    "Prepend: "+des +" with "+to_prepend +" prob="+ probMut+sim
  }
  override def hashCode(): Int ={
    return 5
  }

  override def equals(obj: Any): Boolean ={
    if(!obj.isInstanceOf[PrepMut]){
      return false
    }

    val objAppMut = obj.asInstanceOf[PrepMut]
    if(objAppMut.getDes().getLine() == des.getLine() && objAppMut.getToPrep().getLine() == to_prepend.getLine())
      return true
    else
      return false
  }
}
case class PrepAppMut() extends MutGene{
  private var possibleMuts = new ArrayBuffer[MutGene]()
  def addToPossibleMuts(pmut: MutGene) = possibleMuts.append(pmut)
  def removePrepMut() = possibleMuts = possibleMuts.filter(p => !p.isInstanceOf[PrepMut])
  def getPossibleMuts() = possibleMuts
  def choosePrepOrApp(rng: Random): MutGene = random(possibleMuts, rng)
  override def toString() = "PrepApp: "+possibleMuts
}
case class DelMut(to_del: Identifier[Any]) extends MutGene{
  //probMut = RepairOptions.delMutProb
  def getToDel() = to_del
  override def getMutatedLineProb() = to_del.getProb()
  override def toString() = "Del: "+to_del+" "+ probMut
  override def hashCode(): Int ={
    return 2
  }

  override def equals(obj: Any): Boolean ={
    if(!obj.isInstanceOf[DelMut]){
      return false
    }

    val objAppMut = obj.asInstanceOf[DelMut]
    if(objAppMut.getToDel().getJavaNode().equals(to_del.getJavaNode()))
      return true
    else
      return false
  }
}
case class RepMut(des: Identifier[Any], to_rep: Identifier[Any]) extends MutGene{
  //probMut = RepairOptions.repMutProb
  def getDes() = des
  def getToRep() = to_rep
  override def getMutatedLineProb() = des.getProb()
  override def toString() = {
    val sim=if(to_rep != null) " sim="+to_rep.getFixProb() else ""
    "Rep: "+des +" by "+to_rep+" prob="+ probMut+sim

  }
  override def hashCode(): Int ={
    return 3
  }

  override def equals(obj: Any): Boolean ={
    if(!obj.isInstanceOf[RepMut]){
      return false
    }

    val objRepMut = obj.asInstanceOf[RepMut]
    if(objRepMut.getDes().getJavaNode().equals(des.getJavaNode())) {
      if(objRepMut.getToRep().getJavaNode().equals(to_rep.getJavaNode())) {
        //println(objRepMut.getToRep().getJavaNode().toString)
        return true
      }else {
        if (objRepMut.getToRep().getJavaNode().toString.equals(to_rep.getJavaNode().toString)) // avoid the equals method of ASTNode in case new object created
          return true
        else
          return false
      }
    }
    else
      return false
  }
}
case class SwapMut(s1: Identifier[Any], s2: Identifier[Any]) extends MutGene{
  //probMut = RepairOptions.swapMutProb
  def getS1() = s1
  def getS2() = s2
  override def toString() = "Swap: "+s1 +" with "+s2+" "+ probMut
  override def hashCode(): Int ={
    return 4
  }

  override def equals(obj: Any): Boolean ={
    if(!obj.isInstanceOf[SwapMut]){
      return false
    }

    val objSwapMut = obj.asInstanceOf[SwapMut]
    if(objSwapMut.getS1().getJavaNode().equals(s1.getJavaNode()) && objSwapMut.getS2().getJavaNode().equals(s2.getJavaNode())) {
      return true
    }
    else
      return false
  }
}
case class ExpressionMut(parentExp:Identifier[Any], rng: Random, mutActions: AvailableMutActions) extends MutGene{
  var targetMutant: Identifier[Any] = null
  var sourceExp: Identifier[Any] = null

  override def toString() = "ExpMut: "+parentExp.getExps()
  override def equals(obj: Any): Boolean ={
    throw new RuntimeException("Impossible: MutGene, Expression")
  }

  override def hashCode(): Int ={
    throw new RuntimeException("Impossible: MutGene, Expression")
  }

  def setTargetMutant(mut: Identifier[Any]) = targetMutant = mut


  def toMutateExpression() = rng.nextBoolean()

  def mutate(): (Identifier[Any], Identifier[Any])={
    println("Source Exps ="+parentExp.getExps())
    val oneSourceExp = random(parentExp.getExps(), rng)
    val (sourceExpForMutated,mutatedExp) =mutateOneExp(oneSourceExp)
    println("Source: "+sourceExpForMutated)
    println("Mutated: "+mutatedExp)
    if(mutatedExp == null){ // mutation exp not successfull
      (null,null)
    }else {
      targetMutant = mutatedExp
      this.sourceExp = sourceExpForMutated
      (sourceExpForMutated, mutatedExp)
    }
  }

  private def inheritInforFromParentToExp(exp: Identifier[Any]) ={
    if(exp.getLine()==95)
      println("DB")
    exp.setFieldVars(parentExp.getFieldVars())
    exp.setCurrentMethodVars(parentExp.getCurrentMethodVars())
    exp.setforLoopVars(parentExp.getForLoopVars())
    //exp.setScope(parentExp.getScope())
    if(exp.getScope() == null){
      exp.setScope(ASTUtils.getScope(exp.getJavaNode()))
    }
  }

  private def mutateOneExp(exp: Identifier[Any], inheritInforFromParent: Boolean = true) : (Identifier[Any],Identifier[Any]) ={
    if(inheritInforFromParent){
      inheritInforFromParentToExp(exp)
    }
    var trial = 0
    def helper(): (Identifier[Any],Identifier[Any]) =
    {
        trial += 1
        val mutator = mutActions.chooseAppropriateExpMutAction(exp, parentExp, rng)
        if(mutator == null){
          return (exp, null)
        }

        val (src, mutated)=mutator.mutateExp(exp)
        if(mutated!=null)// success, return the result
           (src, mutated)
        else if(trial < 5){// try 5 times
           helper()
        }else{
          (src, null)// after 5 times, give up and return null
        }
    }
    return helper()
  }
}
case class ClassCastChecker() extends MutGene{//For PAR
  var castExp : CastExpression= null
  override def isAppropriateMut(currentNode: Identifier[Any]): Boolean = {
    if(currentNode.getJavaNode().isInstanceOf[CastExpression]) {
      castExp = currentNode.getJavaNode().asInstanceOf[CastExpression]
      return true
    }
    else if(currentNode.getJavaNode().isInstanceOf[Assignment]){
      val as = currentNode.getJavaNode().asInstanceOf[Assignment]
      if(as.getRightHandSide.isInstanceOf[CastExpression]) {
        castExp=as.getRightHandSide.asInstanceOf[CastExpression]
        return true
      }
    }
    return false
  }

  def castChecker(currentNode: Identifier[Any]): Identifier[Any] ={
    val jvNode = currentNode.getJavaNode()
    val checker = jvNode.getAST.newIfStatement()
    val ckExpression = jvNode.getAST.newInstanceofExpression()
    ckExpression.setLeftOperand(ASTNode.copySubtree(castExp.getExpression.getAST, castExp.getExpression).asInstanceOf[Expression])
    ckExpression.setRightOperand(ASTNode.copySubtree(castExp.getType.getAST, castExp.getType).asInstanceOf[Type])
    checker.setExpression(ASTNode.copySubtree(ckExpression.getAST,ckExpression).asInstanceOf[Expression])
    val thenStmt=jvNode.getAST.newExpressionStatement(ASTNode.copySubtree(jvNode.getAST, jvNode).asInstanceOf[Expression])
    checker.setThenStatement(thenStmt.asInstanceOf[Statement])
    return new JavaNodeIden(currentNode.getFileName(),checker,currentNode.getProb())
  }
}

case class NullChecker() extends MutGene{//For PAR
  var names : mutable.HashMap[String, ArrayBuffer[ITypeBinding]]= new mutable.HashMap[String, ArrayBuffer[ITypeBinding]]
  override def isAppropriateMut(currentNode: Identifier[Any]): Boolean = {
    import scala.collection.JavaConversions._
    if(currentNode.hasExps()){
      currentNode.getExps().map(exp => ASTUtils.getVariableUsed(exp.getJavaNode()).map(n => names.add(n)))
      if(names.size >0)
        return true
      else
        return false
    }
    return false
  }

  /*def nullChecker(currentNode: Identifier[Any]): Identifier[Any] ={
    val jvNode = currentNode.getJavaNode()
    val checker = jvNode.getAST.newIfStatement()

    val allExps: ArrayBuffer[InfixExpression] = new ArrayBuffer[InfixExpression]()
    names.map(name =>{
      val ckExpression = jvNode.getAST.newInstanceofExpression()
      ckExpression.setLeftOperand(ckExpression.getAST.newSimpleName(name._1))
      ckExpression.setRightOperand(name._2(0).getT)
    })
    checker.setExpression(ASTNode.copySubtree(ckExpression.getAST,ckExpression).asInstanceOf[Expression])
    val thenStmt=jvNode.getAST.newExpressionStatement(ASTNode.copySubtree(jvNode.getAST, jvNode).asInstanceOf[Expression])
    checker.setThenStatement(thenStmt.asInstanceOf[Statement])
    return new JavaNodeIden(currentNode.getFileName(),checker,currentNode.getProb())
  }*/
}