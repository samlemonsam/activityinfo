package org.activityinfo.geoadmin.match;

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
}
