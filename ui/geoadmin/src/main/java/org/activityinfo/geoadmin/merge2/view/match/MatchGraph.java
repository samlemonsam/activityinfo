package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import org.activityinfo.geoadmin.match.ScoreMatrix;

import java.io.File;
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



    public static class Candidate {
        private int index;
        private double[] scores;

        public Candidate(int targetIndex, double[] scores) {
            this.index = targetIndex;
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

        public int getIndex() {
            return index;
        }


        @Override
        public String toString() {
            return index + ": " + Arrays.toString(scores);
        }
    }

    
    private static final Logger LOGGER = Logger.getLogger(MatchGraph.class.getName());

    private Multimap<Integer, Candidate> sourceCandidates = HashMultimap.create();
    private Multimap<Integer, Candidate> targetCandidates = HashMultimap.create();


    /**
     * Map from source instance index to the "best" target candidate.
     */
    private Map<Integer, Candidate> sourceMatches;

    /**
     * Map from target instance index to the "best" source candidate
     */
    private Map<Integer, Candidate> targetMatches;
    

    private final ScoreMatrix matrix;

    /**
     * A weight in the range of 0-1 for each matrix dimension, computed
     * as one minus the mean score per dimension.
     * 
     * <p>Essentially a measure of how discriminating a particular key is: we want
     * to place more weight on fields like a unique key, rather than a general field like province or district name</p>
     */
    private double[] specificityWeights;
    
    public MatchGraph(ScoreMatrix matrix) {
        this.matrix = matrix;
    }

    public MatchGraph build() {
        computeSpecificityWeights();
        findCandidates();
        findBestUnambiguousMatches();
        LOGGER.info("Match graph complete.");

        try {
            File tempFile = File.createTempFile("match", "R");
            CharSink charSink = Files.asCharSink(tempFile, Charsets.UTF_8);
            matrix.writeTable(charSink);
            System.out.println("Wrote match graph to " + tempFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
    
    private void computeSpecificityWeights() {
        specificityWeights = new double[matrix.getDimensionCount()];
        for (int i = 0; i < matrix.getDimensionCount(); i++) {
            specificityWeights[i] = computeSpecificityWeight(i);
        }
        
        LOGGER.info("Specificity weights: " + Arrays.toString(specificityWeights));
    }

    private double computeSpecificityWeight(int dimensionIndex) {
        double sumOfScores = 0;
        int count = 0;
        for(int i=0;i<matrix.getRowCount();++i) {
            for (int j = 0; j < matrix.getColumnCount(); ++j) {
                double score = matrix.score(i, j, dimensionIndex);
                if(!Double.isNaN(score)) {
                    sumOfScores += score;
                    count++;
                }
            }
        }

        double scoreMean = sumOfScores / (double)count;
        
        return 1.0 - scoreMean;
    }

    /**
     *  Identify potential candidate matches among source and targets
     */
    private void findCandidates() {
        // N.B. This part has O^2 running time!
        // The only way to avoid this is to develop indices for LatinPlaceNameMatcher, which
        // we don't currently have.
        int numDims = matrix.getDimensionCount();
        double scores[] = new double[numDims];

        for(int i=0;i<matrix.getRowCount();++i) {
            for(int j=0;j<matrix.getColumnCount();++j) {

                // FOR EACH potential pair...

                // Compute scores across all dimensions
                double maxScore = 0;
                for (int d = 0; d < scores.length; d++) {
                    double score = matrix.score(i, j, d);
                    if(score > maxScore) {
                        maxScore = score;
                    }
                    scores[d] = score;
                }

                // Must have a substantial score on at least ONE dimension
                // to be considered a viable candidate, otherwise we end up with too much noise
                if(maxScore > 0) {
                    sourceCandidates.put(i, new Candidate(j, scores));
                    targetCandidates.put(j, new Candidate(i, scores));
                }
            }
        }
    }

    private void findBestUnambiguousMatches() {
        this.sourceMatches = findBestUnambiguousMatches(sourceCandidates);
        this.targetMatches = findBestUnambiguousMatches(targetCandidates);
    }

    private Map<Integer, Candidate> findBestUnambiguousMatches(Multimap<Integer, Candidate> candidates) {

        Map<Integer, Candidate> map = new HashMap<>();

        for (Integer index : candidates.keySet()) {
            Optional<Candidate> best = findBestCandidate(candidates.get(index));
            if(best.isPresent()) {
                map.put(index, best.get());
            }
        }

        return map;
    }

    /**
     * Finds the best, unambiguous match from among the set of candidates, if one exists.
     * 
     * <p>We consider a match unambiguous if it is the single member of the pareto frontier:
     * that is, it is better than all other candidates in at least one dimension, and not worse
     * than any other candidates in any other dimension.
     * 
     * @param candidates
     * @return the best unambiguous match, if it exists
     */
    private Optional<Candidate> findBestUnambiguousMatch(Collection<Candidate> candidates) {

        List<Candidate> frontier = paretoFrontier(candidates);

        if(frontier.size() == 1) {
            return Optional.of(frontier.get(0));
        } else {
            return Optional.absent();
        }
    }

    private List<Candidate> paretoFrontier(Collection<Candidate> candidates) {
        List<Candidate> frontier = new ArrayList<>();

        // Find the pareto frontier for this source instance,
        // which includes all candidates that are NOT dominated by another solution
        // Keep track of these as we may ask the user to choose between them
        for (Candidate candidate : candidates) {
            if(!isDominated(candidates, candidate)) {
                frontier.add(candidate);
            }
        }
        return frontier;
    }


    public ScoreMatrix getMatrix() {
        return matrix;
    }
    
    private Optional<Candidate> findBestCandidate(Collection<Candidate> candidates) {
        if(candidates.isEmpty()) {
            return Optional.absent();

        } else if(candidates.size() == 1) {
            return Optional.of(Iterables.getOnlyElement(candidates));

        } else {
            double bestScore = 0;
            Optional<Candidate> bestCandidate = Optional.absent();

            for (Candidate candidate : candidates) {
                double score = weightedCombinedScore(candidate.scores);
                if (score > bestScore) {
                    bestScore = score;
                    bestCandidate = Optional.of(candidate);
                }
            }
            return bestCandidate;
        }
    }

    private double weightedCombinedScore(double[] scores) {
        double combined = 0;
        for (int d = 0; d < scores.length; d++) {
            if(!Double.isNaN(scores[d])) {
                combined += scores[d] * specificityWeights[d];
            }
        }        
        return combined;
    }

    private boolean isDominated(Collection<Candidate> candidates, Candidate candidate) {
        for (Candidate other : candidates) {
            if (other.dominates(candidate)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Returns the index of the best match in the source collection.
     *
     * @return the index of the source instance or -1 if there is no one best match
     */
    public int getBestMatchForSource(int sourceIndex) {
        return getBestMatch(sourceIndex, sourceMatches, targetMatches);
    }
    
    public int getBestMatchForTarget(int targetIndex) {
        return getBestMatch(targetIndex, targetMatches, sourceMatches);
    }

    private int getBestMatch(int indexToMatch, Map<Integer, Candidate> matchMap, Map<Integer, Candidate> inverseMatchMap) {
        Candidate bestMatch = matchMap.get(indexToMatch);
        if (bestMatch == null) {
            // No clear, unambiguous match
            return -1;
        }
        
        // HOORAY, we have a match!
        // But are WE their best match too?
        Candidate competingMatch = inverseMatchMap.get(bestMatch.index);
        if(competingMatch == null) {
            // no competition, it's ours!
            return bestMatch.index;
        }
        
        if(competingMatch.index == indexToMatch) {
            // the match is mutual!
            return bestMatch.index;
        }
        
        // Nope, our BEST is taken by another, we will have 
        // to go unmatched
        return -1;
    }

    private boolean conflicts(int toMatch, Candidate bestMatch, Collection<Candidate> competingMatches) {
        for (Candidate competingMatch : competingMatches) {
            if(competingMatch.getIndex() != toMatch && !bestMatch.dominates(competingMatch)) {
                return true;
            }
        }
        return false;
    }
    
    public List<Integer> getParetoFrontier(int index, MatchSide side) {
        if (side == MatchSide.SOURCE) {
            return indexesOf(paretoFrontier(sourceCandidates.get(index)));
        } else {
            return indexesOf(paretoFrontier(targetCandidates.get(index)));
        }
    }


    private List<Integer> indexesOf(List<Candidate> candidates) {
        List<Integer> indexes = new ArrayList<>();
        for (Candidate candidate : candidates) {
            indexes.add(candidate.getIndex());
        }
        return indexes;
    }

    public Collection<Candidate> getParetoFrontierForSource(int sourceIndex) {
        return Collections.emptyList();
    }
    
}
