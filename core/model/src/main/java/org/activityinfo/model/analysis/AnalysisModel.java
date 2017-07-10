package org.activityinfo.model.analysis;

import org.activityinfo.json.JsonObject;

public interface AnalysisModel {

    String getTypeId();

    JsonObject toJson();
}
