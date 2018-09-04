package org.activityinfo.model.account;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import java.util.Date;

/**
 * The status of the user's billing account
 */
public class AccountStatus implements JsonSerializable {

    private boolean subscribed;
    private int expirationTime;
    private int userLimit;
    private int userCount;

    private AccountStatus() {
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public int getUserCount() {
        return userCount;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("subscribed", subscribed);
        object.put("expirationTime", expirationTime);
        object.put("userLimit", userLimit);
        object.put("userCount", userCount);
        return object;
    }

    public static AccountStatus fromJson(JsonValue object) {
        AccountStatus status = new AccountStatus();
        status.subscribed = object.getBoolean("subscribed");
        status.expirationTime = (int) object.getNumber("expirationTime");
        status.userLimit = (int) object.getNumber("userLimit");
        status.userCount = (int) object.getNumber("userCount");
        return status;
    }

    public static class Builder {
        private AccountStatus status = new AccountStatus();

        public Builder setExpirationTime(Date time){
            status.expirationTime = (int) (time.getTime() / 1000);
            return this;
        }

        public Builder setUserLimit(int limit) {
            status.userLimit = limit;
            return this;
        }

        public Builder setUserCount(int count) {
            status.userCount = count;
            return this;
        }

        public Builder setSubscribed(boolean subscribed) {
            status.subscribed = subscribed;
            return this;
        }

        public AccountStatus build() {
            return status;
        }
    }
}
