package repair.geneticprogramming;

/**
 * Created by xuanbach on 1/19/16.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.AbstractEvolutionEngine;
import org.uncommons.watchmaker.framework.CandidateFactory;
import org.uncommons.watchmaker.framework.EvaluatedCandidate;
import org.uncommons.watchmaker.framework.EvolutionaryOperator;
import org.uncommons.watchmaker.framework.FitnessEvaluator;
import org.uncommons.watchmaker.framework.SelectionStrategy;

public class CustomEvolutionEngine<T> extends AbstractEvolutionEngine<T> {
    private final EvolutionaryOperator<T> evolutionScheme;
    private final FitnessEvaluator<? super T> fitnessEvaluator;
    private final SelectionStrategy<? super T> selectionStrategy;

    public CustomEvolutionEngine(CandidateFactory<T> candidateFactory, EvolutionaryOperator<T> evolutionScheme, FitnessEvaluator<? super T> fitnessEvaluator, SelectionStrategy<? super T> selectionStrategy, Random rng) {
        super(candidateFactory, fitnessEvaluator, rng);
        this.evolutionScheme = evolutionScheme;
        this.fitnessEvaluator = fitnessEvaluator;
        this.selectionStrategy = selectionStrategy;
    }

    /*public CustomEvolutionEngine(CandidateFactory<T> candidateFactory, EvolutionaryOperator<T> evolutionScheme, InteractiveSelection<T> selectionStrategy, Random rng) {
        this(candidateFactory, evolutionScheme, new NullFitnessEvaluator(), selectionStrategy, rng);
    }*/

    protected List<EvaluatedCandidate<T>> nextEvolutionStep(List<EvaluatedCandidate<T>> evaluatedPopulation, int eliteCount, Random rng) {
        ArrayList population = new ArrayList(evaluatedPopulation.size());
        ArrayList elite = new ArrayList(eliteCount);
        Iterator iterator = evaluatedPopulation.iterator();

        while(elite.size() < eliteCount) {
            elite.add(((EvaluatedCandidate)iterator.next()).getCandidate());
        }

        population.addAll(this.selectionStrategy.select(evaluatedPopulation, this.fitnessEvaluator.isNatural(), evaluatedPopulation.size() - eliteCount, rng));
        List population1 = this.evolutionScheme.apply(population, rng);
        population1.addAll(elite);
        return this.evaluatePopulation(population1);
    }
}
