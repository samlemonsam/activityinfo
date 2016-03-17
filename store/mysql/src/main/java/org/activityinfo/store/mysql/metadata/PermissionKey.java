package org.activityinfo.store.mysql.metadata;


public class PermissionKey {
    int userId;
    int databaseId;

    public PermissionKey(int userId, int databaseId) {
        this.userId = userId;
        this.databaseId = databaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionKey that = (PermissionKey) o;

        if (userId != that.userId) return false;
        return databaseId == that.databaseId;

    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + databaseId;
        return result;
    }
}
