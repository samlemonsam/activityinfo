package org.activityinfo.geoadmin.merge2.view.model;

import com.google.common.base.Function;
import org.activityinfo.observable.Observable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes the
 */
public class RowMatching {

    private FormMapping mapping;
    private List<RowMatch> rows;

    public RowMatching(FormMapping mapping, List<RowMatch> rows) {
        this.mapping = mapping;
        this.rows = rows;
    }

    public RowMatch get(int index) {
        return rows.get(index);
    }

    public int getRowCount() {
        return rows.size();
    }

    public static Observable<RowMatching> compute(Observable<AutoRowMatching> autoMatching) {
        return autoMatching.transform(new Function<AutoRowMatching, RowMatching>() {
            @Override
            public RowMatching apply(AutoRowMatching input) {
                FormMapping formMapping = input.getFormMapping();
                List<RowMatch> rows = new ArrayList<RowMatch>();
                Set<Integer> matchedSources = new HashSet<>();

                // Add a row for each target row
                for (int targetRow = 0; targetRow < formMapping.getTarget().getRowCount(); ++targetRow) {
                    int sourceRow = input.getBestSourceMatchForTarget(targetRow);
                    if(sourceRow == -1) {
                        rows.add(new RowMatch(-1, targetRow, null));

                    } else {
                        rows.add(new RowMatch(sourceRow, targetRow, input.getScores(sourceRow, targetRow)));
                        matchedSources.add(sourceRow);
                    }
                }

                // Add finally add an output row for each unmatched source
                for (int sourceRow = 0; sourceRow < formMapping.getSource().getRowCount(); ++sourceRow) {
                    if (!matchedSources.contains(sourceRow)) {
                        rows.add(new RowMatch(sourceRow, -1, null));
                    }
                }
                
                return new RowMatching(formMapping, rows);
            }
        });
    }

}
