package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseGrantTest {

    private DatabaseModelTestResources RESOURCES = new DatabaseModelTestResources();

    @Test
    public void serialization() {
        serializeAndMatch(RESOURCES.validGrantNoModels());
        serializeAndMatch(RESOURCES.validGrantWithModels());
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

}