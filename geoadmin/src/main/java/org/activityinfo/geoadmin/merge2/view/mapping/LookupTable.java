package org.activityinfo.geoadmin.merge2.view.mapping;

import org.activityinfo.geoadmin.merge2.view.match.FieldMatching;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.swing.merge.MatchLevel;
import org.activityinfo.model.query.ColumnView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Table providing look up from instances in the source collection to instances
 * in the range of a reference field.
 */
public class LookupTable {
    
    private final SourceKeySet sourceKeySet;
    private final List<FieldProfile> targetKeyFields = new ArrayList<>();
    private final FieldMatching fieldMatching;
    private final LookupGraph graph;

    /**
     * Matches 
     */
    private int[] matching;

    public LookupTable(SourceKeySet sourceKeySet, FieldMatching fieldMatching) {
        this.sourceKeySet = sourceKeySet;
        this.fieldMatching = fieldMatching;

        for (FieldProfile sourceField : sourceKeySet.getSourceFields()) {
            targetKeyFields.add(fieldMatching.sourceToTarget(sourceField).get());
        }

        graph = new LookupGraph(sourceKeySet, fieldMatching.getTarget());
        matching = graph.matchBest();
    }
    
    public int getRowCount() {
        return sourceKeySet.distinct().size();
    }
    
    public List<FieldProfile> getSourceKeyFields() {
        return sourceKeySet.getSourceFields();
    }
    
    public List<FieldProfile> getTargetKeyFields() {
        return targetKeyFields;
    }
    
    public String getSourceKey(int keyIndex, int fieldIndex) {
        return sourceKeySet.distinct().get(keyIndex).get(fieldIndex);
    }

    public LookupGraph getGraph() {
        return graph;
    }

    public MatchLevel getLookupConfidence(int keyIndex) {
        return graph.getLookupConfidence(keyIndex, matching[keyIndex]);
    }
  
    
    public ColumnView getSourceView(final int fieldIndex) {
        return new AbstractStringView() {
            @Override
            public int numRows() {
                return sourceKeySet.distinct().size();
            }

            @Override
            public String getString(int row) {
                return sourceKeySet.distinct().get(row).get(fieldIndex);
            }
        };
    }
    
    public ColumnView getTargetView(final int fieldIndex) {
        return new AbstractStringView() {
            @Override
            public int numRows() {
                return sourceKeySet.distinct().size();
            }

            @Override
            public String getString(int row) {
                int targetRowIndex = matching[row];
                if(targetRowIndex == -1) {
                    return null;
                } else {
                    return targetKeyFields.get(fieldIndex).getView().getString(targetRowIndex);
                }
            }
        };
    }

    public Collection<Integer> getTargetCandidateRows(int sourceKeyIndex) {
        return graph.getCandidates(sourceKeyIndex);
    }

    public int getTargetMatchRow(int sourceKeyIndex) {
        return matching[sourceKeyIndex];
    }

    public SourceLookupKey getSourceKey(int sourceKeyIndex) {
        return sourceKeySet.distinct().get(sourceKeyIndex);
    }
}
