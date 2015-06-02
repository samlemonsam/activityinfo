package org.activityinfo.geoadmin.merge2.view.mapping;

import com.google.common.base.Function;
import org.activityinfo.geoadmin.merge2.model.ReferenceMatch;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.geoadmin.merge2.view.swing.merge.MatchLevel;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.ObservableSet;

import java.util.*;

/**
 * Table that combines the automatic matching from the {@link org.activityinfo.geoadmin.merge2.view.mapping.LookupGraph}
 * with user-provided {@link org.activityinfo.geoadmin.merge2.model.ReferenceMatch}es.
 */
public class LookupTable {

    private final LookupGraph graph;
    private final SourceKeySet sourceKeySet;

    /**
     * Matches 
     */
    private int[] matching;

    public LookupTable(LookupGraph lookupGraph, Set<ReferenceMatch> referenceMatches) {
        this.graph = lookupGraph;
        this.sourceKeySet = lookupGraph.getSourceKeySet();
        
        matching = new int[sourceKeySet.size()];
        Map<SourceLookupKey, Integer> userMap = buildUserMap(referenceMatches);
        
        for(int i=0;i<sourceKeySet.size();++i) {
            SourceLookupKey key = sourceKeySet.get(i);
            Integer targetIndex = userMap.get(key);
            if(targetIndex != null) {
                // User has provided an explicit lookup result
                matching[i] = targetIndex;
            } else {
                // Otherwise use pareto optimal if available
                matching[i] = graph.getParetoOptimalMatch(i);
            }
        }
    }

    public static Observable<LookupTable> compute(final ReferenceFieldMapping fieldMapping, ImportView viewModel) {
        return compute(fieldMapping.getLookupGraph(), viewModel.getModel().getReferenceMatches());
    }

    public static Observable<LookupTable> compute(final LookupGraph graph, ObservableSet<ReferenceMatch> matches) {
        return matches.asObservable().transform(new Function<Set<ReferenceMatch>, LookupTable>() {
            @Override
            public LookupTable apply(Set<ReferenceMatch> input) {
                return new LookupTable(graph, input);
            }
        });
    }
    
    
    private Map<SourceLookupKey, Integer> buildUserMap(Set<ReferenceMatch> matchSet) {
        Map<SourceLookupKey, Integer> map = new HashMap<>();
        
        for (ReferenceMatch match : matchSet) {
            SourceLookupKey key = userMatchToKey(match.getSourceValues());
            if(key != null) {
                map.put(key, graph.getTargetForm().indexOf(match.getTargetInstanceId()));
            }
        }
        return map;
    }
    
    private SourceLookupKey userMatchToKey(Map<FieldPath, String> valueMap) {
        List<FieldProfile> keyFields = sourceKeySet.getSourceFields();
        String[] keyValues = new String[keyFields.size()];
        for(int i=0;i<keyFields.size();++i) {
            FieldPath keyPath = keyFields.get(i).getPath();
            if(!valueMap.containsKey(keyPath)) {
                // This user-provided match is inconsistent with the current FieldMatching used
                // to create the SourceKeySet. This can occur when the user defines one or more
                // ReferenceMatches, and then changes the FieldMatching between the source and the reference
                // column. 
                // 
                // In order to observe unidirectional data flow, we do *not* update the ReferenceMatch set
                // in response to the model change, but rather consider the reconciliation between the two
                // independent pieces of state to be an intermediary computation.
                
                return null;
            }
            keyValues[i] = valueMap.get(keyPath);
        }
        return new SourceLookupKey(keyValues);
    }
    
    
    public int getRowCount() {
        return sourceKeySet.distinct().size();
    }
    
    public List<FieldProfile> getSourceKeyFields() {
        return sourceKeySet.getSourceFields();
    }
    
    public List<FieldProfile> getTargetKeyFields() {
        return graph.getTargetKeyFields();
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
                    return graph.getTargetKeyFields().get(fieldIndex).getView().getString(targetRowIndex);
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
    
    public FormProfile getTargetForm() {
        return graph.getTargetForm();
    }
}
