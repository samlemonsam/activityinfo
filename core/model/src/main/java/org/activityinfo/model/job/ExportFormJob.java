package org.activityinfo.model.job;

import org.activityinfo.json.JsonObject;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.resource.ResourceId;

import java.util.ArrayList;
import java.util.List;

import static org.activityinfo.json.Json.createObject;

/**
 * Exports a single form to a CSV table
 */
public class ExportFormJob implements JobDescriptor<ExportResult> {

    public static final String TYPE = "exportForm";

    private ResourceId formId;
    private List<ExportColumn> columns;

    public ExportFormJob(ResourceId formId, List<ExportColumn> columns) {
        this.formId = formId;
        this.columns = columns;
    }

    public ExportFormJob(TableModel tableModel) {
        this.formId = tableModel.getFormId();
        this.columns = new ArrayList<>();
    }

    public ResourceId getFormId() {
        return formId;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public List<ExportColumn> getColumns() {
        return columns;
    }

    @Override
    public ExportResult parseResult(org.activityinfo.json.JsonObject resultObject) {
        return ExportResult.fromJson(resultObject);
    }

    public TableModel getTableModel() {
        return ImmutableTableModel.builder().formId(formId).build();
    }

    @Override
    public org.activityinfo.json.JsonObject toJsonObject() {
        JsonObject object = createObject();
        object.put("formId", formId.asString());
        return object;
    }

    public static ExportFormJob fromJson(org.activityinfo.json.JsonObject object) {

        List<ExportColumn> columns = new ArrayList<>();
        if(object.hasKey("columns")) {
            for (JsonValue jsonElement : object.get("columns").getAsJsonArray().values()) {
                columns.add(ExportColumn.fromJson(jsonElement.getAsJsonObject()));
            }
        }

        return new ExportFormJob(ResourceId.valueOf(object.get("formId").asString()), columns);
    }
}
