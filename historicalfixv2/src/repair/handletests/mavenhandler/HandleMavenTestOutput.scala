package repair.handletests.mavenhandler

import org.apache.maven.shared.invoker.InvocationOutputHandler

/**
 * Created by dxble on 8/2/15.
 */
class HandleMavenTestOutput extends InvocationOutputHandler{
  var compilationError = false
  var buildSuccess = false

  override def consumeLine(s: String): Unit = {
    println("Processing output line: "+s)
    if(s.toLowerCase.contains("compilation error"))
      compilationError = true
    if(s.toLowerCase.contains("build success"))
      buildSuccess = true
    if(s.toLowerCase.contains("timeout"))
      buildSuccess = false
  }

}
