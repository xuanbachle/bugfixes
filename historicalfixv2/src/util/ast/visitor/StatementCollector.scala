package util.ast.visitor

import java.util.List

import localizations.{JavaNodeIden, Identifier}
import localizations.fixlocalization.methodsimilarity.MethodVector
import org.eclipse.jdt.core.dom.{ITypeBinding, IBinding, ASTNode, ASTVisitor}
import localizations.Identifier
import util.ast.ASTUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by dxble on 8/4/15.
 */

class StatementCollector(fileName: String, exceptStmts: Seq[Identifier[Any]], currentNode: Identifier[Any], checkScope: Boolean = true, methodSimilarity: Boolean = true) extends ASTVisitor {
    import org.eclipse.jdt.core.dom.{AbstractTypeDeclaration, AnnotationTypeDeclaration, AnnotationTypeMemberDeclaration, AnonymousClassDeclaration, ArrayAccess, ArrayCreation, ArrayInitializer, ArrayType, AssertStatement, Assignment, ASTNode, Block, BlockComment, BooleanLiteral, BreakStatement, CastExpression, CatchClause, CharacterLiteral, ClassInstanceCreation, CompilationUnit, ConditionalExpression, ConstructorInvocation, ContinueStatement, DoStatement, EmptyStatement, EnhancedForStatement, EnumConstantDeclaration, EnumDeclaration, ExpressionStatement, FieldAccess, FieldDeclaration, ForStatement, IfStatement, ImportDeclaration, InfixExpression, Initializer, InstanceofExpression, Javadoc, LabeledStatement, LineComment, MarkerAnnotation, MemberRef, MemberValuePair, MethodDeclaration, MethodInvocation, MethodRef, MethodRefParameter, Modifier, NormalAnnotation, NullLiteral, NumberLiteral, PackageDeclaration, ParameterizedType, ParenthesizedExpression, PostfixExpression, PrefixExpression, PrimitiveType, QualifiedName, QualifiedType, ReturnStatement, SimpleName, SimpleType, SingleMemberAnnotation, SingleVariableDeclaration, StringLiteral, SuperConstructorInvocation, SuperFieldAccess, SuperMethodInvocation, SwitchCase, SwitchStatement, SynchronizedStatement, TagElement, TextElement, ThisExpression, ThrowStatement, TryStatement, TypeDeclaration, TypeDeclarationStatement, TypeLiteral, TypeParameter, UnionType, VariableDeclarationExpression, VariableDeclarationFragment, VariableDeclarationStatement, WhileStatement, WildcardType}
    val statements = new ArrayBuffer[Identifier[Any]]()
    val fieldNames = new mutable.HashMap[String, ArrayBuffer[ITypeBinding]]
    val currentMethodVars = new mutable.HashMap[String, ArrayBuffer[ITypeBinding]]
    val currentVarsInForLoop = new mutable.HashMap[String, ArrayBuffer[ITypeBinding]]
    var currentNodeMethodVector: MethodVector = null
    var anotherMethodVector: MethodVector = null
    var pendingStatements : mutable.HashMap[MethodVector, ArrayBuffer[Identifier[Any]]] = new  mutable.HashMap[MethodVector, ArrayBuffer[Identifier[Any]]]()
    var visitingFaultyMethod = false
    var retrievedVarsInForStmt = false
    var visitedFaultyMethod = false

    def astNode2Identifier(aSTNode: ASTNode): Identifier[Any] ={
      return new JavaNodeIden(fileName, aSTNode, 0.0)
    }

    def exitsInExceptStmts(toCompare: Identifier[Any]): Boolean ={
        for(eachIden <- exceptStmts){
            if(eachIden.getFileName().equals(toCompare.getFileName()) && eachIden.getLine() == toCompare.getLine()){
                return true
            }
        }
        return false
    }

