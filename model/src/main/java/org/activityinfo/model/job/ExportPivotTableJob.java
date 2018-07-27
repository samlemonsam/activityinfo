package org.activityinfo.model.job;


import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.pivot.PivotModel;

/**
 * Export a Pivot Table in CSV format
 */
public class ExportPivotTableJob implements JobDescriptor<ExportResult> {

    public static  final String TYPE = "exportPivotTable";

    private PivotModel pivotModel;

    public ExportPivotTableJob(PivotModel pivotModel) {
        this.pivotModel = pivotModel;
    }

    public PivotModel getPivotModel() {
        return pivotModel;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ExportResult parseResult(JsonValue resultObject) {
        return ExportResult.fromJson(resultObject);
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("pivotModel", pivotModel.toJson());
        return object;
    }

    public static ExportPivotTableJob fromJson(JsonValue object) {
        if (!object.hasKey("pivotModel")) {
            throw new IllegalArgumentException("PivotModel not defined");
        }
        return new ExportPivotTableJob(PivotModel.fromJson(object.get("pivotModel")));

    }

}
