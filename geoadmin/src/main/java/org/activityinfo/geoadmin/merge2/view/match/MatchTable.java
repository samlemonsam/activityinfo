package org.activityinfo.geoadmin.merge2.view.match;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.activityinfo.geoadmin.match.MatchLevel;
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
    
    private ImportModel model;
    private Scheduler scheduler;

    private final InstanceMatchSet matchSet;

    private final Observable<FieldMatching> fieldMatching;
    private final Observable<AutoRowMatching> autoRowMatching;

    private List<TableObserver> observers = new CopyOnWriteArrayList<>();
    
    private final SubscriptionSet subscriptions = new SubscriptionSet();
    
    private Observable<List<MatchTableColumn>> columns;
    private List<MatchRow> rows = new ArrayList<>();
    
    public MatchTable(ImportModel model, Scheduler scheduler, Observable<FieldMatching> fieldMatching) {
        this.model = model;
        this.scheduler = scheduler;
        this.matchSet = model.getInstanceMatchSet();
        this.fieldMatching = fieldMatching;
        autoRowMatching = fieldMatching.transform(scheduler, new AutoMatcher());
        columns = autoRowMatching.transform(new ColumnListBuilder());
    }

    public Observable<FieldMatching> getFieldMapping() {
        return fieldMatching;
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
        subscriptions.add(autoRowMatching.subscribe(new Observer<AutoRowMatching>() {
            @Override
            public void onChange(Observable<AutoRowMatching> observable) {
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
                    if(row.getMatchLevel() != MatchLevel.EXACT && !row.isResolved()) {
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
        if(autoRowMatching.isLoading() || matchSet.isLoading()) {
            return;
        }
        FieldMatching fieldMatching = autoRowMatching.get().getFieldMatching();
        AutoRowMatching autoMatching = autoRowMatching.get();
        
        List<MatchRow> rows = new ArrayList<>();
        Set<Integer> matchedSources = new HashSet<>();

        // Add a row for each target row
        for (int targetRow = 0; targetRow < fieldMatching.getTarget().getRowCount(); ++targetRow) {
            
            MatchRow row = new MatchRow(autoMatching);
            row.setTargetRow(targetRow);
            
            ResourceId targetId = fieldMatching.getTarget().getRowId(targetRow);
            Optional<InstanceMatch> explicitMatch = matchSet.find(targetId);
            
            if(explicitMatch.isPresent()) {

                // the user has provided an explicit match between the two rows

                ResourceId sourceId = explicitMatch.get().getSourceId();
                int sourceRow = fieldMatching.getSource().indexOf(sourceId);

                row.setSourceRow(sourceRow);
                row.setResolved(true);
                
                matchedSources.add(sourceRow);
            
            } else {
                
                // Use the closest automatic match
                
                int sourceRow = autoMatching.getBestSourceMatchForTarget(targetRow);
                if(sourceRow != MatchRow.UNMATCHED) {
                    
                    // If we don't have complete confidence in the match, flag the row
                    // for resolution by the user
                    row.setSourceRow(sourceRow);
                    matchedSources.add(sourceRow);
                }
            }
            rows.add(row);
        }

        // Add finally add an output row for each unmatched source
        for (int sourceRow = 0; sourceRow < fieldMatching.getSource().getRowCount(); ++sourceRow) {
            if (!matchedSources.contains(sourceRow)) {
                MatchRow row = new MatchRow(autoRowMatching.get());
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
        return autoRowMatching.isLoading() || matchSet.isLoading();
    }

    public Observable<List<MatchTableColumn>> getColumns() {
        return columns;
    }

    private class ColumnListBuilder implements Function<AutoRowMatching, List<MatchTableColumn>> {

        @Override
        public List<MatchTableColumn> apply(AutoRowMatching input) {
            List<MatchTableColumn> columns = new ArrayList<>();
            FieldMatching fieldMatching = input.getFieldMatching();

            columns.add(new ResolutionColumn(MatchTable.this, matchSet));

            // Show the existing instances, paired with the matching column
            for (FieldProfile targetField : fieldMatching.getTarget().getFields()) {
                Optional<FieldProfile> sourceField = fieldMatching.targetToSource(targetField);
                if(sourceField.isPresent()) {
                    columns.add(new MatchedColumn(MatchTable.this, targetField, sourceField.get(), MatchSide.TARGET));
                    columns.add(new MatchedColumn(MatchTable.this, targetField, sourceField.get(), MatchSide.SOURCE));
                } else {
                    columns.add(new UnmatchedColumn(targetField, fromTarget(targetField.getView())));
                }
            }
            
            // Also include the unmatched source columns as reference
            for (FieldProfile sourceField : fieldMatching.getSource().getFields()) {
                if(!fieldMatching.sourceToTarget(sourceField).isPresent()) {
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
