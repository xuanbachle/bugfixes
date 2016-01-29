package util.ast

import org.eclipse.jdt.core.dom.{IBinding, ITypeBinding, ASTNode, CompilationUnit}
import util.ast.visitor.{FieldAccessCollector, NameCollector, ScopeCollector, TypeCollector}
import org.apache.log4j.Logger
import java.util.Set
import java.util.TreeSet
import scala.collection.mutable
import scala.collection.mutable._

object ASTUtils {
  private var logger: Logger = Logger.getLogger(ASTUtils.getClass)

  def sameASTNode(node1: ASTNode, node2: ASTNode): Boolean ={
    return node1.toString.equals(node2.toString)
  }

  def compatibleTypes(t1: ITypeBinding, t2: ITypeBinding): Boolean = {
    if (t1 == null || t2 == null) return false
    if (t1.isSubTypeCompatible(t2) || t1.isCastCompatible(t2) || t1.isAssignmentCompatible(t2) || (t1.toString == t2.toString)) {
      return true
    }
    else return false
  }

  def strictlyCompatibleTypes(t1: ITypeBinding, t2: ITypeBinding): Boolean = {
    if (t1 == null && t2 == null) return true
    else if(t1 == null && t2 != null)
      return false
    else if(t1 !=null && t2 == null)
      return false
    else if (t1.toString.compareTo(t2.toString) == 0) {
      return true
    }
    else return false
  }

  def getStatementLineNo(node: ASTNode): Int = {
    val root: ASTNode = node.getRoot
    var lineno: Int = -1
    if (root.isInstanceOf[CompilationUnit]) {
      val cu: CompilationUnit = root.asInstanceOf[CompilationUnit]
      lineno = cu.getLineNumber(node.getStartPosition)
    }
    else {
      logger.error("Root is not CU?")
    }
    return lineno
  }

  def getStatementEndLineNo(node: ASTNode): Int = {
    val root: ASTNode = node.getRoot
    var lineno: Int = -1
    if (root.isInstanceOf[CompilationUnit]) {
      val cu: CompilationUnit = root.asInstanceOf[CompilationUnit]
      lineno = cu.getLineNumber(node.getStartPosition + node.getLength)
    }
    else {
      logger.error("Root is not CU?")
    }
    return lineno
  }

  def getNames(node: ASTNode): Set[String] = {
    val names: TreeSet[String] = new TreeSet[String]
    if (node != null) {
      val visitor: NameCollector = new NameCollector(names)
      node.accept(visitor)
    }
    return names
  }

  def getTypes(node: ASTNode): Set[String] = {
    val types: TreeSet[String] = new TreeSet[String]
    if (node != null) {
      val visitor: TypeCollector = new TypeCollector(types)
      node.accept(visitor)
    }
    return types
  }

  def getScope(node: ASTNode): mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = {
    val scope: mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = new  mutable.HashMap[String, ArrayBuffer[ITypeBinding]]
    if (node != null) {
      val visitor: ScopeCollector = new ScopeCollector(scope)
      node.accept(visitor)
    }
    return scope
  }

  def getFieldAccess(node: ASTNode): mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = {
    val fieldAcc: mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = new  mutable.HashMap[String, ArrayBuffer[ITypeBinding]]
    if(node != null){
      val visitor = new FieldAccessCollector(fieldAcc)
      node.accept(visitor)
    }
    return fieldAcc
  }

  def getVariableUsed(node: ASTNode): mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = {
    var variablesUsed: mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = new mutable.HashMap[String, ArrayBuffer[ITypeBinding]]
    variablesUsed=getScope(node)
    getFieldAccess(node).map{
      case (name, associatedTypes) =>{
        variablesUsed.get(name) match {
          case None => {
            val types = new ArrayBuffer[ITypeBinding]()
            types.appendAll(associatedTypes)
            variablesUsed.put(name,types)
          }
          case Some(v) => v.appendAll(associatedTypes)
        }
      }
    }
    return variablesUsed
  }
}