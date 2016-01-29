package repair.representation

import java.io.File
import java.net.URL

import mainscala.RepairOptions
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import parsers.javaparser.{ASTRewriteFactory, ASTRewriteUtils}
import repair.handletests.junithandler.compiler.JavaJDKCompiler
import util.FileFolderUtils

import scala.collection.mutable.ArrayBuffer

/**
 * Created by xuanbach32bit on 4/22/15.
 */
class GenProgIndividual (genome:ArrayBuffer[MutGene], seedCandidate: Boolean = false) {
  private var ind_id = GenProgIndividual.getMaxIndId()
  private var parent_id = 0
  private var testScore = 0.0
  private var generScore = 0.003
  private var probability = 0.0
  private var totalProb = 0.0
  private var timeFoundSolution: Long = 0L
  //private val rewriters = new ASTRewriterIndividual
  //private val backingRewriters = new ASTRewriterIndividual
  private var hasValidGenome = true
  private var alreadyUpdatedProbability = false
  private var triedCompile = false
  private var compilability = false
  private var isSolution = false
  private var incentiveForPassedTest = 0.0

  def getIncentiveForPassedTest() = incentiveForPassedTest
  def setIncentiveForPassedTest(incentive: Double) = incentiveForPassedTest = incentive

  def getTotalProb() = totalProb
  def addLastProb2TotalProb(lastProb: Double) = totalProb = totalProb + lastProb
  def setTotalProb(total: Double) = totalProb = total

  def setIsSolution(sol: Boolean) ={
    isSolution = sol
  }

  def getIsSolution() = isSolution

  def getAverageMutatedLineProb(): Double ={
    val sum=getGenome.foldLeft(0.0){
      (res, aGene) =>{
        res+aGene.getMutatedLineProb()
      }
    }
    return sum/getGenome.size
  }

  def getClassPaths(): Array[URL] ={
    val bytecodeOutput: String = this.getVariantClassFolder()
    val variantOutputFile: File = new File(bytecodeOutput)
    var bc: Array[URL] = null
    val originalURL: Array[URL] = FileFolderUtils.getURLforVariant(RepairOptions.defaultVariantNumber)
    if (this.getHasValidGenome()) {
      bc = FileFolderUtils.redefineURL(variantOutputFile, originalURL)
    }
    else {
      bc = originalURL
    }
    return bc
  }

  def getClassPathsString(): java.util.ArrayList[String] ={
    this.getClassPaths().foldLeft(new java.util.ArrayList[String]()){
      (res, path) =>{
        res.add(path.toString)
        res
      }
    }
  }

  def getAverageSimilarityScore(): Double ={
    var count =0
    val sum=getGenome.foldLeft(0.0){
      (res, aGene) =>{
        if(aGene.isInstanceOf[AppMut]){
          count += 1
          val appGene = aGene.asInstanceOf[AppMut]
          res + appGene.getToApp().getFixProb()
        }else if(aGene.isInstanceOf[RepMut]){
          count += 1
          val repGene = aGene.asInstanceOf[RepMut]
          res + repGene.getToRep().getFixProb()
        }else {
          res
        }
      }
    }
    if(count > 0)
      return sum/count
    else
      return 1.0
  }

  def compile(): Boolean ={
    if(triedCompile)
      return compilability

    triedCompile = true
    if(this.getGenome.length == 0) {
      compilability = true
      return true
    }

    val path2VariantClassFiles = FileFolderUtils.getOutDirWithPrefix(getInd_id().toString)
    val fileSourceMap = ASTRewriteFactory.getModifiedRewriterContent
    val compiler: JavaJDKCompiler = new JavaJDKCompiler(path2VariantClassFiles,this.getClassPathsString(),fileSourceMap, null, null)
    val status=compiler.compile()
    if(status == JavaJDKCompiler.Status.COMPILED) {
      compilability = true
    }
    else {
      compilability = false
    }

    return compilability
  }

  def setTimeFoundSolution(tFound: Long) = timeFoundSolution = tFound
  def isAlreadyUpdatedProbability() = alreadyUpdatedProbability

  def isSeedCandidate(): Boolean = seedCandidate
  def setGenerScore(score: Double) = generScore=score
  def getGenerScore(): Double = generScore

