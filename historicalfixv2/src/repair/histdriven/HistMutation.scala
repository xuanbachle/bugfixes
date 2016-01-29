package repair.histdriven

import java.util.Random

import localizations.fixlocalization.methodsimilarity.FindFixStrategies
import localizations.fixlocalization.methodsimilarity.FindFixStrategies.FixStrat.FixStrat
import localizations.{Identifier, PrecomputedInfor}
import mainscala.RepairOptions
import org.apache.log4j.Logger
import org.eclipse.jdt.core.dom._
import org.uncommons.watchmaker.framework.EvolutionaryOperator
import parsers.javaparser.ASTRewriteFactory
import repair.geneticprogramming.selectionschemes.{FaultSelections, SelectionScheme}
import repair.mutationoperators._
import repair.representation._
import repair.terminations.SolutionProbability
import util.ast.ASTUtils

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
 * Created by xuanbach32bit on 4/22/15.
 */
class HistMutation (prec: PrecomputedInfor, strat: FixStrat) extends EvolutionaryOperator[GenProgIndividual] {
  /*def this(){
    this(null,null)
  }*/

  private val logger = Logger.getLogger(classOf[HistMutation])
  //private var mutationProbability: NumberGenerator[Probability] = null


  // implement from the interface
  def apply(selectedCandidates: java.util.List[GenProgIndividual], rng: Random): java.util.List[GenProgIndividual] = {
    import scala.collection.JavaConversions._
    val mutatedPopulation: ListBuffer[GenProgIndividual] = new ListBuffer[GenProgIndividual]
    for (s <- selectedCandidates) {
      if(!s.getIsSolution())
        mutatedPopulation.add(mutateIndividual(s, rng))
    }
    return mutatedPopulation.toList
  }

  private def decideOperatorsForCurrentNode(currentNode: Identifier[Any], rng: Random): ArrayBuffer[MutGene] ={
    val appendMut = new AppMut(null, null)
    val prependMut = new PrepMut(null,null)
    val deleteMut = new DelMut(null)
    val replaceMut = new RepMut(null, null)
    val prepAppMut = new PrepAppMut()
    //val swapMut =  new SwapMut(null, null)
    val operators: ArrayBuffer[MutGene] = ArrayBuffer[MutGene]()
    //println("Current Node: "+currentNode+ " JAVA: "+currentNode.getJavaNode())
    if(currentNode.transformToJavaNode()){
     // println("After transformed Current Node: "+currentNode+ " JAVA: "+currentNode.getJavaNode())
      //println(currentNode.hasExps())
      if(currentNode.hasExps){// currently consider expression in the if and while statements
       // println("Exp of current node: "+currentNode.getExps())
        val expMut = new ExpressionMut(currentNode, rng, new HistMutActions)
        expMut.resetProbMut(0.30)
        deleteMut.resetProbMut(0.20)
        replaceMut.resetProbMut(0.25)
        prepAppMut.resetProbMut(0.25)

        operators.append(expMut)
        if(currentNode.isReturnStatement()){// prevent adding statement after return statement because of deadcode problem
          appendMut.resetProbMut(-1.0) // prevent adding statement after return statement

          prependMut.resetProbMut(1.0)
          prepAppMut.addToPossibleMuts(prependMut)

        }else if(currentNode.getJavaNode().isInstanceOf[BooleanLiteral]){
          prepAppMut.resetProbMut(-1.0)
          deleteMut.resetProbMut(-1.0)
          replaceMut.resetProbMut(-1.0)
          expMut.resetProbMut(1.0)
        }else{
          try{// to make sure append is useful, does not generate not compilable variant
            currentNode.getJavaNode().getParent.asInstanceOf[Block]// test prepend, append applicable

            prependMut.resetProbMut(0.5)
            appendMut.resetProbMut(0.5)
            prepAppMut.addToPossibleMuts(prependMut)
            prepAppMut.addToPossibleMuts(appendMut)
          }catch {
            case e: Throwable => {
              //prependMut.resetProbMut(1.0)
              //prepAppMut.addToPossibleMuts(prependMut)
            }
          }
        }
      }else{// we dont have exp mutation any more
        prependMut.resetProbMut(0.5)
        appendMut.resetProbMut(0.5)
        prepAppMut.addToPossibleMuts(prependMut)
        prepAppMut.addToPossibleMuts(appendMut)
      }
    }
    if(currentNode.getJavaNode().isInstanceOf[ConstructorInvocation])
      prepAppMut.removePrepMut()

    operators.append(prepAppMut)
    operators.append(deleteMut)
    operators.append(replaceMut)
    //operators.append(swapMut)
    logger.info("Possible mutation: "+operators)
    return operators
  }

