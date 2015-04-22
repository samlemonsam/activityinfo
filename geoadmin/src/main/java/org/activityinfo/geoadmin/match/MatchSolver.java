package org.activityinfo.geoadmin.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Solves a bipartite graph matching problem
 */
public class MatchSolver<T> {

    private DistanceMatrix matrix;
    private int[] targetSubGraph;
    private int[] sourceSubGraph;
    private int subGraphCount;
    private List<T> target;
    private List<T> source;

    public Map<T, T> solve(List<T> target, List<T> source, DistanceFunction<T> distanceFunction) {
        this.target = target;
        this.source = source;


        // first build the distance matrix
        matrix = new DistanceMatrix(target.size(), source.size(), distanceFunction.getDimensionCount());
        double[] distance = new double[distanceFunction.getDimensionCount()];
        
        for(int i=0;i<target.size();++i) {
            for(int j=0;j<source.size();++j) {
                if(distanceFunction.compute(target.get(i), source.get(j), distance)) {
                    matrix.set(i, j, distance);
                }
            }
        }
        
        // now divide into pairs of subgraphs
        targetSubGraph = new int[target.size()];
        sourceSubGraph = new int[source.size()];
        
        for(int i=0;i< targetSubGraph.length;++i) {
            if(targetSubGraph[i] == 0) {
                int subGraphId = subGraphCount+1;
                addTargetToSubGraph(i, subGraphId);
                subGraphCount++;
            }
        }
        
        return null;
    }

    /**
     * Adds a target item and all matching sources to the given subgraph
     */
    private void addTargetToSubGraph(int targetIndex, int subGraphIndex) {
        targetSubGraph[targetIndex] = subGraphIndex;
        
        for(int i=0;i<sourceSubGraph.length;++i) {
            if(sourceSubGraph[i] == 0 && matrix.isAdjacent(targetIndex, i)) {
                addSourceToSubGraph(i, subGraphIndex);
            }
        }
        
    }

    private void addSourceToSubGraph(int sourceIndex, int subGraphIndex) {
        sourceSubGraph[sourceIndex] = subGraphIndex;
        
        for(int i=0;i<targetSubGraph.length;++i) {
            if(targetSubGraph[i] == 0 && matrix.isAdjacent(i, sourceIndex)) {
                addTargetToSubGraph(i, subGraphIndex);
            }
        }
    }
    
    private List<T> getSubGraph(List<T> items, int[] labels, int subGraphIndex) {
        List<T> members = new ArrayList<>();
        for(int i=0;i<labels.length;++i) {
            if(labels[i] == subGraphIndex) {
                members.add(items.get(i));
            }
        }
        return items;
    }
    
    public List<T> getTargetSubGraph(int subGraphIndex) {
        return getSubGraph(target, targetSubGraph, subGraphIndex);
    }
    
    public List<T> getSourceSubGraph(int subGraphIndex) {
        return getSubGraph(source, sourceSubGraph, subGraphIndex);
    }
    
    public void dumpSubGraphs() {
        for(int i=0;i<subGraphCount;++i) {
            System.out.printf("Subgraph %d: %s <=> %s\n", i, getTargetSubGraph(i), getSourceSubGraph(i));
        }
    }
    
}
