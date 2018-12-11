package org.activityinfo.model.database;

import com.google.common.collect.Lists;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDateInterval;

import java.util.Collections;

/**
 * Resources for testing DatabaseMeta, DatabaseGrant, and UserDatabaseMeta objects
 */
public class DatabaseModelTestResources {

    public static final ResourceId DB_ID = ResourceId.valueOf("TESTDB");
    public static final int USER_ID = 1;
    public static final long VERSION = 1L;

    public static final ResourceId ROOT_RES_PRIVATE_ID = ResourceId.valueOf("RES_PRIVATE");
    public static final ResourceId ROOT_RES_PUBLIC_ID = ResourceId.valueOf("RES_PUBLIC");
    public static final ResourceId ROOT_RES_DB_PRIVATE_ID = ResourceId.valueOf("RES_DB_PRIVATE");
    public static final ResourceId FOLDER_ID = ResourceId.valueOf("FOLDER");
    public static final ResourceId FOLDER_RES_ID = ResourceId.valueOf("FOLDER_RES");

    public static final ResourceId LOCK_ID = ResourceId.valueOf("LOCK");

    public Resource rootFormResource_private() {
        return new Resource.Builder()
                .setId(ROOT_RES_PRIVATE_ID)
                .setParentId(DB_ID)
                .setLabel("Test Resource (Private)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    public Resource rootFormResource_public() {
        return new Resource.Builder()
                .setId(ROOT_RES_PUBLIC_ID)
                .setParentId(DB_ID)
                .setLabel("Test Resource (Public)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    public Resource rootFormResource_dbPrivate() {
        return new Resource.Builder()
                .setId(ROOT_RES_DB_PRIVATE_ID)
                .setParentId(DB_ID)
                .setLabel("Test Resource (Database Private)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    public Resource FolderResource() {
        return new Resource.Builder()
                .setId(FOLDER_ID)
                .setParentId(DB_ID)
                .setLabel("Folder Resource")
                .setType(ResourceType.FOLDER)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    public Resource folderFormResource() {
        return new Resource.Builder()
                .setId(FOLDER_RES_ID)
                .setParentId(FOLDER_ID)
                .setLabel("Test Resource (Folder)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    public RecordLock lock() {
        return new RecordLock.Builder()
                .setId(LOCK_ID)
                .setResourceId(ROOT_RES_PRIVATE_ID)
                .setLabel("Test Lock")
                .setDateRange(LocalDateInterval.year(2000))
                .build();
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

    private static GrantModel grant() {
        return new GrantModel.Builder()
                .setResourceId(FOLDER_ID)
                .addOperation(Operation.VIEW)
                .build();
    }

    public DatabaseMeta validUserOwnedDb() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(USER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .build();
    }

    public DatabaseMeta validUserOwnedDbWithResources() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(USER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addResources(Lists.newArrayList(
                        rootFormResource_dbPrivate(),
                        rootFormResource_public(),
                        rootFormResource_private(),
                        folderFormResource(),
                        folderFormResource()))
                .build();
    }

    public DatabaseMeta validUserOwnedDbWithLocks() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(USER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addLocks(Collections.singletonList(lock()))
                .build();
    }

    public DatabaseMeta validUserOwnedDbNoDesc() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(USER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .build();
    }

    public DatabaseMeta validUserOwnedDbDeleted() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(USER_ID)
                .setVersion(VERSION + 1)
                .setLabel("Test Database")
                .setDeleted(true)
                .build();
    }

    public DatabaseMeta validUserOwnedDbPendingTransfer() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(USER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .setPendingTransfer(true)
                .build();
    }

    public DatabaseMeta validPublishedDb() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(0)
                .setVersion(VERSION)
                .setLabel("Published Database")
                .setDescription("More information here")
                .setPublished(true)
                .build();
    }

}
