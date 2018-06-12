package org.activityinfo.model.database.transfer;

import com.google.common.base.Preconditions;
import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import javax.validation.constraints.NotNull;

public class TransferAuthorized implements JsonSerializable {

    private int currentOwner;
    private int proposedOwner;
    private int database;
    private String token;

    public TransferAuthorized() {
    }

    public TransferAuthorized(@NotNull int currentOwner,
                              @NotNull int proposedOwner,
                              @NotNull int database,
                              @NotNull String token) {
        Preconditions.checkState(currentOwner != proposedOwner, "Cannot transfer to same user");

        this.currentOwner = currentOwner;
        this.proposedOwner = proposedOwner;
        this.database = database;
        this.token = token;
    }

    public int getCurrentOwner() {
        return currentOwner;
    }

    public int getProposedOwner() {
        return proposedOwner;
    }

    public int getDatabase() {
        return database;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("currentOwner", currentOwner);
        object.put("proposedOwner", proposedOwner);
        object.put("database", database);
        object.put("token", token);
        return object;
    }

    public static TransferAuthorized fromJson(JsonValue object) {
        int currentOwner = object.get("currentOwner").asInt();
        int proposedOwner = object.get("proposedOwner").asInt();
        int database = object.get("database").asInt();
        String token = object.get("token").asString();
        return new TransferAuthorized(currentOwner, proposedOwner, database, token);
    }
}
