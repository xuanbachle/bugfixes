package util.ast.visitor

import localizations.Identifier
import org.eclipse.jdt.core.dom._
import util.ast.ASTUtils
import org.apache.log4j.Logger

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 8/22/15.
 */
class MethodCallInforCollector(currentBuggyNode: Identifier[Any]) extends ASTVisitor{
  private val logger = Logger.getLogger(classOf[MethodCallInforCollector])

  val possibleInvokers = new ArrayBuffer[Expression]()
  val possibleMethodCallRep = new ArrayBuffer[SimpleName]()
  var buggyNodeInvokerType: ITypeBinding = null
  var buggyNodeMethodName: SimpleName = null
  var buggyNodeMethodCallType: ITypeBinding = null
  var buggyNodeTypeBinding: IMethodBinding = null
  var mc: MethodInvocation = null

  private def resolveTypeOfInvoker(): MethodInvocation ={
    if(mc == null) {
      if (currentBuggyNode.transformToJavaNode()) {
        val jvNode = currentBuggyNode.getJavaNode()
        //println(ASTNode.nodeClassForType(jvNode.getNodeType))
        if(jvNode.isInstanceOf[ExpressionStatement])
          mc = jvNode.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[MethodInvocation]
        if (jvNode.isInstanceOf[MethodInvocation]) {
          mc = jvNode.asInstanceOf[MethodInvocation]
        }
        if(mc.getExpression() != null)
         buggyNodeInvokerType = mc.getExpression.resolveTypeBinding()
        if(mc.getName() != null) {
          buggyNodeMethodName = mc.getName
          buggyNodeMethodCallType=buggyNodeMethodName.resolveTypeBinding()
        }
        this.buggyNodeTypeBinding = mc.resolveMethodBinding()
        return mc
        //println("Resolved: "+buggyNodeInvokerType)
      }
    }
    return mc
    //throw new RuntimeException("Not a valid method invocation mutation!")
  }

  override def visit (node : FieldDeclaration) : Boolean = {
    import scala.collection.JavaConversions._
    val mc = resolveTypeOfInvoker()
    for (o <- node.fragments) {
      if (o.isInstanceOf[VariableDeclarationFragment]) {
        val v: VariableDeclarationFragment = o.asInstanceOf[VariableDeclarationFragment]
        if(compatibleTypes(v.resolveBinding().getType,buggyNodeInvokerType) && !v.getName.getIdentifier.equals(mc.getExpression.toString))
          this.possibleInvokers.append(v.getName)
      }
    }
    return super.visit(node)
  }

  override def visit (node : MethodDeclaration) : Boolean = {
    val nodeLine=ASTUtils.getStatementLineNo(node)
    val nodeEndline=ASTUtils.getStatementEndLineNo(node)
    resolveTypeOfInvoker()
    val nodeBinding = node.resolveBinding()
    val nodeType=nodeBinding.getReturnType
    if(!(nodeLine < currentBuggyNode.getLine() && nodeEndline >= currentBuggyNode.getLine())) {
      if (compatibleTypes(nodeType, buggyNodeInvokerType)) {
        val mc = currentBuggyNode.getJavaNode().getAST.newMethodInvocation()
        mc.setName(currentBuggyNode.getJavaNode().getAST.newSimpleName(node.getName.getIdentifier))
        possibleInvokers.append(mc)
      }

      if(compatibleTypes(buggyNodeMethodCallType,nodeType)) {
        if(node.parameters().size() == mc.arguments().size()){
          // check same args
          if(ASTUtils.getStatementLineNo(mc)==1417)
            println("Debug here")
          logger.debug(buggyNodeTypeBinding.getParameterTypes.size)
          buggyNodeTypeBinding.getParameterTypes.map(t => logger.debug(t.toString))
          nodeBinding.getParameterTypes.map(t => logger.debug(t.toString))
          if((buggyNodeTypeBinding.getParameterTypes,nodeBinding.getParameterTypes).zipped.exists{
            (nodeArg, buggyNodeArg) =>
              !ASTUtils.strictlyCompatibleTypes(nodeArg, buggyNodeArg)
          })
          {

          } else
            possibleMethodCallRep.append(node.getName)
        }
      }

    }
    return true
    //node.getAST.newEx
    //val mc= node.getAST.newMethodInvocation()
    //mc.setName(null)
    //mc.setExpression()

  }

  private def compatibleTypes(t1: ITypeBinding, t2: ITypeBinding): Boolean ={
    if(t1 == null || t2 == null)
      return false

    if (t1.isSubTypeCompatible(t2) || t1.isCastCompatible(t2)
      || t1.isAssignmentCompatible(t2) || t1.toString.equals(t2.toString)) {
      return true
    }else
      return false
  }
}

