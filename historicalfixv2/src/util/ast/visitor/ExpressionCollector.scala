package util.ast.visitor

import org.eclipse.jdt.core.dom._
import util.ast.ASTUtils

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 8/2/15.
 */
class ExpressionCollector extends ASTVisitor{
  var collectedExps: Seq[ASTNode] = new ArrayBuffer[ASTNode]()

  override def preVisit2(aSTNode: ASTNode): Boolean={
    val expVisitor = new ExpressionCollector
    if(aSTNode.isInstanceOf[IfStatement]){
      aSTNode.asInstanceOf[IfStatement].getExpression.accept(expVisitor)
      collectedExps = expVisitor.collectedExps
      return false
    }else if(aSTNode.isInstanceOf[WhileStatement]){
      aSTNode.asInstanceOf[WhileStatement].getExpression.accept(expVisitor)
      collectedExps = expVisitor.collectedExps
      return false
    }else {
      //collectedExps = expVisitor.collectedExps
      return true
    }
  }

  override def visit(aSTNode: InfixExpression): Boolean ={
    /*if(ASTUtils.getStatementLineNo(aSTNode)==1288){// Debugging purpose
      println(ASTNode.nodeClassForType(aSTNode.getLeftOperand.getNodeType))
      println(ASTNode.nodeClassForType(aSTNode.getRightOperand.getNodeType))
    }*/
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }

  override def visit(aSTNode: PrefixExpression): Boolean ={
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }

  override def visit(aSTNode: PostfixExpression): Boolean ={
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }

  override def visit(aSTNode: MethodInvocation): Boolean ={
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }

  override def visit(aSTNode: ConstructorInvocation): Boolean ={
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }

  override def visit(aSTNode: SuperConstructorInvocation): Boolean ={
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }

  override def visit(aSTNode: BooleanLiteral): Boolean={
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }

  override def visit(aSTNode: FieldAccess): Boolean={
    collectedExps=collectedExps.:+(aSTNode)
    return false
  }

  override def visit(aSTNode: QualifiedName): Boolean={
    if(aSTNode.getParent.isInstanceOf[InfixExpression])
      collectedExps=collectedExps.:+(aSTNode)
    return false
  }

  override def visit(aSTNode: ParenthesizedExpression): Boolean={
    collectedExps=collectedExps.:+(aSTNode)
    return true
  }
}
