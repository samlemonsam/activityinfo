package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.model.InstanceMatch;
import org.activityinfo.geoadmin.merge2.model.InstanceMatchSet;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.observable.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Visualizes the matching between two collections as a table.
 * 
 * <p>This view is a function of the automatic matching and user-defined overrides.</p>
 */
public class MatchTable {
    
    private final InstanceMatchSet matchSet;

    private final Observable<MatchGraph> graph;
    private final Observable<List<MatchTableColumn>> columns;

    private List<TableObserver> observers = new CopyOnWriteArrayList<>();
    
    private final SubscriptionSet subscriptions = new SubscriptionSet();
    
    private List<MatchRow> rows = new ArrayList<>();
    
    public MatchTable(ImportModel model, Observable<MatchGraph> graph) {
        this.matchSet = model.getInstanceMatchSet();
        this.columns = graph.transform(new ColumnListBuilder());
        this.graph = graph;
    }

    public Subscription subscribe(final TableObserver observer) {
        if(observers.isEmpty()) {
            onConnect();
        }
        observers.add(observer);
        return new Subscription() {
            @Override
            public void unsubscribe() {
                observers.remove(observer);
                if(observers.isEmpty()) {
                    onDisconnect();
                }
            }
        };
    }

    private void onConnect() {
        subscriptions.add(graph.subscribe(new Observer<MatchGraph>() {
            @Override
            public void onChange(Observable<MatchGraph> observable) {
                recompute();
            }
        }));
        
        subscriptions.add(matchSet.subscribe(new SetObserver<InstanceMatch>() {
            @Override
            public void onChange() {
                recompute();
            }

            @Override
            public void onElementAdded(InstanceMatch element) {
                recompute();
            }

            @Override
            public void onElementRemoved(InstanceMatch element) {
                recompute();
            }

        }));
    }
    
    public Observable<MatchTable> asObservable() {
        return new Observable<MatchTable>() {

            private Subscription subscription;
            
            @Override
            protected void onConnect() {
                subscription = MatchTable.this.subscribe(new TableObserver() {
                    @Override
                    public void onRowsChanged() {
                        fireChange();
                    }

                    @Override
                    public void onRowChanged(int index) {
                        fireChange();
                    }
                });
            }

            @Override
            protected void onDisconnect() {
                subscription.unsubscribe();
            }

            @Override
            public boolean isLoading() {
                return MatchTable.this.isLoading();
            }

            @Override
            public MatchTable get() {
                return MatchTable.this;
            }
        };
    }
    
    public Observable<Integer> getUnresolvedCount() {
        return asObservable().transform(new Function<MatchTable, Integer>() {
            @Override
            public Integer apply(MatchTable input) {
                int count = 0;
                for (int i = 0; i < input.getRowCount(); i++) {
                    MatchRow row = input.get(i);
                    if(!row.isResolved()) {
                        count++;
                    }
                }
                return count;
            }
        });
    }

    private void onDisconnect() {
        subscriptions.unsubscribeAll();
    }

    private void recompute() {
        if(graph.isLoading() || matchSet.isLoading()) {
            return;
        }
        KeyFieldPairSet keyFields = graph.get().getKeyFields();
        MatchGraph graph = this.graph.get();
        
        List<MatchRow> rows = new ArrayList<>();
        Set<Integer> matchedSources = new HashSet<>();

        // Add a row for each target row
        for (int targetRow = 0; targetRow < keyFields.getTarget().getRowCount(); ++targetRow) {
            
            MatchRow row = new MatchRow(keyFields);
            row.setTargetRow(targetRow);
            
            ResourceId targetId = keyFields.getTarget().getRowId(targetRow);
            Optional<InstanceMatch> explicitMatch = matchSet.find(targetId);
            
            if(explicitMatch.isPresent()) {

                // the user has provided an explicit match between the two rows

                ResourceId sourceId = explicitMatch.get().getSourceId();
                int sourceRow = keyFields.getSource().indexOf(sourceId);

                row.setSourceRow(sourceRow);
                row.setResolved(true);
                row.setInputRequired(true);
                
                matchedSources.add(sourceRow);
            
            } else {
                
                // Use the closest automatic match
                
                int sourceRow = graph.getBestMatchForTarget(targetRow);
                if(sourceRow == MatchRow.UNMATCHED) {
                    row.setResolved(false);
                    row.setInputRequired(true);
                } else {
                    row.setSourceRow(sourceRow);
                    row.setResolved(true);
                    matchedSources.add(sourceRow);
                }
            }
            rows.add(row);
        }

        // Add finally add an output row for each unmatched source
        for (int sourceRow = 0; sourceRow < keyFields.getSource().getRowCount(); ++sourceRow) {
            if (!matchedSources.contains(sourceRow)) {
                MatchRow row = new MatchRow(keyFields);
                row.setSourceRow(sourceRow);
                row.setResolved(false);
                rows.add(row);
            }
        }
        
        this.rows = rows;
        fireRowsChanged();
    }

    private void fireRowsChanged() {
        for (TableObserver observer : observers) {
            observer.onRowsChanged();
        }
    }


    public MatchRow get(int index) {
        return rows.get(index);
    }

    public int getRowCount() {
        return rows.size();
    }


    public boolean isLoading() {
        return graph.isLoading() || matchSet.isLoading();
    }

    public Observable<List<MatchTableColumn>> getColumns() {
        return columns;
    }

    private class ColumnListBuilder implements Function<MatchGraph, List<MatchTableColumn>> {

        @Override
        public List<MatchTableColumn> apply(MatchGraph graph) {
            KeyFieldPairSet keyFields = graph.getKeyFields();
            List<MatchTableColumn> columns = new ArrayList<>();
 
            columns.add(new ResolutionColumn(MatchTable.this, matchSet));

            // Show the existing instances, paired with the matching column
            for (FieldProfile targetField : keyFields.getTarget().getFields()) {
                Optional<FieldProfile> sourceField = keyFields.targetToSource(targetField);
                if(sourceField.isPresent()) {
                    columns.add(new MatchedColumn(MatchTable.this, targetField, sourceField.get(), MatchSide.TARGET));
                    columns.add(new MatchedColumn(MatchTable.this, targetField, sourceField.get(), MatchSide.SOURCE));
                } else {
                    columns.add(new UnmatchedColumn(targetField, fromTarget(targetField.getView())));
                }
            }
            
            // Also include the unmatched source columns as reference
            for (FieldProfile sourceField : keyFields.getSource().getFields()) {
                if(!keyFields.sourceToTarget(sourceField).isPresent()) {
                    columns.add(new UnmatchedColumn(sourceField, fromSource(sourceField.getView())));
                }
            }

            return columns;
        }
    }

    private ColumnView fromSource(ColumnView view) {
        return new MappedView(view) {
            @Override
            protected int transformRow(int row) {
                return rows.get(row).getSourceRow();
            }

            @Override
            public int numRows() {
                return rows.size();
            }
        };
    }
    
    private ColumnView fromTarget(ColumnView view) {
        return new MappedView(view) {
            @Override
            protected int transformRow(int row) {
                return rows.get(row).getTargetRow();
            }

            @Override
            public int numRows() {
                return rows.size();
            }
        };
    }
}
