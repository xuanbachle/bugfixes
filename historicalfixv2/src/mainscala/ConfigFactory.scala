package mainscala


/**
 * Created by dxble on 8/6/15.
 */
object ConfigFactory {

  def process_bug(bugName: String) ={
      bugName match {
      case "m2" => math_2_settings()// change parenthesis and remove type casting [par not] v
      case "m5" => math_5_settings()// fixed 2 round, 3 fls [verified ok] [par fix not real] v, replace return
      //case "gcd" => gcd_settings()
      //case "expMut" => expMut_settings()
      case "m22" => math_22_settings()// fixed 2 round, 2 fls [verified ok] [par not] v, replace true false
      case "m11" => math_11_settings()// change param * => /, 0.5 => 2
      case "m32" => math_32_settings()// FL 135, not tried (add if cond) [par maybe] v
      case "m33" => math_33_settings()// testing, 21 fls, change 1 method call param [par maybe] v, replace method call param
      case "m34" => math_34_settings()// fixed, 1 fls [par maybe fixed real] v, replace method call name
      case "m50" => math_50_settings()// fixed, 29 fls, FL 186 [verified ok] [par not] trying, delete statement
      case "m53" => math_53_settings()// fixed, ... fls, FL ... [verified ok] [par not] trying, add statement
      case "m70" => math_70_settings()// fixed, 1 fls [par maybe] trying -, replace method call name (overload)
      case "m72" => math_72_settings()// localization bad, FL: 115,127 but only localize 115
      case "m98" => math_98_settings()// havent tried, fix 2 files [par not]
      case "m82" => math_82_settings()// fixed, 10 fls, FL ..., same score [par not], change >=
      case "m85" => math_85_settings()
      case "t19" => time_19_settings()// fixed 2 round, 12 fls, FL 900 [par not], change >=
      case "l43" => lang_43_settings()// fixed before?, 4 fls, FL ..., same score [par not], add statement
      case "l6" => lang_6_settings()// fixed, 9 fls, FL ... [verified ok] [par maybe] v, change method call param
      //case "l6test" => lang_6test_settings()
      case "l10" => lang_10_settings()
      case "l51" => lang_51_settings()// fixed, 29 fls, FL ... [verified ok] [par not], add statement
      case "l57" => lang_57_settings()// fixed 2 round, 1 fls, [par maybe] v, change invoker
      case "l58" => lang_58_settings()// fixed but not ok quality, fail other test, 22 fls [par not],
      case "l59" => lang_59_settings()// fixed so so quality, 6 fls, same score [par maybe] trying - replace method call param
      case "c10" => closure_10_settings()// fixed by 2 round tournament (1 round actually because fault space small), 3 fls [par maybe] v, replace method call name
      case "c14" => closure_14_settings()//fixed by 2-round tournament, 20 fls (-10), FL ... [par maybe] v, replace method call param
      case "c18" => closure_18_settings()// localization too bad, correct faulty line at bottom, tried divide FLs, produce weird fix
      case "c31" => closure_31_settings()// localization too bad, correct faulty line at bottom, assume 10 lines, produce fix but not same human, bad fix
      case "c62" => closure_62_settings()//fixed by 2-round tournament, 21 fls (-10), FL ... [par not], change >=
      case "c70" => closure_70_settings()//fixed, 12 fls (-2), FL ... [par maybe little chance] v, change true false
      case "c73" => closure_73_settings()//fixed, 11 fls (-1), FL ... [par not], change >=
      case "c78" => closure_78_settings()// localization bad, 2 fault lines, but only one gets localized
      case "c86" => closure_86_settings()// problem in localization, run test case
      case "c123" => closure_123_settings()// localization bad, FL: too far from top FLs
      case "c126" => closure_126_settings()// fixed, but 4 failed => 2 failed, 42 fls (-30), FL ... [par not], delete statement
      case "ch1" => chart_1_settings()// fixed 2 round, 7 fls, FL ... [par not], change !=
      case "ch8" => chart_8_settings()// fixed 2 round, 1 fault loc, but need assume give manually since Ochiai doesnt give [par maybe], replace method call param
      case "ch7" => chart_7_settings()// change 2 params at 2 diff places [par maybe]
      case "ch20" => chart_20_settings()//FL: 95, change 2 params [par maybe little chance]

      //------------
      case "m3" => math_3_settings()
      //======
      // Trying: add condition to if
      case "c51" => closure_51_settings() //fixed //[par maybe], if condition adder
      case "c59" => closure_59_settings()
      case "c113" => closure_113_settings()
      case "c130" => closure_130_settings()
      //ase "m32" => _32_settings()
    }
  }

