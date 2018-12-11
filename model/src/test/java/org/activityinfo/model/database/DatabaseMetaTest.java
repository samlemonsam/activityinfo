package org.activityinfo.model.database;

import org.activityinfo.json.Json;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDateInterval;
import org.junit.Test;

import java.util.Collections;
import java.util.Objects;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class DatabaseMetaTest {

    @Test
    public void serialization() {
        serializeAndMatch(validUserOwnedDb());
        serializeAndMatch(validUserOwnedDbNoDesc());
        serializeAndMatch(validUserOwnedDbDeleted());
        serializeAndMatch(validUserOwnedDbPendingTransfer());
        serializeAndMatch(validUserOwnedDbWithResources());
        serializeAndMatch(validUserOwnedDbWithLocks());
        serializeAndMatch(validPublishedDb());
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

    private DatabaseMeta validUserOwnedDb() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(ResourceId.valueOf("TESTDB"))
                .setOwnerId(111)
                .setVersion(1L)
                .setLabel("Test Database")
                .setDescription("More information here")
                .build();
    }

    private DatabaseMeta validUserOwnedDbWithResources() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(ResourceId.valueOf("TESTDB"))
                .setOwnerId(111)
                .setVersion(1L)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addResources(Collections.singletonList(resource()))
                .build();
    }

    private Resource resource() {
        return new Resource.Builder()
                .setId(ResourceId.valueOf("RES"))
                .setParentId(ResourceId.valueOf("TESTDB"))
                .setLabel("Test Resource")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    private DatabaseMeta validUserOwnedDbWithLocks() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(ResourceId.valueOf("TESTDB"))
                .setOwnerId(111)
                .setVersion(1L)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addLocks(Collections.singletonList(lock()))
                .build();
    }

    private RecordLock lock() {
        return new RecordLock.Builder()
                .setId(ResourceId.valueOf("LOCK"))
                .setResourceId(resource().getId())
                .setLabel("Test Lock")
                .setDateRange(LocalDateInterval.year(2000))
                .build();
    }

    private DatabaseMeta validUserOwnedDbNoDesc() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(ResourceId.valueOf("TESTDB"))
                .setOwnerId(111)
                .setVersion(1L)
                .setLabel("Test Database")
                .build();
    }

    private DatabaseMeta validUserOwnedDbDeleted() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(ResourceId.valueOf("TESTDB"))
                .setOwnerId(111)
                .setVersion(2L)
                .setLabel("Test Database")
                .setDeleted(true)
                .build();
    }

    private DatabaseMeta validUserOwnedDbPendingTransfer() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(ResourceId.valueOf("TESTDB"))
                .setOwnerId(111)
                .setVersion(1L)
                .setLabel("Test Database")
                .setDescription("More information here")
                .setPendingTransfer(true)
                .build();
    }

    private DatabaseMeta validPublishedDb() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(ResourceId.valueOf("TESTDB"))
                .setOwnerId(0)
                .setVersion(1L)
                .setLabel("Published Database")
                .setDescription("More information here")
                .setPublished(true)
                .build();
    }

}