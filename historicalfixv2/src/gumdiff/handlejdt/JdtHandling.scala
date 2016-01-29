package gumdiff.handlejdt

import com.github.gumtreediff.tree.ITree
import org.eclipse.jdt.core.dom.{ASTNode, ASTVisitor, CompilationUnit}

/**
 * Created by dxble on 7/23/15.
 */
object JdtHandling {

  def getJDTNodeOfITree(cUnit: ASTNode, to_find: ITree): ASTNode ={
    val v = new ASTVisitor() {
      var foundNode: ASTNode = null

      override def preVisit2(aSTNode: ASTNode): Boolean ={
        if(aSTNode.getStartPosition==to_find.getPos && aSTNode.getLength == to_find.getLength && aSTNode.getNodeType==to_find.getType){
          foundNode = aSTNode
          return false
        }
        return true
      }
    }
    cUnit.accept(v)
    v.foundNode
  }
}
