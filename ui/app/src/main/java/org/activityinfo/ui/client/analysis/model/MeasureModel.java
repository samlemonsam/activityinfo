package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;

import java.util.Collections;
import java.util.Set;

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
     * @return the statistics function used to combine values.
     */
    @org.immutables.value.Value.Default
    public Set<Statistic> getStatistics() {
        return Collections.singleton(Statistic.SUM);
    }
}
