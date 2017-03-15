package org.activityinfo.ui.client.analysis.model;

import org.activityinfo.model.resource.ResourceId;

/**
 * A measure contributes quantities to an analysis.
 */
public final class MeasureModel {

    private final String id;
    private final String label;

    private final ResourceId formId;
    private final String formula;

    private AggregationType aggregation = AggregationType.SUM;

    public MeasureModel(String id, String label, ResourceId formId, String formula) {
        this.id = id;
        this.label = label;
        this.formId = formId;
        this.formula = formula;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    /**
     *
     * @return the id of the form that is the source of this measure.
     */
    public ResourceId getFormId() {
        return formId;
    }

    public String getFormula() {
        return formula;
    }

    public AggregationType getAggregation() {
        return aggregation;
    }

    public MeasureModel setAggregation(AggregationType aggregation) {
        this.aggregation = aggregation;
        return this;
    }
}