  def addNewGene(newGene: MutGene) ={
    getGenome.append(newGene)
    alreadyUpdatedProbability = false
  }

  def updateGenerScore() = {
    val score = (1.0 / scala.math.pow(2.0, scala.math.pow(getGenome.size + 1.0, 2.0))) * (1.0 / (1.0/2.0 + 1.0/16.0 + 1.0/512.0))
    generScore = score
  }

  def getHasValidGenome(): Boolean = hasValidGenome
  def setHasValidGenome(valid: Boolean) = hasValidGenome = valid

  def getGenome: ArrayBuffer[MutGene] = {
    return genome
  }

  override def hashCode(): Int ={
    genome.foldLeft(0){
      (result, eachGen) =>{
        result + eachGen.hashCode()
      }
    }
  }

  override def equals(obj: Any): Boolean ={
    val objIndividual = obj.asInstanceOf[GenProgIndividual]
    val objGenome = objIndividual.getGenome
    if(genome.length != objGenome.length)
      return false

    genome.foldLeft(0){
      (index, eachCurrentGen) =>{
        //println("Comparing: "+eachCurrentGen+" with "+objGenome(index))
        if(!eachCurrentGen.equals(objGenome(index))){
          return false
        }
        index + 1
      }
    }
    //println("Identical" + genome + " and "+objGenome)
    return true
  }

  def setTestScore(score: Double) = testScore = score
  def getTestScore(): Double = testScore

  def setProbability(score: Double) = {
    if(alreadyUpdatedProbability)
      throw new RuntimeException("Cannot set probability for a candidate second time! It is expesive to compute probability all over again!")

    probability = score
    alreadyUpdatedProbability = true
  }

  def setProbabilityByCopy(prob: Double) ={
    probability = prob
  }

  def getProbability(): Double = probability

  def getInd_id() = ind_id

  def getVariantClassFolder(): String = {
    var folderID = getInd_id().toString
    if(ind_id == 0)
      folderID = RepairOptions.defaultVariantNumber

    FileFolderUtils.getOutDirWithPrefix(folderID)
  }

  def getParent_id() = parent_id
  def setInd_id (id:Int) = ind_id = id
  def setParent_id (id: Int) = parent_id = id

  def copy(): GenProgIndividual = {
    val copyGenome = getGenome.foldLeft(new ArrayBuffer[MutGene]){
      (res, gene) =>{
        res.append(gene)
        res
      }
    }
    val copyIndividual = new GenProgIndividual(copyGenome)
    copyIndividual.setTotalProb(getTotalProb())
    copyIndividual.setInd_id(GenProgIndividual.getMaxIndId())
    copyIndividual.setProbabilityByCopy(getProbability())
    copyIndividual.setTestScore(getTestScore())
    copyIndividual.setIncentiveForPassedTest(getIncentiveForPassedTest())
    copyIndividual.setGenerScore(getGenerScore())
    copyIndividual.setParent_id(getParent_id())
    copyIndividual.setHasValidGenome(getHasValidGenome())
    return copyIndividual
  }

  def cleanVariantClassFolder()={
    val variantClassFolder = FileFolderUtils.getOutDirWithPrefix(getInd_id().toString)
    FileFolderUtils.removeDir(variantClassFolder)
  }

  override def toString: String = {
    return "ID="+ind_id + " ParentID="+parent_id + " TestScore="+ testScore + " Probability=" + probability + " Gener Score "+generScore+" Genome: "+getGenome + " Compile? "+hasValidGenome +" Found after: "+timeFoundSolution+" milisec"

  }

