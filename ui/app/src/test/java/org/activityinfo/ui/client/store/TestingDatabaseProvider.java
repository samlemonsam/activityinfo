package org.activityinfo.ui.client.store;

import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.resource.ResourceId;

import java.util.*;

public class TestingDatabaseProvider {

    private List<UserDatabaseMeta> databases = new ArrayList<>();
    private Map<ResourceId, UserDatabaseMeta> resourceMap = new HashMap<>();


    public TestingDatabaseProvider() {
    }

    public void add(UserDatabaseMeta database) {
        databases.add(database);
        for (Resource resource : database.getResources()) {
            resourceMap.put(resource.getId(), database);
        }
    }

    public Optional<UserDatabaseMeta> lookupDatabase(ResourceId resourceId) {
        return Optional.ofNullable(resourceMap.get(resourceId));
    }

}
