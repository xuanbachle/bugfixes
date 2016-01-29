package repair.mutationoperators

import java.util.Random

import localizations.{Identifier, JavaNodeIden}
import org.apache.log4j.Logger
import org.eclipse.core.internal.expressions.Expressions
import org.eclipse.jdt.core.dom._
import repair.geneticprogramming.selectionschemes.SelectionScheme
import util.ast.ASTUtils

import scala.collection.immutable.TreeSet
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 8/21/15.
 */
abstract class ExpressionMutActions {
  def mutateExp(exp: Identifier[Any]) : (Identifier[Any],Identifier[Any])
  def isAppropriateAction(exp: Identifier[Any]): Boolean
  //def getActualExpression(exp: Ex)
  protected def random[T](pick: Seq[T], rng: Random): T = return pick(rng.nextInt(pick.size))
}
object ExpOperatorMut{
  val arithmeticOps = Array[InfixExpression.Operator](InfixExpression.Operator.DIVIDE, InfixExpression.Operator.MINUS, InfixExpression.Operator.PLUS,InfixExpression.Operator.TIMES, InfixExpression.Operator.REMAINDER)
  val comparisonDisequalityOps = Array[InfixExpression.Operator](InfixExpression.Operator.GREATER, InfixExpression.Operator.GREATER_EQUALS, InfixExpression.Operator.LESS, InfixExpression.Operator.LESS_EQUALS)
  val comparisonEqualityOps = Array[InfixExpression.Operator](InfixExpression.Operator.EQUALS, InfixExpression.Operator.NOT_EQUALS)
  val bitOps = Array[InfixExpression.Operator](InfixExpression.Operator.LEFT_SHIFT, InfixExpression.Operator.RIGHT_SHIFT_SIGNED, InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED)
  val booleanOps = Array[InfixExpression.Operator](InfixExpression.Operator.CONDITIONAL_AND, InfixExpression.Operator.CONDITIONAL_OR)
  val bitwiseOps = Array[InfixExpression.Operator](InfixExpression.Operator.XOR,InfixExpression.Operator.AND, InfixExpression.Operator.OR )
}
case class ExpOperatorMut(/*exp: Identifier[Any]*/ rng: Random) extends ExpressionMutActions{
  // mutate one operator in the exp only, mutate operator inside expression, e.g., a > b then mutate the operator > to operator >=

  private def getAppropriateInfixOps(op: InfixExpression.Operator): Array[InfixExpression.Operator] ={
    if(ExpOperatorMut.arithmeticOps.contains(op))
      ExpOperatorMut.arithmeticOps.filter(arithOp => !arithOp.equals(op) )
    else if(ExpOperatorMut.comparisonDisequalityOps.contains(op)){
      ExpOperatorMut.comparisonDisequalityOps.filter(comOp => !comOp.equals(op))
    }else if(ExpOperatorMut.comparisonEqualityOps.contains(op)){
      ExpOperatorMut.comparisonEqualityOps.filter(compOp => !compOp.equals(op))
    }
    else if(ExpOperatorMut.bitOps.contains(op)){
      ExpOperatorMut.bitOps.filter(bitOp => !bitOp.equals(op))
    }else if(ExpOperatorMut.booleanOps.contains(op)){
      ExpOperatorMut.booleanOps.filter(boolOp => !boolOp.equals(op))
    }else{
      ExpOperatorMut.bitwiseOps.filter(bitwiseOp => !bitwiseOp.equals(op))
    }
  }

  def mutateExp(exp: Identifier[Any]): (Identifier[Any], Identifier[Any]) ={
    if(exp.transformToJavaNode()){
      val jvNode=exp.getJavaNode()
      if(jvNode.isInstanceOf[InfixExpression]){
        val op=jvNode.asInstanceOf[InfixExpression].getOperator
        val appropriateOps = getAppropriateInfixOps(op)
        val choosenOp = random(appropriateOps, rng)
        val desireNode = ASTNodeFactory.deepCopyWithCaching(jvNode.getAST, choosenOp, jvNode) //ASTNode.copySubtree(jvNode.getAST, jvNode) //
        desireNode.asInstanceOf[InfixExpression].setOperator(choosenOp)
        return (exp, new JavaNodeIden(exp.getFileName(), desireNode,exp.getProb()))
      } else {
        //throw new Exception("Not Yet supported expression mutation: " + jvNode)
        return (exp, null)
      }
    }else {
      throw new RuntimeException("Transform to java node not successful: " + exp)
    }
  }

  def isAppropriateAction(exp: Identifier[Any]): Boolean ={
    if(exp.transformToJavaNode()) {
      val jvNode = exp.getJavaNode()
      if (jvNode.isInstanceOf[InfixExpression])
        return true
    }
    return false
  }
}
case class ExpClassCastInsertion(/*exp: Identifier[Any]*/ rng: Random) extends ExpressionMutActions{
  val possiblePrimitiveTypeCode = Array[PrimitiveType.Code](PrimitiveType.INT, PrimitiveType.DOUBLE, PrimitiveType.LONG, PrimitiveType.FLOAT)

  def chooseAppropriateTypes(typeBinding: ITypeBinding, expNode: ASTNode): Type = {
    if(typeBinding != null){
      if(typeBinding.isPrimitive){// fow now we consider primitive type cast only
        println(typeBinding)
        val primType = expNode.getAST.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName))
        println("Type Binding: "+ typeBinding.getName +" prim type: "+primType)
        val filteredPossibleType = new ArrayBuffer[PrimitiveType.Code]()
        for( typeCode <-possiblePrimitiveTypeCode){
          /*if(primType.getPrimitiveTypeCode == PrimitiveType.INT || primType.getPrimitiveTypeCode == PrimitiveType.DOUBLE
            || primType.getPrimitiveTypeCode == PrimitiveType.LONG || primType.getPrimitiveTypeCode == PrimitiveType.FLOAT){
            primType.
          }*/
          if(typeCode != primType.getPrimitiveTypeCode){
            filteredPossibleType.append(typeCode)
          }
        }
        if(filteredPossibleType.size == possiblePrimitiveTypeCode.size){
          return null // it means the current type is not in the possible primitive types that we handle
        }else{
          val toInsertType= random(filteredPossibleType, rng)
          val resType = ASTNode.copySubtree(expNode.getAST, primType)
          resType.asInstanceOf[PrimitiveType].setPrimitiveTypeCode(toInsertType)
          return resType.asInstanceOf[PrimitiveType]
        }
      }else{// currently we only handle primitive type insertion
        return null
      }
    }else {
      return null
    }
  }

  def mutateExp(exp: Identifier[Any]): (Identifier[Any],Identifier[Any]) ={
    if(exp.transformToJavaNode()){
      val jvNode = exp.getJavaNode()
      val expNode=jvNode.asInstanceOf[Expression]//TODO: consider only infix expression for now
      val typeBinding = expNode.resolveTypeBinding()
      val typeToInsert = chooseAppropriateTypes(typeBinding, expNode)
      if(typeToInsert == null)
        return (exp,null)
      val desireNode = expNode.getAST.newCastExpression()
      desireNode.setType(typeToInsert)
      desireNode.setExpression(ASTNode.copySubtree(expNode.getAST, expNode).asInstanceOf[Expression])
      return (exp, new JavaNodeIden(exp.getFileName(), desireNode,exp.getProb()))
    }else{
      throw new RuntimeException("Transform to java node not successful: " + exp)
    }
  }

  def isAppropriateAction(exp: Identifier[Any]): Boolean={
    if(exp.transformToJavaNode()) {
      val jvNode = exp.getJavaNode()
      if (jvNode.isInstanceOf[CastExpression] || jvNode.isInstanceOf[BooleanLiteral] || jvNode.isInstanceOf[ConstructorInvocation]
      || jvNode.isInstanceOf[SuperConstructorInvocation])
        return false
    }
    return true
  }
}
case class ExpClassCastDeletion(/*exp: Identifier[Any]*/) extends ExpressionMutActions{
  def mutateExp(exp: Identifier[Any]): (Identifier[Any],Identifier[Any]) ={
    println("Exp Cast deletion!")
    if(exp.transformToJavaNode()){
      val jvNode = exp.getJavaNode()
      if(jvNode.isInstanceOf[CastExpression]){
        val castExp = jvNode.asInstanceOf[CastExpression]
        val desireNode = ASTNode.copySubtree(castExp.getAST, castExp.getExpression)// take only the expression, not the type
        println("Deleted cast: "+desireNode)
        return(exp,new JavaNodeIden(exp.getFileName(), desireNode,exp.getProb()))
      }else{
        return (exp, null)
      }
    }else{
      throw new RuntimeException("Transform to java node not successful: " + exp)
    }
  }

  def isAppropriateAction(exp: Identifier[Any]): Boolean={
    if(exp.transformToJavaNode()){
      if(exp.getJavaNode().isInstanceOf[CastExpression])
        return true
    }
    return false
  }
}
case class ExpBooleanNegation() extends ExpressionMutActions{
  def mutateExp(exp: Identifier[Any]): (Identifier[Any],Identifier[Any]) ={
    if(exp.transformToJavaNode()) {
      val jvNode = exp.getJavaNode()
      if (jvNode.isInstanceOf[BooleanLiteral]) {
        val bl = jvNode.asInstanceOf[BooleanLiteral]
        val value = bl.booleanValue()
        val desireNode = ASTNode.copySubtree(jvNode.getAST, bl)
        desireNode.asInstanceOf[BooleanLiteral].setBooleanValue(!value)
        return (exp, new JavaNodeIden(exp.getFileName(), desireNode, exp.getProb()))
      }else{
        return (exp, null)
      }
    }else{
      throw new RuntimeException("Transform to java node not successful: " + exp)
    }
  }

  def isAppropriateAction(exp: Identifier[Any]): Boolean={
    if(exp.transformToJavaNode()){
      if(exp.getJavaNode().isInstanceOf[BooleanLiteral])
        return true
    }
    return false
  }
}