  def convertOneGene2ASTRewriter(aGene: MutGene): (ASTRewrite,String) ={
    val (astRewriter, fileName)=aGene match {
      case AppMut(des, to_app) => {
        try {
          val (desRewriter, desNode, fileName) = ASTRewriteUtils.getIdenRefInfor(des, true)
          val (_, to_appNode, _) = ASTRewriteUtils.getIdenRefInfor(to_app, false)
          //println("Appending: " + to_appNode + " to " + desNode)
          val ret = (ASTRewriteUtils.appendNode(desRewriter, desNode, to_appNode), fileName)
          //println("End Append")
          ret
        } catch {
          case e: Throwable => {
            println("Append not successful: " + AppMut(des, to_app));
            e.printStackTrace(); (null, null)
          }
        }
      }
      case PrepMut(des, to_prep) => {
        try {
          val (desRewriter, desNode, fileName) = ASTRewriteUtils.getIdenRefInfor(des, true)
          val (_, to_appNode, _) = ASTRewriteUtils.getIdenRefInfor(to_prep, false)
          //println("prepending: " + to_appNode + " to " + desNode)
          val ret = (ASTRewriteUtils.prependNode(desRewriter, desNode, to_appNode), fileName)
          //println("End Prepend")
          ret
        } catch {
          case e: Throwable => {
            println("Prepend not successful: " + PrepMut(des, to_prep));
            e.printStackTrace(); (null, null)
          }
        }
      }
      case DelMut(to_del) => {
        try {
          val (delRewriter, to_delNode, fileName) = ASTRewriteUtils.getIdenRefInfor(to_del, true)
          //println("Removing: " + to_delNode)
          val ret = (ASTRewriteUtils.removeNode(delRewriter, to_delNode), fileName)
          //println("End Remove")
          ret
        } catch {
          case e: Throwable => {
            println("Delete not successful: " + DelMut(to_del));
            e.printStackTrace(); (null, null)
          }
        }
      }
      case RepMut(des, to_rep) => {
        try {
          val (desRewriter, desNode, fileName) = ASTRewriteUtils.getIdenRefInfor(des, true)
          val (_, to_repNode, _) = ASTRewriteUtils.getIdenRefInfor(to_rep, false)
          //println("Replace: " + desNode + " with " + to_repNode)
          val ret = (ASTRewriteUtils.replaceNode(desRewriter, desNode, to_repNode), fileName)
          //println("End Replace")
          ret
        }
        catch {
          case e: Throwable => {
            println("Replace not successful: " + RepMut(des, to_rep));
            e.printStackTrace(); (null, null)
          }
        }
      }
    }
    return (astRewriter, fileName)
  }

  def transferNotLastGene2StaticWriterFactory(): Boolean ={
    ASTRewriteFactory.assertEmptyModifiedRewriter()
    if(this.getGenome.size <= 1)
      return true

    val notLastGenes=this.getGenome.take(this.getGenome.size - 1)
    notLastGenes.map{
      gene => {
        val (astRw, fileToWrite) = convertOneGene2ASTRewriter(gene)
        if (astRw != null) {
          ASTRewriteFactory.updateModifiedRewriters(fileToWrite, astRw) // keep modified rewriters upadted in the factory
        } else {
          //throw new RuntimeException("Rewriter is null!")
          this.setHasValidGenome(false)
        }
      }
    }

    return this.hasValidGenome
  }

  def transferLastGene2StaticWriterFactory(): Boolean ={
    if(this.getGenome.size == 0)
      return true

    val lastGene = this.getGenome.last
    val (astRw, fileToWrite) = convertOneGene2ASTRewriter(lastGene)
    if (astRw != null) {
      ASTRewriteFactory.updateModifiedRewriters(fileToWrite, astRw) // keep modified rewriters upadted in the factory
    } else {
      //throw new RuntimeException("Rewriter is null!")
      this.setHasValidGenome(false)
    }
    return this.hasValidGenome
  }

  def transferGeneToStaticWriterFactory(): Boolean ={
    ASTRewriteFactory.assertEmptyModifiedRewriter() // modified rewriters need be empty before writing a variant
    //if(candidate.getGenome.size > 1)
    //println("Gene size > 2")
    for (e: MutGene <- getGenome) {
      val (astRw, fileToWrite) = convertOneGene2ASTRewriter(e)
      if (astRw != null) {
        ASTRewriteFactory.updateModifiedRewriters(fileToWrite, astRw) // keep modified rewriters upadted in the factory
      } else {
        //throw new RuntimeException("Rewriter is null!")
        this.setHasValidGenome(false)
      }
    }

    //ASTRewriteFactory.clearAllModifiedRewriters()
    return this.getHasValidGenome()
  }
}
 object GenProgIndividual{
   private var maxIndId=0

   def getMaxIndId(): Int= {
     maxIndId = maxIndId + 1
     return maxIndId
   }
 }