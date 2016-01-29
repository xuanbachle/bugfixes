package gumdiff.difftemplates

import java.io.File
import java.util

import gumdiff.customLib.CustomLib
import gumdiff.handlecommits.ExtractDiff
import myLib.MyScalaLib
import operategit.OptParser
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, HashMap}
import com.github.gumtreediff.actions.model.Action

/**
 * Created by dxble on 7/13/15.
 */
object MatchWithTemplates{
  def main(args: Array[String]) {
    val matchTempl = new MatchWithTemplates
    //matchTempl.processFromRoot("/home/dxble/Desktop/data_bigquery/test/")
    //matchTempl.processWholeData("/home/dxble/Desktop/data_bigquery/alldata_raw/", false)
    //matchTempl.collectResultWholeData("/home/dxble/Desktop/data_bigquery/alldata_raw/")
    val (matched, noMatched)= //matchTempl.collectResultWholeData("/home/dxble/Desktop/data_bigquery/test_graph")
      matchTempl.collectResultWholeData("/home/dxble/Desktop/data_bigquery/alldata_withUnit")
    val wholeData = matched.foldLeft(new ArrayBuffer[String]){ case (keyset,(key,_)) => {keyset.append(key); keyset}} ++: noMatched
    println("Generating graph data")
    //val testData = new ArrayBuffer[String]
    //testData.append("/home/dxble/Desktop/data_bigquery/alldata_withUnit/all/ModeShape_modeshape/modifiedFiles/2")
    val total = matchTempl.generateDataForMining(wholeData,0)
    println("End Generating graph data")
    println("Total serialized graphs:" + total)
    /*import scala.collection.JavaConversions._
    val linput: util.List[util.List[String]] = new util.ArrayList[util.List[String]]()
    println("Size: "+linput.size())
    val temp =new util.ArrayList[String]
    temp.add("/home/dxble/Test/tester/Test1.java")
    temp.add("/home/dxble/Test/tester/Test2.java")
    for(inp <- temp) println(inp)
    linput.add(temp)
    println("Size: "+linput.size())

    for(inp <- linput) println(inp)
    println(linput.get(0))
    println(matchTempl.generateDiffManyPairs(linput,0,"test"))*/
  }
}

class MatchWithTemplates {

  def testing()={
    //val rootFolder = "/home/dxble/Desktop/data_bigquery/alldata_raw/myresults_results-20150622-222249_more_100000.csv/pentaho_pentaho-platform"
    //OptParser.runParseArgs(args)
    val rootFolder = "/home/dxble/Desktop/data_bigquery/alldata_raw/myresults_results-20150622-223455_1.5t_1t.csv"//OptParser.filename
    val matchTempl = new MatchWithTemplates
    //matchTempl.processOneProject(rootFolder)
    //matchTempl.processFromRoot(rootFolder)
    val (matched, noMatched)=matchTempl.collectMatchedResultsFromRoot(rootFolder,true,false)
    Statistics.statisticMatchTemplates(matched, noMatched)
    //matchTempl.collectMatchedResultsOneProject()
    //println(matchTempl.countTotalDiffsCrawled("/home/dxble/Desktop/data_bigquery/alldata_raw/myresults_results-20150622-222249_more_100000.csv"))
    //println(matchTempl.countTotalDiffsCrawled("/home/dxble/Desktop/data_bigquery/alldata_raw/myresults_results-20150622-223410_2t_1.5t.csv"))
    //println(matchTempl.countTotalDiffsCrawled("/home/dxble/Desktop/data_bigquery/alldata_raw/myresults_results-20150622-223455_1.5t_1t.csv"))
    //println(matchTempl.countTotalDiffsCrawled("/home/dxble/Desktop/data_bigquery/alldata_raw/myresults_results-20150622-223035_10t_5t.csv"))
  }

  def processWholeData(rootData: String, rematch: Boolean = true) ={
    val allData =  CustomLib.getListOfSubDirectories(rootData)
    allData.map(eachData => processFromRoot(eachData, rematch))
  }

  def collectResultWholeData(rootData: String) ={
    val allData =  CustomLib.getListOfSubDirectories(rootData)
    val (wholeMatched, wholeNoMatched) = allData.foldLeft((new mutable.HashMap[String, Array[String]](), new ArrayBuffer[String]()))((res, eachData) => {
        println("Trying collect data: "+ eachData)
        val (matched, noMatched) = collectMatchedResultsFromRoot(eachData, true, false)
        Statistics.statisticMatchTemplates(matched, noMatched)
        res match {
          case (totalMatched, totalNoMatched) =>
            (totalMatched ++: matched, totalNoMatched ++: noMatched)
        }
    })

    println("================The final result for whole data================")
    Statistics.statisticMatchTemplates(wholeMatched, wholeNoMatched)
    (wholeMatched, wholeNoMatched)
  }

