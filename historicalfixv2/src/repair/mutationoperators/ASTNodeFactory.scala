package repair.mutationoperators

import org.eclipse.jdt.core.dom.{InfixExpression, AST, ASTNode}

import scala.collection.mutable

/**
 * Created by dxble on 8/3/15.
 */
object ASTNodeFactory {
  var map: mutable.HashMap[ASTNode, mutable.HashMap[InfixExpression.Operator, ASTNode]] = new mutable.HashMap[ASTNode,mutable.HashMap[InfixExpression.Operator, ASTNode]]()
  def deepCopyWithCaching(ast: AST, operator: InfixExpression.Operator, jvNode: ASTNode): ASTNode ={
    if(!map.contains(jvNode)){
      val internalMap = new mutable.HashMap[InfixExpression.Operator,ASTNode]
      map += jvNode -> internalMap
    }
    if(!map.get(jvNode).contains(operator)){
      val copied=ASTNode.copySubtree(ast, jvNode)
      map.get(jvNode) match {
        case None => throw new RuntimeException("Impossible! ")
        case Some(v) => v += (operator -> copied)
      }
    }
    val value= map.get(jvNode)
    value match {
      case None => throw new RuntimeException("Impossible! ")
      case Some (v) => v.get(operator)
        match{
          case None => throw new RuntimeException("Impossible! ")
          case Some (y) => y
        }
    }
  }
}
