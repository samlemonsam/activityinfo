package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.view.model.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.model.RowMatching;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.activityinfo.io.match.names.LatinPlaceNameScorer;
import org.activityinfo.observable.Observable;

import java.awt.*;
import java.util.List;


public class TargetColumn implements MergeTableColumn {

    private final Observable<RowMatching> matching;
    private final FieldProfile targetField;
    private final java.util.List<SourceFieldMapping> mappings;
    private final LatinPlaceNameScorer scorer = new LatinPlaceNameScorer();

    public TargetColumn(Observable<RowMatching> matching, FieldProfile targetField, List<SourceFieldMapping> mappings) {
        this.matching = matching;
        this.targetField = targetField;
        this.mappings = mappings;
    }

    @Override
    public String getHeader() {
        return targetField.getLabel();
    }

    @Override
    public String getValue(int rowIndex) {
        if(matching.isLoading()) {
            return null;
        }
        Object value = getTargetValue(rowIndex);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private Object getTargetValue(int rowIndex) {
        int targetRow = matching.get().get(rowIndex).getTargetRow();
        if(targetRow == -1) {
            return null;
        }
        return targetField.getView().get(targetRow);
    }

    private Object getSourceValue(int rowIndex) {
        int sourceRow = matching.get().get(rowIndex).getSourceRow();
        if(sourceRow == -1) {
            return null;
        }
        return mappings.get(0).getSourceField().getView().get(sourceRow);
    }


    @Override
    public Color getColor(int rowIndex) {
        if (matching.isLoading() || mappings.isEmpty()) {
            return Color.WHITE;
        }
        Object sourceValue = getSourceValue(rowIndex);
        Object targetValue = getTargetValue(rowIndex);

        if (sourceValue instanceof String && targetValue instanceof String) {
            double score = scorer.score((String) sourceValue, (String) targetValue);
            if (score > 0.99) {
                return Color.decode("#006600");
            } else if (score > 0.80) {
                return Color.decode("#FF6600");
            }
        }
        return Color.RED;
    }

    @Override
    public int getTextAlignment() {
        return 0;
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
