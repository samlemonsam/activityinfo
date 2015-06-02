package org.activityinfo.geoadmin.merge2.view.match;

import org.activityinfo.geoadmin.match.MatchLevel;
import org.activityinfo.geoadmin.merge2.model.InstanceMatchSet;

import javax.swing.*;
import java.awt.*;

/**
 * Displays the resolution status of the row
 */
public class ResolutionColumn extends MatchTableColumn {
    
    public static final String WARNING_ICON = "⚠";
    
    public static final String CHECK_ICON = "\u2713";
    
    
    private final MatchTable rows;
    private InstanceMatchSet matchSet;

    public ResolutionColumn(MatchTable model, InstanceMatchSet matchSet) {
        this.rows = model;
        this.matchSet = matchSet;
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
        MatchRow row = rows.get(rowIndex);
        if(row.getMatchLevel() != MatchLevel.EXACT) {
            if(row.isResolved()) {
                return CHECK_ICON;
            } else {
                return WARNING_ICON;
            }
        }
        return null;
    }

    @Override
    public Color getColor(int rowIndex) {
        return Color.WHITE;
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
