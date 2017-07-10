package org.activityinfo.model.analysis;

public class TypedAnalysis<T extends AnalysisModel> {
    private String id;
    private String label;
    private String parentId;
    private T model;

    public TypedAnalysis(String id, String label, String parentId, T model) {
        this.id = id;
        this.label = label;
        this.parentId = parentId;
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getParentId() {
        return parentId;
    }

    public T getModel() {
        return model;
    }

}
