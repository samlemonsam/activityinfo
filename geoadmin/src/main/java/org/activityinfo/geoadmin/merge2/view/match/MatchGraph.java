package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.geoadmin.match.RankedScoreMatrix;
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


        @Override
        public String toString() {
            return targetIndex + ": " + Arrays.toString(scores);
        }
    }
    
    
    private static final Logger LOGGER = Logger.getLogger(MatchGraph.class.getName());

    private final KeyFieldPairSet keyFields;


    /**
     * Map from <em>source</em> instance index to indexes of potential matches within in the <em>target</em> collection.
     */
    private Multimap<Integer, Candidate> sourceFrontier = HashMultimap.create();

    
    /**
     * Map from target instance index to indexes of potential matches within in the source collection.
     */
    private Multimap<Integer, Integer> targetFrontier = HashMultimap.create();


    /**
     * Map from source instance index to the "best" target match.
     */
    private Map<Integer, Integer> sourceMatches = Maps.newHashMap();

    /**
     * Map from source instance index to the "best" target match.
     */
    private Multimap<Integer, Integer> targetMatches = HashMultimap.create();
    
    private final InstanceMatrix matrix;

    private RankedScoreMatrix rankedScoreMatrix;

    public static Observable<MatchGraph> build(Scheduler scheduler, Observable<KeyFieldPairSet> keyFields) {
        return keyFields.transform(scheduler, new Function<KeyFieldPairSet, MatchGraph>() {
            @Override
            public MatchGraph apply(KeyFieldPairSet input) {
                return new MatchGraph(input).build();
            }
        });
    }

    public MatchGraph(KeyFieldPairSet keyFieldSet) {
        this.keyFields = keyFieldSet;
        this.matrix = new InstanceMatrix(keyFieldSet);
    }

    public MatchGraph build() {
        rankScoreMatrix();
        findCandidates();

        LOGGER.info("Match graph complete.");
        
        return this;
    }

    void rankScoreMatrix() {
        rankedScoreMatrix = new RankedScoreMatrix(matrix);
    }

    @VisibleForTesting
    void findCandidates() {
        // Identify potential candidates among source and targets
        // N.B. This part has O^2 running time!
        // The only way to avoid this is to develop indices for LatinPlaceNameMatcher, which
        // we don't currently have.

        LOGGER.info("Identifying candidates...");


        for(int i=0;i< keyFields.getSourceCount();++i) {
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
        for(int j=0;j< keyFields.getTargetCount();++j) {


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
            //    keyFields.dumpPair(sourceIndex, j);
                candidates.add(new Candidate(j, scores));
            }
        }
        
        // Find the pareto frontier for this source instance,
        // which includes all candidates that are NOT dominated by another solution
        // Keep track of these as we may ask the user to choose between them
        for (Candidate candidate : candidates) {
            if(!isDominated(candidates, candidate)) {
                sourceFrontier.put(sourceIndex, candidate);
                targetFrontier.put(candidate.targetIndex, sourceIndex);
            }
        }
        
        // If we have a pareto frontier with a size > 1, then choose the "best"
        Candidate bestCandidate = findBestCandidate(sourceFrontier.get(sourceIndex));
        
        if(bestCandidate != null) {
            sourceMatches.put(sourceIndex, bestCandidate.targetIndex);
            targetMatches.put(bestCandidate.targetIndex, sourceIndex);
        }
    }

    private Candidate findBestCandidate(Collection<Candidate> candidates) {
        if(candidates.size() == 1) {
            return Iterables.getOnlyElement(candidates);
        } else {
            double bestScore = 0;
            Candidate bestCandidate = null;

            for (Candidate candidate : candidates) {
                double score = rankedScoreMatrix.meanRank(candidate.scores);
                //keyFields.getTarget().dump(candidate.getTargetIndex());
                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = candidate;
                }
            }
            return bestCandidate;
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

    public KeyFieldPairSet getKeyFields() {
        return keyFields;
    }

    /**
     * Returns the index of the best match in the source collection.
     * 
     * @return the index of the source instance or -1 if there is no one best match
     */
    public int getBestMatchForTarget(int targetIndex) {
        Collection<Integer> sourceMatches = targetMatches.get(targetIndex);
        if(sourceMatches.size() == 1) {
            return Iterables.getOnlyElement(sourceMatches);
        }
        return -1;
    }
    
    public int getBestMatchForSource(int sourceIndex) {
        Integer targetIndex = sourceMatches.get(sourceIndex);
        if(targetIndex == null) {
            return -1;
        } else {
            return targetIndex;
        }
    }

    public List<Integer> getParetoFrontier(int index, MatchSide side) {
        List<Integer> frontier = new ArrayList<>();
        if(side == MatchSide.SOURCE) {
            for (Candidate candidate : sourceFrontier.get(index)) {
                frontier.add(candidate.getTargetIndex());
            }
        } else {
            frontier.addAll(targetFrontier.get(index));
        }
        return frontier;
    }
    
    public Collection<Candidate> getParetoFrontierForSource(int sourceIndex) {
        return sourceFrontier.get(sourceIndex);
    }


    public double rank(Candidate candidate) {
        return rankedScoreMatrix.meanRank(candidate.scores);

    }
}
