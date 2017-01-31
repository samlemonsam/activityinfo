package org.activityinfo.ui.client.analysis.model;

public class DimensionModel {

    private String id;
    private String label;
    private DimensionSourceModel sourceModel;

    public DimensionModel(String id, DimensionSourceModel sourceModel) {
        this.id = id;
        this.sourceModel = sourceModel;
        this.label = sourceModel.getLabel();
    }

    public String getLabel() {
        return label;
    }

    public DimensionSourceModel getSourceModel() {
        return sourceModel;
    }


    public String getId() {
        return id;
    }
}
