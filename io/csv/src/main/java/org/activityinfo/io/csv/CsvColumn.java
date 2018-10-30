package org.activityinfo.io.csv;

import org.activityinfo.analysis.table.ColumnRenderer;

public class CsvColumn {

    private String heading;
    private ColumnRenderer renderer;

    public CsvColumn(String heading, ColumnRenderer renderer) {
        this.heading = heading;
        this.renderer = renderer;
    }

    public String getHeading() {
        return heading;
    }

    public boolean isMissing(int row) {
        return renderer.render(row) == null;
    }

    public Object getValue(int row) {
        return renderer.render(row);
    }
}
