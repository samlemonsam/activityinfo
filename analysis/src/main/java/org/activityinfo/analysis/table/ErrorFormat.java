package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnModel;

import java.util.Collections;
import java.util.List;

public class ErrorFormat implements ColumnFormat {


    @Override
    public List<ColumnModel> getColumnModels() {
        return Collections.emptyList();
    }

    @Override
    public <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor) {
        return visitor.visitErrorColumn(columnModel, this);
    }
}
