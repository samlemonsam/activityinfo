package org.activityinfo.model.pipeline;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;

public class RequestDatabaseTransferJobDescriptor implements PipelineJobDescriptor {

    public static final String TYPE = "transferDatabase";

    int from;
    int to;
    int database;

    public RequestDatabaseTransferJobDescriptor(int from, int to, int database) {
        this.from = from;
        this.to = to;
        this.database = database;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("from", from);
        object.put("to", to);
        object.put("database", database);
        return object;
    }

    public static RequestDatabaseTransferJobDescriptor fromJson(JsonValue object) {
        int from = object.get("from").asInt();
        int to = object.get("to").asInt();
        int database = object.get("database").asInt();
        return new RequestDatabaseTransferJobDescriptor(from, to, database);
    }

}
