package parsers.javatypeinfor

import org.eclipse.jdt.core.dom._
import repair.mutationoperators.ExpOperatorMut
import util.ast.ASTUtils
import scala.collection.JavaConversions._

/**
 * Created by larcuser on 16/10/15.
 */
class JavaClassTypeInforVisitor extends ASTVisitor{
  private val javaClsTypeInfor = new JavaClassTypeInfor

  private def isBooleanReturnTypeExp(node: ASTNode): Boolean  ={
    val parent = node.getParent
    if(parent.isInstanceOf[IfStatement] || parent.isInstanceOf[WhileStatement]){
      return true
    }else if(parent.isInstanceOf[InfixExpression]){
      return ExpOperatorMut.booleanOps.contains(parent.asInstanceOf[InfixExpression].getOperator)
    }else
      return false
  }

  override def visit (node : MethodDeclaration) : Boolean = {
    val methodCallName = node.getName.getIdentifier
    val binding = node.resolveBinding()
    binding.getParameterTypes.map(arg => {
      val usage = new JavaClassMethodCallParamUsage(JavaClassTypeUsage.nullInvoker,methodCallName, binding.getReturnType, binding.getParameterTypes)
      if(usage.addToUsageContext(node)) {
        javaClsTypeInfor.addUsageOfAType(arg.toString, usage)
      }
    })
    true
  }

  override def visit(aSTNode: MethodInvocation): Boolean = {
    val binding = aSTNode.resolveMethodBinding()
    //if(ASTUtils.getStatementLineNo(aSTNode) == 243){
    //  println("DB here")
    //}
    val invokerType = if(aSTNode.getExpression != null) {
      val binding=aSTNode.getExpression.resolveTypeBinding()
      if(binding != null)
        binding.toString
      else JavaClassTypeUsage.nullInvoker
    }
    else JavaClassTypeUsage.nullInvoker

    val methodCallName = aSTNode.getName.getIdentifier
    if(aSTNode.arguments() != null) {
      aSTNode.arguments().map(arg => {
        val argBinding = arg.asInstanceOf[Expression].resolveTypeBinding()
        val usage = new JavaClassMethodCallParamUsage(invokerType, methodCallName, binding.getReturnType, binding.getParameterTypes)
        if (usage.addToUsageContext(aSTNode))
          javaClsTypeInfor.addUsageOfAType(argBinding.toString, usage)
      })
    }
    if(invokerType.compareTo(JavaClassTypeUsage.nullInvoker) != 0) {
      val useAsInvoker = new JavaClassMethodCallInvokerUsage(methodCallName, binding.getReturnType, binding.getParameterTypes)
      if(useAsInvoker.addToUsageContext(aSTNode))
        javaClsTypeInfor.addUsageOfAType(invokerType, useAsInvoker)
    }

    false
  }

  def getCollectedClassTypeInfor() = javaClsTypeInfor

}
