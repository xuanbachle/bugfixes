package parsers

import org.eclipse.jdt.core.dom.ASTNode

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by larcuser on 16/10/15.
 */
abstract class AbstractClassTypeUsage [T,K]{// K is astnode, T is return type can be string or itypebinding
  //statement kind where this type is used in as key (stmt kind e.g., assignment), statement content as value
  //usageContext should only care about boolean return type of the context, e.g., A.getX() > y instead of y=A.getX()
  protected val usageContext= new mutable.HashMap[String, mutable.ArrayBuffer[K]]()
  protected def statementKind(stmt: K): String
  def extractRealContext(stmt: K): K
  def getReturnType(): T
  def getUsageContext(): mutable.HashMap[String, mutable.ArrayBuffer[K]]

  def addToUsageContext(stmt: K): Boolean = {
    val context = extractRealContext(stmt)
    if (context!= null) {
      val stmtKind = statementKind(stmt)
      val stmts = usageContext.get(stmtKind).getOrElse(null)
      if (stmts != null) {
        stmts.append(context)
        usageContext.update(stmtKind, stmts)
      } else {
        val usedStmts = new ArrayBuffer[K]()
        usedStmts.append(context)
        usageContext.update(stmtKind, usedStmts)
      }
      return true
    }else
      return false
  }
}