case class ExpMethodCallInvokerReplacement(rng: Random) extends ExpressionMutActions{
  //TODO: to move the code of method and field collector to statement collector so that we dont need to traverse the compilation unit many times.
  // al so for consistency of fix strategy (Local file of current node, etc)
  private val logger = Logger.getLogger(classOf[ExpMethodCallInvokerReplacement])

  def mutateExp(exp: Identifier[Any]): (Identifier[Any],Identifier[Any]) ={// note: the java node of exp is already an expression not expression statement
    val choosenInvoker = random(exp.getPossibleInvokers(), rng)
    val desireNode = ASTNode.copySubtree(exp.getJavaNode().getAST, exp.getJavaNode())
    try {
      desireNode.asInstanceOf[MethodInvocation].setExpression(ASTNode.copySubtree(desireNode.getAST,choosenInvoker).asInstanceOf[Expression])
    }catch{
      case e: Throwable => {
        e.printStackTrace()
        return (exp, null)
      }
    }
    logger.debug("desire node:"+desireNode)
    return (exp,new JavaNodeIden(exp.getFileName(), desireNode, exp.getProb()))
  }

  def isAppropriateAction(exp: Identifier[Any]): Boolean={
    if(exp.transformToJavaNode()){
      if(exp.getJavaNode().isInstanceOf[MethodInvocation]){
        logger.debug("Possible Invk: "+exp.getPossibleInvokers())
        if(exp.getPossibleInvokers()!=null)
          if(!exp.getPossibleInvokers().isEmpty)
           return true
      }
    }
    return false
  }
}
case class ExpMethodCallNameReplacement(rng: Random) extends ExpressionMutActions {
  private val logger = Logger.getLogger(classOf[ExpMethodCallParameterReplacement])

  override def mutateExp(exp: Identifier[Any]): (Identifier[Any], Identifier[Any]) = {
    val chosenMethodName = random(exp.getPossibleMethodCallRep(), rng)
    val desireNode = ASTNode.copySubtree(exp.getJavaNode().getAST, exp.getJavaNode())
    desireNode.asInstanceOf[MethodInvocation].getName.setIdentifier(chosenMethodName.getIdentifier)
    return (exp,new JavaNodeIden(exp.getFileName(), desireNode, exp.getProb()))
  }

  override def isAppropriateAction(exp: Identifier[Any]): Boolean = {
    if(exp.transformToJavaNode()){
      if(exp.getJavaNode().isInstanceOf[MethodInvocation]){
        if(exp.getPossibleMethodCallRep()!=null)
          if(!exp.getPossibleMethodCallRep().isEmpty)
            return true
      }
    }
    return false
  }
}
case class ExpMethodCallParameterReplacement(rng: Random) extends ExpressionMutActions{
  //TODO: to move the code of method and field collector to statement collector so that we dont need to traverse the compilation unit many times.
  // al so for consistency of fix strategy (Local file of current node, etc)
  private val logger = Logger.getLogger(classOf[ExpMethodCallParameterReplacement])

  def mutateExp(exp: Identifier[Any]): (Identifier[Any],Identifier[Any]) ={// note: the java node of exp is already an expression not expression statement
    val expMethodCallArgs = extractArgs(exp)
    import scala.collection.JavaConversions._
    logger.debug("ARGs: "+expMethodCallArgs)
    /*val methodArguments = exp.getScope().foldLeft(new ArrayBuffer[String]){
      (res, arg) => {res.append(arg._1); res}
    }*/
    if(exp.getLine() == 95)
      println("DB")
    val chosenArgument = random(expMethodCallArgs, rng)
    //logger.debug("Args: "+methodArguments)
    logger.debug("Chosen arg: "+chosenArgument + " " +chosenArgument.getClass)

    try {
      //chosenArgument.asInstanceOf[ASTNode]
      if(chosenArgument.isInstanceOf[SimpleName]) {
        val chosenArgSimpleName = chosenArgument.asInstanceOf[SimpleName]
        val possibleReplacement = getPossibleVarRepByScope(exp, chosenArgSimpleName.resolveTypeBinding(), chosenArgSimpleName.getIdentifier )
        logger.debug("Possible rep:" + possibleReplacement)
        val toRepVar = random(possibleReplacement, rng)
        logger.debug("Chosen torep: " + toRepVar)
        val desireNode = ASTNode.copySubtree(chosenArgSimpleName.getAST, chosenArgSimpleName)
        desireNode.asInstanceOf[SimpleName].setIdentifier(toRepVar)
        logger.debug("desire node:" + desireNode)
        return (new JavaNodeIden(exp.getFileName(), chosenArgSimpleName, exp.getProb()), new JavaNodeIden(exp.getFileName(), desireNode, exp.getProb()))
      }else if(chosenArgument.isInstanceOf[QualifiedName]){
        val chosenArgQualifiedName = chosenArgument.asInstanceOf[QualifiedName]
        val possibleRepLocal = getPossibleVarRepByLocalFile(chosenArgQualifiedName)//getPossibleVarRepByFieldsInSameClass(chosenArgQualifiedName)
        //TODO: newly added, check if this affect other fixed bugs
        if(possibleRepLocal.isEmpty) // safer do this to avoid effect on other fixed bugs
        {
          val possibleRep=getPossibleVarRepByScope(exp,chosenArgQualifiedName.resolveTypeBinding(),chosenArgQualifiedName.getFullyQualifiedName)
          logger.debug("Q1 Possible rep:" + possibleRep)
          val toRepVarName = random(possibleRep, rng)
          logger.debug("Q1 Chosen torep: " + toRepVarName)
          val desireNode = ASTNode.copySubtree(chosenArgQualifiedName.getName.getAST, chosenArgQualifiedName.getName)
          logger.debug("Q1 desire node:" + desireNode)
          desireNode.asInstanceOf[SimpleName].setIdentifier(toRepVarName)
          return (new JavaNodeIden(exp.getFileName(), chosenArgQualifiedName, exp.getProb()), new JavaNodeIden(exp.getFileName(), desireNode, exp.getProb()))

        }else {
          logger.debug("Q Possible rep:" + possibleRepLocal)
          val toRepVarName = random(possibleRepLocal, rng)
          logger.debug("Q Chosen torep: " + toRepVarName)
          val desireNode = ASTNode.copySubtree(chosenArgQualifiedName.getAST, chosenArgQualifiedName)
          logger.debug("Q desire node:" + desireNode)
          desireNode.asInstanceOf[QualifiedName].getName.setIdentifier(toRepVarName)
          return (new JavaNodeIden(exp.getFileName(), chosenArgQualifiedName, exp.getProb()), new JavaNodeIden(exp.getFileName(), desireNode, exp.getProb()))
        }
      }
      return (null,null)
    }catch{
      case e: Throwable => {
        e.printStackTrace()
        return (exp, null)
      }
    }
  }

  /*
  This cannot be used yet because dont know why the chosenBinding is null
  * */
  def getPossibleVarRepByFieldsInSameClass(chosenArgQualifiedName: QualifiedName): mutable.Seq[IVariableBinding] ={
    val chosenBinding = chosenArgQualifiedName.resolveTypeBinding()//chosenArgQualifiedName.resolveTypeBinding()
    logger.debug(chosenBinding)
    val fields=chosenBinding.getDeclaredFields
    val filteredFields=fields.filter(fi => !fi.getName.equals(chosenArgQualifiedName.getName))
    return filteredFields.filter(fi => ASTUtils.compatibleTypes(fi.getType, chosenBinding)).toBuffer
  }

  def getPossibleVarRepByLocalFile(chosenArgQualifiedName: QualifiedName): Seq[String] ={
    val possibleRepName = new mutable.HashSet[String]()
    chosenArgQualifiedName.getRoot.accept(new ASTVisitor() {
      override def visit(node: QualifiedName): Boolean ={
        if(node.getQualifier.getFullyQualifiedName.equals(chosenArgQualifiedName.getQualifier.getFullyQualifiedName)
        && !node.getName.getIdentifier.equals(chosenArgQualifiedName.getName.getIdentifier)){
          possibleRepName.add(node.getName.getIdentifier)
        }
        return false
      }
    })
    return possibleRepName.toBuffer
  }

  //TODO: sometimes the chosenArgumentBinding is null, for now, we just exhaustively add
  def getPossibleVarRepByScope(exp: Identifier[Any],chosenArgumentBinding: ITypeBinding, chosenArgumentName: String): ArrayBuffer[String] ={
    val possibleReplacement = new mutable.TreeSet[String]()
    if(exp.getScope() !=null)
      for(vars <- exp.getScope()){
        for(types <- vars._2){
          if(chosenArgumentBinding == null)
            possibleReplacement.add(vars._1)
          else if(ASTUtils.compatibleTypes(types, chosenArgumentBinding) && (vars._1.compareTo(chosenArgumentName)!=0))
            possibleReplacement.add(vars._1)

        }
      }
    if(exp.getCurrentMethodVars()!=null)
      for(vars <- exp.getCurrentMethodVars()){
        for(types <- vars._2){
          if(chosenArgumentBinding == null)
            possibleReplacement.add(vars._1)
          else
          if(ASTUtils.compatibleTypes(types, chosenArgumentBinding) && (vars._1.compareTo(chosenArgumentName)!=0))
            possibleReplacement.add(vars._1)
        }
      }
    if(exp.getFieldVars() != null)
    for(vars <- exp.getFieldVars()){
      for(types <- vars._2){
        if(chosenArgumentBinding == null)
          possibleReplacement.add(vars._1)
        else
        if(ASTUtils.compatibleTypes(types, chosenArgumentBinding) && (vars._1.compareTo(chosenArgumentName)!=0))
          possibleReplacement.add(vars._1)
      }
    }
    if(exp.getForLoopVars()!=null)
    for(vars <- exp.getForLoopVars()){
      for(types <- vars._2){
        if(chosenArgumentBinding == null)
          possibleReplacement.add(vars._1)
        else
        if(ASTUtils.compatibleTypes(types, chosenArgumentBinding) && (vars._1.compareTo(chosenArgumentName)!=0))
          possibleReplacement.add(vars._1)
      }
    }
    return possibleReplacement.foldLeft(new ArrayBuffer[String]){
      (res, rep) =>{
        res.append(rep)
        res
      }
    }
  }

  private def extractArgs(exp: Identifier[Any]): mutable.Seq[ASTNode] ={
    import scala.collection.JavaConversions._
    if(exp.getJavaNode().isInstanceOf[MethodInvocation]) {
      if (exp.getJavaNode().asInstanceOf[MethodInvocation].arguments().size() > 0) {
        return exp.getJavaNode().asInstanceOf[MethodInvocation].arguments().map( vi => vi.asInstanceOf[ASTNode])
        //return exp.getJavaNode().asInstanceOf[MethodInvocation].arguments()
      }
    }
    else if(exp.getJavaNode().isInstanceOf[ConstructorInvocation]) {
        if (exp.getJavaNode().asInstanceOf[ConstructorInvocation].arguments().size() > 0)
           return exp.getJavaNode().asInstanceOf[ConstructorInvocation].arguments().map( vi => vi.asInstanceOf[ASTNode])
    }
    else {
        //if(exp.getJavaNode().isInstanceOf[ConstructorInvocation])
        if (exp.getJavaNode().asInstanceOf[SuperConstructorInvocation].arguments().size() > 0)
           return exp.getJavaNode().asInstanceOf[SuperConstructorInvocation].arguments().map( vi => vi.asInstanceOf[ASTNode])
    }
    return null
  }

  def isAppropriateAction(exp: Identifier[Any]): Boolean={
    if(exp.transformToJavaNode()){
      if(exp.getJavaNode().isInstanceOf[MethodInvocation] || exp.getJavaNode().isInstanceOf[ConstructorInvocation]
      || exp.getJavaNode().isInstanceOf[SuperConstructorInvocation]){
        if(exp.getScope()!=null)
          if(!exp.getScope().isEmpty && extractArgs(exp)!= null) {
            return true
          }
      }
      /*if(exp.getJavaNode().isInstanceOf[ExpressionStatement]){
        val temp=exp.getJavaNode().asInstanceOf[ExpressionStatement]
        if(temp.getExpression.isInstanceOf[MethodInvocation])
      }*/
    }
    return false
  }
}
case class ExpInfixDeletion() extends ExpressionMutActions{
  override def mutateExp(exp: Identifier[Any]): (Identifier[Any], Identifier[Any]) = {
    println("Trying delete: "+exp.getLine()+" "+exp)
    val jvNode=exp.getJavaNode()
    val chosenParentJvNode = jvNode.getParent.asInstanceOf[InfixExpression]
    val desireNode = if(ASTUtils.sameASTNode(chosenParentJvNode.getLeftOperand, jvNode)){
      val rightOP = chosenParentJvNode.getRightOperand
      if(!chosenParentJvNode.hasExtendedOperands||chosenParentJvNode.extendedOperands().size()==0)
        ASTNode.copySubtree(rightOP.getAST, rightOP)
      else{
        val parentCopy = ASTNode.copySubtree(chosenParentJvNode.getAST, chosenParentJvNode).asInstanceOf[InfixExpression]
        val toBeRightOp=ASTNode.copySubtree(parentCopy.getRightOperand.getAST,parentCopy.extendedOperands().get(0).asInstanceOf[Expression])
        parentCopy.setLeftOperand(ASTNode.copySubtree(parentCopy.getLeftOperand.getAST,parentCopy.getRightOperand).asInstanceOf[Expression])
        parentCopy.setRightOperand(toBeRightOp.asInstanceOf[Expression])
        parentCopy.extendedOperands().remove(0)
        parentCopy
      }
    }else if(ASTUtils.sameASTNode(chosenParentJvNode.getRightOperand, jvNode)) {
      val leftOP = chosenParentJvNode.getLeftOperand
      if(!chosenParentJvNode.hasExtendedOperands||chosenParentJvNode.extendedOperands().size()==0)
        ASTNode.copySubtree(leftOP.getAST, leftOP)
      else{
        val parentCopy = ASTNode.copySubtree(chosenParentJvNode.getAST, chosenParentJvNode).asInstanceOf[InfixExpression]
        val toBeRightOp=ASTNode.copySubtree(parentCopy.getRightOperand.getAST,parentCopy.extendedOperands().get(0).asInstanceOf[Expression])
        parentCopy.setRightOperand(toBeRightOp.asInstanceOf[Expression])
        parentCopy.extendedOperands().remove(0)
        parentCopy
      }
    }else{//extended operands
      val parentCopy = ASTNode.copySubtree(chosenParentJvNode.getAST, chosenParentJvNode).asInstanceOf[InfixExpression]
      parentCopy.extendedOperands().remove(jvNode)
      parentCopy
    }
    //if(desireNode == null)
    //  throw new RuntimeException("Impossible in expInfixDeletion! "+"Parent" +chosenParentJvNode + "Left: "+chosenParentJvNode.getLeftOperand +" Right: "+chosenParentJvNode.getRightOperand)
    return (new JavaNodeIden(exp.getFileName(), jvNode.getParent, exp.getProb()),new JavaNodeIden(exp.getFileName(),desireNode, exp.getProb()))

    /*val chosenParentNode = jvNode.getParent.asInstanceOf[InfixExpression]
    val desireNode = if(chosenParentNode.getOperator == InfixExpression.Operator.CONDITIONAL_AND){
      val repTrue = jvNode.getAST.newBooleanLiteral(true)
      repTrue
    }else if(chosenParentNode.getOperator == InfixExpression.Operator.CONDITIONAL_OR){
      val repFalse = jvNode.getAST.newBooleanLiteral(false)
      repFalse
    }else{
      throw  new RuntimeException("We do not handle operator: "+chosenParentNode.getOperator)
    }
    return (new JavaNodeIden(exp.getFileName(), jvNode, exp.getProb()),new JavaNodeIden(exp.getFileName(),desireNode, exp.getProb()))
    */
  }

  override def isAppropriateAction(exp: Identifier[Any]): Boolean = {
    if(exp.transformToJavaNode()){
      if(exp.getJavaNode().getParent.isInstanceOf[InfixExpression]){
        if(exp.getJavaNode().getParent.asInstanceOf[InfixExpression].getOperator == InfixExpression.Operator.CONDITIONAL_AND
        || exp.getJavaNode().getParent.asInstanceOf[InfixExpression].getOperator == InfixExpression.Operator.CONDITIONAL_OR)
        return true
      }
    }
    return false
  }
}
case class ExpReplacer(parentOfExp: Identifier[Any], rng: Random) extends ExpressionMutActions{
  var possibleRepExp: Seq[Identifier[Any]] = null

  override def mutateExp(exp: Identifier[Any]): (Identifier[Any], Identifier[Any]) = {
    assert(possibleRepExp != null && !possibleRepExp.isEmpty)
    val selectedExp=SelectionScheme.random(possibleRepExp, rng)
    val infixExp=exp.getJavaNode().getAST.newInfixExpression()
    infixExp.setRightOperand(ASTNode.copySubtree(exp.getJavaNode().getAST, exp.getJavaNode()).asInstanceOf[Expression])
    if (rng.nextBoolean())
      infixExp.setOperator(InfixExpression.Operator.CONDITIONAL_AND)
    else
      infixExp.setOperator(InfixExpression.Operator.CONDITIONAL_OR)
    infixExp.setLeftOperand(ASTNode.copySubtree(selectedExp.getJavaNode().getAST, selectedExp.getJavaNode()).asInstanceOf[Expression])
    return (exp, new JavaNodeIden(exp.getFileName(), infixExp, exp.getProb()))
  }

  override def isAppropriateAction(exp: Identifier[Any]): Boolean = {
    if(parentOfExp.getJavaNode().isInstanceOf[IfStatement]){
      if(exp.getLine()==135){
        println("DB")
      }
      val cond=BooleanConditionSynthesizer.expFilterByScope(exp)
      if(cond != null){
        if(!cond.isEmpty) {
          possibleRepExp = cond
          return true
        }else
          return false
      }else{
        return false
      }
    }else
      return false
  }
}
case class ExpAdder(parentOfExp: Identifier[Any], rng: Random) extends ExpressionMutActions{
  var condToAdd: Identifier[Any] = null
  override def mutateExp(exp: Identifier[Any]): (Identifier[Any], Identifier[Any]) = {
    assert(condToAdd != null)
    println(condToAdd)
    println(exp.getJavaNode())
    try {
      val parentThesisExp = exp.getJavaNode().getAST.newParenthesizedExpression()
      val parentThesisExp2 = exp.getJavaNode().getAST.newParenthesizedExpression()
      val infixExp = exp.getJavaNode().getAST.newInfixExpression()
      val expCopy = ASTNode.copySubtree(exp.getJavaNode().getAST, exp.getJavaNode())
      infixExp.setLeftOperand(expCopy.asInstanceOf[Expression])
      val condJavaNode = condToAdd.getJavaNode()
      if(condJavaNode == null || ASTNode.copySubtree(condJavaNode.getAST, condJavaNode) == null){
        println("FK debug here")
      }
      parentThesisExp2.setExpression(ASTNode.copySubtree(condJavaNode.getAST, condJavaNode).asInstanceOf[Expression])
      infixExp.setRightOperand(parentThesisExp2)
      if (rng.nextBoolean())
        infixExp.setOperator(InfixExpression.Operator.CONDITIONAL_AND)
      else
        infixExp.setOperator(InfixExpression.Operator.CONDITIONAL_OR)
      parentThesisExp.setExpression(infixExp)

      return (exp, new JavaNodeIden(exp.getFileName(), parentThesisExp, exp.getProb()))
    }catch {
      case e: Throwable => {
        println(e.getStackTrace)
        throw new RuntimeException("Why error here?")
      }
    }
    //val fullCondition = ASTNode.copySubtree(exp.getJavaNode().getAST, exp.getJavaNode())
    //fullCondition.
  }

  override def isAppropriateAction(exp: Identifier[Any]): Boolean = {
    if(parentOfExp.getJavaNode().isInstanceOf[IfStatement]){
      //Currently, we only handle method call with boolean return type
      //if(exp.getLine() == 255){
      //  println("DB here")
      //}

      if(exp.getJavaNode().isInstanceOf[MethodInvocation]) {
        val binding = exp.getJavaNode().asInstanceOf[MethodInvocation].resolveMethodBinding()
        if(binding == null)
          return false

        val expRetType=binding.getReturnType
        if(!(expRetType.toString.compareTo(PrimitiveType.BOOLEAN.toString) == 0))
          return false
      }

      val variablesInIfExp = ASTUtils.getVariableUsed(parentOfExp.getJavaNode().asInstanceOf[IfStatement].getExpression)
      if(variablesInIfExp.isEmpty)
        return false

      val cond=BooleanConditionSynthesizer.synthesizeCondIngredientForIfStmt(parentOfExp, variablesInIfExp, exp.getJavaNode().getAST, rng)
      if(cond != null) {
        condToAdd = cond
        return true
      }
      else return false
    }else
      return false
  }
}

