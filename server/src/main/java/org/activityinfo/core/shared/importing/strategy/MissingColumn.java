package org.activityinfo.core.shared.importing.strategy;

import org.activityinfo.core.shared.importing.source.SourceRow;

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
