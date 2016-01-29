package parsers

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by larcuser on 16/10/15.
 */
abstract class AbstractClassTypeInfor[T,K]{//T can be IBinding, K can be astnode
  //def lookUpASTForAClass(clsQualifiedName: String): T
  protected val typesUsage = new mutable.HashMap[T, ArrayBuffer[AbstractClassTypeUsage[T,K]]]()
  def addUsageOfAType (currentType: T, usage: AbstractClassTypeUsage[T,K]) ={
    val us = lookUpUsageOfAType(currentType)
    if(us == null){// usage of curentType never existed before
      val usageSet = new ArrayBuffer[AbstractClassTypeUsage[T,K]]
      usageSet.append(usage)
      typesUsage.put(currentType, usageSet)
    }else{// existed before
      //TODO: add new usage or update existing usage?
      us.append(usage)
      typesUsage.put(currentType, us)
    }
  }
  //return null if never exists before
  def lookUpUsageOfAType(currentType: T): ArrayBuffer[AbstractClassTypeUsage[T,K]]

  def getTypesUsage() = typesUsage
}
