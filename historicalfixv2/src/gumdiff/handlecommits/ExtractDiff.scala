package gumdiff.handlecommits

import gumdiff.difftemplates.SettingsAndUtils
import operategit.{RangeInformation, GitDiff, GitDiffParser, OptParser, Information}
import java.io.File
import scala.swing.Separator
import github.GitHubScala


/**
 * @author xuanbach
 */

object ExtractDiff{
  def main(args: Array[String]): Unit = {
    OptParser.runParseArgs(args)
    val rootFolder = OptParser.filename
    val ed = new ExtractDiff
    val subFolders=ed.getListOfSubDirectories(rootFolder)
    if(OptParser.runparallel)
      subFolders.par.map { folder  =>
          println("Running: " + folder)
          val fullURL = SettingsAndUtils.findURL(folder)
          if (fullURL != null) {
            val commitFile = SettingsAndUtils.findCommitFile(folder, true)
            if (commitFile != null) {
              println("Found with Unit Test!")
              var sleep = true;
              do {
                try {
                  ed.crawlMofidiedFilesFromCommits(commitFile, fullURL, true, commitFile.getParent)
                  sleep=false
                } catch {
                  case e : Throwable=> {
                    //sleep = true; // bad loop forever possibility
                    println("Sleeping: "+commitFile)
                    Thread.sleep(1000)
                  }
                }
              }while(sleep)
            } else {
              //println("Not Found with Unit Test!")
            }
          } else {
            //println("Not Found with Unit Test!")
          }
      }
    else subFolders.map { folder =>
        println("Running: "+folder)
        val fullURL=SettingsAndUtils.findURL(folder)
        if(fullURL != null) {
          val commitFile = SettingsAndUtils.findCommitFile(folder, true)
          if(commitFile != null) {
            println("Found with Unit Test!")
            ed.crawlMofidiedFilesFromCommits(commitFile, fullURL, true, commitFile.getParent)
          }else{
            println("Not Found with Unit Test!")
          }
        }else{
          println("Not Found with Unit Test!")
        }
        //sleepStep +=1
      }
  }
}

class ExtractDiff {
  val originalModifiedFiles = "modifiedFiles"
  
  def crawlMofidiedFilesFromCommits(commitFile: File, fullURL: String, withUnitTest: Boolean, baseDir: String) = {
	  //val baseDir = commitFile.getParent
    val commitPairs=myLib.Lib.readFile2Lines(commitFile)
    var i=2;
    for(pair <- commitPairs if(!pair.equals(""))){
      crawlToCountFolder(pair,i,baseDir,fullURL,withUnitTest)
      i=i+2;
    }
  }

  def crawlToCountFolder(pair: String, i: Int, baseDir: String, fullURL: String, withUnitTest: Boolean) ={
    val commits=pair.split("\\s+")
    println(pair)
    val oldCommit = commits(0)
    val fixCommit = commits(1)
    var diff= new File("");
    val testCases = if(withUnitTest) commits(2).split(";") else null
    if(withUnitTest){
      diff=myLib.Lib.search4FilesByName(new File(baseDir+File.separator+"withUnit_diff"), i+".diff").get(0)
    }else{
      diff=myLib.Lib.search4FilesByName(new File(baseDir+File.separator+"withoutUnit_diff"), i+".diff").get(0)
    }
    val (oldFile,newFile)=modifiedFilesOfDiff(diff.getCanonicalPath)
    //println("reach here")

    def folderEmptyOrNotExists(folder: String): Boolean={
      if(new File(folder).exists()) {
        if (new File(folder).listFiles().isEmpty)
          true
        else
          false
      }
      else
        true
    }

    if(oldFile.equals(newFile)){
      var saveToFolder = baseDir+File.separator+originalModifiedFiles
      if(myLib.Lib.folderExist(saveToFolder)){
        saveToFolder = saveToFolder+File.separator+i
        if(myLib.Lib.folderExist(saveToFolder)){
          val saveOldFolder = saveToFolder+ File.separator +"old"
          println("****crawling old")
          if(folderEmptyOrNotExists(saveOldFolder)) {
            //println(new File(saveOldFolder).exists())
            //println(new File(saveOldFolder).listFiles().size)
            myLib.Lib.initializeFolder(saveOldFolder)
            crawlFileFromURL(constructURLForGitHubCrawler(oldFile, oldCommit, fullURL), saveOldFolder)
          }
          println("****ended crawling old")

          val saveFixFolder = saveToFolder+ File.separator +"fix"
          println("****crawling fix")
          if(folderEmptyOrNotExists(saveFixFolder)) {
            myLib.Lib.initializeFolder(saveFixFolder)
            crawlFileFromURL(constructURLForGitHubCrawler(newFile, fixCommit, fullURL), saveFixFolder)
          }
          println("****ended crawling fix")

          println("****crawling test")
          if(testCases != null){
            val testCasesFolder = saveToFolder + File.separator +"tests"
            if(folderEmptyOrNotExists(testCasesFolder)) {
              myLib.Lib.initializeFolder(testCasesFolder)
              for (test <- testCases) {
                crawlFileFromURL(constructURLForGitHubCrawler(test, fixCommit, fullURL), testCasesFolder)
                myLib.MyScalaLib.appendFile(testCasesFolder + File.separator + "fullTestsName", test)

              }
            }
          }
          println("****ended crawling test")
        }else{
          myLib.Lib.initializeFolder(saveToFolder)
        }
      }else{
        myLib.Lib.initializeFolder(saveToFolder)
      }
    }else{
      println("Bachle: warning oldFile and newFile different!")
    }
  }

  def modifiedFilesOfDiff(pathToDiffFile: String) : (String, String) ={
    val diffList = GitDiffParser.parseFromFile(pathToDiffFile)
    import scala.collection.JavaConversions._
    for(diff <- diffList){
        if(diff.chunks.length ==1 && diff.oldFile.endsWith(".java") && diff.newFile.endsWith(".java") && 
            !diff.oldFile.endsWith("Test1.java")){
          return (diff.oldFile,diff.newFile)
        }
    }
    return (null,null)
  }

  def modifiedLinesOfDiff(pathToDiffFile: String) : (Int) ={
    val diffList = GitDiffParser.parseFromFile(pathToDiffFile)
    import scala.collection.JavaConversions._
    for(diff <- diffList){
      if(diff.chunks.length ==1 && diff.oldFile.endsWith(".java") && diff.newFile.endsWith(".java") &&
        !diff.oldFile.endsWith("Test1.java")){
        val chunk=diff.chunks.get(0)
        val info = chunk.rangeInformation
        return info.asInstanceOf[RangeInformation].newOffset
      }
    }
    return 0
  }

  def constructURLForGitHubCrawler(fileName: String, commit: String, fullURL: String) : String ={
    var crawlingURL = "https://raw.githubusercontent.com"  
    crawlingURL = crawlingURL + GitHubScala.extractUserProjFromURL(fullURL)
    crawlingURL = crawlingURL + File.separator + commit + File.separator + fileName
    return crawlingURL
  }
  
  def crawlFileFromURL(url: String, inFolder: String) ={
    import scala.sys.process._
    try {
      println("*******crawling")
      val cmd = Seq("wget", url, "-P", inFolder, "--no-check-certificate")
      println("*******crawled")
      val res = cmd.!!
    }catch {
      case e: Throwable => println("Maybe deleted file: "+url)
    }
    //println(url)
  }
  
  def getListOfSubDirectories(directoryName: String): Array[String] = {
    return (new File(directoryName)).listFiles.filter(_.isDirectory).map(_.getCanonicalPath)
  }
}