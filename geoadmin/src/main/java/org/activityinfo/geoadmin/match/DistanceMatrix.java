package org.activityinfo.geoadmin.match;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;


public class DistanceMatrix {
    
    private SparseDoubleMatrix2D[] matrices;
    
    public DistanceMatrix(int nx, int ny, int dimensionCount) {
        matrices = new SparseDoubleMatrix2D[dimensionCount];
        for(int i=0;i<dimensionCount;++i) {
            matrices[i] = new SparseDoubleMatrix2D(nx, ny);
        }
    }

    public void set(int i, int j, double[] distance) {
        assert distance.length == matrices.length;
        for(int d=0;d < distance.length; ++d) {
            matrices[d].set(i, j, distance[d]);
        }
    }


    public boolean isAdjacent(int i, int j) {
        for(int d=0;d < matrices.length; ++d) {
            if(matrices[d].get(i, j) > 0) {
                return true;
            }
        } 
        return false;
    }
}
