package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.match.MatchGraph;
import org.activityinfo.geoadmin.merge2.view.match.MatchRow;
import org.activityinfo.geoadmin.merge2.view.match.MatchSide;

import javax.swing.table.AbstractTableModel;
import java.util.List;

class CandidateTableModel extends AbstractTableModel {
    private final MatchGraph graph;
    private final KeyFieldPairSet keyFields;
    private final MatchSide side;
    private final List<Integer> frontier;

    public CandidateTableModel(MatchGraph graph, MatchRow matchRow, MatchSide side) {
        this.graph = graph;
        this.keyFields = graph.getKeyFields();
        this.side = side;
        frontier = graph.getParetoFrontier(matchRow.getRow(side), side);
    }

    public int getColumnCount() { return keyFields.size(); }

    public int getRowCount() { 
    
    //    return keyFields.getForm(side).getRowCount(); 
        return frontier.size();
    }

    public Object getValueAt(int row, int col) {
        int index = frontier.get(row);
        return keyFields.getField(col, side.opposite()).getView().getString(index);
    }
}
