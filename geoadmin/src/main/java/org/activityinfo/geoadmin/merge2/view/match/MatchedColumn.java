package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;

import javax.swing.*;
import java.awt.*;


/**
 * Displays a column from the source or target collection that is matched to the opposing collection
 */
public class MatchedColumn extends MatchTableColumn {

    public static final Color WARNING_COLOR = Color.decode("#FF6600");
    private final MatchTable matching;
    private final FieldProfile sourceField;
    private final FieldProfile targetField;
    private final LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();
    private final MatchSide side;

    public MatchedColumn(MatchTable matching, FieldProfile targetField, FieldProfile sourceField, MatchSide side) {
        this.matching = matching;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.side = side;
    }

    @Override
    public String getHeader() {
        if(side == MatchSide.SOURCE) {
            return sourceField.getLabel();
        } else {
            return targetField.getLabel();
        }
    }

    @Override
    public String getValue(int rowIndex) {
        if(matching.isLoading()) {
            return null;
        }
        if(side == MatchSide.SOURCE) {
            return formatValue(getSourceValue(rowIndex));
        } else {
            return formatValue(getTargetValue(rowIndex));
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private Object getTargetValue(int rowIndex) {
        int targetRow = matching.get(rowIndex).getTargetRow();
        if(targetRow == -1) {
            return null;
        }
        return targetField.getView().get(targetRow);
    }

    private Object getSourceValue(int rowIndex) {
        int sourceRow = matching.get(rowIndex).getSourceRow();
        if(sourceRow == -1) {
            return null;
        }
        return sourceField.getView().get(sourceRow);
    }

    @Override
    public Color getColor(int rowIndex) {
        if (matching.isLoading()) {
            return Color.WHITE;
        }
        Object sourceValue = getSourceValue(rowIndex);
        Object targetValue = getTargetValue(rowIndex);

        // If the user has marked this field as resolved, "mute" the 
        // the warning colors
        if(matching.get(rowIndex).isResolved()) {
            return Color.GREEN;
        }
        
        if (sourceValue instanceof String && targetValue instanceof String) {
            double score = scorer.score((String) sourceValue, (String) targetValue);
            if (score > 0.99) {
                return Color.GREEN; 
            } else if (score > 0.80) {
                return WARNING_COLOR;
            }
        }
        return Color.RED;
    }

    @Override
    public int getTextAlignment() {
        return SwingConstants.LEFT;
    }

    @Override
    public int getWidth() {
        return -1;
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
