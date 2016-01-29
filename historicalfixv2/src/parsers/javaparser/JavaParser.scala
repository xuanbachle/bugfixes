package parsers.javaparser

import java.io.{BufferedReader, File, FileReader, IOException}
import java.util
import java.util.Random

import _root_.util.FileFolderUtils
import localizations.{JavaNodeIden, LineIden, Identifier, PrecomputedInfor}
import mainscala.RepairOptions
import org.eclipse.jdt.core.JavaCore
import org.eclipse.osgi.framework.adaptor.FilePath
import parsers.AbstractParser
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite
import org.eclipse.jdt.core.dom._
import repair.mutationoperators._
import myLib.{MyjavaLib, Lib}
import repair.representation.GenProgIndividual

import scala.collection.mutable.ArrayBuffer

/**
 * Created by xuanbach32bit on 4/22/15.
 */

object JavaParser extends AbstractParser[CompilationUnit]{

  def parse[B >:CompilationUnit](str: String, filePath: String): CompilationUnit = {
    val file = new File(filePath)
    var parser: ASTParser = ASTParser.newParser(AST.JLS4)
    //parser.setEnvironment(RepairOptions.libs,Array(file.getParent, file.getParentFile.getParent), null, true)
    parser.setEnvironment(RepairOptions.getDependenciesList().toArray(Array[String]()),Array(new File(RepairOptions.sourceFolder).getParent), null, true)
    parser.setUnitName(FileFolderUtils.relativePath(new File(RepairOptions.homeFolder).getParent, filePath))
    import scala.collection.JavaConversions._
    //parser.setEnvironment(Array(str), Array(str),Array("UTF-8"), true)
    parser.setSource(str.toCharArray)
    parser.setKind(ASTParser.K_COMPILATION_UNIT)
    parser.setResolveBindings(true)
    parser.setBindingsRecovery(true)
    parser.setStatementsRecovery(true)

    val options = JavaCore.getOptions();
    JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
    parser.setCompilerOptions(options);

    try {
      val root = parser.createAST(null)
      val cu: CompilationUnit = root.asInstanceOf[CompilationUnit]
      //cu.recordModifications
      assert(cu != null)
      return cu
    }catch {
      case e: Throwable => {
        println(filePath)
        e.printStackTrace()
        sys.error("CU is null when parsing!!!"+filePath)
      }
    }
  }

  def getCompilationUnit(fileName: String): CompilationUnit ={
    globalASTs.get(fileName)
  }

  def getPackageName[B >:CompilationUnit](cu: CompilationUnit, filePath: String): String = {
    val pkg=cu.getPackage
    val file = new File(filePath)
    if(pkg!=null) {
      val pkg_name=pkg.toString.stripLineEnd.split(" ")(1).split(";")(0)
      return pkg_name + "." + file.getName.split("\\.")(0)
    }else{
      return file.getName.split("\\.")(0) // this is to strip the .java at the end
    }
  }

  @throws(classOf[IOException])
  def ParseFilesInDir(dirPath: String) {
    assert(this.prec!=null)
    val root: File = new File(dirPath)
    val files: java.util.List[File] = MyjavaLib.walk(dirPath,".java",new java.util.ArrayList[File])
    ParseFiles(files)
  }

  def batchParsingInDir(dirPath: String): Unit ={
    assert(this.prec!=null)
    val root: File = new File(dirPath)
    val files: java.util.List[File] = MyjavaLib.walk(dirPath,".java",new java.util.ArrayList[File])
    batchParseFiles(files)
  }

  override def batchParse(sourceFilePaths: Seq[String], faultFiles: util.HashMap[String, Boolean]): Unit={

    val astParser = ASTParser.newParser(AST.JLS4);

      // set up libraries (.jar, .class or .java)
    astParser.setEnvironment(RepairOptions.getDependenciesList().toArray(Array[String]()),Array(new File(RepairOptions.sourceFolder).getParent), null, true)

    astParser.setResolveBindings(true);

      // with Bingding Recovery on, the compiler can detect
      // binding among the set of compilation units
      astParser.setBindingsRecovery(true);

      // set default options, especially for Java 1.5
      val options = JavaCore.getOptions();
      JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
      astParser.setCompilerOptions(options);


       val requestor = new FileASTRequestor() {
         override def acceptAST(sourceFilePath: String, ast: CompilationUnit) {
           val fileName=convertFilePath2FilePackageName(sourceFilePath)
           // if file being read is a fault file, keep its content
           // println("Processed FileName"+fileName)
           if(faultFiles.containsKey(fileName)) {
              readFileToString(sourceFilePath, faultFiles)
           }
           globalASTs.put(getPackageName(ast,sourceFilePath), ast)
        }

         override def acceptBinding(bindingKey: String, binding: IBinding) {
          // do nothing
          // System.out.println("Accept Binding:... " + bindingKey);
          // System.out.println(binding);
        }
      };

      astParser.createASTs(sourceFilePaths.toArray,
        null, 			/*  use default encoding */
        Array[String](), /* no binding key */
        requestor,
        null			/* no IProgressMonitor */
      );
  }

  def setPrecomputedInfor(prec:PrecomputedInfor)={
    this.prec=prec
  }

