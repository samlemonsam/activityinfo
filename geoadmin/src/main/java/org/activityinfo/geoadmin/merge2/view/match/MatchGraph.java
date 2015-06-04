package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Scheduler;

import java.util.Collection;
import java.util.Map;
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
    
    private static final Logger LOGGER = Logger.getLogger(MatchGraph.class.getName());

    /**
     * Describes the fields which are used as "keys" to perform the matching between the source and target.
     */
    private KeyFieldPairSet keyFields;

    /**
     * Map from <em>source</em> instance index to indexes of potential matches within in the <em>target</em> collection.
     */
    private Multimap<Integer, Integer> sourceCandidates = HashMultimap.create();

    
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
        findDominantMatches();

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
            findCandidatesForSource(i);
        }
    }

    @VisibleForTesting
    void findCandidatesForSource(int i) {
        for(int j=0;j<keyFieldSet.getTargetCount();++j) {
            if(matrix.matches(i, j)) {
                sourceCandidates.put(i, j);
                targetCandidates.put(j, i);
            }
        }
    }

    private void findDominantMatches() {
        LOGGER.info("Finding dominant matches...");


        // Now for each source, determine whether there is a mutually dominant matching
        for(int i=0;i<keyFieldSet.getSourceCount();++i) {
            int targetIndex = findBestTargetForSource(i);
            if(targetIndex != -1) {
                sourceMatches.put(i, targetIndex);
                targetMatches.put(targetIndex, i);
            }
        }
    }
    
    @VisibleForTesting
    int findBestTargetForSource(int sourceIndex) {

        // The candidates arrays holds the indices of all
        // the target instances which are a possible match for this source
        int[] candidates = toArray(sourceCandidates.get(sourceIndex));

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

            double sum = sum(scores[i]);
            if(sum > bestScore) {
                bestScore = sum;
                bestCandidate = i;
            }
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
        Collection<Integer> sourceMatches = targetMatches.get(targetIndex);
        if(sourceMatches.size() == 1) {
            return sourceMatches.iterator().next();
        } else {
            return -1;
        }
    }

    public Collection<Integer> getCandidatesForSource(int sourceIndex) {
        return sourceCandidates.get(sourceIndex);
    }
}
