package parsers.javatypeinfor

import org.eclipse.jdt.core.dom.{ASTNode}
import parsers.{AbstractClassTypeUsage, AbstractClassTypeInfor}

import scala.collection.mutable.{ArrayBuffer}

/**
 * Created by larcuser on 16/10/15.
 */
class JavaClassTypeInfor extends AbstractClassTypeInfor[String, ASTNode]{
  override def lookUpUsageOfAType(currentType: String): ArrayBuffer[AbstractClassTypeUsage[String,ASTNode]] = {
    typesUsage.find(p =>currentType.compareTo(p._1) == 0) match {
      case None => return null
      case Some(p) => return p._2
    }
  }
}
