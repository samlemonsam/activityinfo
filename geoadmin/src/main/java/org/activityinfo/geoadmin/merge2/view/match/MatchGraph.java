package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Scheduler;

import java.util.*;
import java.util.logging.Logger;

/**
 * Graph a matching between imported instances in the source collection and existing instances 
 * in the target collection.
 *
 * <p>Each\ <em>source</em> instance may be matched to either one <em>target</em> instance, or none at all.</p>
 * 
 * <p>Likewise, each target instance may be matched to either one source instance, or none at all.</p>
 * 
 * <h3>Concrete Example</h3>
 * 
 * <p>You might have an existing list of schools in ActivityInfo identified by name, along with the territory, district,
 * and province in which the school is located. These fields together form a natural key which can be used to uniquely
 * identify a school.</p>
 * 
 * <p>Now say that you have received an Excel sheet with an updated list of schools. This list is the definitive
 * list from the Ministry of Education, and we wish to update our school list in ActivityInfo. Because other data
 * refers to the existing schools in AI, we can't simply replace the schools, we need to <strong>match</strong> each
 * school in the Excel sheet (the <em>source</em>) with the schools in AI's list of schools (the <em>target</em>).</p>
 * 
 * 
 */
public class MatchGraph {

    private static final double MIN_SCORE = 0.5;

    public static class Candidate {
        private int targetIndex;
        private double[] scores;

        public Candidate(int targetIndex, double[] scores) {
            this.targetIndex = targetIndex;
            this.scores = Arrays.copyOf(scores, scores.length);
        }
        
        public boolean dominates(Candidate other) {
            assert scores.length == other.scores.length;

            // must be superior on at least one dimension
            boolean superiorOnAtLeastOneDimension = false;
            
            for (int d = 0; d < scores.length; d++) {
                double x = scores[d];
                double y = other.scores[d];
                
                if(x > y) {
                    superiorOnAtLeastOneDimension = true;
                } else if(y > x) {
                    // cannot dominate the other solution if it is superior in even one dimension
                    return false;
                }
            }
            
            return superiorOnAtLeastOneDimension;
        }

        public int getTargetIndex() {
            return targetIndex;
        }
    }
    
    
    private static final Logger LOGGER = Logger.getLogger(MatchGraph.class.getName());
    

    /**
     * Map from <em>source</em> instance index to indexes of potential matches within in the <em>target</em> collection.
     */
    private Multimap<Integer, Candidate> sourceCandidates = HashMultimap.create();

    
    /**
     * Map from target instance index to indexes of potential matches within in the source collection.
     */
    private Multimap<Integer, Integer> targetCandidates = HashMultimap.create();


    /**
     * Map from source instance index to the "best" target match.
     */
    private Map<Integer, Integer> sourceMatches = Maps.newHashMap();

    /**
     * Map from source instance index to the "best" target match.
     */
    private Multimap<Integer, Integer> targetMatches = HashMultimap.create();
    
    private final InstanceMatrix matrix;
    private KeyFieldPairSet keyFieldSet;


    public static Observable<MatchGraph> build(Scheduler scheduler, Observable<KeyFieldPairSet> keyFields) {
        return keyFields.transform(scheduler, new Function<KeyFieldPairSet, MatchGraph>() {
            @Override
            public MatchGraph apply(KeyFieldPairSet input) {
                return new MatchGraph(input).build();
            }
        });
    }

    public MatchGraph(KeyFieldPairSet keyFieldSet) {
        this.keyFieldSet = keyFieldSet;
        this.matrix = new InstanceMatrix(keyFieldSet);
    }

    public MatchGraph build() {
        findCandidates();
  //      findDominantMatches();

        LOGGER.info("Match graph complete.");
        
        return this;
    }


    @VisibleForTesting
    void findCandidates() {
        // Identify potential candidates among source and targets
        // N.B. This part has O^2 running time!
        // The only way to avoid this is to develop indices for LatinPlaceNameMatcher, which
        // we don't currently have.

        LOGGER.info("Identifying candidates...");


        for(int i=0;i<keyFieldSet.getSourceCount();++i) {
            buildParetoFrontierForSource(i);
        }
    }

