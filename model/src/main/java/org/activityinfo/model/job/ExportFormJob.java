package org.activityinfo.model.job;

import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.ImmutableTableModel;
import org.activityinfo.model.analysis.TableModel;
import org.activityinfo.model.resource.ResourceId;

import static org.activityinfo.json.Json.createObject;

/**
 * Exports a single form to a CSV table
 */
public class ExportFormJob implements JobDescriptor<ExportResult> {

    public static final String TYPE = "exportForm";

    private TableModel tableModel;

    public ExportFormJob(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    public ResourceId getFormId() {
        return tableModel.getFormId();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ExportResult parseResult(JsonValue resultObject) {
        return ExportResult.fromJson(resultObject);
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = createObject();
        object.put("model", tableModel.toJson());
        return object;
    }

    public static ExportFormJob fromJson(JsonValue object) {
        TableModel tableModel;
        if(object.hasKey("formId")) {
            tableModel = ImmutableTableModel.builder()
                    .formId(ResourceId.valueOf(object.get("formId").asString()))
                    .build();
        } else {
            tableModel = TableModel.fromJson(object.get("model"));
        }
        return new ExportFormJob(tableModel);
    }
}
