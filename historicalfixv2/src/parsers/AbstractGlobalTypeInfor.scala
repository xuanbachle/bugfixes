package parsers

import localizations.Identifier

import scala.collection.mutable.HashMap

/**
 * Created by larcuser on 16/10/15.
 */
abstract class AbstractGlobalTypeInfor[T,K] {
  private val typeInforClasses = new HashMap[String, AbstractClassTypeInfor[T,K]]

  def buildLocalClassTypeInfor(exp: Identifier[Any]) : Unit
  protected def addTypeInforForAClass(clsQualifiedName: String, absTypeInfor: AbstractClassTypeInfor[T,K]): Unit ={
    if(!typeInforClasses.contains(clsQualifiedName)) {
      typeInforClasses.put(clsQualifiedName, absTypeInfor)
    }
  }

  def lookUpTypeInforForAClass(clsQualifiedName: String): AbstractClassTypeInfor[T,K] = {
    typeInforClasses.get(clsQualifiedName).getOrElse(null)
  }

  def getTypeInforForClasses() = typeInforClasses
}