  @throws(classOf[IOException])
  def main(args: Array[String]) {
    System.out.println("Running...")
    this.prec = PrecomputedInfor.computeFixFaultSpaces_old()
    val dirs: File = new File("/home/dxble/MyWorkSpace/mygcd/src/main/java/")
    val dirPath: String = "/Users/larcuser/workspace/mygcd/src/main/java" //"/home/dxble/MyWorkSpace/mygcd/src/main/java/"  //dirs.getCanonicalPath
    RepairOptions.homeFolder="/home/dxble/MyWorkSpace/mygcd"
    RepairOptions.sourceFolder="/home/dxble/MyWorkSpace/mygcd/src/main/java/"
    RepairOptions.testFolder="/home/dxble/MyWorkSpace/mygcd/src/test"
    ParseFilesInDir(dirPath)
    val filename: String = "Gcd"
    val cu: CompilationUnit = globalASTs.get(filename)

    val id: LineIden = new LineIden (filename,7,1.0)
    val id2: LineIden  = new LineIden (filename,8,1.0)
    val id3: LineIden  = new LineIden (filename,10,1.0)
    val id4: LineIden  = new LineIden (filename,100,1.0)
    val id5: LineIden  = new LineIden (filename,15,1.0)
    val id6: LineIden  = new LineIden (filename,22,1.0)
    val id7: LineIden  = new LineIden (filename,18,1.0)
    //var rew: ASTRewrite = ASTRewriteUtils.getASTRewrite(cu)
    //var found: ASTNode = ASTRewriteUtils.findNode(cu, id5)
    //println(found)
    //var found2: ASTNode = ASTRewriteUtils.findNode(cu, id6)
    //println(found2)
    var found: ASTNode = ASTRewriteUtils.findNode(cu, id7)
    /*val mc = found4.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[MethodInvocation]
    val mc_exp=mc.getExpression
    println(mc_exp.resolveTypeBinding().getName)
    val x=mc.getAST.newMethodInvocation()
    x.setName(mc.getAST.newSimpleName("xxxxx"))
    println(x)
    println(found4.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[MethodInvocation].getName)*/

    //For finding possible method call invokers
    /*val iden = new JavaNodeIden("", found4,0.0)
    val mf = new MethodAndFieldCollector(iden)
    found4.getRoot.asInstanceOf[CompilationUnit].accept(mf)
    println(mf.possibleInvokers)
    val jvNode = new JavaNodeIden("",found4.asInstanceOf[ExpressionStatement].getExpression,0.0)
    val mutator = new ExpMethodCallInvokerReplacement(new Random()).mutateExp(jvNode)//ExpressionMutActions.chooseAppropriateExpMutAction(jvNode, new Random())
    val expMut = mutator//mutator.mutateExp(jvNode)
    println(expMut)*/

    //For testing exp mutation
    /*var found3: ASTNode = ASTRewriteUtils.findNode(cu, id7)
    val exp=found3.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[Assignment].getRightHandSide
    println(exp)
    val javaNode = JavaNodeIden("", exp, 0.0)
    val mutator = ExpressionMutActions.chooseAppropriateExpMutAction(javaNode, new Random())
    val expMut = mutator.mutateExp(javaNode)
    println(expMut)*/

    //For testing statement collector
    /*val compilationUnitCurrentNode = JavaParser.globalASTs.get("Gcd")
    val curnode = new JavaNodeIden("Gcd",found.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[MethodInvocation], 0.9)
    import scala.collection.JavaConversions._
    for ( arg <-found.asInstanceOf[ExpressionStatement].getExpression.asInstanceOf[MethodInvocation].arguments()){
      println("xxxx"+arg.getClass())
    }*/

    //val checkInScopeNode = ASTRewriteUtils.findNode(cu, id6)
    /*val compilationUnitStatements = new StatementCollector("Gcd",/*prec.getFaultSpace()*/ new ArrayBuffer[Identifier[Any]](), curnode, checkScope = true)
    compilationUnitCurrentNode.accept(compilationUnitStatements)
    println(compilationUnitStatements.statements)
    println(curnode.getScope())
    val mutator = new ExpMethodCallParameterReplacement(new Random())
    println("Mutated: "+mutator.mutateExp(curnode))*/

    val expFound = new JavaNodeIden("Gcd", found, 1.0)
    expFound.hasExps()
    val exps=expFound.getExps()
    val mut = new ExpInfixDeletion
    val todel = new JavaNodeIden("Gcd",exps(1).getJavaNode(),1.0 )
    println(mut.mutateExp(todel))
    //For testing cached individuals
    /*val javaNode = JavaNodeIden("", found, 0.0)
    val javaNode2 = JavaNodeIden("", found2, 0.0)
    val mutGene1 = new RepMut(javaNode, javaNode)
    val mutGene2 = new RepMut(javaNode, javaNode2)
    val g1 = new ArrayBuffer[MutGene]()
    g1.append(mutGene1)
    val g2 = new ArrayBuffer[MutGene]()
    g2.append(mutGene2)
    val indiv1 = new GenProgIndividual(g1)
    val indiv2 = new GenProgIndividual(g2)
    println("Equal indiv: "+indiv1.equals(indiv1))
    println("Equal indiv: "+indiv1.equals(indiv2))
    ChoosenIndividuals.addToGeneratedIndividual(indiv1)
    ChoosenIndividuals.addToGeneratedIndividual(indiv1)
    ChoosenIndividuals.addToGeneratedIndividual(indiv2)
    println(ChoosenIndividuals.getMutatedIndividuals().size)*/

    /*rew = ASTRewriteUtils.removeNode(ASTRewriteUtils.getASTRewrite(cu), found)
    found = ASTRewriteUtils.findNode(cu, id2)
    val found2: ASTNode = ASTRewriteUtils.findNode(cu, id3)
    //found = ASTRewriteUtils.findNode(cu, id4.asInstanceOf[Identifier[AnyRef]])
    rew = ASTRewriteUtils.appendNode(rew, found2, found)

    ASTRewriteUtils.applyEdits( rew, filename)
    cu.accept(new printASTVisitor(cu));

    found= ASTRewriteUtils.findNode(cu, id)
    */
  }
}

