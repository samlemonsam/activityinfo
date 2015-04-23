package org.activityinfo.geoadmin.merge.model;

import cern.colt.list.tint.IntArrayList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import org.activityinfo.geoadmin.match.DistanceMatrix;
import org.activityinfo.geoadmin.match.HungarianAlgorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Builds a matching between a set of "source" items and a set of "target" items
 * assumed to be equivalent.
 * 
 * <p>This is primarily used to find matchings between existing (or "target") 
 * admin entities and a set of features imported from a shapefile (the "source" items)
 * based on spatial overlap and similarities between names.</p>
 * 
 * <p>However, this </p>
 */
public class MatchBuilder {

    /**
     * The number of items in the "target" set
     */
    private final int numTargets;

    /**
     * The number of items in the "source" set
     */
    private final int numSources;

    /**
     * A matrix providing distances between items in the target set (rows) and the column set
     */
    private final DistanceMatrix matrix;
    
    private int[] targetSubGraph;
    private int[] sourceSubGraph;
    private int subGraphCount;

    public MatchBuilder(DistanceMatrix matrix) {
        this.numTargets = matrix.getRowCount();
        this.numSources = matrix.getColumnCount();
        this.matrix = matrix;
        
        buildSubGraphs();
    }


    private void buildSubGraphs() {
        // now divide into pairs of sub graphs
        targetSubGraph = new int[numTargets];
        sourceSubGraph = new int[numSources];

        for(int i=0;i< targetSubGraph.length;++i) {
            if(targetSubGraph[i] == 0) {
                int subGraphId = subGraphCount+1;
                addTargetToSubGraph(i, subGraphId);
                subGraphCount++;
            }
        }
    }
    
    /**
     * Adds a target item and all matching sources to the given sub graph
     */
    private void addTargetToSubGraph(int targetIndex, int subGraphIndex) {
        targetSubGraph[targetIndex] = subGraphIndex;
        
        for(int i=0;i<sourceSubGraph.length;++i) {
            if(sourceSubGraph[i] == 0 && matrix.matches(targetIndex, i)) {
                addSourceToSubGraph(i, subGraphIndex);
            }
        }
        
    }

    private void addSourceToSubGraph(int sourceIndex, int subGraphIndex) {
        sourceSubGraph[sourceIndex] = subGraphIndex;
        
        for(int i=0;i<targetSubGraph.length;++i) {
            if(targetSubGraph[i] == 0 && matrix.matches(i, sourceIndex)) {
                addTargetToSubGraph(i, subGraphIndex);
            }
        }
    }
    
    private <T> List<T> getSubGraph(List<T> items, int[] labels, int subGraphIndex) {
        List<T> members = new ArrayList<>();
        for(int i=0;i<labels.length;++i) {
            if(labels[i] == subGraphIndex) {
                members.add(items.get(i));
            }
        }
        return members;
    }
    
    private IntArrayList getSubGraph(int[] labels, int subGraphIndex) {
        IntArrayList indices = new IntArrayList();
        for(int i=0;i<labels.length;++i) {
            if(labels[i] == subGraphIndex) {
                indices.add(i);
            }
        }
        return indices;
    }

    public <T, S> BiMap<T, S> buildMap(List<T> target, List<S> source) {

        BiMap<T, S> matches = HashBiMap.create();

        for(int i=1;i<=subGraphCount;++i) {
            List<T> targetMembers = getSubGraph(target, targetSubGraph, i);
            List<S> sourceMembers = getSubGraph(source, sourceSubGraph, i);

            if(targetMembers.size() == 1 && sourceMembers.size() == 1) {
                // Trivial match: one and only choice
                matches.put(targetMembers.get(0), sourceMembers.get(0));
            }
        }

        return matches;
    }

    public <T, S> void dumpSubGraphs(List<T> target, List<S> source) {
        for(int i=1;i<=subGraphCount;++i) {
            System.out.printf("Subgraph %d: %s <=> %s\n", i, 
                    getSubGraph(target, targetSubGraph, i), 
                    getSubGraph(source, sourceSubGraph, i));
        }
    }
    
    public LinkedList<MatchRow> buildMatchList() {
        LinkedList<MatchRow> matches = Lists.newLinkedList();

        for(int i=1;i<=subGraphCount;++i) {
            buildMatchList(matches, i);
        }

        return matches;
    }

    private void buildMatchList(List<MatchRow> matches, int subGraphIndex) {
        IntArrayList target = getSubGraph(targetSubGraph, subGraphIndex);
        IntArrayList source = getSubGraph(sourceSubGraph, subGraphIndex);
        
        if(target.size() == 1 && source.size() == 1) {
            // Trivial match
            matches.add(new MatchRow(target.get(0), source.get(0)));
        
        } else if(source.size() > 0) {
            
            // build a distance matrix 
            double[][] distance = new double[target.size()][source.size()];
            for(int i=0;i<target.size();++i) {
                for(int j=0;j<source.size();++j) {
                    distance[i][j] = matrix.distance(target.get(i), source.get(j));
                }
            }
            
            // find the best match between amongst the members of this subgraph
            HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(distance);
            int[] targetToSource = hungarianAlgorithm.execute();
            
            for(int i=0;i<targetToSource.length;++i) {
                int targetIndexWithinSubGraph = i;
                int targetIndex = target.get(targetIndexWithinSubGraph);
                int sourceIndexWithinSubGraph = targetToSource[targetIndexWithinSubGraph];
                if(sourceIndexWithinSubGraph == -1) {
                    matches.add(new MatchRow(targetIndex, MatchRow.UNMATCHED));
                } else {
                    int sourceIndex = source.get(sourceIndexWithinSubGraph);
                    matches.add(new MatchRow(targetIndex, sourceIndex));
                }
            }
            
        } else {
            // Add each independently
            for(int i=0;i<target.size();++i) {
                matches.add(new MatchRow(target.get(i), MatchRow.UNMATCHED));
            }
        }
        
        // And add the source items that had no corresponding matches with the target set
        for(int i=0;i<sourceSubGraph.length;++i) {
            if(sourceSubGraph[i] == 0) {
                matches.add(new MatchRow(MatchRow.UNMATCHED, i));
            }
        }
    }
}
