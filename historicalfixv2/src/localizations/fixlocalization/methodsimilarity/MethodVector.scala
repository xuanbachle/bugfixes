package localizations.fixlocalization.methodsimilarity

/**
 * Created by dxble on 8/18/15.
 */
class MethodVector(fileName: String, methodName: String, methodLine: Int, methodEndline: Int ) {

  private var vector = new Array[Int](20)
  def getVector() = this.vector

  def getMethodName() = methodName
  def getMethodLine() = methodLine
  def getMethodEndline() = methodEndline
  def getFileName() = fileName
  override def hashCode() = fileName.hashCode + methodName.hashCode + methodLine + methodEndline
  override def equals(obj: Any): Boolean ={
    if(obj.isInstanceOf[MethodVector]) {
      val objMethodVec = obj.asInstanceOf[MethodVector]
      if (fileName.equals(objMethodVec.getFileName()) && methodName.equals(objMethodVec.getMethodName()) && methodLine == objMethodVec.getMethodLine() && methodEndline == objMethodVec.getMethodEndline())
        return true
      else
        return false
    }else
      return false
  }
  def increaseIfCount() = vector(0) += 1
  def increaseReturnCount() = vector(1) += 1
  def increaseForCount() = vector(2) += 1
  def increaseWhileCount() = vector(3) +=1
  def increaseDoCount() = vector(4) += 1
  def increaseAssertCount() = vector(5) += 1
  def increaseBreakCount() = vector(6) +=1
  def increaseContinueCount() = vector(7) +=1
  def increaseConstructorInvocCount() = vector(8) +=1
  def increaseSuperConstructorInvocCount() = vector(9) +=1
  def increaseAssignmentCount() = vector(10) +=1
  def increaseEnhancedForCount() = vector(11) += 1
  def increaseSyncCount() = vector(12) +=1
  def increaseSuperMethodCount() =vector(13) +=1
  def increaseSwitchStatementCount() =vector(14) += 1
  def increaseThrowCount() =vector(15) +=1
  def increaseTryCount()=vector(16) +=1
  def increaseVarDeclVount() =vector(17) +=1
  def computeSimilarityWith(anotherMethodVector: MethodVector): Double = {
    val anotherVector = anotherMethodVector.getVector()
    var numerator = 0.0
    for (i <- 0 to vector.length-1) {
      numerator += vector(i) * anotherVector(i)
    }

    val d1=math.sqrt(vector.foldLeft(0.0){
      (res, vi) =>{
        res + vi * vi
      }
    })
    val d2=math.sqrt(anotherVector.foldLeft(0.0){
      (res, vi) =>{
        res + vi * vi
      }
    })
    return numerator / (d1 * d2)
  }
}
