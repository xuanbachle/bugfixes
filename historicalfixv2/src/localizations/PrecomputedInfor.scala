package localizations

import java.io.File
import java.util

import _root_.util.FileFolderUtils
import localizations.faultlocalization.ochiai.{SuspiciousCode, Ochiai}
import mainscala.RepairOptions
import repair.handletests.{NegativeTestCaseOnly, AbstractTestInvoker}
import localizations.Identifier

import scala.collection.mutable.ArrayBuffer
import scala.io.Source._

class PrecomputedInfor (var fix: util.HashMap[String,ArrayBuffer[Identifier[Any]]],var fault: Seq[Identifier[Any]]){

  def getFaultFiles(): util.HashMap[String,Boolean] ={
    val res= new util.HashMap[String,Boolean]()
    fault.map (faultLoc => res.put(faultLoc.getFileName(),false))
    res
  }

  def isFaultFile(s: String): Boolean =
    fault.exists(a => a.getFileName().compareTo(s)==0)

  def getFixSpace(): util.HashMap[String,ArrayBuffer[Identifier[Any]]] = fix
  def getFaultSpace(): Seq[Identifier[Any]] = fault
  def setFaultSpace(refindedFaultSpace: Seq[Identifier[Any]]): Unit = {
    fault = refindedFaultSpace
  }

  def writeFault2File() ={
    if(!(new File(RepairOptions.faultFile)).exists()) {
      getFaultSpace().map(fault => {
        fault.getFileName()
        fault.getMethodName()
        myLib.MyScalaLib.appendFile(RepairOptions.faultFile,
          fault.getFileName() + " " + fault.getMethodName() + " " + fault.getLine() + " " + fault.getProb())
      })
    }
  }

  def refineFaultSpace(fileFilter: String, filterByMethodName: Boolean = false): Unit = {// remove the identifier give us the null java node, which means there is no java node corresponding to this identifier
  val filterLines = scala.io.Source.fromFile(fileFilter).getLines()
    val filterInfor = filterLines.foldLeft(new ArrayBuffer[(String,String, Int, Int)]()) {
      (res, line) => {
        val sp = line.split(" ")
        res.append((sp(0), sp(1), sp(2).toInt, sp(3).toInt))
        res
      }
    }

    def toFilter(faultLocIden: Identifier[Any]) = {
      filterInfor.find { case (fileName, methodName, lowerBound, upperBound) =>
        faultLocIden.getMethodName().equals(methodName) && faultLocIden.getFileName().equals(fileName) &&
          faultLocIden.getLine() > lowerBound && faultLocIden.getLine() < upperBound
      }
    }

    val refined=getFaultSpace().foldLeft(new ArrayBuffer[Identifier[Any]]) {
      (refinedFaultLocs, faultLocIden) => {
        val javaNode= if(faultLocIden.transformToJavaNode())  faultLocIden.getJavaNode() else null
        if (javaNode != null) {
          var possibleFault = true
          if(filterByMethodName){
            if(!faultLocIden.isInstanceOf[LineIdenGZoltar]){
              println("May be fault is represented in LineIden form!")
              throw new RuntimeException("Fault space with method name now only support the LineIdenGzoltar!")
            }

            possibleFault = toFilter(faultLocIden).getOrElse(null) != null
          }
          if(possibleFault) {
            println("Refining fault space: " + faultLocIden)
            faultLocIden.setJavaNode(javaNode)
            refinedFaultLocs.append(faultLocIden)
          }
        }
        refinedFaultLocs
      }
    }
    setFaultSpace(refined)
    //println("After refinded fault space: "+refined)
    //println("Continue by processing this fault space!")
  }

  //override def toString()=
  //  fix.foldLeft("")((str,fi)=>str+fi.toString+"\n") + fault.foldLeft("")((str,fi)=>str+fi.toString+"\n")
}

object PrecomputedInfor{

  def readFile(filePath:String): ArrayBuffer[Identifier[Any]] ={
    val lines = fromFile(filePath).getLines
    val res=lines.foldLeft(new ArrayBuffer[Identifier[Any]]())(
      (arr,sline)=> {
        if(!sline.startsWith("#") && !sline.isEmpty) {
          val s = sline.split(" +")
          val susCode = new SuspiciousCode(s(0),s(1), s(2).toInt, s(3).toDouble)
          arr.append(new LineIdenGZoltar(susCode))
        }
        arr
      }
    )
    return res
  }

