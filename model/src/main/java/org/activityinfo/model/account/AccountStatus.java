package org.activityinfo.model.account;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

/**
 * The status of the user's billing account
 */
public class AccountStatus implements JsonSerializable {

    public static final int FREE_TRIAL_LIMIT = 10;

    public static final int DAYS_PER_WEEK = 7;
    private int userAccountId;
    private String billingAccountName;
    private boolean legacy;
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

    public int getUserAccountId() {
        return userAccountId;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public LocalDate getExpirationDate() {
        return new LocalDate(new Date(expirationTime * 1000));
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

    public String getBillingAccountName() {
        return billingAccountName;
    }

    public int hoursUntilExpiration(Date now) {
        int secondsUntil = secondsUntilExpiration(now);
        int hours = Math.floorDiv(secondsUntil, 3600);
        return hours;
    }

    private int secondsUntilExpiration(Date now) {
        int secondsNow = (int) (now.getTime() / 1000);
        return getExpirationTime() - secondsNow;
    }


    public int daysUntilExpiration(Date now) {
        int hours = hoursUntilExpiration(now);
        return Math.floorDiv(hours, 24);
    }

    /**
     * @return human readable string
     */
    public String expiringIn(Date now) {
        String until;
        int hours = hoursUntilExpiration(now);
        if (hours < 1) {
            return "expired";
        } else if (hours <= 48) {
            return "expiring in " + hours + " hours";
        } else {
            int days = Math.floorDiv(hours, 24);
            if (days < 21) {
                return "expiring in " + days + " days";
            } else {
                int weeks = Math.floorDiv(days, 7);
                return "expiring in " + weeks + " weeks";
            }
        }
    }

    /**
     * True if the user should be warned about expiration
     */
    public boolean shouldWarn(Date now) {
        if(databaseCount == 0) {
            return false;
        }
        int daysLeft = daysUntilExpiration(now);
        if(trial) {
            if(legacy) {
                return true;
            } else {
                return daysLeft <= (2 * DAYS_PER_WEEK);
            }
        } else {
            return daysLeft <= (8 * DAYS_PER_WEEK);
        }
    }

    /**
     * @return the date after which the user should be again warned
     */
    public LocalDate snoozeDate(Date now) {
        int daysLeft = daysUntilExpiration(now);
        LocalDate today = new LocalDate(now);

        if(trial) {
            if(daysLeft < 7) {
                return today.plusDays(1);
            } else {
                return today.plusDays(4);
            }
        } else {
            LocalDate expirationDay = new LocalDate(new Date(expirationTime * 1000));
            int weeksLeft = Math.floorDiv(daysLeft, 7);
            if(weeksLeft <= 6) {
                return expirationDay.plusDays(-4 * DAYS_PER_WEEK);
            } else if(weeksLeft <= 4) {
                return expirationDay.plusDays(-3 * DAYS_PER_WEEK);
            } else if(weeksLeft <= 3) {
                return expirationDay.plusDays(-2 * DAYS_PER_WEEK);
            } else if(weeksLeft <= 2) {
                return expirationDay.plusDays(-1 * DAYS_PER_WEEK);
            } else {
                return today.plusDays(1);
            }
        }
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("trial", trial);
        object.put("expirationTime", expirationTime);
        object.put("userLimit", userLimit);
        object.put("userCount", userCount);
        object.put("databaseCount", databaseCount);
        object.put("legacy", legacy);
        object.put("userAccountId", userAccountId);
        object.put("billingAccountName", billingAccountName);
        return object;
    }

    public static AccountStatus fromJson(JsonValue object) {
        AccountStatus status = new AccountStatus();
        status.userAccountId = (int)object.getNumber("userAccountId");
        status.trial = object.getBoolean("trial");
        status.legacy = object.getBoolean("legacy");
        status.expirationTime = (int) object.getNumber("expirationTime");
        status.userLimit = (int) object.getNumber("userLimit");
        status.userCount = (int) object.getNumber("userCount");
        status.databaseCount = (int)object.getNumber("databaseCount");
        status.billingAccountName = object.getString("billingAccountName");
        return status;
    }

    public boolean isExpired() {
        return secondsUntilExpiration(new Date()) < 0;
    }

    public boolean isSuspended() {
        // For now, we are not suspending paid accountss
        return isTrial() && isExpired();
    }

    public boolean isNewDatabaseAllowed() {
        return !isExpired();
    }

    public static class Builder {
        private AccountStatus status = new AccountStatus();

        public Builder setExpirationTime(Date time) {
            if(time != null) {
                long seconds = (time.getTime() / 1000L);
                if(seconds > Integer.MAX_VALUE) {
                    status.expirationTime = Integer.MAX_VALUE;
                } else {
                    status.expirationTime = (int)seconds;
                }
            }
            return this;
        }

        public Builder setExpirationTime(LocalDate date) {
            return setExpirationTime(date.atMidnightInMyTimezone());
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

        public Builder setTrial(boolean trial) {
            status.trial = trial;
            return this;
        }

        public Builder setLegacy(boolean legacy) {
            status.legacy = legacy;
            return this;
        }

        public Builder setUserAccountId(int userAccountId) {
            status.userAccountId = userAccountId;
            return this;
        }

        public Builder setBillingAccountName(String name) {
            status.billingAccountName = name;
            return this;
        }

        public AccountStatus build() {
            return status;
        }

    }
}
