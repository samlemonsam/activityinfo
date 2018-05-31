package org.activityinfo.model.pipeline;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class AdditionJobDescriptor implements PipelineJobDescriptor {

    public static final String TYPE = "add";

    private int a;
    private int b;

    public AdditionJobDescriptor(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("a", a);
        object.put("b", b);
        return object;
    }

    public static AdditionJobDescriptor fromJson(JsonValue object) {
        int a = object.get("a").asInt();
        int b = object.get("b").asInt();
        return new AdditionJobDescriptor(a,b);
    }
}
