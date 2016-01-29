package localizations

import java.lang.Boolean

import localizations.faultlocalization.ochiai.SuspiciousCode
import localizations.fixlocalization.methodsimilarity.FindFixStrategies
import localizations.fixlocalization.methodsimilarity.FindFixStrategies.FixStrat.FixStrat
import org.eclipse.jdt.core.dom._
import parsers.javaparser.{ASTRewriteUtils, JavaParser}
import util.ast.ASTUtils
import util.ast.visitor.{ExpressionCollector, MethodCallInforCollector}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class Identifier[+T] {
  // representation of a single statement, a statement can be determined as a java node,
  // can be determined as a line number, etc
  private var names: java.util.Set[String] = null
  private var scope: mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = null
  protected var javaNode: ASTNode = null
  private var fixSpace: ArrayBuffer[Identifier[Any]] = null
  private var fixProb = 0.5
  private var triedTransform: Boolean = false
  protected var exps: Seq[Identifier[Any]] = null
  private var hasExpsInJavaNode: Boolean = false
  private var triedCollectExps = false
  private var possibleInvokers: ArrayBuffer[Expression] = null // for replacing invoker in method call
  private var possibleMethodCallRep: ArrayBuffer[SimpleName] = null // for replacing the method call name
  private var methodReturnType : ITypeBinding = null
  private var triedCollectInvokers = false
  private var currentMethodVars : mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = null
  private var fieldVars : mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = null
  private var forLoopVars : mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = null

  def getPrepAppSources(prec: PrecomputedInfor, strat: FixStrat): ArrayBuffer[Identifier[Any]] = {
    FindFixStrategies.chooseAStrategy(prec,this,strat)
  }

  def getRepSources(prec: PrecomputedInfor, strat: FixStrat): ArrayBuffer[_ <: Identifier[Any]] = {
    FindFixStrategies.chooseAStrategy(prec,this,strat)
  }

  def getExpMutSources(prec: PrecomputedInfor, strat: FixStrat): ArrayBuffer[_ <: Identifier[Any]] = {
    //if(this.getLine()==329)
      //println("DB")
    FindFixStrategies.chooseAStrategy(prec,this,strat)
  }

  def getSwapSources(prec: PrecomputedInfor, strat: FixStrat): ArrayBuffer[_ <: Identifier[Any]] = {
    FindFixStrategies.chooseAStrategy(prec,this,strat)
  }

  protected def getJavaNodeShortString(): String ={
    if(getJavaNode()!=null){
      if(getJavaNode().isInstanceOf[IfStatement]){
        var ifStr= ""
        ifStr = ifStr + "if("+getJavaNode().asInstanceOf[IfStatement].getExpression.toString+"){...}"
        return ifStr
      }

      if(getJavaNode().isInstanceOf[ForStatement]){
        var forStr= ""
        forStr = forStr + "for("+getJavaNode().asInstanceOf[ForStatement].getExpression.toString+"){...}"
        return forStr
      }

      if(getJavaNode().isInstanceOf[WhileStatement]){
        var whileStr= ""
        whileStr = whileStr + "while("+getJavaNode().asInstanceOf[WhileStatement].getExpression.toString+"){...}"
        return whileStr
      }

      return getJavaNode().toString
    }else{
      return null
    }
  }

  def setCurrentMethodVars(vars: mutable.HashMap[String, ArrayBuffer[ITypeBinding]]) = currentMethodVars = vars
  def getCurrentMethodVars() = currentMethodVars
  def setFieldVars(vars: mutable.HashMap[String, ArrayBuffer[ITypeBinding]]) = fieldVars = vars
  def getFieldVars() = fieldVars
  def setforLoopVars(vars: mutable.HashMap[String, ArrayBuffer[ITypeBinding]]) = forLoopVars = vars
  def getForLoopVars() = forLoopVars

  def setMethodReturnType(mtype: ITypeBinding) = this.methodReturnType = mtype
  def getMethodReturnType()=methodReturnType

  def setInvokers(invokers: ArrayBuffer[Expression]) ={
    possibleInvokers = invokers
  }
  def getPossibleInvokers() = {
    if(!triedCollectInvokers) {
      triedCollectInvokers = true
      val mf = new MethodCallInforCollector(this)
      this.getJavaNode().getRoot.asInstanceOf[CompilationUnit].accept(mf)
      if(!mf.possibleInvokers.isEmpty)
        this.possibleInvokers=mf.possibleInvokers
      if(!mf.possibleMethodCallRep.isEmpty)
        this.possibleMethodCallRep=mf.possibleMethodCallRep

      println("Invokers: "+possibleInvokers)
    }
    possibleInvokers
  }

  def getPossibleMethodCallRep() ={
    if(!triedCollectInvokers) {
      triedCollectInvokers = true
      val mf = new MethodCallInforCollector(this)
      this.getJavaNode().getRoot.asInstanceOf[CompilationUnit].accept(mf)
      if(!mf.possibleInvokers.isEmpty)
        this.possibleInvokers=mf.possibleInvokers
      if(!mf.possibleMethodCallRep.isEmpty)
        this.possibleMethodCallRep=mf.possibleMethodCallRep

      println("Method Call Rep: "+possibleMethodCallRep)
    }
    possibleMethodCallRep
  }

  def getFixSpace() = fixSpace
  def setFixSpace(fix: ArrayBuffer[Identifier[Any]]) = fixSpace = fix

  def getFixProb() = fixProb
  def setFixProb(prob: Double)= fixProb = prob

  def getMethodName(): String = {
    //throw new RuntimeException("Not supported")
    return "test/testgcd/gcd"
  }

  def isReturnStatement(): Boolean = {
    if (javaNode != null) {
      return javaNode.isInstanceOf[ReturnStatement]
    }else
      return false
  }

  def hasExps(): Boolean ={
    if(triedCollectExps && !hasExpsInJavaNode)
      return false
    else if(triedCollectExps && hasExpsInJavaNode)
      return true

    triedCollectExps = true
    if(transformToJavaNode()){
      val expVisitor = new ExpressionCollector
      javaNode.accept(expVisitor)
      if(!expVisitor.collectedExps.isEmpty){
        setExps(expVisitor.collectedExps)
        hasExpsInJavaNode = true
        return true
      }else{
        return false
      }
    }else{
      return false
    }
  }

  // To be implemented by subclasses
  protected def setExps(expsInsideJavaNode: Seq[ASTNode]): Unit

  def getExps(): Seq[Identifier[Any]] = exps

  def getScope(): mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = {
    if(this.scope == null)
    this.setScope(ASTUtils.getScope(this.getJavaNode()))
    this.scope
  }
  def setScope(scp: mutable.HashMap[String, ArrayBuffer[ITypeBinding]]) = scope = scp
  def getJavaNode(): ASTNode = javaNode
  def setJavaNode(jvNode: ASTNode) = javaNode = jvNode

  def transformToJavaNode(): Boolean = {
    if(triedTransform && getJavaNode() == null)
      return false
    triedTransform = true
    if(getJavaNode()==null) {
      //sys.error("HAVE NOT SET THE Java Node for node: " + curNode)
      val desCUnit = JavaParser.globalASTs.get(getFileName())
      //println("Finding: "+ getFileName())
      if(desCUnit == null) {
        println(getFileName() + "CU "+ desCUnit)
        println("This may mean we are getting AST node from TestCase!")
      }
      val jvNode= ASTRewriteUtils.findNode(desCUnit, this)
      if (jvNode == null)
        return false
      else {
        setJavaNode(jvNode)
        //println("Setting java node for: " + this + " as " + jvNode)
        return true
      }
    }else{
      return true
    }
  }

  def compareWith[B >:T](b:B): Boolean = true
  def getLine(): Int
  def getProb(): Double
  def getFileName(): String
  def getNames(): java.util.Set[String] = names
  def setNames(nameSet:  java.util.Set[String]): Unit = names = nameSet

  def isInScope[B >:T](toCheckNode: Identifier[B]): Boolean = {
    import scala.collection.JavaConversions._
    //if(toCheckNode.getLine()==161) {
      //toCheckNode.getScope()
      //println("DB")
    //}
    //this.hasExps()//collect exp for current node and collect field, method vars for current node
    for(checkName <- toCheckNode.getScope()){
      var inScope = true
      if(this.scope != null)
        inScope=this.scope.contains(checkName)
      if(this.fieldVars != null)
        inScope= inScope || this.fieldVars.contains(checkName)
      if(this.currentMethodVars != null)
        inScope= inScope || this.currentMethodVars.contains(checkName)
      if(!inScope)
        return false
    }
    return true
  }

}
case class LineIden(fileName: String,line: Int,prob: Double) extends Identifier[Int]{

  override def compareWith[B >:Int] (to_comp: B) = line==to_comp.asInstanceOf[Int]
  override def getLine(): Int = line
  def getProb(): Double = prob
  def getFileName(): String= fileName
  def setExps(expsInsideJavaNode: Seq[ASTNode]): Unit ={
    exps=expsInsideJavaNode.foldLeft(new ArrayBuffer[LineIden]()){
      (res, exp) =>{
        val expNode = new LineIden(fileName, ASTUtils.getStatementLineNo(exp), prob)
        expNode.setJavaNode(exp)
        res.append(expNode)
        res
      }
    }
  }
  //def isInScope(lineIden: LineIden) = this.getNames()
  override def getMethodName() ={
    throw new RuntimeException("LineIden does not support getMethodName!")
  }

  override def toString() = fileName + " "+line+ " " +getJavaNode()+" "+prob
}
case class LineIdenGZoltar(suspiciousCode: SuspiciousCode) extends Identifier[Int]{
  // This representation is used at fault localization level
  //example: methodName=org.apache.commons.math.analysis.solvers.UnivariateRealSolverUtils{bracket(Lorg/apache/commons/math/analysis/UnivariateRealFunction;DDDI)
  override def getMethodName(): String = {
    //println(suspiciousCode.getMethodName)
    try {
      suspiciousCode.getMethodName().split("\\{")(1).split("\\(")(0) // method name from suspicous code
    }catch {
      case e: ArrayIndexOutOfBoundsException =>{
        suspiciousCode.getMethodName //normal method name
      }
    }
  }

  override def compareWith[B >:Int] (to_comp: B) = getLine()==to_comp.asInstanceOf[Int]
  override def getLine(): Int = suspiciousCode.getLineNumber
  def getProb(): Double = suspiciousCode.getSuspiciousValue
  def getFileName(): String= suspiciousCode.getClassName
  //override def toString() = getFileName() + " "+getLine()+" "+getProb()
  def setExps(expsInsideJavaNode: Seq[ASTNode]): Unit ={
    exps=expsInsideJavaNode.foldLeft(new ArrayBuffer[LineIdenGZoltar]()){
      (res, exp) =>{
        val susCode = new SuspiciousCode(suspiciousCode.getClassName, suspiciousCode.getMethodName, ASTUtils.getStatementLineNo(exp), suspiciousCode.getSuspiciousValue)
        val expNode = new LineIdenGZoltar(susCode)
        expNode.setJavaNode(exp)
        res.append(expNode)
        res
      }
    }
  }

  override def toString() = /*suspiciousCode.getClassName + " "+getMethodName()+" "+ */ suspiciousCode.getLineNumber +" " + getJavaNodeShortString() +" "+suspiciousCode.getSuspiciousValue

}
case class JavaNodeIden(fileName: String,curnode: ASTNode,prob:Double) extends Identifier[ASTNode]{
  // This representation is more concrete that LineIdenGzoltar, used after parsing,
  // when we transformed the raw infor (LineIdenGzoltar)
  private var line = -1;
  override def compareWith [B >:ASTNode](to_comp: B) =
    (curnode.getStartPosition == to_comp.asInstanceOf[ASTNode].getStartPosition) &&
      (curnode.getLength == to_comp.asInstanceOf[ASTNode].getLength)

  def getLine (cu:CompilationUnit): Int =
    cu.getLineNumber(curnode.getStartPosition)

  override def getLine(): Int = {
    if(line == -1){
      line = ASTUtils.getStatementLineNo(curnode)
    }
    return line;
  }
  override def getJavaNode() = curnode
  def getProb (): Double = prob
  def getFileName(): String = fileName

  def setExps(expsInsideJavaNode: Seq[ASTNode]): Unit ={
    exps=expsInsideJavaNode.foldLeft(new ArrayBuffer[JavaNodeIden]()){
      (res, exp) =>{
        val expNode = new JavaNodeIden(fileName,exp, prob)
        expNode.setJavaNode(exp)
        res.append(expNode)
        res
      }
    }
  }

  override def transformToJavaNode(): Boolean = {
    if(curnode!=null) {
      javaNode = curnode
      return true
    }else{
      return false
    }
  }

  override def toString() = fileName + " "+getJavaNodeShortString()+" "+prob

}
