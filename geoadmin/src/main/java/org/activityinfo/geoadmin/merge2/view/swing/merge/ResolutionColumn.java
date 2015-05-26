package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.model.RowMatch;
import org.activityinfo.geoadmin.merge2.view.model.RowMatching;
import org.activityinfo.observable.Observable;

import javax.swing.*;
import java.awt.*;

/**
 * Displays the resolution status of the row
 */
public class ResolutionColumn extends MergeTableColumn {
    
    public static final String WARNING_ICON = "⚠";
    
    public static final String CHECK_ICON = "\u2713";
    
    
    private final Observable<RowMatching> rows;

    public ResolutionColumn(MergeModelStore model) {
        this.rows = model.getRowMatching();
    }

    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getValue(int rowIndex) {
        if(rows.isLoading()) {
            return null;
        }
        /**
         * The lowest score from among all the column mappings
         */
        double minScore = rows.get().get(rowIndex).getMinScore();
        MatchLevel matchLevel = MatchLevel.of(minScore);
        
        switch (matchLevel) {
            case WARNING:
            case POOR:
                return WARNING_ICON;
        }
        
        return null;
    }

    @Override
    public Color getColor(int rowIndex) {
        return Color.WHITE;
    }

    @Override
    public void onClick(int rowIndex) {
        if(!rows.isLoading()) {
            RowMatch row = rows.get().get(rowIndex);
            if(row.isMatched()) {
                
            }
        }
    }

    @Override
    public int getTextAlignment() {
        return SwingConstants.CENTER;
    }

    @Override
    public int getWidth() {
        return 10;
    }

    @Override
    public boolean isResizable() {
        return false;
    }
}
