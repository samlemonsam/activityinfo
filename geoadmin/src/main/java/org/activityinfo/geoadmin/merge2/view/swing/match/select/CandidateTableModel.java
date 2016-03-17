package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import com.google.common.base.Strings;
import org.activityinfo.geoadmin.match.ScoreMatrix;
import org.activityinfo.geoadmin.merge2.view.match.InstanceMatchGraph;
import org.activityinfo.geoadmin.merge2.view.match.KeyFieldPairSet;
import org.activityinfo.geoadmin.merge2.view.match.MatchSide;

import javax.swing.table.AbstractTableModel;
import java.util.List;

class CandidateTableModel extends AbstractTableModel {
    private final KeyFieldPairSet keyFields;
    private final MatchSide side;
    private final List<Integer> frontier;
    private final ScoreMatrix matrix;
    private int fromIndex;

    public CandidateTableModel(InstanceMatchGraph graph, int fromIndex, MatchSide fromSide) {
        this.fromIndex = fromIndex;
        this.keyFields = graph.getKeyFields();
        this.side = fromSide;
        frontier = graph.getParetoFrontier(fromIndex, fromSide);
        matrix = graph.getMatrix();
        dumpMatchDetails(graph);
    }

    private void dumpMatchDetails(InstanceMatchGraph graph) {
        System.out.println("=== PARETO FRONTIER === ");
    }

    public int getColumnCount() { return keyFields.size(); }

    public int getRowCount() { 
        return frontier.size();
    }

    public Object getValueAt(int row, int col) {
        int index = candidateRowToInstanceIndex(row);
        String value = keyFields.getField(col, side.opposite()).getView().getString(index);
        double score = rowScore(matrix, index, col);
        
        return String.format("%s [%.2f]", Strings.nullToEmpty(value), score);
    }

    private double rowScore(ScoreMatrix matrix, int row, int col) {
        int sourceIndex;
        int targetIndex;
        switch (side) {
            case TARGET:
                // matching TARGET to SOURCE: candidates are source rows
                sourceIndex = row;
                targetIndex = fromIndex;
                break;
            case SOURCE:
                sourceIndex = fromIndex;
                targetIndex = row;
                break;
            default:
                throw new IllegalStateException("side: " + side);
        }
        
        return matrix.score(sourceIndex, targetIndex, col);
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