    private def collectVarsInBoundedForStatement(): Unit ={
        def getBoundedForStmt(): ForStatement = {
            if(currentNode.getJavaNode().isInstanceOf[ForStatement])
                return currentNode.getJavaNode().asInstanceOf[ForStatement]

            var parent = currentNode.getJavaNode().getParent
            while (parent != null && !parent.isInstanceOf[MethodDeclaration] && !parent.isInstanceOf[CompilationUnit]) {
                if (parent.isInstanceOf[ForStatement])
                    return parent.asInstanceOf[ForStatement]
                parent = parent.getParent
            }
            return null
        }
        if(!retrievedVarsInForStmt) {
            retrievedVarsInForStmt = true
            var boundedForStmt = getBoundedForStmt()

            if(boundedForStmt != null) {
                val inits = boundedForStmt.initializers()
                import scala.collection.JavaConversions._
                for(eachInit <- inits){
                    val varDecFrags=eachInit.asInstanceOf[VariableDeclarationExpression].fragments()
                    import scala.collection.JavaConversions._
                    for(eachFrag <-varDecFrags) {
                        val v = eachFrag.asInstanceOf[VariableDeclarationFragment]
                        currentVarsInForLoop.get(v.getName.getIdentifier) match {
                            case None => {
                                val types = new ArrayBuffer[ITypeBinding]
                                types.append(v.getName.resolveTypeBinding())
                                currentVarsInForLoop.put(v.getName.getIdentifier, types)
                            }
                            case Some(t) => t.append(v.getName.resolveTypeBinding())
                        }
                    }
                }
            }
        }
    }

    private def addPendingStatements(methVec: MethodVector, iden: Identifier[Any]) ={
        pendingStatements.get(methVec) match {
            case None => {
                val arr = new ArrayBuffer[Identifier[Any]]()
                arr.append(iden)
                pendingStatements.put(methVec, arr)
            }
            case Some(v) => {
                v.append(iden)
            }
        }
    }

