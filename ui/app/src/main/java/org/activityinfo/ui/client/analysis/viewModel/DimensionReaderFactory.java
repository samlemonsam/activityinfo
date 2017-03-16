package org.activityinfo.ui.client.analysis.viewModel;

import org.activityinfo.model.query.ColumnSet;


public interface DimensionReaderFactory {

//    /**
//     *
//     * @return the index of this dimension in the overall DimensionSet.
//     */
//    int getDimensionIndex();

    DimensionReader createReader(ColumnSet columnSet);
}
