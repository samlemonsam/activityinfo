package org.activityinfo.model.database.transfer;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

public class TransferDecision implements JsonSerializable {

    private boolean cancelled;

    public TransferDecision(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static TransferDecision cancelled() {
        return new TransferDecision(true);
    }

    public static TransferDecision rejected() {
        return new TransferDecision(false);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isRejected() {
        return !cancelled;
    }

    public static TransferDecision fromJson(JsonValue object) {
        if (object.hasKey("cancelled")) {
            return new TransferDecision(object.get("cancelled").asBoolean());
        } else {
            throw new IllegalArgumentException("Json Object does not have 'cancelled' value");
        }
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("cancelled", cancelled);
        return object;
    }
}
