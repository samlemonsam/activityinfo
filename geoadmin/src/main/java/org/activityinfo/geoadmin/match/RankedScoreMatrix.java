package org.activityinfo.geoadmin.match;

/**
 * A ScoreMatrix which transforms an input ScoreMatrix whose scores are percentiles of the 
 * within the input dimension's score distributions.
 */
public class RankedScoreMatrix extends ScoreMatrix {

    private ScoreMatrix source;
    private final int numRows;
    private final int numCols;
    private final int dimCount;
    private final ScoreDistribution[] distributions;

    public RankedScoreMatrix(ScoreMatrix input) {
        this.source = input;
        numRows = input.getRowCount();
        numCols = input.getColumnCount();
        dimCount = input.getDimensionCount();

        ScoreDistribution.Builder histograms[] = new ScoreDistribution.Builder[dimCount];
        for (int d = 0; d < dimCount; d++) {
            histograms[d] = new ScoreDistribution.Builder();
        }

        // Loop through all possible matches between items in the source and target sets
        // to build a histogram of all possible scores.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                for (int d = 0; d < dimCount; d++) {
                    histograms[d].add(input.score(i, j, d));
                }
            }
        }

        distributions = new ScoreDistribution[dimCount];
        for (int d = 0; d < dimCount; d++) {
            distributions[d] = histograms[d].build();
        }
    }

    @Override
    public int getDimensionCount() {
        return dimCount;
    }

    @Override
    public int getRowCount() {
        return numRows;
    }

    @Override
    public int getColumnCount() {
        return numCols;
    }

    @Override
    public boolean matches(int i, int j) {
        return source.matches(i, j);
    }

    @Override
    public double score(int i, int j, int d) {
        return distributions[d].percentile(source.score(i, j, d));
    }
    
    public double[] rank(double[] scores) {
        assert scores.length == dimCount;

        double ranked[] = new double[dimCount];
        for (int d = 0; d < dimCount; d++) {
            ranked[d] = distributions[d].percentile(scores[d]);
        }
        return ranked;
    }
    
    public double meanRank(double[] scores) {
        assert scores.length == dimCount;

        double sum = 0;
        int count = 0;
        for (int d = 0; d < dimCount; d++) {
            if (!Double.isNaN(scores[d])) {
                sum += distributions[d].percentile(scores[d]);
                count++;
            }
        }
        if(count == 0) {
            return Double.NaN;
        } else {
            return sum / (double) count;
        }
    }
}
