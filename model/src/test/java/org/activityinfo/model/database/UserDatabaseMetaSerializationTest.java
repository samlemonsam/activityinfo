package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class UserDatabaseMetaSerializationTest {

    private final DatabaseModelTestResources RESOURCES = new DatabaseModelTestResources();

    @Test
    public void serialization() {
        serializeAndMatch(owner());
        serializeAndMatch(deleted());
        serializeAndMatch(granted());
        serializeAndMatch(grantless());
    }

    private UserDatabaseMeta owner() {
        DatabaseMeta meta = RESOURCES.validDbWithResources();
        return UserDatabaseMeta.buildOwnedUserDatabaseMeta(meta);
    }

    private UserDatabaseMeta deleted() {
        DatabaseMeta meta = RESOURCES.validDbDeleted();
        return UserDatabaseMeta.buildDeletedUserDatabaseMeta(meta, RESOURCES.USER_ID);
    }

    private UserDatabaseMeta granted() {
        DatabaseMeta meta = RESOURCES.validDbWithResources();
        DatabaseGrant grant = RESOURCES.validGrantWithFolderGrantModel();
        return UserDatabaseMeta.buildUserDatabaseMeta(grant, meta);
    }

    private UserDatabaseMeta grantless() {
        DatabaseMeta meta = RESOURCES.validDbWithResources();
        return UserDatabaseMeta.buildGrantlessUserDatabaseMeta(meta, RESOURCES.UNAUTH_USER_ID);
    }

    private void serializeAndMatch(UserDatabaseMeta dbMeta) {
        String serializedDbMeta = serialize(dbMeta);
        UserDatabaseMeta deserializedDbMeta = deserialize(serializedDbMeta);
        match(dbMeta, deserializedDbMeta);
    }

    private void match(UserDatabaseMeta dbMeta, UserDatabaseMeta deserializedDbMeta) {
        assert dbMeta.isVisible() == deserializedDbMeta.isVisible();
        assert dbMeta.getDatabaseId().equals(deserializedDbMeta.getDatabaseId());
        assert dbMeta.getUserId() == deserializedDbMeta.getUserId();
        assert dbMeta.getVersion().equals(deserializedDbMeta.getVersion());
        if (dbMeta.isVisible()) {
            assert dbMeta.getLabel().equals(deserializedDbMeta.getLabel());
            assert dbMeta.getDescription().equals(deserializedDbMeta.getDescription());
            matchResources(dbMeta, deserializedDbMeta);
            matchGrants(dbMeta, deserializedDbMeta);
            matchLocks(dbMeta, deserializedDbMeta);
        }
    }

    private void matchResources(UserDatabaseMeta dbMeta, UserDatabaseMeta deserializedDbMeta) {
        assert dbMeta.getResources().size() == deserializedDbMeta.getResources().size();
        dbMeta.getResources().forEach(r -> assertTrue(deserializedDbMeta.hasResource(r.getId())));
    }

    private void matchGrants(UserDatabaseMeta dbMeta, UserDatabaseMeta deserializedDbMeta) {
        assert dbMeta.getGrants().size() == deserializedDbMeta.getGrants().size();
        dbMeta.getGrants().forEach(g -> assertTrue(deserializedDbMeta.hasGrant(g.getResourceId())));
    }

    private void matchLocks(UserDatabaseMeta dbMeta, UserDatabaseMeta deserializedDbMeta) {
        assert dbMeta.getLocks().size() == deserializedDbMeta.getLocks().size();
    }

    private String serialize(UserDatabaseMeta dbMeta) {
        return dbMeta.toJson().toJson();
    }

    private UserDatabaseMeta deserialize(String serializedDbMeta) {
        return UserDatabaseMeta.fromJson(Json.parse(serializedDbMeta));
    }

}
