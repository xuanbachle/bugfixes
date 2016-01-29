package parsers.javatypeinfor

import java.util.Random

import org.eclipse.jdt.core.dom._
import parsers.AbstractClassTypeUsage
import repair.geneticprogramming.selectionschemes.SelectionScheme
import repair.mutationoperators.ExpOperatorMut
import util.ast.ASTUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by larcuser on 16/10/15.
 */
abstract class JavaClassTypeUsage() extends AbstractClassTypeUsage[String,ASTNode]{
  def statementKind(stmt: ASTNode) : String = ASTNode.nodeClassForType(stmt.getNodeType).toString
  override def equals(obj: Any): Boolean ={
    return obj.isInstanceOf[JavaClassTypeUsage]
  }

  override def getUsageContext() = this.usageContext
  //This default context check is used for method call, focusing on boolean condition, e.g., Obj.getA() > x
  override def extractRealContext(stmt: ASTNode): ASTNode={
    //println(stmt)
    //println(stmt.getParent)
    val parent = stmt.getParent
    if(parent.isInstanceOf[InfixExpression]){
      val infixExp = parent.asInstanceOf[InfixExpression]
      if(ExpOperatorMut.booleanOps.contains(infixExp.getOperator))
        return stmt
      else
        extractRealContext(infixExp)
        //return null// not appropriate context
    }else if(parent.isInstanceOf[PrefixExpression] || parent.isInstanceOf[PostfixExpression]){
      return extractRealContext(parent)
    }else if(getReturnType().compareTo(PrimitiveType.BOOLEAN.toString) == 0 || stmt.isInstanceOf[InfixExpression]) {
      return stmt
    }else
      return null
  }
}
case class JavaClassMethodCallInvokerUsage(methodName: String, returnType: ITypeBinding, paramTypes: Array[ITypeBinding]) extends JavaClassTypeUsage{
  //method calls from this type and the frequency of using the method call, key is method name
  //protected val methodCalls = new mutable.HashMap[String, Int]()

  override def getReturnType() = returnType.toString

  override def equals(obj: Any): Boolean ={
    if(obj.isInstanceOf[JavaClassMethodCallInvokerUsage]){
      val castedUsage = obj.asInstanceOf[JavaClassMethodCallInvokerUsage]
      return castedUsage.methodName.compareTo(this.methodName) == 0
    }else
      return false
  }

  override def hashCode(): Int = 1

}
case class JavaClassMethodCallParamUsage(invokerType: String, methodName: String, returnType: ITypeBinding, paramTypes: Array[ITypeBinding]) extends JavaClassTypeUsage{
  //method where variable of this type is used as input parameter, key is methodname, value is set of types of invoker
  //protected val usedInMethods = new mutable.HashMap[String, mutable.HashSet[String]]()

  override def getReturnType() = returnType.toString

  override def equals(obj: Any): Boolean ={
    if(obj.isInstanceOf[JavaClassMethodCallParamUsage]){
      val castedUsage = obj.asInstanceOf[JavaClassMethodCallParamUsage]
      return castedUsage.invokerType.compareTo(invokerType) == 0 &&
        castedUsage.methodName.compareTo(methodName) == 0
    }else
      return false
  }

  override def hashCode(): Int = 2
}

object JavaClassTypeUsage{
  val nullInvoker = "NULL_INVK"

  //Wrap a method call that has boolean return type with negation, e.g., P.isDouble() wrapped to !P.isDouble()
  private def negateMethodCallRetBool(methodCall: MethodInvocation, attachedAST: AST): PrefixExpression={
    val prefixExp = attachedAST.newPrefixExpression()
    prefixExp.setOperand(methodCall)
    prefixExp.setOperator(PrefixExpression.Operator.NOT)
    return prefixExp
  }

  def synthesizeMethodCallParams(signature: Array[ITypeBinding], possibleInstances: mutable.HashMap[String, ArrayBuffer[ITypeBinding]], rng: Random): ArrayBuffer[String]={
    val pool = new ArrayBuffer[String]()// chosen instances
    signature.map(paramType =>{
      val instancesSameParamType =
        possibleInstances.filter(
          instance => instance._2.exists(itype => ASTUtils.strictlyCompatibleTypes(itype, paramType))
      ).foldLeft(new ArrayBuffer[String])((res,instance) => {res.append(instance._1); res})
      if(instancesSameParamType.isEmpty)
        return null
      else
        pool.append(SelectionScheme.random(instancesSameParamType, rng))
    })
    return pool
  }

  def instantiateAnUsage(randomlyPickedVar: String,associatedVarType: ITypeBinding,
                         typeUsage: AbstractClassTypeUsage[String, ASTNode], variableInBody: mutable.HashMap[String, ArrayBuffer[ITypeBinding]],attachedAST: AST, rng: Random): ASTNode ={
    //Currently we ignore the usage context. Future, we need to choose one usage context and turn the context to a new one
    if(typeUsage.isInstanceOf[JavaClassMethodCallInvokerUsage]){
      val castedTypeUsage = typeUsage.asInstanceOf[JavaClassMethodCallInvokerUsage]
      var methodInvoc=attachedAST.newMethodInvocation()
      //For now we only handle the case of no argument to the method call
      methodInvoc.setExpression(attachedAST.newName(randomlyPickedVar))
      methodInvoc.setName(attachedAST.newSimpleName(castedTypeUsage.methodName))
      if(!castedTypeUsage.paramTypes.isEmpty){
        val params=synthesizeMethodCallParams(castedTypeUsage.paramTypes, variableInBody, rng)
        if(params == null)
          return  null
        else{
          params.map(p =>
            methodInvoc=if(!p.contains("."))// not qualified name
              AddArgsMethodCall.addArg(methodInvoc, attachedAST.newSimpleName(p))
            else{// qualified name
              AddArgsMethodCall.addArg(methodInvoc, attachedAST.newName(p))
            })
        }
      }
      val negatedMethodInvoc = negateMethodCallRetBool(methodInvoc, attachedAST)
      if(rng.nextBoolean())
        return methodInvoc
      else
        return negatedMethodInvoc

    }else if(typeUsage.isInstanceOf[JavaClassMethodCallParamUsage]){
      val castedTypeUsage = typeUsage.asInstanceOf[JavaClassMethodCallParamUsage]
      var methodInvoc = attachedAST.newMethodInvocation()
      //For now we handle only the case null invoker
      if(castedTypeUsage.invokerType.compareTo(nullInvoker) == 0){
        methodInvoc.setName(attachedAST.newSimpleName(castedTypeUsage.methodName))
        methodInvoc=AddArgsMethodCall.addArg(methodInvoc,attachedAST.newSimpleName(randomlyPickedVar))

        val negatedMethodInvoc = negateMethodCallRetBool(methodInvoc, attachedAST)
        if(rng.nextBoolean())
          return methodInvoc
        else
          return negatedMethodInvoc
      }else{
        return null
      }
    }else{
      return null
    }
  }
}