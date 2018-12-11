package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class DatabaseGrantTest {

    private static final ResourceId DB_ID = ResourceId.valueOf("TESTDB");
    private static final int USER_ID = 1;
    private static final long VERSION = 1L;

    private static final ResourceId RES_ID = ResourceId.valueOf("RES");

    @Test
    public void serialization() {
        serializeAndMatch(validGrantNoModels());
        serializeAndMatch(validGrantWithModels());
    }

    private void serializeAndMatch(DatabaseGrant grant) {
        String serializedGrant = serialize(grant);
        DatabaseGrant deserializedGrant = deserialize(serializedGrant);
        match(grant, deserializedGrant);
    }

    private String serialize(DatabaseGrant grant) {
        return grant.toJson().toJson();
    }

    private DatabaseGrant deserialize(String json) {
        return DatabaseGrant.fromJson(Json.parse(json));
    }

    private void match(DatabaseGrant grant, DatabaseGrant deserializedGrant) {
        assert grant.getDatabaseId().equals(deserializedGrant.getDatabaseId());
        assert grant.getUserId() == deserializedGrant.getUserId();
        assert grant.getVersion() == deserializedGrant.getVersion();
        assert grant.getGrants().size() == deserializedGrant.getGrants().size();
        grant.getGrants().forEach((r,g) -> assertTrue(deserializedGrant.getGrants().containsKey(r)));
    }

    public DatabaseGrant validGrantNoModels() {
        return new DatabaseGrant.Builder()
                .setDatabaseId(DB_ID)
                .setUserId(USER_ID)
                .setVersion(VERSION)
                .build();
    }

    public DatabaseGrant validGrantWithModels() {
        return new DatabaseGrant.Builder()
                .setDatabaseId(DB_ID)
                .setUserId(USER_ID)
                .setVersion(VERSION)
                .addGrants(Collections.singletonList(grant()))
                .build();
    }

    private GrantModel grant() {
        return new GrantModel.Builder()
                .setResourceId(RES_ID)
                .addOperation(Operation.VIEW)
                .build();
    }

}