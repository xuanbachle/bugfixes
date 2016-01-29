package repair.terminations;

import de.parsemis.ProbabilityComputerNew;
import de.parsemis.ProbabilityComputerRemine;
import mainscala.RepairOptions;
import org.eclipse.jface.text.BadLocationException;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;
import parsers.javaparser.ASTRewriteFactory;
import repair.representation.GenProgIndividual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SolutionProbability implements TerminationCondition {
    private final double targetTestScore;
    private final boolean natural;
    private final int solutionCount;
    private List<GenProgIndividual> solutionsFound;
    private HashMap<GenProgIndividual,Boolean> solutionCache;
    private static ProbabilityComputerNew probComputer = null;
    private static ProbabilityComputerRemine probRemine = null;
    private static SolutionProbability instance = null;
    //private String minedPatternFile = "./allLibs/workingdata/test.lg";
    private int totalGraphs = 300; //150
    private Long timeLimit; //3600000;
    private Long startTime;

    public Long getStartTime(){
        return startTime;
    }

    public static SolutionProbability getInstance(double targetTestScore, boolean natural, int solutionCount, Long
                                                  timeLimit, Long startTime){
        if (instance == null) {
            instance = new SolutionProbability(targetTestScore, natural, solutionCount, timeLimit, startTime);
        }
        if (instance.natural != natural && instance.targetTestScore != targetTestScore && instance.solutionCount != solutionCount)
            throw new RuntimeException("Invalid SolutionProbability");
        return instance;

    }

    private SolutionProbability(double targetTestScore, boolean natural, int solutionCount, Long timeLimit, Long
                                 startTime) {
        this.targetTestScore = targetTestScore;
        this.natural = natural;
        this.solutionsFound = new ArrayList<>();
        this.solutionCount = solutionCount;
        this.solutionCache = new HashMap<>();
        this.timeLimit = timeLimit;
        this.startTime = startTime;
        probComputer = new ProbabilityComputerNew(RepairOptions.minedPatternFile(), totalGraphs);
        //probRemine = new ProbabilityComputerRemine(minedPatternFile, totalGraphs);
    }

    public void addSolution(GenProgIndividual solution){
        if (!this.solutionCache.containsKey(solution)) {
            GenProgIndividual cpSolution = solution.copy();
            this.solutionCache.put(cpSolution, true);
            cpSolution.setTimeFoundSolution(System.currentTimeMillis()-startTime);
            this.solutionsFound.add(cpSolution);
        }
    }

    //this method deprecated
    public static double computeProbabilityForVariant() throws IOException, BadLocationException {
        String graphVariant= ASTRewriteFactory.computeGraphsForModifiedRewriters();
        if(graphVariant.equals("")) // no change compared to the original buggy version
            return 0.0;

        double prob=probComputer.computeProbabilityFromGraphContent(graphVariant);
        return prob;
    }

    public static double computeProbabilityForVariant(GenProgIndividual variant) throws IOException, BadLocationException {
        if(variant.isAlreadyUpdatedProbability())
            return variant.getProbability();

        String graphVariant= ASTRewriteFactory.computeGraphsForModifiedRewriters();
        //Lib.writeText2File(graphVariant,new File("/home/dxble/MyWorkSpace/historicalFix/temps/tempGraph.lg"));
        if(graphVariant.compareTo("") == 0) // no change compared to the original buggy version
            return 0.0;

        double prob= probComputer.computeProbabilityFromGraphContent(graphVariant); //probRemine.computeProbability("/home/dxble/MyWorkSpace/historicalFix/temps/tempGraph.lg");
        System.out.println("Computed prob: "+prob+" for variant: "+ variant);
        variant.setProbability(prob);
        return prob;
    }

    public static double computeProbabilityForVariant2(GenProgIndividual variant) throws IOException, BadLocationException {
        if(variant.isAlreadyUpdatedProbability())
            return variant.getProbability();

        String graphVariant= ASTRewriteFactory.computeGraphsForModifiedRewriters2(variant);
        if(graphVariant.compareTo(ASTRewriteFactory.INVALID_GRAPH) == 0)
            return -1.0;
        //Lib.writeText2File(graphVariant,new File("/home/dxble/MyWorkSpace/historicalFix/temps/tempGraph.lg"));
        if(graphVariant.compareTo("") == 0) // no change compared to the original buggy version
            return 0.0;

        double lastProb= probComputer.computeProbabilityFromGraphContent(graphVariant); //probRemine.computeProbability("/home/dxble/MyWorkSpace/historicalFix/temps/tempGraph.lg");
        variant.addLastProb2TotalProb(lastProb);
        System.out.println("Computed last prob: "+lastProb+" Total Prob:"+ variant.getTotalProb()+ " for variant: "+ variant);
        double prob = variant.getTotalProb()/variant.getGenome().length();
        variant.setProbability(prob);
        return prob;
    }

    public double getTargetTestScore() {
        return this.targetTestScore;
    }

    public List<GenProgIndividual> getSolutionsFound(){
        return solutionsFound;
    }

    public boolean shouldTerminate(PopulationData<?> populationData) {
        //boolean thisPopulationContainsSolution = this.natural?populationData.getBestCandidateFitness() >= this.targetFitness:populationData.getBestCandidateFitness() <= this.targetFitness;
        //int numOfSolutions = solutionsFound.size() + (thisPopulationContainsSolution?1:0);
        Long currentTime = System.currentTimeMillis();
        if (solutionsFound.size() >= solutionCount) return true;
        else if(currentTime-startTime >= timeLimit) return true;
        else return false;
    }
}