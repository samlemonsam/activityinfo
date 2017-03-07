package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.ui.client.store.FormStore;

import java.util.Set;

public class DimensionModel {

    private String id;
    private String label;
    private DimensionSource sourceModel;

    DimensionModel(FormStore store, String id, DimensionSource sourceModel) {
        this.id = id;
        this.sourceModel = sourceModel;
        this.label = sourceModel.getLabel();

    }

    public String getLabel() {
        return label;
    }

    public DimensionSource getSourceModel() {
        return sourceModel;
    }

    public Set<ColumnModel> getRequiredColumns() {
        return sourceModel.getRequiredColumns(this.id);
    }

    public String getId() {
        return id;
    }


    public DimensionReader createReader(MeasureLabels measureLabels, FormClass formClass, ColumnSet columnSet) {
        return sourceModel.createReader(id, measureLabels, formClass, columnSet);
    }

    public JsonElement toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("label", label);
        object.add("source", sourceModel.toJsonObject());
        return object;
    }
}