  def math_common_settings(version: String) ={// FL: 338
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v"+version+"-pre-fix"
    //val failing: String = "org.apache.commons.math.optimization.linear.SimplexSolverTest#testMath713NegativeVariable"
    val versionFolder="v"+version+"b"
    val location: String = RepairOptions.root+"/workspace/bug2run/Math/"+versionFolder
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/"+versionFolder
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/"+versionFolder
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/"+versionFolder+"/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/"+versionFolder+"/src/test"
    //RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m"+version+".txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m"+version+".fault"
  }

  def closure_common_settings(version: String) ={
    val folder: String = "closure-v"+version
    val versionFolder="v"+version+"b"
    //val failing: String = "com.google.javascript.jscomp.PeepholeFoldConstantsTest#testIssue821"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/"+versionFolder
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/"+versionFolder+"/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/"+versionFolder+"/test"
    //RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c"+version+".txt"
    RepairOptions.faultFile =  RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c"+version+".fault"
  }

  //def closure_42_settings() ={
  //  closure_common_settings("42")

  //}

  def gcd_settings() ={
    //val dependenciespath: String = "/home/xuanbach32bit/workspace/mygcd/junit-4.4.jar"
    val folder: String = "mygcd"
    val failing: String = "Gcd5Test;Gcd6Test"
    val location: String = RepairOptions.root + "/workspace/mygcd"
    RepairOptions.pkgInstrument = ""
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root + "/workspace/mygcd"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/mygcd"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/mygcd/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/mygcd/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/gcd.txt"
    RepairOptions.faultFile =RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/gcd.fault"
  }

