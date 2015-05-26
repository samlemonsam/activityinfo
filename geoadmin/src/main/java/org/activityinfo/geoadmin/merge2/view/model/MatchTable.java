package org.activityinfo.geoadmin.merge2.view.model;

import com.google.common.base.Function;
import org.activityinfo.observable.Observable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the matching of FormInstances in two collections as a flat table in which each
 * row represents either 
 */
public class MatchTable {

    private FormMapping mapping;
    private List<MatchRow> rows;

    public MatchTable(FormMapping mapping, List<MatchRow> rows) {
        this.mapping = mapping;
        this.rows = rows;
    }

    public MatchRow get(int index) {
        return rows.get(index);
    }

    public int getRowCount() {
        return rows.size();
    }

    public static Observable<MatchTable> compute(Observable<AutoRowMatching> autoMatching) {
        return autoMatching.transform(new Function<AutoRowMatching, MatchTable>() {
            @Override
            public MatchTable apply(AutoRowMatching input) {
                FormMapping formMapping = input.getFormMapping();
                List<MatchRow> rows = new ArrayList<MatchRow>();
                Set<Integer> matchedSources = new HashSet<>();

                // Add a row for each target row
                for (int targetRow = 0; targetRow < formMapping.getTarget().getRowCount(); ++targetRow) {
                    int sourceRow = input.getBestSourceMatchForTarget(targetRow);
                    if(sourceRow == -1) {
                        rows.add(new MatchRow(-1, targetRow, null));

                    } else {
                        rows.add(new MatchRow(sourceRow, targetRow, input.getScores(sourceRow, targetRow)));
                        matchedSources.add(sourceRow);
                    }
                }

                // Add finally add an output row for each unmatched source
                for (int sourceRow = 0; sourceRow < formMapping.getSource().getRowCount(); ++sourceRow) {
                    if (!matchedSources.contains(sourceRow)) {
                        rows.add(new MatchRow(sourceRow, -1, null));
                    }
                }
                
                return new MatchTable(formMapping, rows);
            }
        });
    }

}
