package org.activityinfo.model.database;

import com.google.common.collect.Lists;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.time.LocalDateInterval;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resources for testing DatabaseMeta, DatabaseGrant, and UserDatabaseMeta objects
 */
public class DatabaseModelTestResources {

    public final ResourceId DB_ID = ResourceId.valueOf("TESTDB");
    public final int OWNER_ID = 1;
    public final int USER_ID = 2;
    public final int UNAUTH_USER_ID = 3;
    public final long VERSION = 1L;

    public final ResourceId ROOT_FORM_PRIVATE_ID = ResourceId.valueOf("FORM_PRIVATE");
    public final ResourceId ROOT_FORM_PUBLIC_ID = ResourceId.valueOf("FORM_PUBLIC");
    public final ResourceId ROOT_FORM_DB_PRIVATE_ID = ResourceId.valueOf("FORM_DB_PRIVATE");
    public final ResourceId FOLDER_ID = ResourceId.valueOf("FOLDER");
    public final ResourceId FOLDER_FORM_ID = ResourceId.valueOf("FOLDER_FORM");

    public final ResourceId LOCK_ID = ResourceId.valueOf("LOCK");

    public Resource rootFormResource_private() {
        return new Resource.Builder()
                .setId(ROOT_FORM_PRIVATE_ID)
                .setParentId(DB_ID)
                .setLabel("Test Resource (Private)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    public Resource rootFormResource_public() {
        return new Resource.Builder()
                .setId(ROOT_FORM_PUBLIC_ID)
                .setParentId(DB_ID)
                .setLabel("Test Resource (Public)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PUBLIC)
                .build();
    }

    public Resource rootFormResource_dbPrivate() {
        return new Resource.Builder()
                .setId(ROOT_FORM_DB_PRIVATE_ID)
                .setParentId(DB_ID)
                .setLabel("Test Resource (Database Private)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.DATABASE_USERS)
                .build();
    }

    public Resource folderResource() {
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
                .setId(FOLDER_FORM_ID)
                .setParentId(FOLDER_ID)
                .setLabel("Test Resource (Folder)")
                .setType(ResourceType.FORM)
                .setVisibility(Resource.Visibility.PRIVATE)
                .build();
    }

    public List<Resource> resources() {
        return Lists.newArrayList(
                rootFormResource_dbPrivate(),
                rootFormResource_public(),
                rootFormResource_private(),
                folderResource(),
                folderFormResource());
    }

    public List<Resource> publicResources() {
        return resources().stream()
                .filter(res -> res.getVisibility() == Resource.Visibility.PUBLIC)
                .collect(Collectors.toList());
    }

    public List<Resource> privateResources() {
        return resources().stream()
                .filter(res -> res.getVisibility() == Resource.Visibility.PRIVATE)
                .collect(Collectors.toList());
    }

    public List<Resource> privateAndDatabasePrivateResources() {
        return resources().stream()
                .filter(res -> res.getVisibility() != Resource.Visibility.PUBLIC)
                .collect(Collectors.toList());
    }

    public List<Resource> databasePrivateResources() {
        return resources().stream()
                .filter(res -> res.getVisibility() == Resource.Visibility.DATABASE_USERS)
                .collect(Collectors.toList());
    }

    public List<Resource> folderResources() {
        return Lists.newArrayList(
                folderResource(),
                folderFormResource());
    }

    public RecordLock lock() {
        return new RecordLock.Builder()
                .setId(LOCK_ID)
                .setResourceId(ROOT_FORM_PRIVATE_ID)
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

    public DatabaseGrant validGrantWithRootGrantModel() {
        return new DatabaseGrant.Builder()
                .setDatabaseId(DB_ID)
                .setUserId(USER_ID)
                .setVersion(VERSION)
                .addGrants(Collections.singletonList(rootGrant()))
                .build();
    }

    public DatabaseGrant validGrantWithFolderGrantModel() {
        return new DatabaseGrant.Builder()
                .setDatabaseId(DB_ID)
                .setUserId(USER_ID)
                .setVersion(VERSION)
                .addGrants(Collections.singletonList(folderGrant()))
                .build();
    }

    public DatabaseGrant validGrantWithFormGrantModel() {
        return new DatabaseGrant.Builder()
                .setDatabaseId(DB_ID)
                .setUserId(USER_ID)
                .setVersion(VERSION)
                .addGrants(Collections.singletonList(formGrant()))
                .build();
    }

    private GrantModel formGrant() {
        return new GrantModel.Builder()
                .setResourceId(ROOT_FORM_PRIVATE_ID)
                .addOperation(Operation.VIEW)
                .build();
    }

    private GrantModel folderGrant() {
        return new GrantModel.Builder()
                .setResourceId(FOLDER_ID)
                .addOperation(Operation.VIEW)
                .build();
    }

    private GrantModel rootGrant() {
        return new GrantModel.Builder()
                .setResourceId(DB_ID)
                .addOperation(Operation.VIEW)
                .build();
    }

    public DatabaseMeta validDb() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(OWNER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .build();
    }

    public DatabaseMeta validDbWithResources() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(OWNER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addResources(resources())
                .build();
    }

    public DatabaseMeta validDbWithOnlyPrivateResources() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(OWNER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addResources(privateResources())
                .build();
    }

    public DatabaseMeta validDbWithPrivateAndDatabasePrivateResources() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(OWNER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addResources(privateAndDatabasePrivateResources())
                .build();
    }

    public DatabaseMeta validDbWithResourcesAndLocks() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(OWNER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .setDescription("More information here")
                .addResources(resources())
                .addLocks(Collections.singletonList(lock()))
                .build();
    }

    public DatabaseMeta validDbNoDesc() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(OWNER_ID)
                .setVersion(VERSION)
                .setLabel("Test Database")
                .build();
    }

    public DatabaseMeta validDbDeleted() {
        return new DatabaseMeta.Builder()
                .setDatabaseId(DB_ID)
                .setOwnerId(USER_ID)
                .setVersion(VERSION + 1)
                .setLabel("Test Database")
                .setDescription("More information here")
                .setDeleted(true)
                .build();
    }

    public DatabaseMeta validDbPendingTransfer() {
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
