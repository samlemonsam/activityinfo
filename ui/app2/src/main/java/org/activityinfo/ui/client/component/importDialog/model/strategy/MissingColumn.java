package org.activityinfo.ui.client.component.importDialog.model.strategy;

import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;

public class MissingColumn implements ColumnAccessor {

    public final static MissingColumn INSTANCE = new MissingColumn();

    private MissingColumn() {
    }

    private String header;

    public MissingColumn(String header) {
        this.header = header;
    }

    @Override
    public String getHeading() {
        return header;
    }

    @Override
    public String getValue(SourceRow row) {
        return null;
    }

    @Override
    public boolean isMissing(SourceRow row) {
        return true;
    }
}