  private def chooseAMutOperator(rng: Random, currentNode: Identifier[Any]): MutGene = {
    val operators = decideOperatorsForCurrentNode(currentNode, rng)
    while (true) {
      val op: MutGene = operators(rng.nextInt(operators.length))
      if (op.getProbMut() >= rng.nextDouble)
        return op
    }
    null
  }

  private def randomNum(rng: Random): Double = {
    return rng.nextDouble
  }

  //TODO: cache the resultant mutated candidate so that we do not generate a duplicate candidate
  def mutateIndividual/*_new*/(candidate: GenProgIndividual, rng: Random,init:Boolean=false): GenProgIndividual = {
    ASTRewriteFactory.assertEmptyModifiedRewriter()
    val poolIndividuals = new mutable.HashSet[GenProgIndividual]()
    def addToPool(indiv: GenProgIndividual) = if(!ChosenIndividualsCache.alreadyChosenBefore(indiv) && computeFrequencyForIndividual2(indiv))
      {
        logger.debug("Added "+indiv)
        poolIndividuals.add(indiv)
      }else
      {
        logger.debug("Already chosen before? "+ChosenIndividualsCache.alreadyChosenBefore(indiv))
        logger.debug("Freq computation success? "+computeFrequencyForIndividual2(indiv))
        logger.debug("Not added "+indiv)
      }

    def chooseSourceBySimScore(iden: Identifier[Any], rng: Random): Boolean ={
      if(iden.getFixProb() >= rng.nextDouble())
        return true
      else
        return false
    }
    var currentPoolSize = 0
    var count = 0
    val faultSpace = prec.getFaultSpace
    //logger.info("Trying to generate gene for a candidate! Current pool size " + poolIndividuals.size)

    val selectedFaultLocs = if(rng.nextBoolean()) FaultSelections.faultLocSelControlMajority(faultSpace, rng, 10,10) //FaultSelections.faultLocSelection2(faultSpace,rng)
                            else FaultSelections.faultLocSelection(faultSpace, rng)
    var countLoopTimes: Int = 0
    while(poolIndividuals.size == 0) {// pool size should be at least 1, and this individuals should be compliable
      countLoopTimes += 1
      if(countLoopTimes >= 10)// if loop more than 10 times, we return original candidate without any modifications
        return candidate

      selectedFaultLocs.map(current => {
        val transformedSuccess = current.transformToJavaNode()
        if (transformedSuccess) {
          val op: MutGene = chooseAMutOperator(rng, current)
          if (op.isInstanceOf[PrepAppMut]) {
            try {
              val sources = current.getPrepAppSources(prec,strat)
              val (chosenSource: Identifier[Any], success) = SelectionScheme.randomWithCondition(sources, chooseSourceBySimScore, rng, trialLimit = sources.size)
              if (success) {
                val copyIndiv = candidate.copy()
                val chosenOp = op.asInstanceOf[PrepAppMut].choosePrepOrApp(rng)
                if (chosenOp.isInstanceOf[AppMut]) {
                  copyIndiv.addNewGene(new AppMut(current, chosenSource))
                } else {
                  // prepend
                  copyIndiv.addNewGene(new PrepMut(current, chosenSource))
                }
                logger.debug("copied: " + copyIndiv.getGenome)
                //println("original: " + candidate.getGenome)
                //poolIndividuals.add(copyIndiv)
                addToPool(copyIndiv)
              }
            } catch {
              case e: IllegalArgumentException => e.printStackTrace()
            }
          }
          else if (op.isInstanceOf[DelMut]) {
            try {
              val copyIndiv = candidate.copy()
              copyIndiv.addNewGene(new DelMut(current))
              logger.debug("copied: " + copyIndiv.getGenome)
              //println("original: "+ candidate.getGenome)
              //poolIndividuals.add(copyIndiv)
              addToPool(copyIndiv)
            } catch {
              case e: IllegalArgumentException => e.printStackTrace()
            }
          }
          else if (op.isInstanceOf[RepMut]) {
            try {
              val sources = current.getRepSources(prec,strat)
              val (to_rep, success) = SelectionScheme.randomWithCondition(sources, chooseSourceBySimScore, rng, trialLimit = sources.size)
              var compatibleType = true
              if (to_rep.asInstanceOf[Identifier[Any]].isReturnStatement()) {
                val returnExp = to_rep.asInstanceOf[Identifier[Any]].getJavaNode().asInstanceOf[ReturnStatement].getExpression
                if (returnExp != null)
                  compatibleType = ASTUtils.compatibleTypes(returnExp.resolveTypeBinding(), current.getMethodReturnType())
                else {
                  compatibleType = try {
                    current.getMethodReturnType().asInstanceOf[PrimitiveType].getPrimitiveTypeCode.equals(PrimitiveType.VOID)
                  } catch {
                    case e: Throwable => {
                      e.printStackTrace();
                      false
                    }
                  }
                }
                //if(compatibleType)
                //  println("Debug here: "+to_rep)
              }
              if (success && compatibleType) {
                val copyIndiv = candidate.copy()
                copyIndiv.addNewGene(new RepMut(current, to_rep))
                logger.debug("copied: " + copyIndiv.getGenome)
                //println("original: "+ candidate.getGenome)
                //poolIndividuals.add(copyIndiv)
                addToPool(copyIndiv)
              }
            } catch {
              case e: IllegalArgumentException => e.printStackTrace()
            }
          }
          else if (op.isInstanceOf[SwapMut]) {//ignore this swap mutation. This is here for legacy purpose
            try {
              val (to_swap: Identifier[Any], success) = SelectionScheme.randomWithCondition(current.getSwapSources(prec,strat), chooseSourceBySimScore, rng)
              if (success) {
                val copyIndiv = candidate.copy()
                copyIndiv.addNewGene(new SwapMut(current, to_swap))
                logger.debug("copied: " + copyIndiv.getGenome)
                //println("original: "+ candidate.getGenome)
                //poolIndividuals.add(copyIndiv)
                addToPool(copyIndiv)
              }
            } catch {
              case e: IllegalArgumentException => e.printStackTrace()
            }
          }
          else if (op.isInstanceOf[ExpressionMut]) {
            logger.debug("Attempting to mutate expression! " + current)
            current.getExpMutSources(prec,strat)
            val expMutator = op.asInstanceOf[ExpressionMut]
            val (sourceExp, mutatedExp) = expMutator.mutate()

            if (sourceExp != null && mutatedExp != null) {
              val copyIndiv = candidate.copy()
              copyIndiv.addNewGene(new RepMut(sourceExp, mutatedExp))
              logger.debug("copied: " + copyIndiv.getGenome)
              //println("original: "+ candidate.getGenome)
              //poolIndividuals.add(copyIndiv)
              addToPool(copyIndiv)
            }
          }
          else {
            println("Why not any Mut!!!!")
          }
        }
      })
    }
    assert(poolIndividuals.size>0)
    logger.info("Pool size: "+poolIndividuals.size)
    try {
      def getFrequency(candidate: GenProgIndividual): Double ={
        if(!computeFrequencyForIndividual(candidate))
          return 0.0

        return candidate.getProbability()+candidate.getIncentiveForPassedTest()
      }

      def compilableIndividual(candidate: GenProgIndividual): Boolean ={
          return candidate.getHasValidGenome()
      }

      val chosenIndividual = SelectionScheme.stochasticUniversalSampling[GenProgIndividual](poolIndividuals.toBuffer, 1 /*RepairOptions.tournamentSize*/, rng, getFrequency, compilableIndividual)

      //// Start code for debugging purpose only
      if(RepairOptions.debug) {
        poolIndividuals.map(indiv => computeFrequencyForIndividual(indiv))
        logger.debug("=====Chosen Pool of Individuals: ")
        poolIndividuals.map(indiv => {
          logger.debug("--------");
          logger.debug(indiv)
        })
        //logger.debug("Filtered "+poolIndividuals.size +" To" +filterPopulation.size)
        logger.debug("=====End Chosen Pool of Individuals=======")
      }
      /// End code for debugging purpose only

      chosenIndividual(0).setParent_id(candidate.getInd_id())
      ChosenIndividualsCache.addToChosenIndividual(chosenIndividual(0))
      logger.info("Chosen candidate: " + chosenIndividual(0))

      return chosenIndividual(0)

    }catch {
      case e: IllegalArgumentException => return candidate.copy()
    }
  }

