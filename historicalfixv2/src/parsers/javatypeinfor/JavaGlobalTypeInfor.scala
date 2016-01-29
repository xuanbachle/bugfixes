package parsers.javatypeinfor

import localizations.Identifier
import org.eclipse.jdt.core.dom.{ITypeBinding, CompilationUnit, ASTNode, IBinding}
import parsers.AbstractGlobalTypeInfor
import parsers.javaparser.JavaParser

/**
 * Created by larcuser on 16/10/15.
 */
object JavaGlobalTypeInfor extends AbstractGlobalTypeInfor[String, ASTNode]{

  //This is the core method to build type infor of each class
  override def buildLocalClassTypeInfor(exp: Identifier[Any]) ={
    if(this.lookUpTypeInforForAClass(exp.getFileName()) == null) {// not exist before then we build
      val cu=JavaParser.getCompilationUnit(exp.getFileName())
      val visitor = new JavaClassTypeInforVisitor()
      cu.accept(visitor)
      addTypeInforForAClass(exp.getFileName(),visitor.getCollectedClassTypeInfor())
    }
  }
}
