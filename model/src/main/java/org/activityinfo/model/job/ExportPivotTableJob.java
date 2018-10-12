package org.activityinfo.model.job;


import com.google.common.collect.Maps;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.analysis.pivot.PivotModel;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Export a Pivot Table in CSV format
 */
public class ExportPivotTableJob implements JobDescriptor<ExportResult> {

    public static  final String TYPE = "exportPivotTable";

    private PivotModel pivotModel;
    private boolean includeFolderLabels;
    private Map<ResourceId,String> folderMapping;

    public ExportPivotTableJob(@NotNull PivotModel pivotModel,
                               @NotNull boolean includeFolderLabels,
                               @Nullable Map<ResourceId,String> folderMapping) {
        this.pivotModel = pivotModel;
        this.includeFolderLabels = includeFolderLabels;
        this.folderMapping = folderMapping;
    }

    public PivotModel getPivotModel() {
        return pivotModel;
    }

    public boolean isIncludeFolderLabels() {
        return includeFolderLabels;
    }

    public Map<ResourceId, String> getFolderMapping() {
        return folderMapping;
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
        object.put("includeFolderLabels", includeFolderLabels);
        if (includeFolderLabels) {
            JsonValue array = Json.createArray();
            folderMapping.entrySet().forEach(mapping -> {
                JsonValue map = Json.createObject();
                map.put("formId", mapping.getKey().asString());
                map.put("folderLabel", mapping.getValue());
                array.add(map);
            });
            object.put("folderMapping", array);
        }
        return object;
    }

    public static ExportPivotTableJob fromJson(JsonValue object) {
        if (!object.hasKey("pivotModel")) {
            throw new IllegalArgumentException("PivotModel not defined");
        }

        JsonValue pivotModel = object.get("pivotModel");

        boolean includeFolderLabels = object.get("hasFolderLabels").asBoolean();
        Map<ResourceId,String> folderMapping = null;

        if (includeFolderLabels) {
            folderMapping = Maps.newHashMap();
            JsonValue array = object.get("folderMapping");
            for (int i=0; i<array.length(); i++) {
                JsonValue map = array.get(i);
                ResourceId formId = ResourceId.valueOf(map.get("formId").asString());
                String folderLabel = map.get("folderLabel").asString();
                folderMapping.put(formId, folderLabel);
            }
        }

        return new ExportPivotTableJob(PivotModel.fromJson(pivotModel), includeFolderLabels, folderMapping);
    }

}
