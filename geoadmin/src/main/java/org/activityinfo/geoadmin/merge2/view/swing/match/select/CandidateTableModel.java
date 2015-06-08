package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.match.MatchGraph;
import org.activityinfo.geoadmin.merge2.view.match.MatchSide;

import javax.swing.table.AbstractTableModel;
import java.util.List;

class CandidateTableModel extends AbstractTableModel {
    private final KeyFieldPairSet keyFields;
    private final MatchSide side;
    private final List<Integer> frontier;

    public CandidateTableModel(MatchGraph graph, int fromIndex, MatchSide fromSide) {
        this.keyFields = graph.getKeyFields();
        this.side = fromSide;
        frontier = graph.getParetoFrontier(fromIndex, fromSide);
    }

    public int getColumnCount() { return keyFields.size(); }

    public int getRowCount() { 
        return frontier.size();
    }

    public Object getValueAt(int row, int col) {
        int index = candidateRowToInstanceIndex(row);
        return keyFields.getField(col, side.opposite()).getView().getString(index);
    }

    /**
     * Maps an index within the candidate list (which is displayed here as rows)
     * to the index of the actual instance within the source or target collection.
     * 
     */
    public int candidateRowToInstanceIndex(int row) {
        return frontier.get(row);
    }
    
    public int instanceIndexToCandidateRow(int instanceIndex) {
        return frontier.indexOf(instanceIndex);
    }
}
