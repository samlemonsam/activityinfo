package org.activityinfo.model.permission;

import com.google.common.collect.Lists;
import org.activityinfo.model.database.Resource;
import org.activityinfo.model.database.ResourceType;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.resource.ResourceId;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PermissionOracleTest {

    // Owner UserDatabaseMeta (permitted to everything)
    // UserDatabaseMeta with correct permissions for:
    // - Record permissions for VIEW, CREATE_RECORD, EDIT_RECORD, DELETE_RECORD, EXPORT_RECORD, IMPORT_RECORD on:
    //  -- Form in Root (implicit)
    //  -- Form in Root (explicit)
    //  -- Form in Folder (implicit)
    //  -- Form in Folder (explicit)
    // - Resource permissions for VIEW, CREATE_RESOURCE, EDIT_RESOURCE, DELETE_RESOURCE:
    //  -- On Root
    //  -- Form in Root (implicit)
    //  -- Form in Root (explicit)
    //  -- Folder in Root (implicit)
    //  -- Folder in Root (explicit)
    //  -- Form in Folder (implicit)
    //  -- Form in Folder (explicit)
    //  -- Folder in Folder (implicit)
    //  -- Folder in Folder (explicit)
    // - Manage targets and locks:
    //  -- On Root
    //  -- Form in Root (implicit)
    //  -- Form in Root (explicit)
    //  -- Folder in Root (implicit)
    //  -- Folder in Root (explicit)
    //  -- Form in Folder (implicit)
    //  -- Form in Folder (explicit)
    //  -- Folder in Folder (implicit)
    //  -- Folder in Folder (explicit)
    // - Publicly visible resources:
    //  -- Explicitly granted
    //  -- Granted to us as we are Database User
    //  -- Granted to us as it is Public
    // - Special Resources:
    //  -- Project Form
    //  -- Partner Form
    //  -- Admin Level Form
    // Published Databases
    // Filter checks:
    // - Correct Operation Filters
    // - Multi-level Operation Filters
    // Unauthorised Permission Checks (all of the above with unauthorised users)

    // Databases:
    // (1) Database 1
    private static final ResourceId database = ResourceId.valueOf("d0000000001");

    // ResourceIds
    // Private:
    // (a0000000001)  (Root) Root Form
    // (f0000000001)  (Root) Root Folder
    // (a0000000002)  (Root Folder) Form-in-Folder
    // (f0000000002)  (Root Folder) Folder-in-Folder
    // Public:
    // (a0000000003)  (Root) Public Form
    // (f0000000003)  (Root) Public Folder
    // (a0000000004)  (Public Folder) Public Form-in-Folder
    // (f0000000004)  (Public Folder) Public Folder-in-Folder
    // Database-Private:
    // (a0000000005)  (Root) Root Form
    // (f0000000005)  (Root) Root Folder
    // (a0000000006)  (Root Folder) Form-in-Folder
    // (f0000000006)  (Root Folder) Folder-in-Folder

    private static final ResourceId rootFormId = ResourceId.valueOf("a0000000001");
    private static final ResourceId rootFolderId = ResourceId.valueOf("f0000000001");
    private static final ResourceId formInFolderId = ResourceId.valueOf("a0000000002");
    private static final ResourceId folderInFolderId = ResourceId.valueOf("f0000000002");

    private static final ResourceId publicFormId = ResourceId.valueOf("a0000000003");
    private static final ResourceId publicFolderId = ResourceId.valueOf("f0000000003");
    private static final ResourceId publicFormInFolderId = ResourceId.valueOf("a0000000004");
    private static final ResourceId publicFolderInFolderId = ResourceId.valueOf("f0000000004");

    private static final ResourceId dbPrivateRootFormId = ResourceId.valueOf("a0000000005");
    private static final ResourceId dbPrivateRootFolderId = ResourceId.valueOf("f0000000005");
    private static final ResourceId dbPrivateFormInFolderId = ResourceId.valueOf("a0000000006");
    private static final ResourceId dbPrivateFolderInFolderId = ResourceId.valueOf("f0000000006");

    private static final Resource rootForm = resource(rootFormId, "Root Form (Private)", database, ResourceType.FORM, Resource.Visibility.PRIVATE);
    private static final Resource rootFolder = resource(rootFolderId, "Root Folder (Private)", database, ResourceType.FOLDER, Resource.Visibility.PRIVATE);
    private static final Resource formInFolder = resource(formInFolderId, "Form-In-Folder (Private)", rootFolderId, ResourceType.FORM, Resource.Visibility.PRIVATE);
    private static final Resource folderInFolder = resource(folderInFolderId, "Folder-in-Folder (Private)", rootFolderId, ResourceType.FOLDER, Resource.Visibility.PRIVATE);

    private static final Resource publicForm = resource(publicFormId, "Root Form (Public)", database, ResourceType.FORM, Resource.Visibility.PUBLIC);
    private static final Resource publicFolder = resource(publicFolderId, "Root Folder (Public)", database, ResourceType.FOLDER, Resource.Visibility.PUBLIC);
    private static final Resource publicFormInFolder = resource(publicFormInFolderId, "Form-in-Folder (Public)", publicFolderId, ResourceType.FORM, Resource.Visibility.PUBLIC);
    private static final Resource publicFolderInFolder = resource(publicFolderInFolderId, "Folder-in-Folder (Public)", publicFolderId, ResourceType.FOLDER, Resource.Visibility.PUBLIC);

    private static final Resource dbPrivateRootForm = resource(dbPrivateRootFormId, "Root Form (Database-Private)", database, ResourceType.FORM, Resource.Visibility.DATABASE_USERS);
    private static final Resource dbPrivateRootFolder = resource(dbPrivateRootFolderId, "Root Folder (Database-Private)", database, ResourceType.FOLDER, Resource.Visibility.DATABASE_USERS);
    private static final Resource dbPrivateFormInFolder = resource(dbPrivateFormInFolderId, "Form-In-Folder (Database-Private)", dbPrivateRootFolderId, ResourceType.FORM, Resource.Visibility.DATABASE_USERS);
    private static final Resource dbPrivateFolderInFolder = resource(dbPrivateFolderInFolderId, "Folder-in-Folder (Database-Private)", dbPrivateRootFolderId, ResourceType.FOLDER, Resource.Visibility.DATABASE_USERS);

    private static final Predicate<Resource> formResource() {
        return resource -> resource.getType().equals(ResourceType.FORM);
    }

    private static final Predicate<Resource> folderResource() {
        return resource -> resource.getType().equals(ResourceType.FOLDER);
    }

    private static final Predicate<Resource> privateResource() {
        return resource -> resource.getVisibility().equals(Resource.Visibility.PRIVATE);
    }

    private static final Predicate<Resource> databasePrivateResource() {
        return resource -> resource.getVisibility().equals(Resource.Visibility.DATABASE_USERS);
    }

    private static final Predicate<Resource> publicResource() {
        return resource -> resource.getVisibility().equals(Resource.Visibility.PUBLIC);
    }

    private static final List<Resource> allResources = Lists.newArrayList(rootForm,rootFolder,formInFolder,folderInFolder,
            dbPrivateRootForm,dbPrivateRootFolder,dbPrivateFormInFolder,dbPrivateFolderInFolder,
            publicForm,publicFolder,publicFormInFolder,publicFolderInFolder);

    private static final List<Resource> formResources = allResources.stream().filter(formResource()).collect(Collectors.toList());
    private static final List<Resource> folderResources = allResources.stream().filter(folderResource()).collect(Collectors.toList());

    private static final List<Resource> privateResources = allResources.stream().filter(privateResource()).collect(Collectors.toList());
    private static final List<Resource> dbPrivateResources = allResources.stream().filter(databasePrivateResource()).collect(Collectors.toList());
    private static final List<Resource> publicResources = allResources.stream().filter(publicResource()).collect(Collectors.toList());

    // Users:
    // (1) Owner of Database 1
    // (2) Authorised User on Database 1
    // (3) Unauthorised User on Database 1
    private static final int owner = 1;
    private static final int authUser = 2;
    private static final int unauthUser = 3;

    private static UserDatabaseMeta ownerDatabase(List<Resource> resources) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(database)
                .setUserId(owner)
                .setVersion("1")
                .setLabel("Database")
                .setOwner(true)
                .setPendingTransfer(false)
                .setPublished(false)
                .setSuspended(false)
                .addResources(resources)
                .build();
    }

    private static UserDatabaseMeta authUserDatabase(List<Resource> resources, List<GrantModel> grants) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(database)
                .setUserId(authUser)
                .setVersion("1")
                .setLabel("Database")
                .setOwner(false)
                .setPublished(false)
                .setSuspended(false)
                .addResources(resources)
                .addGrants(grants)
                .build();
    }

    private static UserDatabaseMeta unAuthUserDatabase(List<Resource> resources) {
        return new UserDatabaseMeta.Builder()
                .setDatabaseId(database)
                .setUserId(unauthUser)
                .setVersion("1")
                .setLabel("Database")
                .setOwner(false)
                .setPublished(false)
                .setSuspended(false)
                .addResources(resources)
                .build();
    }

    private static Resource resource(ResourceId id, String label, ResourceId parentId, ResourceType type, Resource.Visibility visibility) {
        return new Resource.Builder()
                .setId(id)
                .setLabel(label)
                .setParentId(parentId)
                .setType(type)
                .setVisibility(visibility)
                .build();
    }

    private static List<GrantModel> rootDatabaseGrant() {
        return Collections.singletonList(new GrantModel.Builder()
                .setResourceId(database)
                .addOperation(Operation.VIEW)
                .addOperation(Operation.CREATE_RECORD)
                .addOperation(Operation.DELETE_RECORD)
                .addOperation(Operation.EDIT_RECORD)
                .addOperation(Operation.CREATE_RESOURCE)
                .addOperation(Operation.DELETE_RESOURCE)
                .addOperation(Operation.EDIT_RESOURCE)
                .addOperation(Operation.LOCK_RECORDS)
                .addOperation(Operation.IMPORT_RECORDS)
                .addOperation(Operation.EXPORT_RECORDS)
                .addOperation(Operation.MANAGE_TARGETS)
                .addOperation(Operation.MANAGE_USERS)
                .build());
    }

    private static List<GrantModel> folderGrant() {
        return Collections.singletonList(new GrantModel.Builder()
                .setResourceId(rootFolderId)
                .addOperation(Operation.VIEW)
                .addOperation(Operation.CREATE_RECORD)
                .addOperation(Operation.DELETE_RECORD)
                .addOperation(Operation.EDIT_RECORD)
                .addOperation(Operation.CREATE_RESOURCE)
                .addOperation(Operation.DELETE_RESOURCE)
                .addOperation(Operation.EDIT_RESOURCE)
                .addOperation(Operation.LOCK_RECORDS)
                .addOperation(Operation.IMPORT_RECORDS)
                .addOperation(Operation.EXPORT_RECORDS)
                .addOperation(Operation.MANAGE_TARGETS)
                .addOperation(Operation.MANAGE_USERS)
                .build());
    }

    @Test
    public void owner_transferDatabase() {
        UserDatabaseMeta db = ownerDatabase(allResources);
        assertTrue(PermissionOracle.canTransferDatabase(db));
    }

    @Test
    public void owner_deleteDatabase() {
        UserDatabaseMeta db = ownerDatabase(allResources);
        assertTrue(PermissionOracle.canDeleteDatabase(db));
    }

    @Test
    public void owner_editDatabase() {
        UserDatabaseMeta db = ownerDatabase(allResources);
        assertTrue(PermissionOracle.canEditDatabase(db));
    }

    @Test
    public void owner_view() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // User should have VIEW permissions on root database and all resources
        assertTrue(PermissionOracle.canView(db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canView(resource.getId(), db));
        }
    }

    @Test
    public void owner_createRecord() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have CREATE_RECORD permissions on all FORM resources
        for (Resource resource : formResources) {
            assertTrue(PermissionOracle.canCreateRecord(resource.getId(), db));
        }
    }

    @Test
    public void owner_deleteRecord() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have DELETE_RECORD permissions on all FORM resources
        for (Resource resource : formResources) {
            assertTrue(PermissionOracle.canDeleteRecord(resource.getId(), db));
        }
    }

    @Test
    public void owner_editRecord() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have EDIT_RECORD permissions on all FORM resources
        for (Resource resource : formResources) {
            assertTrue(PermissionOracle.canEditRecord(resource.getId(), db));
        }
    }

    @Test
    public void owner_createResource() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have CREATE_RESOURCE permissions on root database and all FOLDER resources
        assertTrue(PermissionOracle.canCreateResource(database, db));
        for (Resource resource : folderResources) {
            assertTrue(PermissionOracle.canCreateResource(resource.getId(), db));
        }
    }

    @Test
    public void owner_deleteResource() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have DELETE_RESOURCE permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canDeleteResource(resource.getId(), db));
        }
    }

    @Test
    public void owner_editResource() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have EDIT_RESOURCE permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canEditResource(resource.getId(), db));
        }
    }

    @Test
    public void owner_lockRecords() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have LOCK_RECORDS permissions on root database and all resources
        assertTrue(PermissionOracle.canLockRecords(database, db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canLockRecords(resource.getId(), db));
        }
    }

    @Test
    public void owner_importRecords() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have IMPORT_RECORDS permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canImportRecords(resource.getId(), db));
        }
    }

    @Test
    public void owner_exportRecords() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have EXPORT_RECORDS permissions on root database and all resources
        assertTrue(PermissionOracle.canExportRecords(database, db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canExportRecords(resource.getId(), db));
        }
    }

    @Test
    public void owner_manageTargets() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have MANAGE_TARGETS permissions on root database and all resources
        assertTrue(PermissionOracle.canManageTargets(database, db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canManageTargets(resource.getId(), db));
        }
    }

    @Test
    public void owner_manageUsers() {
        UserDatabaseMeta db = ownerDatabase(allResources);

        // Owner should have MANAGE_USERS permissions on root database and all resources
        assertTrue(PermissionOracle.canManageUsers(db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canManageUsers(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_transferDatabase() {
        // User should NOT have database transfer permissions
        UserDatabaseMeta db = unAuthUserDatabase(allResources);
        assertFalse(PermissionOracle.canTransferDatabase(db));
    }

    @Test
    public void unauthUser_deleteDatabase() {
        // User should NOT have database deletion permissions
        UserDatabaseMeta db = unAuthUserDatabase(allResources);
        assertFalse(PermissionOracle.canDeleteDatabase(db));
    }

    @Test
    public void unauthUser_editDatabase() {
        // User should NOT have database modification permissions
        UserDatabaseMeta db = unAuthUserDatabase(allResources);
        assertFalse(PermissionOracle.canEditDatabase(db));
    }

    @Test
    public void unauthUser_view() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have VIEW permissions on root database
        assertFalse(PermissionOracle.canView(db));

        // User should NOT have VIEW permissions on any PRIVATE resources
        for (Resource resource : privateResources) {
            assertFalse(PermissionOracle.canView(resource.getId(), db));
        }

        // User should NOT have VIEW permissions on any DATABASE_USERS resources
        for (Resource resource : dbPrivateResources) {
            assertFalse(PermissionOracle.canView(resource.getId(), db));
        }

        // User SHOULD have VIEW permissions on any PUBLIC resources
        for (Resource resource : publicResources) {
            assertTrue(PermissionOracle.canView(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_createRecord() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have CREATE_RECORD permissions on any resources
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canCreateRecord(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_deleteRecord() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have DELETE_RECORD permissions on any resources
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canDeleteRecord(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_editRecord() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have EDIT_RECORD permissions on any resources
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canEditRecord(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_createResource() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have CREATE_RESOURCE permissions on root database or any resources
        assertFalse(PermissionOracle.canCreateResource(database, db));
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canCreateResource(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_deleteResource() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have DELETE_RESOURCE permissions on any resources
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canDeleteResource(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_editResource() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have EDIT_RESOURCE permissions on any resources
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canEditResource(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_lockRecords() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have LOCK_RECORDS permissions on root database or any resources
        assertFalse(PermissionOracle.canLockRecords(database, db));
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canLockRecords(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_importRecords() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have IMPORT_RECORDS permissions on any resources
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canImportRecords(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_exportRecords() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have EXPORT_RECORDS permissions on root database or any resources
        assertFalse(PermissionOracle.canExportRecords(database, db));
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canExportRecords(resource.getId(), db));
        }
    }

    @Test
    public void unauthUser_manageTargets() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have MANAGE_TARGETS permissions on root database or any resources
        assertFalse(PermissionOracle.canManageTargets(database, db));
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canManageTargets(resource.getId(), db));
        }
    }

    @Test
    public void unauth_manageUsers() {
        UserDatabaseMeta db = unAuthUserDatabase(allResources);

        // User should NOT have MANAGE_USERS permissions on root database or any resources
        assertFalse(PermissionOracle.canManageUsers(db));
        for (Resource resource : allResources) {
            assertFalse(PermissionOracle.canManageUsers(resource.getId(), db));
        }
    }

    @Test
    public void authUser_transferDatabase_rootGrant() {
        // User should NOT have database transfer permissions
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());
        assertFalse(PermissionOracle.canTransferDatabase(db));
    }

    @Test
    public void authUser_deleteDatabase_rootGrant() {
        // User should NOT have database deletion permissions
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());
        assertFalse(PermissionOracle.canDeleteDatabase(db));
    }

    @Test
    public void authUser_editDatabase_rootGrant() {
        // User should NOT have database modification permissions
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());
        assertFalse(PermissionOracle.canEditDatabase(db));
    }

    @Test
    public void authUser_view_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have VIEW permissions on root database and all resources
        assertTrue(PermissionOracle.canView(db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canView(resource.getId(), db));
        }
    }

    @Test
    public void authUser_createRecord_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have CREATE_RECORD permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canCreateRecord(resource.getId(), db));
        }
    }

    @Test
    public void authUser_deleteRecord_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have DELETE_RECORD permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canDeleteRecord(resource.getId(), db));
        }
    }

    @Test
    public void authUser_editRecord_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have EDIT_RECORD permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canEditRecord(resource.getId(), db));
        }
    }

    @Test
    public void authUser_createResource_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have CREATE_RESOURCE permissions on root database all resources
        assertTrue(PermissionOracle.canCreateResource(database, db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canCreateResource(resource.getId(), db));
        }
    }

    @Test
    public void authUser_deleteResource_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have DELETE_RESOURCE permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canDeleteResource(resource.getId(), db));
        }
    }

    @Test
    public void authUser_editResource_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have EDIT_RESOURCE permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canEditResource(resource.getId(), db));
        }
    }

    @Test
    public void authUser_lockRecords_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have LOCK_RECORDS permissions on root database and all resources
        assertTrue(PermissionOracle.canLockRecords(database, db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canLockRecords(resource.getId(), db));
        }
    }

    @Test
    public void authUser_importRecords_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have IMPORT_RECORDS permissions on all resources
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canImportRecords(resource.getId(), db));
        }
    }

    @Test
    public void authUser_exportRecords_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have EXPORT_RECORDS permissions on root database and all resources
        assertTrue(PermissionOracle.canExportRecords(database, db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canExportRecords(resource.getId(), db));
        }
    }

    @Test
    public void authUser_manageTargets_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have MANAGE_TARGETS permissions on root database and all resources
        assertTrue(PermissionOracle.canManageTargets(database, db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canManageTargets(resource.getId(), db));
        }
    }

    @Test
    public void auth_manageUsers_rootGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, rootDatabaseGrant());

        // User should have MANAGE_USERS permissions on root database and all resources
        assertTrue(PermissionOracle.canManageUsers(db));
        for (Resource resource : allResources) {
            assertTrue(PermissionOracle.canManageUsers(resource.getId(), db));
        }
    }

    @Test
    public void authUser_transferDatabase_folderGrant() {
        // User should NOT have database transfer permissions
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());
        assertFalse(PermissionOracle.canTransferDatabase(db));
    }

    @Test
    public void authUser_deleteDatabase_folderGrant() {
        // User should NOT have database deletion permissions
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());
        assertFalse(PermissionOracle.canDeleteDatabase(db));
    }

    @Test
    public void authUser_editDatabase_folderGrant() {
        // User should NOT have database modification permissions
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());
        assertFalse(PermissionOracle.canEditDatabase(db));
    }

    @Test
    public void authUser_view_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have VIEW permissions on folder and all contained resources, but NOT on root database or root form
        assertFalse(PermissionOracle.canView(db));
        assertFalse(PermissionOracle.canView(rootFormId, db));
        assertTrue(PermissionOracle.canView(rootFolderId, db));
        assertTrue(PermissionOracle.canView(formInFolderId, db));
        assertTrue(PermissionOracle.canView(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should have VIEW permissions on all database private and public resources
        for (Resource publicResource : publicResources) {
            assertTrue(PermissionOracle.canView(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertTrue(PermissionOracle.canView(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_createRecord_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have CREATE_RECORD permissions on folder and all contained resources, but NOT on root form
        assertFalse(PermissionOracle.canCreateRecord(rootFormId, db));
        assertTrue(PermissionOracle.canCreateRecord(rootFolderId, db));
        assertTrue(PermissionOracle.canCreateRecord(formInFolderId, db));
        assertTrue(PermissionOracle.canCreateRecord(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have CREATE_RECORD permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canCreateRecord(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canCreateRecord(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_deleteRecord_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have DELETE_RECORD permissions on folder and all contained resources, but NOT on root form
        assertFalse(PermissionOracle.canDeleteRecord(rootFormId, db));
        assertTrue(PermissionOracle.canDeleteRecord(rootFolderId, db));
        assertTrue(PermissionOracle.canDeleteRecord(formInFolderId, db));
        assertTrue(PermissionOracle.canDeleteRecord(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have DELETE_RECORD permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canDeleteRecord(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canDeleteRecord(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_editRecord_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have EDIT_RECORD permissions on folder and all contained resources, but NOT on root form
        assertFalse(PermissionOracle.canEditRecord(rootFormId, db));
        assertTrue(PermissionOracle.canEditRecord(rootFolderId, db));
        assertTrue(PermissionOracle.canEditRecord(formInFolderId, db));
        assertTrue(PermissionOracle.canEditRecord(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have EDIT_RECORD permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canEditRecord(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canEditRecord(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_createResource_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have CREATE_RESOURCE permissions on folder and all contained resources, but NOT on root database or root form
        assertFalse(PermissionOracle.canCreateResource(database, db));
        assertFalse(PermissionOracle.canCreateResource(rootFormId, db));
        assertTrue(PermissionOracle.canCreateResource(rootFolderId, db));
        assertTrue(PermissionOracle.canCreateResource(formInFolderId, db));
        assertTrue(PermissionOracle.canCreateResource(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have CREATE_RESOURCE permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canCreateResource(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canCreateResource(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_deleteResource_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have DELETE_RESOURCE permissions on folder and all contained resources, but NOT on root form
        assertFalse(PermissionOracle.canDeleteResource(rootFormId, db));
        assertTrue(PermissionOracle.canDeleteResource(rootFolderId, db));
        assertTrue(PermissionOracle.canDeleteResource(formInFolderId, db));
        assertTrue(PermissionOracle.canDeleteResource(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have DELETE_RESOURCE permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canDeleteResource(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canDeleteResource(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_editResource_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have EDIT_RESOURCE permissions on folder and all contained resources, but NOT on root form
        assertFalse(PermissionOracle.canEditResource(rootFormId, db));
        assertTrue(PermissionOracle.canEditResource(rootFolderId, db));
        assertTrue(PermissionOracle.canEditResource(formInFolderId, db));
        assertTrue(PermissionOracle.canEditResource(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have EDIT_RESOURCE permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canEditResource(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canEditResource(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_lockRecords_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have LOCK_RECORDS permissions on folder and all contained resources, but NOT on root database or root form
        assertFalse(PermissionOracle.canLockRecords(database, db));
        assertFalse(PermissionOracle.canLockRecords(rootFormId, db));
        assertTrue(PermissionOracle.canLockRecords(rootFolderId, db));
        assertTrue(PermissionOracle.canLockRecords(formInFolderId, db));
        assertTrue(PermissionOracle.canLockRecords(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have LOCK_RECORDS permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canLockRecords(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canLockRecords(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_importRecords_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have IMPORT_RECORDS permissions on folder and all contained resources, but NOT on root form
        assertFalse(PermissionOracle.canImportRecords(rootFormId, db));
        assertTrue(PermissionOracle.canImportRecords(rootFolderId, db));
        assertTrue(PermissionOracle.canImportRecords(formInFolderId, db));
        assertTrue(PermissionOracle.canImportRecords(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have IMPORT_RECORDS permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canImportRecords(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canImportRecords(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_exportRecords_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have EXPORT_RECORDS permissions on folder and all contained resources, but NOT on root form
        assertFalse(PermissionOracle.canExportRecords(rootFormId, db));
        assertTrue(PermissionOracle.canExportRecords(rootFolderId, db));
        assertTrue(PermissionOracle.canExportRecords(formInFolderId, db));
        assertTrue(PermissionOracle.canExportRecords(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have EXPORT_RECORDS permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canExportRecords(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canExportRecords(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void authUser_manageTargets_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have MANAGE_TARGETS permissions on folder and all contained resources, but NOT on root database or root form
        assertFalse(PermissionOracle.canManageTargets(database, db));
        assertFalse(PermissionOracle.canManageTargets(rootFormId, db));
        assertTrue(PermissionOracle.canManageTargets(rootFolderId, db));
        assertTrue(PermissionOracle.canManageTargets(formInFolderId, db));
        assertTrue(PermissionOracle.canManageTargets(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have MANAGE_TARGETS permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canManageTargets(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canManageTargets(dbPrivateResource.getId(), db));
        }
    }

    @Test
    public void auth_manageUsers_folderGrant() {
        UserDatabaseMeta db = authUserDatabase(allResources, folderGrant());

        // First check on private resources
        // User should have MANAGE_USERS permissions on folder and all contained resources, but NOT on root database or root form
        assertFalse(PermissionOracle.canManageUsers(db));
        assertFalse(PermissionOracle.canManageUsers(rootFormId, db));
        assertTrue(PermissionOracle.canManageUsers(rootFolderId, db));
        assertTrue(PermissionOracle.canManageUsers(formInFolderId, db));
        assertTrue(PermissionOracle.canManageUsers(folderInFolderId, db));

        // Next check on database-private and public resources
        // User should NOT have MANAGE_USERS permissions on any database private and public resources
        for (Resource publicResource : publicResources) {
            assertFalse(PermissionOracle.canManageUsers(publicResource.getId(), db));
        }
        for (Resource dbPrivateResource : dbPrivateResources) {
            assertFalse(PermissionOracle.canManageUsers(dbPrivateResource.getId(), db));
        }
    }

}