  def processFromRoot(rootFolder: String, rematch: Boolean = true) = {
    val projects=CustomLib.getListOfSubDirectories(rootFolder)
    val run_parallel = true //OptParser.runparallel
    if(run_parallel){
      projects.par.map{
        prj =>
          println(prj)
          processOneProject(prj, rematch)
      }
    }else{
      projects.map{
        prj =>
          println(prj)
          processOneProject(prj, rematch)
      }
    }
  }

  def checkEmptyCrawledFolders(project: String) ={
    val extractDiff = new ExtractDiff
    val url = SettingsAndUtils.findURL(project)
    val commitFile = SettingsAndUtils.findCommitFile(project, SettingsAndUtils.WITH_UNIT_TEST)
    if(commitFile !=null) {
      val linesCommitPairs = myLib.Lib.readFile2Lines(commitFile)

      val modifiedFilesFolder = project + File.separator + "modifiedFiles"
      val byCountFolders = CustomLib.getListOfSubDirectories(modifiedFilesFolder)
      if(byCountFolders.isEmpty)
        extractDiff.crawlMofidiedFilesFromCommits(commitFile,url,SettingsAndUtils.WITH_UNIT_TEST,commitFile.getParent)
      else {
        byCountFolders.map {
          countFolder => if (CustomLib.getListOfSubDirectories(countFolder).length == 0) {
            val index = CustomLib.getFolderName(countFolder).toInt // get the counter
            println("Index of Empty Folder: " + index)
            extractDiff.crawlToCountFolder(linesCommitPairs(index - 2), index, commitFile.getParent, url, SettingsAndUtils.WITH_UNIT_TEST) // to crawl again
          }
        }
      }
      true
    }else
      false
  }

  def processOneProject(project: String, rematch: Boolean = true) ={
    val sucess=checkEmptyCrawledFolders(project)
    if(sucess) {
      val modifiedFilesFolder = project + File.separator + "modifiedFiles"
      if (new File(modifiedFilesFolder).exists()){
        val byCountFolders = CustomLib.getListOfSubDirectories(modifiedFilesFolder)
        byCountFolders.map {
          countFolder => {
            val to_rematch = if(!rematch) !SettingsAndUtils.isAlreadyMatched(countFolder) else rematch
            if (SettingsAndUtils.isSatisfiedDataCondtion(countFolder) && to_rematch) {// this is to further filter the noise in the data
              try {
                val oldFile = myLib.Lib.search4Files(new File(countFolder + File.separator + "old"), "java").get(0)
                val fixFile = myLib.Lib.search4Files(new File(countFolder + File.separator + "fix"), "java").get(0)

                val matchRes = DiffTemplates.matchAllTempaltes(oldFile.getCanonicalPath, fixFile.getCanonicalPath)
                var tempCount = 1;
                var matchAtleastOne = false;
                for (res <- matchRes) {
                  if (res) {
                    println("matchTemplate:" + countFolder + ":" + tempCount + ":" + "true")
                    myLib.MyScalaLib.appendFile(countFolder + File.separator + "matchResult.res", tempCount.toString) // write match results to file
                    matchAtleastOne = true;
                  }
                  tempCount += 1
                }
                if (!matchAtleastOne) {
                  myLib.Lib.writeText2File("No Template Matched! Total checked: " + tempCount, new File(countFolder + File.separator + "nomatch.res"))
                }
              } catch {
                case e: Throwable => println("Bachle: Warning countFolder: " + countFolder + " may be empty!" + e)
                  e.printStackTrace()
              }
            }
          }
        }
      }
    }
  }

  def countTotalDiffsCrawled(rootFolder: String): Int ={
    val projects=CustomLib.getListOfSubDirectories(rootFolder)
    projects.foldLeft(0)((count,prj) => {
        val modifiedFilesFolder = prj + File.separator + "modifiedFiles"
        if(new File(modifiedFilesFolder).exists()) {
          val byCountFolders = CustomLib.getListOfSubDirectories(modifiedFilesFolder)
          count + byCountFolders.length
        }else{
          if(new File(prj + File.separator + "withUnit_diff").exists())
            println(prj)
          count
        }
      }
    )
  }

  def collectMatchedResultsFromRoot(rootFolder: String, withUnitTest: Boolean, withOutUnitTest: Boolean) ={
    val projects=CustomLib.getListOfSubDirectories(rootFolder)

    def helperParallel(projects: Array[String], f: ((HashMap[String, Array[String]],ArrayBuffer[String]),String) => ((HashMap[String, Array[String]],ArrayBuffer[String]))) =
      if(OptParser.runparallel) projects.par.foldLeft((new mutable.HashMap[String, Array[String]](), new ArrayBuffer[String]()))((res,prj) => f(res,prj))
      else projects.foldLeft((new mutable.HashMap[String, Array[String]](), new ArrayBuffer[String]()))((res,prj) => f(res,prj))

    def mainProcess(res:(HashMap[String, Array[String]],ArrayBuffer[String]),prj: String) = {
      res match {
        case (matchedDiffTemplate, noMatchDiffTemplate) => {
          val haveUnitTest = SettingsAndUtils.isProjectWithUnitTest(prj)
          //println(prj+haveUnitTest)
          if((withUnitTest && haveUnitTest) ||
            (withOutUnitTest && !haveUnitTest)) {
            val (matched, nomatched) = collectMatchedResultsOneProject(prj)
            (matchedDiffTemplate ++: matched, noMatchDiffTemplate ++: nomatched)
          }else
            (matchedDiffTemplate, noMatchDiffTemplate)
        }
      }
    }

    val (matched, nomatched)=helperParallel(projects, mainProcess)
    (matched, nomatched)
  }

