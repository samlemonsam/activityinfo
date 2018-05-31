package org.activityinfo.model.pipeline;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class PipelineJobRequest {

    int requesterId;
    PipelineJobDescriptor jobDescriptor;

    public PipelineJobRequest(int requesterId, PipelineJobDescriptor jobDescriptor) {
        this.requesterId = requesterId;
        this.jobDescriptor = jobDescriptor;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(int requestId) {
        this.requesterId = requestId;
    }

    public PipelineJobDescriptor getJobDescriptor() {
        return jobDescriptor;
    }

    public void setJobDescriptor(PipelineJobDescriptor jobDescriptor) {
        this.jobDescriptor = jobDescriptor;
    }

    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("requesterId", requesterId);
        object.put("type", jobDescriptor.getType());
        object.put("descriptor", jobDescriptor.toJson());
        return object;
    }

    public static PipelineJobRequest fromJson(JsonValue object) {
        int requesterId = object.get("requesterId").asInt();
        String type = object.get("type").asString();
        PipelineJobDescriptor descriptor = parseDescriptor(type, object.get("descriptor"));
        return new PipelineJobRequest(requesterId, descriptor);
    }

    public static PipelineJobDescriptor parseDescriptor(String type, JsonValue descriptor) {
        switch(type) {
            case AdditionJobDescriptor.TYPE:
                return AdditionJobDescriptor.fromJson(descriptor);
            default:
                break;
        }
        throw new IllegalArgumentException(type);
    }
}