  def computeFixFaultSpaces_old()={
    val fixGrouped = new util.HashMap[String, ArrayBuffer[Identifier[Any]]]()
    var fault = new ArrayBuffer[Identifier[Any]]()
    new PrecomputedInfor(fixGrouped,fault)
  }

  def computeFixFaultSpacesFromFiles()={
    val appClassDir = RepairOptions.appClassAbsoluteDir()
    val testClassDir = RepairOptions.testClassAbsoluteDir()
    val fullTempDir = FileFolderUtils.setupTempDirectories(appClassDir, testClassDir, RepairOptions.variantOutputDir, RepairOptions.defaultVariantNumber)

    val fixGrouped = new util.HashMap[String, ArrayBuffer[Identifier[Any]]]()
    var fault = new ArrayBuffer[Identifier[Any]]()
    fault = readFile(RepairOptions.faultFile)
    val testInvk1 = RepairOptions.chosenTestInvokerInstance()
    testInvk1.calculateTargetScore(testClassDir, ".class")
    new PrecomputedInfor(fixGrouped,fault)
  }

  /*def computeFixFaultSpaces(project: MainjGenProg, packageToInstrument: String)={
    import scala.collection.JavaConversions._
    val faultLoc = new Ochiai("")
    val suspiciousLocs=faultLoc.faultLocalizationGZoltar(project,packageToInstrument).foldLeft(new ArrayBuffer[LineIdenGZoltar]){
      (res, code) =>{
        val faultLocIden=new LineIdenGZoltar(code)
        res.append(faultLocIden)
        res
      }
    }
    val fixGrouped = new util.HashMap[String, ArrayBuffer[Identifier[Any]]]()
    //var fault = new ArrayBuffer[Identifier[Any]]()
    //if(GenOptions.fixFromFile) {
    /*val fix = readFile(RepairOptions.fixFile)
    fix.map(fi => {
      var value = fixGrouped.get(fi.getFileName())
      if (value == null)
        value = new ArrayBuffer[Identifier[Any]]()
      value.append(fi)
      fixGrouped.put(fi.getFileName(), value)
    }
    )*/
    //}else{

    //}
    // if(GenOptions.faultFromFile) {
    //fault = readFile(RepairOptions.faultFile)
    //}else{

    //}
    new PrecomputedInfor(fixGrouped, suspiciousLocs)
  }*/

  def computeFixFaultSpacesByOchiai()={
    FileFolderUtils.removeDir(RepairOptions.variantOutputDir)
    import scala.collection.JavaConversions._
    val appClassDir = RepairOptions.appClassAbsoluteDir()
    val testClassDir = RepairOptions.testClassAbsoluteDir()
    val fullTempDir = FileFolderUtils.setupTempDirectories(appClassDir, testClassDir, RepairOptions.variantOutputDir, RepairOptions.defaultVariantNumber)

    val pkgInstrument = RepairOptions.pkgInstrument.split(";")

    val classPaths = new ArrayBuffer[String]()
    classPaths.append(appClassDir)
    classPaths.append(testClassDir)
    println("FullTempDir="+fullTempDir)
    println("AppDir="+appClassDir)
    println("TestDir="+testClassDir)
    println("pkg: "+pkgInstrument)
    import scala.collection.JavaConversions._
    classPaths.appendAll(RepairOptions.getDependenciesList())
    val faultLoc = new Ochiai(fullTempDir)

    val testInvk1 = RepairOptions.chosenTestInvokerInstance()
    testInvk1.calculateTargetScore(testClassDir, ".class")
    val suspiciousCodes=faultLoc.searchFaultyLocations(appClassDir,pkgInstrument, classPaths, RepairOptions.thresholdFL, testInvk1,".class")

    val suspiciousLocs=suspiciousCodes.foldLeft(new ArrayBuffer[LineIdenGZoltar]){
      (res, code) =>{
        val faultLocIden=new LineIdenGZoltar(code)
        res.append(faultLocIden)
        res
      }
    }
    val fixGrouped = new util.HashMap[String, ArrayBuffer[Identifier[Any]]]()
    new PrecomputedInfor(fixGrouped, suspiciousLocs)
  }

  def main(args: Array[String]): Unit = {
    val pre=computeFixFaultSpaces_old()
    println(pre.toString)
  }
}