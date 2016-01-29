package mainscala

//import scala.::
//import scala.collection.immutable.::

object ArgParser {
 
  val pf: PartialFunction[List[String], List[String]] = {
    case "-v" :: tail => RepairOptions.debug = true; tail
    case "-f" :: (arg: String) :: tail => RepairOptions.filename = arg; tail
    case "-dir" :: (arg: String) :: tail => RepairOptions.projectdir = arg; tail
    case "-lang" :: (arg: String) :: tail => RepairOptions.language = arg; tail
    case "-bugID" :: (arg: String) :: tail => RepairOptions.bugName = arg; tail

    //Main options
    case "-root" ::(arg: String) :: tail => RepairOptions.root = arg; tail
    case "-homeFolder" ::(arg: String) :: tail => RepairOptions.homeFolder = arg; tail
    case "-srcFolder" ::(arg: String) :: tail => RepairOptions.sourceFolder = arg; tail
    case "-testFolder" ::(arg: String) :: tail => RepairOptions.testFolder = arg; tail
    case "-pkgInstr" :: (arg: String) :: tail => RepairOptions.pkgInstrument = arg; tail
    case "-failing" :: (arg: String) :: tail => RepairOptions.failingTests = arg; tail
    case "-dep" :: (arg: String) :: tail => RepairOptions.dependencies = arg; tail

    case "-filterScore" :: (arg: String) :: tail => RepairOptions.filterFaultScore = arg.toDouble; tail
    case "-filterFault" :: (arg: String) :: tail => RepairOptions.faultFilterFile = arg; tail
    case "-faultFile" :: (arg: String) :: tail => RepairOptions.faultFile = arg; tail

    case "-variantDir" :: (arg: String) :: tail => RepairOptions.variantOutputDir = arg; tail
    case "-appClassDir" :: (arg: String) :: tail => RepairOptions.appClassDir = arg; tail
    case "-testClassDir" :: (arg: String) :: tail => RepairOptions.testClassDir = arg; tail
    case "-minedPattern" :: (arg: String) :: tail => RepairOptions.minedPatternFile = arg; tail

    case "-maxSol" :: (arg: String) :: tail => RepairOptions.maxSolution = arg.toInt; tail
    case "-timeout" :: (arg: String) :: tail => RepairOptions.timeout = arg.toInt; tail
    case "-s" :: (arg: String) :: tail => RepairOptions.showme = arg; tail

    case RepairOptions.unknown(bad) :: tail => die("unknown argument " + bad + "\n" + RepairOptions.usage)
  }

  def main(args: Array[String]) {
    // if there are required args:
    if (args.length == 0) die()
    val arglist = args.toList
    val remainingopts = parseArgs(arglist,pf)

    println("debug=" + RepairOptions.debug)
    println("showme=" + RepairOptions.showme)
    println("filename=" + RepairOptions.filename)
    println("remainingopts=" + remainingopts)
  }

  def parseArgs(args: List[String], pf: PartialFunction[List[String], List[String]]): List[String] = args match {
    case Nil => Nil
    case _ => if (pf isDefinedAt args) parseArgs(pf(args),pf) else args.head :: parseArgs(args.tail,pf)
  }

  def die(msg: String = RepairOptions.usage) = {
    println(msg)
    sys.exit(1)
  }

}