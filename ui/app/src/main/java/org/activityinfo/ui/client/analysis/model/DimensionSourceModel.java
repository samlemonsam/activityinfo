package org.activityinfo.ui.client.analysis.model;


import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;

import java.util.Collections;
import java.util.Set;

/**
 * Models a dimension "source".
 */
public abstract class DimensionSourceModel {

    public abstract String getLabel();


    public Set<ColumnModel> getRequiredColumns(String dimensionId) {
        return Collections.emptySet();
    }

    public abstract DimensionReader createReader(String dimensionId, FormClass formClass, ColumnSet input);


}
