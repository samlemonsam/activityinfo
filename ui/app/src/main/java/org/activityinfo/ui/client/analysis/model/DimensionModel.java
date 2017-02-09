package org.activityinfo.ui.client.analysis.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.ColumnSet;

import java.util.Set;

public class DimensionModel {

    private String id;
    private String label;
    private DimensionSourceModel sourceModel;

    DimensionModel(String id, DimensionSourceModel sourceModel) {
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

    public Set<ColumnModel> getRequiredColumns() {
        return sourceModel.getRequiredColumns(this.id);
    }

    public String getId() {
        return id;
    }


    public DimensionReader createReader(FormClass formClass, ColumnSet columnSet) {
        return sourceModel.createReader(id, formClass, columnSet);
    }


    public JsonElement toJsonObject() {
        JsonObject object = new JsonObject();
        object.addProperty("id", id);
        object.addProperty("label", label);
        object.add("source", sourceModel.toJsonObject());
        return object;
    }
}
