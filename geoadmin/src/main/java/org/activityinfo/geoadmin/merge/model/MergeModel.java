package org.activityinfo.geoadmin.merge.model;

import com.google.common.collect.BiMap;

import java.util.ArrayList;
import java.util.List;


public class MergeModel {

    private MergeForm target;
    private MergeForm source;

    private BiMap<MergeColumn, MergeColumn> columnMapping;
    private MatchBuilder matchBuilder;
    private List<MatchRow> matches;

    public MergeModel(MergeForm target, MergeForm source) {
        this.target = target;
        this.source = source;
    }

    public void build() {
        matchColumns();
        matchRows();
    }

    private void matchColumns() {

        List<MergeColumn> targetColumns = new ArrayList<>(target.getTextFields());
        List<MergeColumn> sourceColumns = new ArrayList<>(source.getTextFields());

        MatchBuilder columnGraph = new MatchBuilder(new ColumnMatrix(targetColumns, sourceColumns));
        columnMapping = columnGraph.buildMap(targetColumns, sourceColumns);
        columnGraph.dumpSubGraphs(targetColumns, sourceColumns);
    }

    private void matchRows() {
        this.matchBuilder = new MatchBuilder(new RowDistanceMatrix(target, source, columnMapping));
        this.matches = matchBuilder.buildMatchList();
    }

    public List<MatchRow> getMatches() {
        return matches;
    }

    public MergeForm getTargetForm() {
        return target;
    }

    public MergeForm getSourceForm() {
        return source;
    }

    public MergeColumn getSourceColumnForTarget(MergeColumn targetColumn) {
        return columnMapping.get(targetColumn);
    }

    public MatchRow getMatch(int matchIndex) {
        return matches.get(matchIndex);
    }

    public MergeColumn getTargetColumnForSource(MergeColumn sourceColumn) {
        return columnMapping.inverse().get(sourceColumn);
    }

    public boolean isMapped(MergeColumn column) {
        return columnMapping.containsKey(column) || columnMapping.containsValue(column);
    }
}
