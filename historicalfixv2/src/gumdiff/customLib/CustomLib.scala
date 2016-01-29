package gumdiff.customLib

import java.io.File

/**
 * Created by dxble on 7/13/15.
 */
object CustomLib {

  def getListOfSubDirectories(directoryName: String): Array[String] = {
    try {
      return (new File(directoryName)).listFiles.filter(_.isDirectory).map(_.getCanonicalPath)
    }catch {
      case e: Throwable => new Array[String](0)
    }
  }

  def getFolderName(fullFolderName: String) ={
    val splitted = fullFolderName.split("/")
    splitted(splitted.size-1)
  }
}