  //TODO: cache the resultant mutated candidate so that we do not generate a duplicate candidate
  def mutateIndividualWithoutTournament(candidate: GenProgIndividual, rng: Random,init:Boolean=false): GenProgIndividual = {
    ASTRewriteFactory.assertEmptyModifiedRewriter()
    val poolIndividuals = new mutable.HashSet[GenProgIndividual]()

    def chooseSourceBySimScore(iden: Identifier[Any], rng: Random): Boolean ={
      if(iden.getFixProb() >= rng.nextDouble())
        return true
      else
        return false
    }
    var currentPoolSize = 0
    var count = 0
    val faultSpace = prec.getFaultSpace
    do {// loop until the candidate get only one gene
      logger.info("Trying to generate gene for a candidate! Current pool size " + poolIndividuals.size)
      if(poolIndividuals.size>currentPoolSize){
        currentPoolSize = poolIndividuals.size
        count = 0
      }else{
        count +=1 // increase the times repaeat the same size
      }
      //println(faultSpace)
      var globalMutRate = 0.0
      if (init)
        globalMutRate = 1.0
      else
        globalMutRate = RepairOptions.globalWmut

      faultSpace.map(faultLoc => {
        if(poolIndividuals.size<=RepairOptions.poolSize * faultSpace.size){
          val current: Identifier[Any] = faultLoc
          val transformedSuccess = current.transformToJavaNode()
          if (transformedSuccess) {
            if (current.getProb >= randomNum(rng) && globalMutRate >= randomNum(rng)) {
              val op: MutGene = chooseAMutOperator(rng, current)
              if (op.isInstanceOf[PrepAppMut]) {
                try {
                  val sources =current.getPrepAppSources(prec,strat)
                  val (chosenSource: Identifier[Any], success) = SelectionScheme.randomWithCondition(sources, chooseSourceBySimScore, rng, trialLimit = sources.size)
                  if(success) {
                    val copyIndiv = candidate.copy()
                    val chosenOp=op.asInstanceOf[PrepAppMut].choosePrepOrApp(rng)
                    if(chosenOp.isInstanceOf[AppMut]) {
                      copyIndiv.addNewGene(new AppMut(current, chosenSource))
                    }else{// prepend
                      copyIndiv.addNewGene(new PrepMut(current, chosenSource))
                    }
                    logger.debug("copied: " + copyIndiv.getGenome)
                    //println("original: " + candidate.getGenome)
                    poolIndividuals.add(copyIndiv)
                  }
                }catch{
                  case e: IllegalArgumentException => e.printStackTrace()
                }
              }
              else if (op.isInstanceOf[DelMut]) {
                try {
                  val copyIndiv = candidate.copy()
                  copyIndiv.addNewGene(new DelMut(current))
                  logger.debug("copied: "+ copyIndiv.getGenome)
                  //println("original: "+ candidate.getGenome)
                  poolIndividuals.add(copyIndiv)
                }catch{
                  case e: IllegalArgumentException => e.printStackTrace()
                }
              }
              else if (op.isInstanceOf[RepMut]) {
                try {
                  val sources=current.getRepSources(prec,strat)
                  val (to_rep, success) = SelectionScheme.randomWithCondition(sources, chooseSourceBySimScore, rng, trialLimit = sources.size)
                  var compatibleType = true
                  if(to_rep.asInstanceOf[Identifier[Any]].isReturnStatement()){
                    val returnExp=to_rep.asInstanceOf[Identifier[Any]].getJavaNode().asInstanceOf[ReturnStatement].getExpression
                    if(returnExp!=null)
                      compatibleType=ASTUtils.compatibleTypes(returnExp.resolveTypeBinding(), current.getMethodReturnType())
                    else {
                      compatibleType = try{
                        current.getMethodReturnType().asInstanceOf[PrimitiveType].getPrimitiveTypeCode.equals(PrimitiveType.VOID)
                      }catch {
                        case e: Throwable =>{
                          e.printStackTrace();
                          false
                        }
                      }
                    }
                    //if(compatibleType)
                    //  println("Debug here: "+to_rep)
                  }
                  if(success && compatibleType) {
                    val copyIndiv = candidate.copy()
                    copyIndiv.addNewGene(new RepMut(current, to_rep))
                    logger.debug("copied: " + copyIndiv.getGenome)
                    //println("original: "+ candidate.getGenome)
                    poolIndividuals.add(copyIndiv)
                  }
                }catch{
                  case e: IllegalArgumentException => e.printStackTrace()
                }
              }
              else if (op.isInstanceOf[SwapMut]) {
                try {
                  val (to_swap: Identifier[Any], success) = SelectionScheme.randomWithCondition(current.getSwapSources(prec,strat), chooseSourceBySimScore, rng)
                  if(success) {
                    val copyIndiv = candidate.copy()
                    copyIndiv.addNewGene(new SwapMut(current, to_swap))
                    logger.debug("copied: " + copyIndiv.getGenome)
                    //println("original: "+ candidate.getGenome)
                    poolIndividuals.add(copyIndiv)
                  }
                }catch{
                  case e: IllegalArgumentException => e.printStackTrace()
                }
              }
              else if (op.isInstanceOf[ExpressionMut]) {
                logger.debug("Attempting to mutate expression! "+current)
                val expMutator = op.asInstanceOf[ExpressionMut]
                expMutator.parentExp.getExpMutSources(prec,strat)
                val (sourceExp, mutatedExp) = expMutator.mutate()

                if (sourceExp != null && mutatedExp != null) {
                  val copyIndiv = candidate.copy()
                  copyIndiv.addNewGene(new RepMut(sourceExp, mutatedExp))
                  logger.debug("copied: "+ copyIndiv.getGenome)
                  //println("original: "+ candidate.getGenome)
                  poolIndividuals.add(copyIndiv)
                }
              }
              else {
                println("Why not any Mut!!!!")
              }
            }
          }
        }
      })
    }while(poolIndividuals.size<=RepairOptions.poolSize * faultSpace.size && count < 20)
    //val sorted=poolIndividuals.toArray.sortWith((indiv1, indiv2) => indiv1.getProbability()> indiv2.getProbability())
    try {
      //val poolArray = poolIndividuals.foldLeft(new ArrayBuffer[GenProgIndividual]) { (res, indiv) => { res.append(indiv); res}}
      val poolMap = poolIndividuals.foldLeft(new mutable.HashMap[Double, ArrayBuffer[GenProgIndividual]]() ) {
        (res, indiv) => {
          /*
          get the average score of weights (probability of a statement to be mutated, given by Fault localization) of all genes,
          e.g., candidate = [app(stmt(1) with stmt(2)), del(stmt(3))] then average mutated Line prob = (weight(stmt(1)) + weight(stmt(3)))/2
          * */
          val prob = indiv.getAverageMutatedLineProb()
          res.get(prob) match {
            case None =>{
              val indivSameProb = new ArrayBuffer[GenProgIndividual]()
              indivSameProb.append(indiv)
              res.put(prob, indivSameProb)
            }
            case Some(v) => v.append(indiv)
          }
          res
        }
      }
      /*
      get only the average scores, which is the keys, out of the hashtable
      * */
      val keyProbs=poolMap.keySet.foldLeft(new ArrayBuffer[Double]){
        (res, key) =>{
          res.append(key)
          res
        }
      }

      def chooseByAverageMutatedLineProb(prob: Double, rand: Random): Boolean ={
        if(prob >= rand.nextDouble())
          return true
        else return false
      }

      val (chosenMutatedLineProb, sucess) = SelectionScheme.randomWithCondition(keyProbs, chooseByAverageMutatedLineProb, rng)
      val associatedIndividuals = poolMap.get(chosenMutatedLineProb).getOrElse(null)
      val (chosenIndividual, success) = SelectionScheme.randomWithCondition(associatedIndividuals, choseByFrequencyProbability, rng, trialLimit = associatedIndividuals.size)
      //if(success) {

      //// Start code for debugging purpose only
      poolIndividuals.map(indiv => computeFrequencyForIndividual(indiv))
      logger.debug("=====Chosen Pool of Individuals: ")
      poolIndividuals.map(indiv => {
        logger.debug("--------");
        logger.debug(indiv)
      })
      logger.debug("=====End Chosen Pool of Individuals=======")
      /// End code for debugging purpose only

      //println("Chosen Associated Pool: "+associatedIndividuals.size+" "+chosenMutatedLineProb+associatedIndividuals)
      chosenIndividual.setParent_id(candidate.getInd_id())
      logger.info("Chosen candidate: " + chosenIndividual)

      return chosenIndividual
      //}else{
      //  println("Return Original Candidate because choosing not successful!")
      //  return candidate
      //}
    }catch {
      case e: IllegalArgumentException => return candidate.copy()
    }
  }


