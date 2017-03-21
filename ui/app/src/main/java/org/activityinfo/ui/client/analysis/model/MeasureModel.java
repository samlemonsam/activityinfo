package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;

/**
 * A measure contributes quantities to an analysis.
 */
@org.immutables.value.Value.Immutable
public abstract class MeasureModel {

    @org.immutables.value.Value.Default
    public String getId() {
        return ResourceId.generateCuid();
    }

    public abstract String getLabel();

    /**
     *
     * @return the id of the form that is the source of this measure.
     */
    public abstract ResourceId getFormId();
    public abstract String getFormula();

    /**
     *
     * @return the aggregation function used to combine values.
     */
    @org.immutables.value.Value.Default
    public Aggregation getAggregation() {
        return Aggregation.SUM;
    }
}
