package org.activityinfo.geoadmin.match;

import java.util.Arrays;

/**
 * Empirical distribution of score values constructed using a histogram of counts.
 */
public class ScoreDistribution {

    public static final int BIN_COUNT = 1000;
    
    private final int[] cumulativeCounts;

    private final double totalCount;

    /**
     * Calculates the index of the bin for the given score.
     * 
     * @param score a score in the range [0, 1]
     * @return the index of the frequency bin in the range [0, BIN_COUNT - 1]
     */
    public static int binIndexOf(double score) {
        assert score >= 0 && score <= 1;
        return (int)Math.floor(score * (double)(BIN_COUNT - 1));
    }

    public static class Builder {
        private int[] counts = new int[BIN_COUNT];
        
        public void add(double score) {
            if(!Double.isNaN(score)) {
                counts[binIndexOf(score)]++;
            }
        }
        
        public ScoreDistribution build() {
            return new ScoreDistribution(counts);
        }
    }
    
    private ScoreDistribution(int counts[]) {
        cumulativeCounts = Arrays.copyOf(counts, counts.length);
        for(int i=1;i<BIN_COUNT;++i) {
            cumulativeCounts[i] += cumulativeCounts[i-1];
        }
        totalCount = cumulativeCounts[BIN_COUNT-1];
    }

    /**
     * Returns a given score's percentile as a proportion in the range [0, 1].
     * 
     * <p>A percentile rank of 1.0 means that the score is equal to or better than 100% of 
     * scores between all possible matches.</p>
     */
    public double percentile(double score) {
        double cumulativeCount = cumulativeCounts[binIndexOf(score)];
        return cumulativeCount / totalCount;
    }

}
