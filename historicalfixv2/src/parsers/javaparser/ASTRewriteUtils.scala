package parsers.javaparser

import java.io.{File, IOException}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import mainscala.RepairOptions
import org.eclipse.jdt.core.dom.rewrite.{ASTRewrite, ITrackedNodePosition, ListRewrite}
import org.eclipse.jdt.core.dom.{ASTNode, ASTVisitor, Block, CompilationUnit}
import org.eclipse.jface.text.{BadLocationException, Document}
import org.eclipse.text.edits.{MalformedTreeException, TextEdit}
import repair.mutationoperators._
import localizations.{JavaNodeIden, LineIdenGZoltar, LineIden, Identifier}
import util.FileFolderUtils

/**
 * Created by xuanbach32bit on 4/22/15.
 */

class findNodeASTVisitor (cu: CompilationUnit, to_find: Identifier[Any]) extends ASTVisitor {
  var found: ASTNode = null

  def compareByLine(line1:Int, line2:Int): Boolean = {
    return line1==line2
  }

  override def preVisit2(node: ASTNode): Boolean = {
    val line: Int = cu.getLineNumber(node.getStartPosition)
    if (compareByLine(to_find.getLine, line)) {
      //System.out.println("Found Node")
      //System.out.println("BL" + node.toString + " *** " + node.getClass + " LN:" + line + " Iden:" + to_find.getLine)
      found = node
      return false
    }
    return true
  }

  def getCompilationUnit: CompilationUnit = {
    return this.cu
  }
}

class printASTVisitor (cu: CompilationUnit)extends ASTVisitor {

  override def preVisit2(node: ASTNode): Boolean = {
      val line: Int = cu.getLineNumber(node.getStartPosition)
      //System.out.println("BL" + node.toString + " *** " + node.getClass + " LN:" + line)
      true
  }
}

object ASTRewriteUtils {

  def applyEdits(rew: ASTRewrite, fileName: String) {
    try {
      val filePath = FileFolderUtils.fileNameAbsolutePath(fileName)
      val str: String = JavaParser.modifyingFiles.get(fileName)
      //System.out.println("Content: "+str)
      //println(JavaParser.modifyingFiles)
      val document: Document = new Document(str)
      val edits: TextEdit = rew.rewriteAST(document, null)
      try {
        edits.apply(document)
        val newSource: String = document.get
        //println("Writing to document: "+newSource)
        Files.write(Paths.get(filePath),newSource.getBytes(StandardCharsets.UTF_8))
        //System.out.println(newSource)
      }
      catch {
        case e: MalformedTreeException => {
          e.printStackTrace
        }
        case e: BadLocationException => {
          e.printStackTrace
        }
      }
    }
    catch {
      case e: IllegalArgumentException => {
        e.printStackTrace
      }
      case e1: IOException => {
        e1.printStackTrace
      }
    }
  }

  //def getASTRewrite(cu: CompilationUnit): ASTRewrite = {
  //  return ASTRewrite.create(cu.getAST)
  //}

  def removeNode(rew: ASTRewrite, node: ASTNode): ASTRewrite = {
    println("REMOVE")

    rew.remove(node, null)
    return rew
  }

  def replaceNode(rew: ASTRewrite, node: ASTNode, replacement: ASTNode): ASTRewrite = {
    var rep: ASTNode = null
    rep = ASTNode.copySubtree(node.getAST(), replacement)
    //val rep: ASTNode = rew.createCopyTarget(replacement)
    println("REPLACE")
    rew.replace(node, rep, null)
    return rew
  }

  def appendNode(rewriter: ASTRewrite, parent: ASTNode, child: ASTNode): ASTRewrite = {
    if (child == null) throw new Exception("This should never happen")
    val to_add: ASTNode = ASTNode.copySubtree(parent.getAST(), child)
    val bl: Block = parent.getParent.asInstanceOf[Block]
    val rewrite: ListRewrite = rewriter.getListRewrite(bl, Block.STATEMENTS_PROPERTY)
    rewrite.insertAfter(to_add, parent, null)
    return rewrite.getASTRewrite
  }

  def prependNode(rewriter: ASTRewrite, parent: ASTNode, child: ASTNode): ASTRewrite = {
    if (child == null) throw new Exception("This should never happen")
    val to_prep: ASTNode = ASTNode.copySubtree(parent.getAST(), child)
    val bl: Block = parent.getParent.asInstanceOf[Block]
    val rewrite: ListRewrite = rewriter.getListRewrite(bl, Block.STATEMENTS_PROPERTY)
    rewrite.insertBefore(to_prep, parent, null)
    return rewrite.getASTRewrite
  }

  def findNode(cu: CompilationUnit, to_find: Identifier[Any]): ASTNode = {
    //if(to_find.isInstanceOf[LineIden]){
      val find: findNodeASTVisitor = new findNodeASTVisitor(cu,to_find)
      if(cu == null) {
        println("CU NULL")
        return null //TODO: to check
      }
      cu.accept(find)
      return find.found
    //}
    //return null
  }

  //deprecated -- should not use anymore for new algorithm
  def getIdenRefInfor (des: Identifier[Any],getRewriter: Boolean = false) = {
    def helper(fileName: String)={
      val desNode = if(des.transformToJavaNode())
                        des.getJavaNode()
                    else null
      if (getRewriter) {
        val desRewriter = ASTRewriteFactory.getRewriterUptoDate(fileName)
        (desRewriter, desNode, fileName)
      }
      else
        (null, desNode, fileName)
    }

    des match {
      case LineIden(fileName, line, probability) => {
        helper(fileName)
      }
      case LineIdenGZoltar(suspiciousCode) =>{
        println("Name: "+suspiciousCode.getClassName)
        helper(suspiciousCode.getClassName)
      }
      case JavaNodeIden(fileName, javaNode, probability) =>{
        helper(fileName)
      }
      case _ => {println("IDENFIFIER IS NOT LINEIDEN!!!"); sys.error("IDENTIFIER NOT SUPPORTED YET")}//(null,null,null)
    }
  }

  /*def findNodeFromRewrite(rew: ASTRewrite, node: ASTNode): ASTNode = {
    try {
      val p: ITrackedNodePosition = rew.track(node)
      println("Found pos:" + p.getStartPosition)
      return node
    }
    catch {
      case e: Exception => {
        return null
      }
    }
  }*/
}

