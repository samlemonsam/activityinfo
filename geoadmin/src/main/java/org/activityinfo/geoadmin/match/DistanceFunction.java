package org.activityinfo.geoadmin.match;

public interface DistanceFunction<T> {
    
    int getDimensionCount();
    
    boolean compute(T x, T y, double[] result);
}