    @VisibleForTesting
    void buildParetoFrontierForSource(int sourceIndex) {

        List<Candidate> candidates = new ArrayList<>();

        // the number of score dimensions
        // a matching might be performed, for example, using 
        // a school name, territory, district, and province,
        // yielding four dimensions, each with their own score
        int numDims = matrix.getDimensionCount();
        
        
        double scores[] = new double[numDims];
        
        // For each potential target...
        for(int j=0;j<keyFieldSet.getTargetCount();++j) {

          //  keyFieldSet.getTarget().dump(j);

            // Compute scores across all dimensions
            double maxScore = 0;
            for (int d = 0; d < scores.length; d++) {
                double score = matrix.score(sourceIndex, j, d);
                if(score > maxScore) {
                    maxScore = score;
                }
                scores[d] = score;
            }

            // Must have a substantial score on at least ONE dimension
            // to be considered a viable candidate
            if(maxScore >= MIN_SCORE) {
                candidates.add(new Candidate(j, scores));
            }
        }
        
        // Find the pareto frontier for this source instance,
        // which includes all candidates that are NOT dominated by another solution
        for (Candidate candidate : candidates) {
            if(!isDominated(candidates, candidate)) {
                sourceCandidates.put(sourceIndex, candidate);
                targetCandidates.put(candidate.targetIndex, sourceIndex);
            }
        }
    }
    
    private boolean isDominated(List<Candidate> candidates, Candidate candidate) {
        for (Candidate other : candidates) {
            if(other.dominates(candidate)) {
                return true;
            }
        }
        return false;
    }

    private void findDominantMatches() {
        LOGGER.info("Finding dominant matches...");

        // Now for each source, determine whether there is a mutually dominant matching
        for(int i=0;i<keyFieldSet.getSourceCount();++i) {
            int targetIndex = findDominantMatchForSource(i);
            if(targetIndex != -1) {
                sourceMatches.put(i, targetIndex);
                targetMatches.put(targetIndex, i);
            }
        }
    }
    
    @VisibleForTesting
    int findDominantMatchForSource(int sourceIndex) {

        // The candidates arrays holds the indices of all
        // the target instances which are a possible match for this source
//        int[] candidates = toArray(sourceCandidates.get(sourceIndex));
        int[] candidates = new int[0];
        
        // Matrix of scores between this source and the targets
        // Might look like this:
        // Candidate | Name | Code | Province Name | Geometry |
        //         0 |  1.0 |  0.0 |          0.25 |     0.84 |
        //         1 |  0.5 |  0.0 |          0.00 |     0.00 |
        //         2 |  0.3 |  0.0 |          0.00 |     0.00 |
        //         3 |  1.0 |  0.0 |          1.00 |     0.05 |
        //         4 |  0.0 |  0.0 |          0.96 |     0.15 |
        
        double[][] scores = new double[candidates.length][];

        // The first thing we do is look for the "best" candidate,
        // which is currently done by summing the individual scores
        // together. 
        
        
        double bestScore = -1;
        int bestCandidate = -1;
        
        for (int i = 0; i < candidates.length; i++) {
            int targetIndex = candidates[i];
            scores[i] = matrix.score(sourceIndex, targetIndex);
        }
        
        
        
        // But we only want to accept the "best" score if it is 
        // also dominant: that is, it has to be equal or better than all
        // other candidates on all dimensions.
        
        // Otherwise, we consider that this source cannot be matched
        // without human intervention, because although summing the individual
        // scores is a useful heuristic, the scores from the various dimensions
        // are not actually comparable and can lead to false matches if 
        // we pretend that they are.

        for (int i = 0; i < candidates.length; i++) {
            if( i != bestCandidate ) {
                if( ! dominates(scores[bestCandidate], scores[i])) {
                    return -1;
                }
            }
        }
        
        return candidates[ bestCandidate ];
    }

    /**
     * Returns true if and only if x[i] >= y[i] for all i.
     */
    private boolean dominates(double[] x, double[] y) {
        assert x.length == y.length : "x and y must have equal lengths";
        
        for (int i = 0; i < x.length; i++) {
            if(y[i] > x[i]) {
                return false;
            }
        }
        return true;
    }

    private double sum(double[] scores) {
        double sum = 0;
        for(int i=0;i!=scores.length;++i) {
            sum += scores[i];
        }
        return sum;
    }

    private int[] toArray(Collection<Integer> candidates) {
        int array[] = new int[candidates.size()];
        int i = 0;
        for (Integer candidate : candidates) {
            array[i++] = candidate;
        }
        return array;
    }

    public KeyFieldPairSet getKeyFields() {
        return keyFieldSet;
    }

    /**
     * Returns the index of the best match in the source collection.
     * 
     * @return the index of the source instance or -1 if there is no one best match
     */
    public int getBestMatchForTarget(int targetIndex) {
        Collection<Integer> sourceMatches = targetCandidates.get(targetIndex);
        if(sourceMatches.size() == 1) {
            int sourceIndex = Iterables.getOnlyElement(sourceMatches);
            // only consider this match unambiguous if the source has only one element (this target)
            // in its pareto frontier
            if(getParetoFrontierForSource(sourceIndex).size() == 1) {
                return sourceIndex;
            }
        }
        return -1;
    }
    
    
    public Collection<Candidate> getParetoFrontierForSource(int sourceIndex) {
        return sourceCandidates.get(sourceIndex);
    }
}
