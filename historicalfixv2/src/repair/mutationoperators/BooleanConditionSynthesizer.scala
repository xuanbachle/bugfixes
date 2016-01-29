package repair.mutationoperators

import java.util.Random

import localizations.{JavaNodeIden, Identifier}
import org.eclipse.jdt.core.dom._
import parsers.javatypeinfor.{JavaClassTypeUsage, JavaGlobalTypeInfor}
import randomController.RandomController
import util.ast.ASTUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by larcuser on 17/10/15.
 */
class BooleanConditionSynthesizer {
  val expCollector = new BooleanConditionCollector

  class BooleanConditionCollector extends ASTVisitor{
    //var visitingBooleanCondition = false
    val collectedExp = new ArrayBuffer[Expression]

    override def visit(node: IfStatement): Boolean ={
      if(ASTUtils.getStatementLineNo(node)==161)
        println("DB")
      booleanExpCollector(node.getExpression)
      //visitingBooleanCondition = true
      true
    }

    override def visit(node: WhileStatement): Boolean ={
     // visitingBooleanCondition = true
      booleanExpCollector(node.getExpression)
      true
    }

    def booleanExpCollector(booleanExp: Expression): Unit ={
      if(booleanExp.isInstanceOf[InfixExpression]){
        //||booleanExp.isInstanceOf[PrefixExpression]||booleanExp.isInstanceOf[PostfixExpression]){
        collectedExp.append(booleanExp)
        if(ExpOperatorMut.booleanOps.contains(booleanExp.asInstanceOf[InfixExpression].getOperator)) {
          booleanExpCollector(booleanExp.asInstanceOf[InfixExpression].getLeftOperand)
          booleanExpCollector(booleanExp.asInstanceOf[InfixExpression].getLeftOperand)
        }
      }else{
        collectedExp.append(booleanExp)
      }
    }

    /*override def visit(node: ExpressionStatement): Boolean ={
      if(visitingBooleanCondition){
        visitingBooleanCondition = false
        val booleanExp=node.getExpression
        booleanExpCollector(booleanExp)
      }
      false
    }*/
  }

}
object BooleanConditionSynthesizer{
  val collectedExp = new mutable.HashMap[String,Seq[Identifier[Any]]]()

  private def collectBooleanConditions(cu: CompilationUnit, fileName: String): Boolean ={
    if(collectedExp.contains(fileName))
      return false

    val conditionSynthesizer = new BooleanConditionSynthesizer
    cu.accept(conditionSynthesizer.expCollector)
    val idenExps = conditionSynthesizer.expCollector.collectedExp.map(exp => new JavaNodeIden(fileName, exp, 1.0))
    collectedExp.put(fileName,idenExps)
    return true
  }

  def expFilterByScope(currentNode: Identifier[Any]): Seq[Identifier[Any]] = {
    if(collectedExp.isEmpty || !collectedExp.contains(currentNode.getFileName())){
      collectBooleanConditions(currentNode.getJavaNode().getRoot.asInstanceOf[CompilationUnit], currentNode.getFileName())
    }
    val collectedExpInLocalFile = collectedExp.get(currentNode.getFileName()).getOrElse(null)
    if (collectedExpInLocalFile == null)
      return null
    else {
      if(collectedExpInLocalFile.isEmpty)
        return null

      val inscopeExps=collectedExpInLocalFile.filter(idenExp => {
        if (idenExp.getScope() == null){
          ASTUtils.getScope(idenExp.getJavaNode())
        }
        currentNode.isInScope(idenExp)
      })
      if(inscopeExps.isEmpty)
        return null
      else
        return inscopeExps
    }
  }
  //Given an ifstatement, return possible condition synthesized from if body, etc that can be added to the condition of the if
  def synthesizeCondIngredientForIfStmt(ifNodeIden: Identifier[Any], variablesInExp: mutable.HashMap[String,ArrayBuffer[ITypeBinding]], attachedAST: AST,rng: Random): Identifier[Any] ={
    val ifNode = ifNodeIden.getJavaNode().asInstanceOf[IfStatement]
    val variablesInBody=ASTUtils.getVariableUsed(ifNode.getThenStatement)
    ASTUtils.getVariableUsed(ifNode.getElseStatement).map(v => variablesInBody.put(v._1,v._2))

    var count = 0// to avoid loop forever
    while(count <= 20) {
      count += 1
      val randomlyPickedVar = RandomController.randomPick(variablesInExp.keySet.foldLeft(new ArrayBuffer[String]) {
        (res, str) => {
          res.append(str);
          res
        }
      }, rng)
      val varTypes = variablesInExp.get(randomlyPickedVar).getOrElse(throw new RuntimeException("Should never happen"))
      if (varTypes != null && !varTypes.isEmpty) {
        val associatedVarType = RandomController.randomPick(varTypes, rng)

        JavaGlobalTypeInfor.buildLocalClassTypeInfor(ifNodeIden)
        val typeInfor = JavaGlobalTypeInfor.lookUpTypeInforForAClass(ifNodeIden.getFileName())
        val allUsage = typeInfor.lookUpUsageOfAType(associatedVarType.toString)
        //Currently, we only care about usage with boolean return type, e.g., P.isDouble() instead of P.getDouble() == 0
        //Future, we need to take into account the latter as well. The latter involves using usage context
        if (allUsage != null) {
          val usageWithRetBool = allUsage.filter(usage =>
            usage.getReturnType().compareTo(PrimitiveType.BOOLEAN.toString) == 0
          )

          if (!usageWithRetBool.isEmpty) {
            //Usage we randomly chosen for a variable that appears in If expression
            val usageOfAVar = RandomController.randomPick(usageWithRetBool, rng)
            val conditionToAdd = JavaClassTypeUsage.instantiateAnUsage(randomlyPickedVar, associatedVarType, usageOfAVar, variablesInBody, attachedAST, rng)
            if (conditionToAdd != null)
              return new JavaNodeIden(ifNodeIden.getFileName(), conditionToAdd, 1.0)
          }
        }
      }
    }
    return null
  }
}