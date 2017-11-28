package org.activityinfo.model.analysis;

import org.activityinfo.json.JsonValue;

public interface AnalysisModel {

    String getTypeId();

    JsonValue toJson();
}
