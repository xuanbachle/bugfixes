package util.ast.visitor

import org.apache.log4j.Logger
import org.eclipse.jdt.core.dom._
import java.util.Set


import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class ScopeCollector extends ASTVisitor {
  private val logger: Logger = Logger.getLogger(classOf[ScopeCollector])
  private var nameSet: mutable.HashMap[String, ArrayBuffer[ITypeBinding]] = null

  def this(o: mutable.HashMap[String, ArrayBuffer[ITypeBinding]]) {
    this()
    nameSet = o
  }

  override def visit(node: SimpleName): Boolean = {
    val name: String = node.getIdentifier
    val binding: IBinding = node.resolveBinding

    var isVarDecl: Boolean = false
    var parent: ASTNode = node.getParent
    def helper(): Boolean = {
      while (parent.getParent != null && !(parent.isInstanceOf[MethodDeclaration]) && !(parent.isInstanceOf[CompilationUnit])) {
        if (parent.getParent == null) {
          logger.info("why null?: " + parent.getClass.getName)
          Runtime.getRuntime.exit(1)
        }
        if (parent.isInstanceOf[VariableDeclarationStatement]) {
          isVarDecl = true
          return true //todo: break is not supported
        }
        if (parent.isInstanceOf[QualifiedName]) {
          isVarDecl = true
          return true //todo: break is not supported
        }
        parent = parent.getParent
      }
      return false
    }
    helper()
    if (!isVarDecl && binding.isInstanceOf[IVariableBinding]) {
      val typebinding: ITypeBinding = node.resolveTypeBinding()
      nameSet.get(name) match {
        case None => {
          val types = new ArrayBuffer[ITypeBinding]()
          types.append(typebinding)
          this.nameSet.put(name,types)
        }
        case Some(v) => v.append(typebinding)// same name but different types, e.g., maybe field and local variable
      }

    }
    return super.visit(node)
  }

}