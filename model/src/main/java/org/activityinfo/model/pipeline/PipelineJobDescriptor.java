package org.activityinfo.model.pipeline;

import org.activityinfo.json.JsonSerializable;

import java.io.Serializable;

public interface PipelineJobDescriptor extends JsonSerializable, Serializable {

    String getType();

}