    def addStatement(node: ASTNode) ={
        val iden = astNode2Identifier(node)
        //if(currentNode.getScope().isEmpty)
        //    throw new RuntimeException("Scope for this statement is NULL! "+currentNode)

        if(!exitsInExceptStmts(iden)){
            //TODO: check scope need to consider field variables (global variables), static field access of a class.
            if(checkScope) {// by default we dont check scope yet
                    if (currentNode.getScope() == null) {
                        // to set scope for this curNode
                        currentNode.setScope(ASTUtils.getScope(currentNode.getJavaNode()))
                    }
                    currentNode.setCurrentMethodVars(currentMethodVars)
                    currentNode.setFieldVars(fieldNames)
                    if(!retrievedVarsInForStmt && currentNode.getForLoopVars() == null) {
                        collectVarsInBoundedForStatement()
                        currentNode.setforLoopVars(currentVarsInForLoop)
                    }
                //}
                //println("Current Scope = " + currentNode.getScope() + " of Node: " + currentNode)
                //println ("Field names: "+fieldNames)
                //println("Method Vars: "+currentMethodVars)
                //println("Checking scope! -- TO check Scope ="+currentNode.getScope())

                /*if (iden.getNames == null) {
                    // to set names for this curNode
                    if (iden.getJavaNode() != null)
                        iden.setNames(ASTUtils.getNames(iden.getJavaNode()))
                }*/
                if (iden.getScope() == null) {
                    // to set scope for this curNode
                    iden.setScope(ASTUtils.getScope(iden.getJavaNode()))
                }
                //println("tocheck scope: " + iden.getScope())
                //if (currentNode.isInScope(iden)) {
                    //statements.append(iden)
                    if(visitingFaultyMethod){
                       addPendingStatements(currentNodeMethodVector, iden)

                    }else{
                        addPendingStatements(anotherMethodVector, iden)
                    }
                    //println("added " + iden + " to scope of " + currentNode)
                //}else{
                    //println("not added " + iden + " to scope of " + currentNode)
                //}
            }else{
                //statements.append(iden)
                if(visitingFaultyMethod){
                    addPendingStatements(currentNodeMethodVector, iden)

                }else{
                    addPendingStatements(anotherMethodVector, iden)
                }
            }
        }
    }
    /*override def visit (node : AnnotationTypeDeclaration) : Boolean = { visitNode(node) }
    override def visit (node : AnnotationTypeMemberDeclaration) : Boolean = { visitNode(node) }
    override def visit (node : AnonymousClassDeclaration) : Boolean = { visitNode(node) }
    override def visit (node : ArrayAccess) : Boolean = { visitNode(node) }
    override def visit (node : ArrayCreation) : Boolean = { visitNode(node) }
    override def visit (node : ArrayInitializer) : Boolean = { visitNode(node) }
    override def visit (node : ArrayType) : Boolean = { visitNode(node) }*/
    override def visit (node : AssertStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseAssertCount()
        }else{
            anotherMethodVector.increaseAssertCount()
        }
        addStatement(node); false
    }

    override def visit (node : Assignment) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseAssignmentCount()
        }
        addStatement(node); false }
    //override def visit (node : Block) : Boolean = { visitNode(node) }
    //override def visit (node : BlockComment) : Boolean = { visitNode(node) }
    //override def visit (node : BooleanLiteral) : Boolean = { visitNode(node) }
    override def visit (node : BreakStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseBreakCount()
        }else{
            anotherMethodVector.increaseBreakCount()
        }
        addStatement(node); false}
    //override def visit (node : CastExpression) : Boolean = { visitNode(node) }
    //override def visit (node : CatchClause) : Boolean = { visitNode(node) }
    //override def visit (node : CharacterLiteral) : Boolean = { visitNode(node) }
    //override def visit (node : ClassInstanceCreation) : Boolean = { visitNode(node) }
    //override def visit (node : CompilationUnit) : Boolean = { visitNode(node) }
    //override def visit (node : ConditionalExpression) : Boolean = { visitNode(node) }
    override def visit (node : ConstructorInvocation) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseConstructorInvocCount()
        }else{
            anotherMethodVector.increaseConstructorInvocCount()
        }
        addStatement(node); false }
    override def visit (node : ContinueStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseContinueCount()
        }else{
            anotherMethodVector.increaseContinueCount()
        }
        addStatement(node); false }
    override def visit (node : DoStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseDoCount()
        }else{
            anotherMethodVector.increaseDoCount()
        }
        addStatement(node); true }
    //override def visit (node : EmptyStatement) : Boolean = { visitNode(node) }
    override def visit (node : EnhancedForStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseEnhancedForCount()
        }else{
            anotherMethodVector.increaseEnhancedForCount()
        }
        addStatement(node); true }
    //override def visit (node : EnumConstantDeclaration) : Boolean = { visitNode(node) }
    //override def visit (node : EnumDeclaration) : Boolean = { visitNode(node) }
    override def visit (node : ExpressionStatement) : Boolean = { addStatement(node); false }
    //override def visit (node : FieldAccess) : Boolean = { visitNode(node) }
    override def visit (node : FieldDeclaration) : Boolean = {
        val parent=node.getParent
        val parent_start = ASTUtils.getStatementLineNo(parent)
        val parent_end = ASTUtils.getStatementEndLineNo(parent)
        if(currentNode.getLine() == 338)
            println("Debug here")
        if(currentNode.getLine() >= parent_start && currentNode.getLine() <= parent_end) {
            import scala.collection.JavaConversions._
            for (o <- node.fragments) {
                if (o.isInstanceOf[VariableDeclarationFragment]) {
                    val v: VariableDeclarationFragment = o.asInstanceOf[VariableDeclarationFragment]
                    fieldNames.get(v.getName.getIdentifier) match {
                        case None => {
                            val types = new ArrayBuffer[ITypeBinding]
                            types.append(v.getName.resolveTypeBinding())
                            fieldNames.put(v.getName.getIdentifier, types)
                        }
                        case Some(t) => t.append(v.getName.resolveTypeBinding())
                    }
                }
            }
        }
        return super.visit(node)
    }
    override def visit (node : ForStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseForCount()
        }else{
            anotherMethodVector.increaseForCount()
        }
        addStatement(node); true }
    override def visit (node : IfStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseIfCount()
        }else{
            anotherMethodVector.increaseIfCount()
        }
        addStatement(node); true }
    //override def visit (node : ImportDeclaration) : Boolean = { visitNode(node) }
    //override def visit (node : InfixExpression) : Boolean = { visitNode(node) }
    override def visit (node : Initializer) : Boolean = {
        val mods: List[_] = node.modifiers
        import scala.collection.JavaConversions._
        for (o <- mods) {
            if (o.isInstanceOf[Modifier]) {
                if ((o.asInstanceOf[Modifier]).isStatic) {
                    this.currentMethodVars.clear()
                }
            }
        }
        addStatement(node);
        false
    }
    //override def visit (node : InstanceofExpression) : Boolean = { visitNode(node) }
    //override def visit (node : Javadoc) : Boolean = { visitNode(node) }
    override def visit (node : LabeledStatement) : Boolean = { addStatement(node); false  }
    //override def visit (node : LineComment) : Boolean = { visitNode(node) }
    //override def visit (node : MarkerAnnotation) : Boolean = { visitNode(node) }
    //override def visit (node : MemberRef) : Boolean = { visitNode(node) }
    //override def visit (node : MemberValuePair) : Boolean = { visitNode(node) }
    override def visit (node : MethodDeclaration) : Boolean = {
        //println(node.getName)
        //println(currentNode.getMethodName())
        val nodeLine=ASTUtils.getStatementLineNo(node)
        val nodeEndline=ASTUtils.getStatementEndLineNo(node)
        /*if(nodeLine >=  298){
            println(node.getName)
            println(currentNode.getMethodName())
        }*/
        if(/*node.getName.equals(currentNode.getMethodName()) &&*/ nodeLine < currentNode.getLine() && nodeEndline >= currentNode.getLine()) {
            if(currentNode.getMethodReturnType() == null) {
                currentNode.setMethodReturnType(node.resolveBinding().getReturnType)
            }
            this.currentMethodVars.clear()
            import scala.collection.JavaConversions._
            for (o <- node.parameters) {
                if (o.isInstanceOf[SingleVariableDeclaration]) {
                    val v: SingleVariableDeclaration = o.asInstanceOf[SingleVariableDeclaration]
                    currentMethodVars.get(v.getName.getIdentifier) match {
                        case None =>{
                            val types = new ArrayBuffer[ITypeBinding]
                            types.append(v.getName.resolveTypeBinding())
                            currentMethodVars.put(v.getName.getIdentifier, types)
                        }
                        case Some(t) => t.append(v.getName.resolveTypeBinding())
                    }
                }
            }
            visitingFaultyMethod = true
            currentNodeMethodVector = new MethodVector(currentNode.getFileName(), currentNode.getMethodName(), nodeLine, nodeEndline)
            anotherMethodVector = null
        }else{
            //TODO: to consier filename later
            visitingFaultyMethod = false
            anotherMethodVector = new MethodVector("", node.getName.toString, nodeLine, nodeEndline)
            //currentNodeMethodVector = null
        }

        return true
        //return super.visit(node)
    }

    override def endVisit(node : MethodDeclaration) ={
        if(visitingFaultyMethod)
            visitedFaultyMethod = true

        if(currentNodeMethodVector != null && visitedFaultyMethod){
            val iter = pendingStatements.iterator
            while(iter.hasNext){
                val pair = iter.next()
                var simScore = 0.1
                if(pair._1 != null){
                    simScore = currentNodeMethodVector.computeSimilarityWith(pair._1)
                }
                for(iden <- pair._2 if (currentNode.isInScope(iden))){
                    iden.setFixProb(simScore)
                    statements.append(iden)
                }
            }
            pendingStatements.clear()
        }
    }
    //override def visit (node : MethodInvocation) : Boolean = { node.resolveMethodBinding() }
    //override def visit (node : MethodRef) : Boolean = { visitNode(node) }
    //override def visit (node : MethodRefParameter) : Boolean = { visitNode(node) }
    //override def visit (node : Modifier) : Boolean = { visitNode(node) }
    //override def visit (node : NormalAnnotation) : Boolean = { visitNode(node) }
    //override def visit (node : NullLiteral) : Boolean = { visitNode(node) }
    //override def visit (node : NumberLiteral) : Boolean = { visitNode(node) }
    //override def visit (node : PackageDeclaration) : Boolean = { visitNode(node) }
    //override def visit (node : ParameterizedType) : Boolean = { visitNode(node) }
    //override def visit (node : ParenthesizedExpression) : Boolean = { visitNode(node) }
    //override def visit (node : PostfixExpression) : Boolean = { visitNode(node) }
    //override def visit (node : PrefixExpression) : Boolean = { visitNode(node) }
    //override def visit (node : PrimitiveType) : Boolean = { visitNode(node) }
    //override def visit (node : QualifiedName) : Boolean = { visitNode(node) }
    //override def visit (node : QualifiedType) : Boolean = { visitNode(node) }
    override def visit (node : ReturnStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseReturnCount()
        }else{
            anotherMethodVector.increaseReturnCount()
        }
        addStatement(node); false  }
    //override def visit (node : SimpleName) : Boolean = { visitNode(node) }
    //override def visit (node : SimpleType) : Boolean = { visitNode(node) }
    //override def visit (node : SingleMemberAnnotation) : Boolean = { visitNode(node) }
    //override def visit (node : SingleVariableDeclaration) : Boolean = { visitNode(node) }
    //override def visit (node : StringLiteral) : Boolean = { visitNode(node) }
    override def visit (node : SuperConstructorInvocation) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseSuperConstructorInvocCount()
        }else{
            anotherMethodVector.increaseSuperConstructorInvocCount()
        }
        addStatement(node); false  }
    //override def visit (node : SuperFieldAccess) : Boolean = { visitNode(node) }
    override def visit (node : SuperMethodInvocation) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseSuperMethodCount()
        }else{
            anotherMethodVector.increaseSuperMethodCount()
        }
        addStatement(node); false  }
    //override def visit (node : SwitchCase) : Boolean = { visitNode(node) }
    override def visit (node : SwitchStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseSwitchStatementCount()
        }else{
            anotherMethodVector.increaseSwitchStatementCount()
        }
        addStatement(node); true  }
    override def visit (node : SynchronizedStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseSyncCount()
        }else{
            anotherMethodVector.increaseSyncCount()
        }
        addStatement(node); false  }
    //override def visit (node : TagElement) : Boolean = { visitNode(node) }
    //override def visit (node : TextElement) : Boolean = { visitNode(node) }
    //override def visit (node : ThisExpression) : Boolean = { visitNode(node) }
    override def visit (node : ThrowStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseThrowCount()
        }else{
            anotherMethodVector.increaseThrowCount()
        }
        addStatement(node); false  }
    override def visit (node : TryStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseTryCount()
        }else{
            anotherMethodVector.increaseTryCount()
        }
        addStatement(node); true  }
    //override def visit (node : TypeDeclaration) : Boolean = { visitNode(node) }
    //override def visit (node : TypeDeclarationStatement) : Boolean = { visitNode(node) }
    //override def visit (node : TypeLiteral) : Boolean = { visitNode(node) }
    //override def visit (node : TypeParameter) : Boolean = { visitNode(node) }
    //override def visit (node : UnionType) : Boolean = { visitNode(node) }
    //override def visit (node : VariableDeclarationExpression) : Boolean = { visitNode(node) }
    //override def visit (node : VariableDeclarationFragment) : Boolean = { visitNode(node) }
    override def visit (node : VariableDeclarationStatement) : Boolean = {
        import scala.collection.JavaConversions._
        if(visitingFaultyMethod){
            for (o <- node.fragments) {
                if (o.isInstanceOf[VariableDeclarationFragment]) {
                    val v: VariableDeclarationFragment = o.asInstanceOf[VariableDeclarationFragment]
                    currentMethodVars.get(v.getName.getIdentifier) match {
                        case None => {
                            val types = new ArrayBuffer[ITypeBinding]
                            types.append(v.getName.resolveTypeBinding())
                            currentMethodVars.put(v.getName.getIdentifier, types)
                        }
                        case Some(t) => t.append(v.getName.resolveTypeBinding())
                    }
                }
            }
            currentNodeMethodVector.increaseVarDeclVount()
        }else{
            anotherMethodVector.increaseVarDeclVount()
        }
        addStatement(node);
        false
    }
    override def visit (node : WhileStatement) : Boolean = {
        if(visitingFaultyMethod){
            currentNodeMethodVector.increaseWhileCount()
        }else{
            anotherMethodVector.increaseWhileCount()
        }
        addStatement(node); true  }
    //override def visit (node : WildcardType) : Boolean = { visitNode(node) }
}