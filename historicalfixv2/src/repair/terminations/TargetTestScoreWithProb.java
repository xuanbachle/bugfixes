package repair.terminations;

import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

/**
 * Created by dxble on 8/3/15.
 */

import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.TerminationCondition;

public class TargetTestScoreWithProb implements TerminationCondition {
    private final double targetFitness;
    private final boolean natural;

    public TargetTestScoreWithProb(double targetFitness, boolean natural) {
        this.targetFitness = targetFitness;
        this.natural = natural;
    }

    public boolean shouldTerminate(PopulationData<?> populationData) {
        return this.natural?populationData.getBestCandidateFitness() >= this.targetFitness:populationData.getBestCandidateFitness() <= this.targetFitness;
    }
}