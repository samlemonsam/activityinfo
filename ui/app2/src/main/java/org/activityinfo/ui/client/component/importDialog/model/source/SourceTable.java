package org.activityinfo.ui.client.component.importDialog.model.source;

import java.util.List;

public interface SourceTable {

    List<SourceColumn> getColumns();

    List<? extends SourceRow> getRows();

    String getColumnHeader(Integer columnIndex);

    /**
     * Parses all rows if not parsed yet. Otherwise if parsed do nothing.
     *
     * @return returns newly parsed rows (if nothing was parsed before returns all rows)
     */
    List<? extends SourceRow> parseAllRows();

    public List<? extends SourceRow> parseNextRows(int numberOfRowsToParse);

    boolean parsedAllRows();

}
