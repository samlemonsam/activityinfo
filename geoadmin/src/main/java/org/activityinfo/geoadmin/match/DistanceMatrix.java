package org.activityinfo.geoadmin.match;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public abstract class DistanceMatrix {

    public abstract int getDimensionCount();

    public abstract int getRowCount();

    public abstract int getColumnCount();

    /**
     *
     * @return {@code true} if the items at {@code rowIndex} and {@code columnIndex} have any
     * degree of similarity
     */
    public abstract boolean matches(int i, int j);


    public abstract double score(int i, int j, int d);


    public final double distance(int i, int j) {
        double sum = 0;
        int dimCount = getDimensionCount();
        for(int d=0;d< dimCount;++d) {
            sum += (1.0 - score(i, j, d));
        }
        return sum;
    }

    public final double sumScores(int i, int j) {
        double sum = 0;
        int dimCount = getDimensionCount();
        for(int d=0;d< dimCount;++d) {
            sum += score(i, j, d);
        }
        return sum;
    }

    public final double[] score(int i, int j) {
        int dimCount = getDimensionCount();
        double scores[] = new double[dimCount];
        for(int d=0;d<dimCount;++d) {
            scores[d] = score(i, j, d);
        }
        return scores;
    }
    
    public final double getMinScore(int i, int j) {
        double min = Double.MAX_VALUE;
        int dimCount = getDimensionCount();
        for(int d=0;d< dimCount;++d) {
            min = Math.min(min, score(i, j, d));
        }
        return min;    
    }
    
    public BlockRealMatrix computeScoreMatrix() {
        // First compute the scores of all possible matches between the source and targets
        int numPairs = getRowCount() * getColumnCount();
        int numDimensions = getDimensionCount();

        BlockRealMatrix matrix = new BlockRealMatrix(numPairs, numDimensions);
        
        int rowIndex = 0;
        for(int i = 0; i < getRowCount(); ++ i) {
            for(int j = 0; j < getColumnCount(); ++j) {
                matrix.setRow(rowIndex++, score(i, j));
            }
        }

        return matrix;
    }

    public double[] computeScoreMeans() {
        
        int numDimensions = getDimensionCount();
        double sums[] = new double[numDimensions];
        int counts[] = new int[numDimensions];

        for(int i = 0; i < getRowCount(); ++ i) {
            for(int j = 0; j < getColumnCount(); ++j) {
                for (int d = 0; d < numDimensions; d++) {
                    double score = score(i, j, d);
                    if(Double.isNaN(score)) {
                        sums[d] += score;
                        counts[d] ++;
                    }
                }
            }
        }

        for (int i = 0; i < numDimensions; i++) {
            sums[i] /= (double)counts[i];
        }
        
        return sums;
    }

    /**
     * Calculates the covariance matrix between the scores using a two-pass algorithm.
     * 
     * @see <a href="http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance">Wikipedia: Algorithms for calculating variance</a>
     * @return a covariance matrix
     */
    public RealMatrix computeCovarianceMatrix() {
        int numRows = getRowCount();
        int numCols = getColumnCount();
        int numDims = getDimensionCount();

        // First pass: calculate means and counts of scores
        // as well as pairwise counts 
        
        double sums[] = new double[numDims];
        int counts[] = new int[numDims];
        int pairwiseCounts[][] = new int[numDims][numDims];

        double score;
        double scores[] = new double[numDims];
        
        for(int i = 0; i < getRowCount(); ++ i) {
            for(int j = 0; j < getColumnCount(); ++j) {
          
                for (int d = 0; d < numDims; d++) {
                    scores[d] = score = score(i, j, d);
                    if(!Double.isNaN(score)) {
                        sums[d] += score;
                        counts[d] ++;
                    }
                }
                for(int d1 = 0; d1 < numDims; ++d1) {
                    for (int d2 = d1 + 1; d2 < numDims; d2++) {
                        if(!Double.isNaN(scores[d1]) && !Double.isNaN(scores[d2])) {
                            pairwiseCounts[d1][d2] ++;
                        }
                    }
                }
            }
        }

        double means[] = new double[numDims];
        for (int i = 0; i < numDims; i++) {
            means[i] = sums[i] / (double)counts[i];
        }

        // Second pass: calculate the covariance between the scores
        double covariance[][] = new double[numDims][numDims];
        
        for(int i = 0; i < numRows; ++ i) {
            for (int j = 0; j < numCols; ++j) {
                
                // center the scores by the means calculated in the first pass
                for (int d = 0; d < numDims; d++) {
                    scores[d] = score(i, j, d) - means[d];
                }
                
                for(int d1 = 0; d1 < numDims; ++d1) {
                    double a = scores[d1];
                    if(!Double.isNaN(a)) {
                        covariance[d1][d1] += (a * a) / (double)counts[d1];

                        for (int d2 = d1 + 1; d2 < numDims; d2++) {
                            double b = scores[d2];
                            if(!Double.isNaN(b)) {
                                covariance[d1][d2] += (a * b) / (double)pairwiseCounts[d1][d2];
                            }
                        }                        
                    }
                }
            }
        }
        
        // Complete the upper triangle of the covariance matrix
        for(int d1 = 0; d1 < numDims; ++d1) {
            for (int d2 = d1 + 1; d2 < numDims; d2++) {
                covariance[d2][d1] = covariance[d1][d2];
            }
        }
 
        return new Array2DRowRealMatrix(covariance);
    }
    
    public double[][] computeScoreVectors() {
        
        // First compute the scores of all possible matches between the source and targets
        int numPairs = getRowCount() * getColumnCount();
        int numDimensions = getDimensionCount();
        
        double scores[][] = new double[getDimensionCount()][numPairs];
        
        int rowIndex = 0;
        for(int i = 0; i < getRowCount(); ++ i) {
            for(int j = 0; j < getColumnCount(); ++j) {
                for (int d = 0; d < numDimensions; d++) {
                    scores[d][rowIndex] = score(i, j, d);
                }
                rowIndex++;
            }
        }
        
        return scores;
    }
    
}
