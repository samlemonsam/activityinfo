package org.activityinfo.geoadmin.merge;

import org.activityinfo.geoadmin.match.MatchSolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 22-4-15.
 */
public class MergeModel {
    
    private MergeFormViewModel source;
    private MergeFormViewModel target;

    public MergeModel(MergeFormViewModel source, MergeFormViewModel target) {
        this.source = source;
        this.target = target;
    }
    
    private void matchColumns() {

        List<MergeColumn> targetColumns = new ArrayList<>(target.getTextFields());
        List<MergeColumn> sourceColumns = new ArrayList<>(source.getTextFields());

        MatchSolver<MergeColumn> solver = new MatchSolver<>();
        solver.solve(targetColumns, sourceColumns, new ColumnDistance());
        solver.dumpSubGraphs();
        
    }
    
    public void build() {
        matchColumns();
    }
}


