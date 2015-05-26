package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.view.model.MatchTable;
import org.activityinfo.geoadmin.merge2.view.model.SourceFieldMapping;
import org.activityinfo.observable.Observable;

import javax.swing.*;
import java.awt.*;

/**
 * Source Column
 */
public class SourceColumn extends MergeTableColumn {

    private Observable<MatchTable> matching;
    private SourceFieldMapping mapping;

    public SourceColumn(Observable<MatchTable> matching, SourceFieldMapping mapping) {
        this.matching = matching;
        this.mapping = mapping;
    }

    @Override
    public String getHeader() {
        return mapping.getSourceField().getLabel();
    }

    @Override
    public String getValue(int rowIndex) {
        if(matching.isLoading()) {
            return null;
        }
        int sourceRow = matching.get().get(rowIndex).getSourceRow();
        if(sourceRow == -1) {
            return null;
        }
        Object value = mapping.getSourceField().getView().get(sourceRow);
        if(value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    public Color getColor(int rowIndex) {
        return Color.WHITE;
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
