package gumdiff.difftemplates

import java.io.File


/**
 * Created by dxble on 7/13/15.
 */
object SettingsAndUtils {
  val WITH_UNIT_TEST = true
  val GSPAN_INPUT = "/home/dxble/MyWorkSpace/diffExamples/src/main/java/e21.lg"
  val GSPAN_INPUT_2 = "/home/dxble/MyWorkSpace/diffExamples/src/main/java/e51.lg"
  def isProjectWithUnitTest(prj: String)={
   if(findCommitFile(prj,true) == null)
     false
   else true
  }

  def findURL(pathToFolder: String): String = {
    try {
      val urlFile = myLib.Lib.search4Files(new File(pathToFolder), "projecturl").get(0)
      val lines = myLib.Lib.readFile2Lines(urlFile)
      return lines(0)
    }catch {
      case e: Throwable => null
    }
  }

  def findCommitFile(pathToFolder: String, withUnitTest: Boolean) ={// fileName = withUnitTest or withoutUnitTest
    try {
      var fileName = ""
      if(withUnitTest)
        fileName = "withUnitTest"
      else fileName = "withoutUnitTest"
      val commitFile= myLib.Lib.search4FilesContainName(new File(pathToFolder), fileName).get(0)
      commitFile
    }catch {
      case e: Throwable => null
    }
  }

  def isSatisfiedDataCondtion(countFolder: String)={ // to further filter noise in the data
    try{
      //val oldFile = myLib.Lib.search4Files(new File(countFolder + File.separator + "old"), "java").get(0)
      val (fixFile,oldFile) = SettingsAndUtils.getFixOldFiles(countFolder)

      def updateJavaDocOnly(oldFile: File, fixFile: File) = {
        val diff = new DiffTemplates
        val (actions,stc,dtc) = diff.getDiffActions(oldFile.getCanonicalPath,fixFile.getCanonicalPath)
        if(actions.size()>0) {
          if(diff.nodeClassName(actions.get(0).getNode).equals("Javadoc"))
            true
          else
            false
        } else
          true
      }
      //println(oldFile.getCanonicalPath.endsWith("TestCase.java") + updateJavaDocOnly(oldFile,fixFile).toString)
      if(oldFile.getCanonicalPath.endsWith("TestCase.java") || updateJavaDocOnly(oldFile,fixFile))
        false
      else
        true
    } catch {
      case e: Throwable => false
    }
  }

  def isAlreadyMatched(countFolder: String)={
    try{
      myLib.Lib.search4FilesContainName(new File(countFolder),"matchResult.res").get(0)
      true
    }catch {
      case e: Throwable => {
        try{
          myLib.Lib.search4FilesContainName(new File(countFolder),"nomatch.res").get(0)
          //true
          false
        }catch {
          case e: Throwable => false
        }
      }
    }
  }

  def getFixOldFiles(countFolder: String) ={
    try{
      val fixFile=myLib.Lib.search4Files(new File(countFolder+File.separator+"fix"),"java").get(0)
      val oldFile=myLib.Lib.search4Files(new File(countFolder+File.separator+"old"),"java").get(0)
      (fixFile,oldFile)
    }catch {
      case e: Throwable => (null,null)
    }
  }
}
