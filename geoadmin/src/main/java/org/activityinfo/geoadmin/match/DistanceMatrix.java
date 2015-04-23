package org.activityinfo.geoadmin.match;

public interface DistanceMatrix {
    
    int getDimensionCount();
    
    int getRowCount();
    
    int getColumnCount();

    /**
     *
     * @return {@code true} if the items at {@code rowIndex} and {@code columnIndex} have any
     * degree of similarity
     */
    boolean matches(int rowIndex, int columnIndex);

    double distance(int i, int j);
}