  def lang_6_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v6-lang-pre-fix"
    val failing: String = "org.apache.commons.lang3.StringUtilsTest#testEscapeSurrogatePairs"
    val location: String = RepairOptions.root + "/workspace/bug2run/Lang/v6b"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root + "/workspace/bug2run/Lang/v6b"
    val sourceToRead="/src/main/java/"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/bug2run/Lang/v6b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/bug2run/Lang/v6b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/bug2run/Lang/v6b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/l6.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/l6.fault"
  }

  def lang_10_settings() ={
    val dependenciespath: String = RepairOptions.root +"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v10-lang-pre-fix"
    val failing: String = "org.apache.commons.lang3.time.FastDateFormat_ParserTest#testLANG_831;"+"org.apache.commons.lang3.time.FastDateParserTest#testLANG_831"
    val location: String = RepairOptions.root +"/workspace/bug2run/Lang/v10b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root +"/workspace/bug2run/Lang/v10b"
    val sourceToRead="/src/main/java"
    RepairOptions.homeFolder=RepairOptions.root +"/workspace/bug2run/Lang/v10b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder=RepairOptions.root +"/workspace/bug2run/Lang/v10b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root +"/workspace/bug2run/Lang/v10b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root +"/workspace/historicalfixv2/faultFilter/l10.txt"
    RepairOptions.faultFile = RepairOptions.root +"/workspace/historicalfixv2/faultFilter/faultFiles/l10.fault"
  }

  def lang_6test_settings() ={
    RepairOptions.dependencies = "/home/xuanbach32bit/Desktop/Apps/idea-IC-141.1532.4/lib/junit-4.11.jar"
    val folder: String = "v6-lang-pre-fix"
    val failing: String = "org.apache.commons.lang3.StringUtilsTest#testEscapeSurrogatePairs"
    val location: String = "/home/xuanbach32bit/workspace/v6b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.05
    val dirPath="//home/xuanbach32bit/workspace/v6b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder="/home/xuanbach32bit/workspace/v6b"
    RepairOptions.sourceFolder="/home/xuanbach32bit/workspace/v6b/src/main/java/"
    RepairOptions.testFolder="/home/xuanbach32bit/workspace/v6b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile="/home/xuanbach32bit/workspace/historicalfixv2/faultFilter/l6test.txt"
    //RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/l6.fault"
  }


  def lang_43_settings() ={
    val dependenciespath: String = RepairOptions.root +"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v43-lang-pre-fix"
    val failing: String = "org.apache.commons.lang.text.ExtendedMessageFormatTest#testEscapedQuote_LANG_477"
    val location: String = RepairOptions.root +"/workspace/bug2run/Lang/v43b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root +"/workspace/bug2run/Lang/v43b"
    val sourceToRead="/src/java/"
    RepairOptions.homeFolder=RepairOptions.root +"/workspace/bug2run/Lang/v43b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder=RepairOptions.root +"/workspace/bug2run/Lang/v43b/src/java/"
    RepairOptions.testFolder=RepairOptions.root +"/workspace/bug2run/Lang/v43b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root +"/workspace/historicalfixv2/faultFilter/l43.txt"
    RepairOptions.faultFile = RepairOptions.root +"/workspace/historicalfixv2/faultFilter/faultFiles/l43.fault"
  }

  def lang_51_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v51-lang-pre-fix"
    val failing: String = "org.apache.commons.lang.BooleanUtilsTest#test_toBoolean_String"
    val location: String = RepairOptions.root + "/workspace/bug2run/Lang/v51b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root + "/workspace/bug2run/Lang/v51b"
    val sourceToRead="/src/java/"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/bug2run/Lang/v51b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/bug2run/Lang/v51b/src/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/bug2run/Lang/v51b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/l51.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/l51.fault"
  }

  def lang_57_settings() ={// FL: 223, fixed like human but still fail test org.apache.commons.lang.LocaleUtilsTest#testCountriesByLanguage
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v57-lang-pre-fix"
    val failing: String = "org.apache.commons.lang.LocaleUtilsTest#testAvailableLocaleSet;" +
    "org.apache.commons.lang.LocaleUtilsTest#testIsAvailableLocale;" + "org.apache.commons.lang.LocaleUtilsTest#testAvailableLocaleList;" /*+"org.apache.commons.lang.LocaleUtilsTest#testCountriesByLanguage;"*/ + "org.apache.commons.lang.LocaleUtilsTest#testLocaleLookupList_LocaleLocale;" +"org.apache.commons.lang.LocaleUtilsTest#testLanguagesByCountry;" + "org.apache.commons.lang.LocaleUtilsTest#testToLocale_1Part;" + "org.apache.commons.lang.LocaleUtilsTest#testToLocale_2Part;" + "org.apache.commons.lang.LocaleUtilsTest#testToLocale_3Part;" + "org.apache.commons.lang.LocaleUtilsTest#testLocaleLookupList_Locale;" + "org.apache.commons.lang.LocaleUtilsTest#testConstructor"
    val location: String = RepairOptions.root+"/workspace/bug2run/Lang/v57b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root+"/workspace/bug2run/Lang/v57b"
    val sourceToRead="/src/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Lang/v57b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/test-classes"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Lang/v57b/src/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Lang/v57b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/l57.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/l57.fault"
  }

  def lang_58_settings() ={// FL: 452 or 455,
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v58-lang-pre-fix"
    val failing: String = "org.apache.commons.lang.math.NumberUtilsTest#testLang300"
    val location: String = RepairOptions.root+"/workspace/bug2run/Lang/v58b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root+"/workspace/bug2run/Lang/v58b"
    val sourceToRead="/src/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Lang/v58b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/test-classes"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Lang/v58b/src/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Lang/v58b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/l58.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/l58.fault"
  }

  def lang_59_settings() ={// FL: 884,
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v59-lang-pre-fix"
    val failing: String = "org.apache.commons.lang.text.StrBuilderAppendInsertTest#testLang299"
    val location: String = RepairOptions.root+"/workspace/bug2run/Lang/v59b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root+"/workspace/bug2run/Lang/v59b"
    val sourceToRead="/src/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Lang/v59b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/tests"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Lang/v59b/src/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Lang/v59b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/tests"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/l59.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/l59.fault"
  }

  def time_19_settings() ={//FL: 900
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v19-time-pre-fix"
    val failing: String = "org.joda.time.TestDateTimeZoneCutover#testDateTimeCreation_london"
    val location: String = RepairOptions.root+"/workspace/bug2run/Time/v19b"
    RepairOptions.pkgInstrument = "org.joda.time"
    val thfl: Double = 0.05
    val dirPath=RepairOptions.root+"/workspace/bug2run/Time/v19b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Time/v19b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Time/v19b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Time/v19b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="build/classes"
    RepairOptions.testClassDir="build/tests"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/t19.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/t19.fault"

  }

  def expMut_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "arithmeticTestMutation"
    val failing: String = "E2Test"
    val location: String = RepairOptions.root + "/workspace/arithmeticTestMutation"
    RepairOptions.pkgInstrument = ""
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root + "/workspace/arithmeticTestMutation"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/arithmeticTestMutation"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/arithmeticTestMutation/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/arithmeticTestMutation/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/expMut.txt"
  }

  def math_2_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v2-pre-fix"
    val failing: String = "org.apache.commons.math3.distribution.HypergeometricDistributionTest#testMath1021"
    val location: String = RepairOptions.root + "/workspace/bug2run/Math/v2b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root + "/workspace/bug2run/Math/v2b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/bug2run/Math/v2b"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/bug2run/Math/v2b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/bug2run/Math/v2b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/m2.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/m2.fault"
  }

  def math_5_settings() ={
    val dependenciespath: String = RepairOptions.root+ "/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "pre-fix"
    val failing: String = "org.apache.commons.math3.complex.ComplexTest#testReciprocalZero" //
    val location: String = RepairOptions.root+ "workspace/bug2run/Math/v5b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v5b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+ "/workspace/bug2run/Math/v5b"
    RepairOptions.sourceFolder=RepairOptions.root+ "/workspace/bug2run/Math/v5b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+ "/workspace/bug2run/Math/v5b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/m5.txt"
    RepairOptions.faultFile = RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/faultFiles/m5.fault"
  }

  def math_11_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "math-v11-pre-fix"
    val failing: String = "org.apache.commons.math3.distribution.MultivariateNormalDistributionTest#testUnivariateDistribution" //
    val location: String = RepairOptions.root + "/workspace/bug2run/Math/v11b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root + "/workspace/bug2run/Math/v11b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/bug2run/Math/v11b"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/bug2run/Math/v11b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/bug2run/Math/v11b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/m11.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/m11.fault"
  }

  def math_22_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v22-pre-fix"
    val failing: String = "org.apache.commons.math3.distribution.FDistributionTest#testIsSupportLowerBoundInclusive;"+"org.apache.commons.math3.distribution.UniformRealDistributionTest#testIsSupportUpperBoundInclusive"
    val location: String = RepairOptions.root+ "/workspace/bug2run/Math/v22b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v22b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v22b"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v22b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v22b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m22.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m22.fault"
  }

  def math_32_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v32-pre-fix"
    val failing: String = "org.apache.commons.math3.geometry.euclidean.threed.PolyhedronsSetTest#testIssue780"
    val location: String = RepairOptions.root+ "/workspace/bug2run/Math/v32b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v32b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v32b"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v32b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v32b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "target/classes"
    RepairOptions.testClassDir = "target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m32.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m32.fault"
  }

  def math_33_settings() ={// FL: 338
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v33-pre-fix"
    val failing: String = "org.apache.commons.math3.optimization.linear.SimplexSolverTest#testMath781"
    val location: String = RepairOptions.root+"/workspace/bug2run/Math/v33b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v33b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v33b"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v33b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v33b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m33.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m33.fault"
  }

  def math_34_settings() ={// FL: 338
    math_common_settings("34")
    RepairOptions.failingTests="org.apache.commons.math3.genetics.ListPopulationTest#testIterator"
  }

  //---------------------
  def math_3_settings() ={
    math_common_settings("3")
    RepairOptions.failingTests="org.apache.commons.math3.util.MathArraysTest#testLinearCombinationWithSingleElementArray"
  }

  def math_9_settings() ={
    math_common_settings("9")
    RepairOptions.failingTests="org.apache.commons.math3.geometry.euclidean.threed.LineTest#testRevert"
  }

  def math_10_settings() ={
    math_common_settings("10")
    RepairOptions.failingTests="org.apache.commons.math3.analysis.differentiation.DerivativeStructureTest#testAtan2SpecialCases"
  }
  //======================
  def math_42_settings() ={// FL: 338
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v42-pre-fix"
    val failing: String = "org.apache.commons.math.optimization.linear.SimplexSolverTest#testMath713NegativeVariable"
    val location: String = RepairOptions.root+"/workspace/bug2run/Math/v42b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v42b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v42b"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v42b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v42b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m42.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m42.fault"
  }

  def math_46_settings() ={// FL: 338
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.12.jar"
    val folder: String = "v46-pre-fix"
    val failing: String = "org.apache.commons.math.complex.ComplexTest#testAtanI;org.apache.commons.math.complex.ComplexTest#testDivideZero"
    val location: String = RepairOptions.root+"/workspace/bug2run/Math/v46b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v46b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v46b"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v46b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v46b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m46.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m46.fault"
  }


  def math_50_settings() ={
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v50-pre-fix"
    val failing: String = "org.apache.commons.math.analysis.solvers.RegulaFalsiSolverTest#testIssue631"
    val location: String = RepairOptions.root+"/workspace/bug2run/Math/v50b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v50b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v50b/"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v50b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v50b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m50.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m50.fault"
  }

  def math_53_settings() ={
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v53-pre-fix"
    val failing: String = "org.apache.commons.math.complex.ComplexTest#testAddNaN"
    val location: String = RepairOptions.root+"/workspace/bug2run/Math/v53b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v53b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v53b"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v53b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v53b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m53.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m53.fault"
  }

  def math_70_settings() ={
    val dependenciespath: String = RepairOptions.root+"/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v70-pre-fix"
    val failing: String = "org.apache.commons.math.analysis.solvers.BisectionSolverTest#testMath369"
    val location: String = RepairOptions.root+"/workspace/bug2run/Math/v70b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root+"/workspace/bug2run/Math/v70b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root+"/workspace/bug2run/Math/v70b"
    RepairOptions.sourceFolder=RepairOptions.root+"/workspace/bug2run/Math/v70b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root+"/workspace/bug2run/Math/v70b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root+"/workspace/historicalfixv2/faultFilter/m70.txt"
    RepairOptions.faultFile = RepairOptions.root+"/workspace/historicalfixv2/faultFilter/faultFiles/m70.fault"
  }


  def math_72_settings() ={//FLs: 115, 127. But can only localize 115
    val dependenciespath: String = RepairOptions.root +"/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v72-pre-fix"
    val failing: String = "org.apache.commons.math.analysis.solvers.BrentSolverTest#testRootEndpoints"
    val location: String = RepairOptions.root +"/workspace/bug2run/Math/v72b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root +"/workspace/bug2run/Math/v72b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root +"/workspace/bug2run/Math/v72b"
    RepairOptions.sourceFolder=RepairOptions.root +"/workspace/bug2run/Math/v72b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root +"/workspace/bug2run/Math/v72b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root +"/workspace/historicalfixv2/faultFilter/m72.txt"
    RepairOptions.faultFile =  RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/m72.fault"
  }

  def math_82_settings() ={
    val dependenciespath: String =  RepairOptions.root +"/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v82-pre-fix"
    val failing: String = "org.apache.commons.math.optimization.linear.SimplexSolverTest#testMath288"
    val location: String =  RepairOptions.root +"/workspace/bug2run/Math/v82b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath= RepairOptions.root +"/workspace/bug2run/Math/v82b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder= RepairOptions.root +"/workspace/bug2run/Math/v82b"
    RepairOptions.sourceFolder= RepairOptions.root +"/workspace/bug2run/Math/v82b/src/main/java/"
    RepairOptions.testFolder= RepairOptions.root +"/workspace/bug2run/Math/v82b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root +"/workspace/historicalfixv2/faultFilter/m82.txt"
    RepairOptions.faultFile =  RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/m82.fault"
  }

  def math_85_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v85-pre-fix"
    val failing: String = "org.apache.commons.math.distribution.NormalDistributionTest#testMath280"
    val location: String = RepairOptions.root + "/workspace/bug2run/Math/v85b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root + "/workspace/bug2run/Math/v85b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/bug2run/Math/v85b"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/bug2run/Math/v85b/src/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/bug2run/Math/v85b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/m85.txt"
    RepairOptions.faultFile =  RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/m85.fault"

  }

  def math_98_settings() ={
    val dependenciespath: String = RepairOptions.root + "/workspace/mygcd/lib/junit-4.4.jar"
    val folder: String = "v98-pre-fix"
    val failing: String = "org.apache.commons.math.linear.BigMatrixImplTest#testMath209;org.apache.commons.math.linear.RealMatrixImplTest#testMath209"
    val location: String = RepairOptions.root + "/workspace/bug2run/Math/v98b"
    RepairOptions.pkgInstrument =  "org.apache.commons"
    val thfl: Double = 0.5
    val dirPath=RepairOptions.root + "/workspace/bug2run/Math/v98b"
    val sourceToRead="/src/main/java/"
    RepairOptions.homeFolder=RepairOptions.root + "/workspace/bug2run/Math/v98b"
    RepairOptions.sourceFolder=RepairOptions.root + "/workspace/bug2run/Math/v98b/src/main/java/"
    RepairOptions.testFolder=RepairOptions.root + "/workspace/bug2run/Math/v98b/src/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir="target/classes"
    RepairOptions.testClassDir="target/test-classes"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile=RepairOptions.root + "/workspace/historicalfixv2/faultFilter/m98.txt"
    RepairOptions.faultFile =  RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/m85.fault"
  }

  def closure_10_settings() ={
    val folder: String = "closure-v10"
    val failing: String = "com.google.javascript.jscomp.PeepholeFoldConstantsTest#testIssue821"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v10b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v10b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v10b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c10.txt"
    RepairOptions.faultFile =  RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c10.fault"
  }

  def closure_14_settings() ={
    val folder: String = "closure-v14"
    val failing: String = "com.google.javascript.jscomp.CheckMissingReturnTest#testIssue779;" + "com.google.javascript.jscomp.ControlFlowAnalysisTest#testDeepNestedFinally;" + "com.google.javascript.jscomp.ControlFlowAnalysisTest#testDeepNestedBreakwithFinally"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v14b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v14b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v14b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c14.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c14.fault"
  }

  def closure_18_settings() ={
    val folder: String = "closure-v14"
    val failing: String = "com.google.javascript.jscomp.IntegrationTest#testDependencySorting"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v18b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v18b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v18b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c18.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c18.fault"
  }

  def closure_31_settings() ={//FL 1284
    val folder: String = "closure-v31"
    val failing: String = "com.google.javascript.jscomp.CommandLineRunnerTest#testDependencySortingWhitespaceMode"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v31b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v31b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v31b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c31.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c31.fault"
  }

  def closure_42_settings() ={
    closure_common_settings("42")
    val failing: String = "com.google.javascript.jscomp.parsing.ParserTest#testForEach"
  }

  def closure_51_settings() ={//FL 241
    val folder: String = "closure-v51"
    val failing: String = "com.google.javascript.jscomp.CodePrinterTest#testIssue582"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v51b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v51b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v51b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c51.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c51.fault"
  }

  def closure_59_settings() ={//FL 255
    val folder: String = "closure-v59"
    val failing: String = "com.google.javascript.jscomp.CommandLineRunnerTest#testCheckGlobalThisOff"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v59b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v59b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v59b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c59.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c59.fault"
  }

  def closure_62_settings() ={
    val folder: String = "closure-v62"
    val failing: String = "com.google.javascript.jscomp.LightweightMessageFormatterTest#testFormatErrorSpaceEndOfLine1;"+"com.google.javascript.jscomp.LightweightMessageFormatterTest#testFormatErrorSpaceEndOfLine2"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v62b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v62b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v62b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c62.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c62.fault"
  }

  def closure_70_settings() ={
    val folder: String = "closure-v70"
    val failing: String = "com.google.javascript.jscomp.LooseTypeCheckTest#testDuplicateLocalVarDecl;"+"com.google.javascript.jscomp.LooseTypeCheckTest#testFunctionArguments13;"+
      "com.google.javascript.jscomp.TypeCheckTest#testScoping12;"+"com.google.javascript.jscomp.TypeCheckTest#testDuplicateLocalVarDecl;"+"com.google.javascript.jscomp.TypeCheckTest#testFunctionArguments13"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v70b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v70b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v70b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c70.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c70.fault"
  }

  def closure_73_settings() ={
    val folder: String = "closure-v73"
    val failing: String = "com.google.javascript.jscomp.CodePrinterTest#testUnicode"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v73b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v73b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v73b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c73.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c73.fault"
  }

  def closure_78_settings() ={// fault lines: 715 722
    val folder: String = "closure-v78"
    val failing: String = "com.google.javascript.jscomp.PeepholeFoldConstantsTest#testFoldArithmetic"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v78b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v78b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v78b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c78.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c78.fault"
  }

  def closure_86_settings() ={
    val folder: String = "closure-v86"
    val failing: String = "com.google.javascript.jscomp.NodeUtilTest#testLocalValue1;"+"com.google.javascript.jscomp.PureFunctionIdentifierTest#testLocalizedSideEffects8;"+
      "com.google.javascript.jscomp.PureFunctionIdentifierTest#testLocalizedSideEffects9;"+"com.google.javascript.jscomp.PureFunctionIdentifierTest#testAnnotationInExterns_new4;"+"com.google.javascript.jscomp.PureFunctionIdentifierTest#testAnnotationInExterns_new6;" + "com.google.javascript.jscomp.PureFunctionIdentifierTest#testIssue303b;"+"com.google.javascript.jscomp.PureFunctionIdentifierTest#testIssue303"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v86b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v86b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v86b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c86.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c86.fault"
  }

  def closure_113_settings() ={//FL 329
  val folder: String = "closure-v113"
    val failing: String = "com.google.javascript.jscomp.VarCheckTest#testNoUndeclaredVarWhenUsingClosurePass"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v113b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v113b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v113b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c113.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c113.fault"
  }

  def closure_123_settings() ={//FL: 287 289 too far from the top FLs
    val folder: String = "closure-v123"
    val failing: String = "com.google.javascript.jscomp.CodePrinterTest#testPrintInOperatorInForLoop"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v123b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v123b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v123b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c123.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c123.fault"
  }

  def closure_126_settings() ={
    val folder: String = "closure-v126"
    val failing: String = "com.google.javascript.jscomp.MinimizeExitPointsTest#testDontRemoveBreakInTryFinally;" + "com.google.javascript.jscomp.MinimizeExitPointsTest#testFunctionReturnOptimization"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v126b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v126b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v126b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c126.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c126.fault"
  }

  def closure_130_settings() ={//FL 329
    val folder: String = "closure-v130"
    val failing: String = "com.google.javascript.jscomp.CollapsePropertiesTest#testIssue931"
    RepairOptions.pkgInstrument =  "com.google.javascript.jscomp"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Closure/v130b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Closure/v130b/src"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Closure/v130b/test"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build/classes"
    RepairOptions.testClassDir = "build/test"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/c130.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/c130.fault"
  }

  def chart_1_settings() ={//FL: 1797, change != to ==
    val folder: String = "chart-v1"
    val failing: String = "org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests#test2947660"
    RepairOptions.pkgInstrument =  "org.jfree.chart"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Chart/v1b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Chart/v1b/source"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Chart/v1b/tests"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build-classes"
    RepairOptions.testClassDir = "build-tests"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/ch1.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/ch1.fault"
  }

  def chart_8_settings() ={//FL: 175, change function call param
    val folder: String = "chart-v8"
    val failing: String = "org.jfree.data.time.junit.WeekTests#testConstructor"
    RepairOptions.pkgInstrument =  "org.jfree.data"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Chart/v8b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Chart/v8b/source"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Chart/v8b/tests"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build-classes"
    RepairOptions.testClassDir = "build-tests"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/ch8.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/ch8.fault"
  }

  def chart_7_settings() ={//FL: 300, 302, change function call params
    val folder: String = "chart-v7"
    val failing: String = "org.jfree.data.time.junit.TimePeriodValuesTests#testGetMaxMiddleIndex"
    RepairOptions.pkgInstrument =  "org.jfree.data"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Chart/v7b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Chart/v7b/source"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Chart/v7b/tests"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build-classes"
    RepairOptions.testClassDir = "build-tests"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/ch7.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/ch7.fault"
  }

  def chart_20_settings() ={//FL: 95, change function call 2 params
    val folder: String = "chart-v20"
    val failing: String = "org.jfree.chart.plot.junit.ValueMarkerTests#test1808376"
    RepairOptions.pkgInstrument =  "org.jfree.chart"
    RepairOptions.homeFolder= RepairOptions.root + "/workspace/bug2run/Chart/v20b"
    RepairOptions.localLibs= RepairOptions.homeFolder+"/build/lib"+";"+RepairOptions.homeFolder+"/lib"
    RepairOptions.sourceFolder= RepairOptions.root + "/workspace/bug2run/Chart/v20b/source"
    RepairOptions.testFolder= RepairOptions.root + "/workspace/bug2run/Chart/v20b/tests"
    RepairOptions.failingTests=failing
    RepairOptions.appClassDir = "build-classes"
    RepairOptions.testClassDir = "build-tests"
    //RepairOptions.fixFile=RepairOptions.root + "/workspace/arithmeticTestMutation/fixLine.txt"
    RepairOptions.faultFilterFile= RepairOptions.root+ "/workspace/historicalfixv2/faultFilter/ch20.txt"
    RepairOptions.faultFile = RepairOptions.root + "/workspace/historicalfixv2/faultFilter/faultFiles/ch20.fault"
  }
}
