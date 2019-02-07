package org.activityinfo.model.database;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.Objects;

public class DatabaseGrantKey {

    public static final String SEP = ":";

    private final int userId;
    private final ResourceId databaseId;

    public DatabaseGrantKey(int userId, ResourceId databaseId) {
        this.userId = userId;
        this.databaseId = databaseId;
    }

    public static DatabaseGrantKey of(int userId, ResourceId databaseId) {
        return new DatabaseGrantKey(userId,databaseId);
    }

    public int getUserId() {
        return userId;
    }

    public ResourceId getDatabaseId() {
        return databaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseGrantKey key = (DatabaseGrantKey) o;
        return userId == key.userId &&
                Objects.equals(databaseId, key.databaseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, databaseId);
    }

    @Override
    public String toString() {
        return userId + SEP + CuidAdapter.getLegacyIdFromCuid(databaseId);
    }

}