package util.ast.visitor

import org.apache.log4j.Logger
import org.eclipse.jdt.core.dom._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by larcuser on 22/10/15.
 */
class FieldAccessCollector extends ASTVisitor{
  private val logger: Logger = Logger.getLogger(classOf[ScopeCollector])
  private var nameSet: mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = null

  def this(o: mutable.HashMap[String, ArrayBuffer[ITypeBinding]]) {
    this()
    nameSet = o
  }

  override def visit(node: FieldAccess): Boolean ={
    val fieldName=node.getName
    val exp= node.getExpression
    putInNameSet(node.toString, fieldName.resolveTypeBinding())
    putInNameSet(exp.toString, exp.resolveTypeBinding())
    return true
  }

  private def visitQualifiedName(node: QualifiedName): Boolean ={
    val binding=node.resolveBinding()
    if(binding.isInstanceOf[IVariableBinding]){
      putInNameSet(node.getFullyQualifiedName, node.resolveTypeBinding())
    }
    val qualifierBinding = node.getQualifier.resolveBinding()
    if(qualifierBinding.isInstanceOf[IVariableBinding]) {
      putInNameSet(node.getQualifier.toString, node.getQualifier.resolveTypeBinding())
    }
    if(node.getQualifier.isQualifiedName()){
      visitQualifiedName(node.getQualifier.asInstanceOf[QualifiedName])
    }
    return true
  }

  override def visit(node: QualifiedName): Boolean ={
    visitQualifiedName(node)
    return true
  }

  private def putInNameSet(name: String, typeBinding: ITypeBinding)={
    nameSet.get(name) match {
      case None => {
        val types = new ArrayBuffer[ITypeBinding]()
        types.append(typeBinding)
        this.nameSet.put(name,types)
      }
      case Some(v) => v.append(typeBinding)
    }
  }

}
