package org.activityinfo.model.database.transfer;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import javax.validation.constraints.NotNull;

public class RequestTransfer implements JsonSerializable {

    private String proposedOwnerEmail;

    public RequestTransfer() {
    }

    public RequestTransfer(@NotNull String proposedOwnerEmail) {
        this.proposedOwnerEmail = proposedOwnerEmail;
    }

    public String getProposedOwnerEmail() {
        return proposedOwnerEmail;
    }

    public void setProposedOwnerEmail(String proposedOwnerEmail) {
        this.proposedOwnerEmail = proposedOwnerEmail;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("proposedOwner", proposedOwnerEmail);
        return object;
    }

    public static RequestTransfer fromJson(JsonValue object) {
        String proposedOwner = object.get("proposedOwner").asString();
        return new RequestTransfer(proposedOwner);
    }
}