  def collectMatchedResultsOneProject(prjFolder: String) ={
    val matchedDiffTemplate = new HashMap[String, Array[String]]()
    val noMatchDiffTemplate = new ArrayBuffer[String]
    def helperCollect(prj: String, matchedRes: HashMap[String, Array[String]], noMatch: ArrayBuffer[String]) = {
      val modifiedFilesFolder = prj + File.separator + "modifiedFiles"
      val byCountFolders = CustomLib.getListOfSubDirectories(modifiedFilesFolder)
      byCountFolders.map {
        countFolder => {
          try{
            val result=myLib.Lib.search4FilesContainName(new File(countFolder),"matchResult.res").get(0)
            import scala.io.Source
            val matchedTemps =  Source.fromFile(result).getLines().toArray //myLib.Lib.readFile2Lines(result)
            matchedRes += (countFolder -> matchedTemps)
          }catch {
            case e: Throwable => {
              try{
                val result=myLib.Lib.search4FilesContainName(new File(countFolder),"nomatch.res").get(0)
                noMatch.+=:(countFolder)
              }catch {
                case e: Throwable => {}
              }

            }
          }
        }
      }
      (matchedRes,noMatch)
    }
    helperCollect(prjFolder, matchedDiffTemplate,noMatchDiffTemplate)
  }


  def generateDataForMining(countFolders: ArrayBuffer[String], id:Int) : Int={
     //println(countFolders)
     countFolders.foldLeft(id){
       (graphID,folder) => {
         //println(folder)
         val diff = new DiffTemplates
         val(fix,old)=SettingsAndUtils.getFixOldFiles(folder)
         println(fix+" "+old)
         val (actions,stc,dtc)=diff.getDiffActions(old.getCanonicalPath,fix.getCanonicalPath)
         if(actions.size()>0) {
           diff.gatherAllInfor(actions, stc, dtc)
           val buf = ActionHandling.serializeDiff2GSpanGraph(actions, graphID, folder, diff)
           for (line <- buf) {
             MyScalaLib.appendFile(SettingsAndUtils.GSPAN_INPUT, line)
             //println(line)
           }
           graphID + 1
         }else
           graphID
       }
     }
  }

  def generateDiffManyPairs(pairs: util.List[util.List[String]], graphID: Int, folder: String): String = {
    import scala.collection.JavaConversions._
    val diff = new DiffTemplates
    val actions = pairs.foldLeft(new util.ArrayList[Action]()){
      (acts,pair) =>{
        println("PAIR: "+pair)
        val (pairActions, stc, dtc)= diff.getDiffActions(pair(0),pair(1))
        if(pairActions.size()>0){
          diff.gatherAllInfor(pairActions, stc,dtc)
        }
        acts.addAll(pairActions)
        acts
      }
    }
    if(actions.size>0) {
      val buf = ActionHandling.serializeDiff2GSpanGraph(actions, graphID, folder, diff)
      buf.foldLeft(""){(res, line)=> res+line+"\n"}
    }else{
      return null
    }
  }

}

/*if(OptParser.runparallel){
  projects.par.foldLeft((new mutable.HashMap[String, Array[String]](), new ArrayBuffer[String]())) {
    (res, prj) => res match {
      case (matchedDiffTemplate, noMatchDiffTemplate) => {
        val haveUnitTest = SettingsAndUtils.isProjectWithUnitTest(prj)
        if((withUnitTest && haveUnitTest) ||
          (withOutUnitTest && !haveUnitTest)) {
          val (matched, nomatched) = collectMatchedResultsOneProject(prj)
          (matchedDiffTemplate ++: matched, noMatchDiffTemplate ++: nomatched)
        }else
          (matchedDiffTemplate, noMatchDiffTemplate)
      }
    }
  }
}else{
  projects.foldLeft((new mutable.HashMap[String, Array[String]](), new ArrayBuffer[String]())) {
    (res, prj) => res match {
      case (matchedDiffTemplate, noMatchDiffTemplate) => {
        val haveUnitTest = SettingsAndUtils.isProjectWithUnitTest(prj)
        //println(prj+haveUnitTest)
        if((withUnitTest && haveUnitTest) ||
          (withOutUnitTest && !haveUnitTest)) {
          val (matched, nomatched) = collectMatchedResultsOneProject(prj)
          (matchedDiffTemplate ++: matched, noMatchDiffTemplate ++: nomatched)
        }else
          (matchedDiffTemplate, noMatchDiffTemplate)
      }
    }
  }
}*/