package org.activityinfo.server.endpoint.rest;

import com.google.inject.Provider;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import javax.persistence.EntityManager;


public class GeoDatabaseProvider {

    public static final ResourceId GEODB_ID = ResourceId.valueOf("geodb");

    private final Provider<EntityManager> entityManager;

    public GeoDatabaseProvider(Provider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    public boolean accept(ResourceId databaseId) {
        return databaseId.equals(GEODB_ID);
    }

    public UserDatabaseMeta queryGeoDb(int userId) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(GEODB_ID)
                .setUserId(userId)
                .setLabel("Geographic Database")
                .setOwner(false)
                .setVersion("1")
                .setPublished(true)
                .build();
    }

}
