package org.activityinfo.model.account;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

import java.util.Date;

/**
 * The status of the user's billing account
 */
public class AccountStatus implements JsonSerializable {

    private boolean trial;
    private int expirationTime;
    private int userLimit;
    private int userCount;
    private int databaseCount;

    private AccountStatus() {
    }

    public boolean isTrial() {
        return trial;
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

    public int getDatabaseCount() {
        return databaseCount;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("trial", trial);
        object.put("expirationTime", expirationTime);
        object.put("userLimit", userLimit);
        object.put("userCount", userCount);
        object.put("databaseCount", databaseCount);
        return object;
    }

    public static AccountStatus fromJson(JsonValue object) {
        AccountStatus status = new AccountStatus();
        status.trial = object.getBoolean("trial");
        status.expirationTime = (int) object.getNumber("expirationTime");
        status.userLimit = (int) object.getNumber("userLimit");
        status.userCount = (int) object.getNumber("userCount");
        status.databaseCount = (int)object.getNumber("databaseCount");
        return status;
    }

    public static class Builder {
        private AccountStatus status = new AccountStatus();

        public Builder setExpirationTime(Date time) {
            if(time != null) {
                status.expirationTime = (int) (time.getTime() / 1000);
            }
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

        public Builder setDatabaseCount(int count) {
            status.databaseCount = count;
            return this;
        }

        public Builder setSubscribed(boolean subscribed) {
            status.trial = subscribed;
            return this;
        }

        public AccountStatus build() {
            return status;
        }
    }
}
