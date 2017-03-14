package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.query.ColumnSet;


public interface DimensionReaderFactory {

    DimensionReader createReader(ColumnSet columnSet);
}