/*
object ExpressionMutActions{
  private val logger = Logger.getLogger(classOf[ExpressionMutActions])
  def random[T](pick: Seq[T], rng: Random): T = return pick(rng.nextInt(pick.size))

  def chooseAppropriateExpMutAction(exp: Identifier[Any], parentExp: Identifier[Any], rng: Random): ExpressionMutActions = {
    val possibleExpMutActions = Array[ExpressionMutActions](new ExpInfixDeletion,new ExpClassCastDeletion, new ExpClassCastInsertion(rng), new ExpBooleanNegation,
      new ExpOperatorMut(rng), new ExpMethodCallInvokerReplacement(rng), new ExpMethodCallParameterReplacement(rng),
      new ExpMethodCallNameReplacement(rng), new ExpAdder(parentExp,rng), new ExpReplacer(parentExp, rng))

    if(exp.transformToJavaNode()) {
      val jvNode = exp.getJavaNode()
      val possibleActions = new ArrayBuffer[ExpressionMutActions]()
      for(action <- possibleExpMutActions){
        if(action.isAppropriateAction(exp)){
          possibleActions.append(action)
        }
      }
      logger.debug("Possible ExpMut Actions: "+possibleActions)
      return random(possibleActions, rng)
    }else{
      throw new RuntimeException("Transform to java node not successful: " + exp)
    }
  }
}
*/