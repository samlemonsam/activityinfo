package org.activityinfo.ui.client.store.http;

import org.activityinfo.api.client.ActivityInfoClientAsync;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;

public class DatabaseRequest implements HttpRequest<UserDatabaseMeta> {

    private final ResourceId databaseId;

    public DatabaseRequest(ResourceId databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public Promise<UserDatabaseMeta> execute(ActivityInfoClientAsync client) {
        return client.getDatabase(databaseId);
    }


    @Override
    public int refreshInterval(UserDatabaseMeta result) {
        return -1;
    }
}