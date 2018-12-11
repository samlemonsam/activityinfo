package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.junit.Test;

import java.util.Objects;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class DatabaseMetaTest {

    private DatabaseModelTestResources RESOURCES = new DatabaseModelTestResources();

    @Test
    public void serialization() {
        serializeAndMatch(RESOURCES.validDb());
        serializeAndMatch(RESOURCES.validDbNoDesc());
        serializeAndMatch(RESOURCES.validDbDeleted());
        serializeAndMatch(RESOURCES.validDbPendingTransfer());
        serializeAndMatch(RESOURCES.validDbWithResources());
        serializeAndMatch(RESOURCES.validDbWithResourcesAndLocks());
        serializeAndMatch(RESOURCES.validPublishedDb());
    }

    private void serializeAndMatch(DatabaseMeta db) {
        String serializedDb = serialize(db);
        DatabaseMeta deserializedDb = deserialze(serializedDb);
        match(db, deserializedDb);
    }

    private void match(DatabaseMeta db, DatabaseMeta deserializedDb) {
        assert db.getDatabaseId().equals(deserializedDb.getDatabaseId());
        assert db.getOwnerId() == deserializedDb.getOwnerId();
        assert db.getVersion() == deserializedDb.getVersion();
        assert Objects.equals(db.getLabel(), deserializedDb.getLabel());
        assert Objects.equals(db.getDescription(), deserializedDb.getDescription());
        assert db.isDeleted() == deserializedDb.isDeleted();
        assert db.isPublished() == deserializedDb.isPublished();
        assert db.isPendingTransfer() == deserializedDb.isPendingTransfer();
        assert db.getResources().size() == deserializedDb.getResources().size();
        db.getResources().values().forEach(r -> assertTrue(deserializedDb.getResources().containsKey(r.getId())));
        db.getLocks().asMap().forEach((r, locks) -> {
            assertTrue(deserializedDb.getLocks().containsKey(r));
            assertThat(locks.size(), equalTo(deserializedDb.getLocks().get(r).size()));
        });
    }

    private String serialize(DatabaseMeta db) {
        return db.toJson().toJson();
    }

    private DatabaseMeta deserialze(String json) {
        return DatabaseMeta.fromJson(Json.parse(json));
    }

}