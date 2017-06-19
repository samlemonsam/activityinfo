package org.activityinfo.analysis.table;

import org.activityinfo.model.query.ColumnModel;

import java.util.List;

public interface ColumnFormat {

    List<ColumnModel> getColumnModels();

    <T> T accept(EffectiveTableColumn columnModel, TableColumnVisitor<T> visitor);
}