  def computeFrequencyForIndividual(indiv: GenProgIndividual): Boolean ={
    if(!indiv.isAlreadyUpdatedProbability()){
      var okVariant = true
      val transformSuccess=indiv.transferGeneToStaticWriterFactory() // after transform to factory, we compute prob
      if(transformSuccess) {
        SolutionProbability.computeProbabilityForVariant(indiv) // after compute prob, we restore factory state to empty
        if(!indiv.compile()) {
          okVariant = false
          indiv.cleanVariantClassFolder()
        }
      }else{
        okVariant = false
      }
      ASTRewriteFactory.clearAllModifiedRewriters() // restore factory to empty state
      if(!okVariant) {
        indiv.setHasValidGenome(false)
        return false
      }
    }
    //logger.debug(indiv)
    return indiv.getHasValidGenome()
  }

  def computeFrequencyForIndividual2(indiv: GenProgIndividual): Boolean ={
    if(!indiv.isAlreadyUpdatedProbability()){
      var okVariant = true
      val prob=SolutionProbability.computeProbabilityForVariant2(indiv) // after compute prob, we restore factory state to empty
      if(prob == -1.0){
        okVariant = false
      }
      else if(!indiv.compile()) {
        okVariant = false
        indiv.cleanVariantClassFolder()
      }

      ASTRewriteFactory.clearAllModifiedRewriters() // restore factory to empty state, after computing prob + compilation
      if(!okVariant) {
        indiv.setHasValidGenome(false)
        return false
      }
    }
    //logger.debug(indiv)
    return indiv.getHasValidGenome()
  }

