package gumdiff.handlejdt

import org.eclipse.jdt.core.dom._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 7/23/15.
 */
class SimpleNameVisitor(cu: CompilationUnit) extends ASTVisitor{
  var allSimpleName = new ArrayBuffer[String]()

  override def visit(aSTNode: SimpleName): Boolean ={
    //println("visit: "+aSTNode.getFullyQualifiedName)
    allSimpleName.append(aSTNode.getFullyQualifiedName)
    return true
  }

  /*override def visit(aSTNode: SimpleName): Boolean ={
    println("Get binding for: "+aSTNode.getFullyQualifiedName)
    //println("Line: "+ (cu.getLineNumber(aSTNode.getStartPosition)-1))
    val binding:IBinding = aSTNode.resolveBinding()
    //if(cu.getLineNumber(aSTNode.getStartPosition)-1==571) {
      //if(binding !=null)
      //if(binding.toString.contains("registeredInput"))
      println("BD: " + binding)
      //println("Node: "+aSTNode)
    //}
    if(binding.isInstanceOf[IVariableBinding]){
      val varBinding: IVariableBinding = binding.asInstanceOf[IVariableBinding]
      //println(varBinding.getName)
      //println(varBinding.isParameter)
    }

    //println(binding.getName)
    return true
  }*/
}