  def choseByFrequencyProbability(indiv: GenProgIndividual, rand: Random): Boolean ={

    val r = rand.nextDouble()

    val freqComputationSuccess = computeFrequencyForIndividual(indiv)
    if(!freqComputationSuccess)
      return false

    logger.debug("Prob: "+ indiv.getProbability() +" "+r)
    val toChoose=indiv.getProbability() >= r && !ChosenIndividualsCache.alreadyChosenBefore(indiv) && indiv.getAverageSimilarityScore() >= rand.nextDouble()
    if(toChoose){
      indiv.updateGenerScore()
      ChosenIndividualsCache.addToChosenIndividual(indiv) // to cache the individual that we have choosen, this is to avoid duplicate
    }
    ASTRewriteFactory.clearAllModifiedRewriters()
    return toChoose
  }

  def mutateIndividualGenProg(candidate: GenProgIndividual, rng: Random,init:Boolean=false): GenProgIndividual = {
    import scala.collection.JavaConversions._
    do {// loop until the candidate get at least one gene
      println("Trying to generate gene for a candidate!")
      val iter: Iterator[Identifier[Any]] = prec.getFaultSpace.iterator
      var globalMutRate = 0.0
      if (init)
        globalMutRate = 1.0
      else
        globalMutRate = RepairOptions.globalWmut

      //var mutationSuccessful = false
      while (iter.hasNext /*&& !mutationSuccessful*/) {
        val current: Identifier[Any] = iter.next
        val transformedSuccess = current.transformToJavaNode()
        if (transformedSuccess) {
          if (current.getProb >= randomNum(rng) && globalMutRate >= randomNum(rng)) {
            val op: MutGene = chooseAMutOperator(rng, current)
            if (op.isInstanceOf[AppMut]) {
              val to_app: Identifier[Any] = SelectionScheme.random(current.getPrepAppSources(prec,strat), rng)
              candidate.getGenome.add(new AppMut(current, to_app))
              //mutationSuccessful = true
            }
            else if (op.isInstanceOf[DelMut]) {
              candidate.getGenome.add(new DelMut(current))
              //mutationSuccessful = true
            }
            else if (op.isInstanceOf[RepMut]) {
              val to_rep: Identifier[Any] = SelectionScheme.random(current.getRepSources(prec,strat), rng)
              candidate.getGenome.add(new RepMut(current, to_rep))
              //mutationSuccessful = true
            }
            else if (op.isInstanceOf[SwapMut]) {
              val to_swap: Identifier[Any] = SelectionScheme.random(current.getSwapSources(prec,strat), rng)
              candidate.getGenome.add(new SwapMut(current, to_swap))
              //mutationSuccessful = true
            }
            else if (op.isInstanceOf[ExpressionMut]) {
              println("Attempting to mutate expression!")
              val expMutator = op.asInstanceOf[ExpressionMut]
              val (sourceExp, mutatedExp) = expMutator.mutate()
              if(sourceExp != null && mutatedExp !=null) {
                candidate.getGenome.add(new RepMut(sourceExp, mutatedExp))
                //mutationSuccessful = true
              }
            }
            else {
              println("Why not any Mut!!!!")
            }
          }
        }
      }
    }while(candidate.getGenome.isEmpty)
    logger.info("Finished generating gene for a candidate!")
    //GenProgIndividual.getMaxIndId()
    candidate.setParent_id(candidate.getInd_id())
    //candidate.setInd_id(GenProgIndividual.maxIndId)
    return candidate
  }

  /*def getPrepAppSources(i: Identifier[Any]): ArrayBuffer[Identifier[Any]] = {
    if (i.isInstanceOf[LineIden]) {
    }
    FindFixStrategies.chooseAStrategy(prec,i,strat)
  }

  def getRepSources(i: Identifier[Any]): ArrayBuffer[_ <: Identifier[Any]] = {
    if (i.isInstanceOf[LineIden]) {
    }
    FindFixStrategies.chooseAStrategy(prec,i,strat)
  }

  def getExpMutSources(i: Identifier[Any]): ArrayBuffer[_ <: Identifier[Any]] = {
    if (i.isInstanceOf[LineIden]) {
    }
    FindFixStrategies.chooseAStrategy(prec,i,strat)
  }

  def getSwapSources(i: Identifier[Any]): ArrayBuffer[_ <: Identifier[Any]] = {
    if (i.isInstanceOf[LineIden]) {
    }
    FindFixStrategies.chooseAStrategy(prec,i,strat)
  }*/
}

