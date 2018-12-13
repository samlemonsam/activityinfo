package org.activityinfo.model.database;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class UserDatabaseMetaConstructionTest {

    private DatabaseModelTestResources RESOURCES = new DatabaseModelTestResources();

    @Test
    public void owned() {
        DatabaseMeta meta = RESOURCES.validDbWithResources();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildOwnedUserDatabaseMeta(meta);
        matchUserDatabaseMeta(meta,
                Optional.empty(),                         // No grant required for owned databases
                userDbMeta,
                RESOURCES.OWNER_ID,
                true,                      // Database should be visible
                RESOURCES.resources());                 // All resources should be present
    }

    @Test
    public void deleted() {
        DatabaseMeta meta = RESOURCES.validDbDeleted();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildDeletedUserDatabaseMeta(meta, RESOURCES.USER_ID);
        matchUserDatabaseMeta(meta,
                Optional.empty(),                         // No grants available for deleted databases
                userDbMeta,
                RESOURCES.USER_ID,
                false,                      // Database should NOT be visible
                Collections.emptyList());                // No resources should be visible
    }

    @Test
    public void grantless_noResources() {
        DatabaseMeta meta = RESOURCES.validDb();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildGrantlessUserDatabaseMeta(meta, RESOURCES.UNAUTH_USER_ID);
        matchUserDatabaseMeta(meta,
                Optional.empty(),                         // No grants assigned for current user
                userDbMeta,
                RESOURCES.UNAUTH_USER_ID,
                false,                      // Database should NOT be visible
                Collections.emptyList());                // No resources should be visible
    }

    @Test
    public void grantless_privateResources() {
        DatabaseMeta meta = RESOURCES.validDbWithOnlyPrivateResources();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildGrantlessUserDatabaseMeta(meta, RESOURCES.UNAUTH_USER_ID);
        matchUserDatabaseMeta(meta,
                Optional.empty(),                         // No grants assigned for current user
                userDbMeta,
                RESOURCES.UNAUTH_USER_ID,
                false,                      // Database should NOT be visible
                Collections.emptyList());                // No resources should be visible
    }

    @Test
    public void grantless_privateAndDatabasePrivateResources() {
        DatabaseMeta meta = RESOURCES.validDbWithPrivateAndDatabasePrivateResources();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildGrantlessUserDatabaseMeta(meta, RESOURCES.UNAUTH_USER_ID);
        matchUserDatabaseMeta(meta,
                Optional.empty(),                         // No grants assigned for current user
                userDbMeta,
                RESOURCES.UNAUTH_USER_ID,
                false,                      // Database should NOT be visible
                Collections.emptyList());                // No resources should be visible
    }

    @Test
    public void grantless_allResources() {
        DatabaseMeta meta = RESOURCES.validDbWithResources();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildGrantlessUserDatabaseMeta(meta, RESOURCES.UNAUTH_USER_ID);
        matchUserDatabaseMeta(meta,
                Optional.empty(),                         // No grants assigned for current user
                userDbMeta,
                RESOURCES.UNAUTH_USER_ID,
                true,                       // Database should be visible as some resources are public
                RESOURCES.publicResources());            // Only PUBLIC resources should be visible
    }

    @Test
    public void granted_root_noResources() {
        DatabaseMeta meta = RESOURCES.validDb();
        DatabaseGrant grant = RESOURCES.validGrantWithRootGrantModel();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildUserDatabaseMeta(grant, meta);
        matchUserDatabaseMeta(meta,
                Optional.of(grant),
                userDbMeta,
                RESOURCES.USER_ID,
                true,                      // Database should be visible
                Collections.emptyList());                // No resources should be present
    }

    @Test
    public void granted_root_privateResources() {
        DatabaseMeta meta = RESOURCES.validDbWithOnlyPrivateResources();
        DatabaseGrant grant = RESOURCES.validGrantWithRootGrantModel();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildUserDatabaseMeta(grant, meta);
        matchUserDatabaseMeta(meta,
                Optional.of(grant),
                userDbMeta,
                RESOURCES.USER_ID,
                true,                      // Database should be visible
                RESOURCES.privateResources());          // Only PRIVATE resources should be present
    }

    @Test
    public void granted_folder_privateResources() {
        DatabaseMeta meta = RESOURCES.validDbWithOnlyPrivateResources();
        DatabaseGrant grant = RESOURCES.validGrantWithFolderGrantModel();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildUserDatabaseMeta(grant, meta);
        matchUserDatabaseMeta(meta,
                Optional.of(grant),
                userDbMeta,
                RESOURCES.USER_ID,
                true,                      // Database should be visible
                RESOURCES.assignedFolderResources());           // Only Folder and Folder Form should be visible
    }

    @Test
    public void granted_folder_privateAndDatabasePrivateResources() {
        DatabaseMeta meta = RESOURCES.validDbWithPrivateAndDatabasePrivateResources();
        DatabaseGrant grant = RESOURCES.validGrantWithFolderGrantModel();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildUserDatabaseMeta(grant, meta);

        // Folder and Folder Form should be visible, AS WELL AS Database Private Resource in root
        List<Resource> expectedResources = new ArrayList<>();
        expectedResources.addAll(RESOURCES.databasePrivateResources());
        expectedResources.addAll(RESOURCES.assignedFolderResources());

        matchUserDatabaseMeta(meta,
                Optional.of(grant),
                userDbMeta,
                RESOURCES.USER_ID,
                true,                        // Database should be visible
                expectedResources);
    }

    @Test
    public void granted_folder_allResources() {
        DatabaseMeta meta = RESOURCES.validDbWithResources();
        DatabaseGrant grant = RESOURCES.validGrantWithFolderGrantModel();
        UserDatabaseMeta userDbMeta = UserDatabaseMeta.buildUserDatabaseMeta(grant, meta);

        // Folder and Folder Form should be visible, AS WELL AS Database Private and Public Resources
        List<Resource> expectedResources = new ArrayList<>();
        expectedResources.addAll(RESOURCES.publicResources());
        expectedResources.addAll(RESOURCES.databasePrivateResources());
        expectedResources.addAll(RESOURCES.assignedFolderResources());

        matchUserDatabaseMeta(meta,
                Optional.of(grant),
                userDbMeta,
                RESOURCES.USER_ID,
                true,                         // Database should be visible
                expectedResources);
    }

    private void matchUserDatabaseMeta(DatabaseMeta meta,
                                       Optional<DatabaseGrant> grant,
                                       UserDatabaseMeta userDbMeta,
                                       int userId,
                                       boolean shouldBeVisible,
                                       List<Resource> expectedResources){
        matchDatabaseMeta(meta, userDbMeta, userId, shouldBeVisible);
        if (grant.isPresent()) {
            matchDatabaseGrant(grant.get(), userDbMeta);
        } else {
            assert userDbMeta.getGrants().isEmpty();
        }
        if (shouldBeVisible) {
            matchVisibleResources(userDbMeta, expectedResources);
        }
    }

    private void matchDatabaseMeta(DatabaseMeta meta, UserDatabaseMeta userDbMeta, int userId, boolean shouldBeVisible) {
        assert userDbMeta.isVisible() == shouldBeVisible;
        assert userDbMeta.getDatabaseId().equals(meta.getDatabaseId());
        assert userDbMeta.getUserId() == userId;
        assert userDbMeta.getDatabaseVersion() == meta.getVersion();
        assert userDbMeta.isOwner() == (meta.getOwnerId() == userId);
        assert userDbMeta.isPendingTransfer() == meta.isPendingTransfer();
        assert userDbMeta.isDeleted() == meta.isDeleted();
        assert userDbMeta.isPublished() == meta.isPublished();
        if (shouldBeVisible) {
            assert userDbMeta.getLabel().equals(meta.getLabel());
            assert userDbMeta.getDescription().equals(meta.getDescription());
        }
    }

    private void matchDatabaseGrant(DatabaseGrant grant, UserDatabaseMeta userDbMeta) {
        assert userDbMeta.getUserId() == grant.getUserId();
        assert userDbMeta.getUserVersion() == grant.getVersion();
        assert userDbMeta.getDatabaseId().equals(grant.getDatabaseId());
        grant.getGrants().keySet().forEach(r -> assertTrue(userDbMeta.hasGrant(r)));
    }

    private void matchVisibleResources(UserDatabaseMeta userDbMeta, List<Resource> expected) {
        assert userDbMeta.getResources().size() == expected.size();
        for (Resource expectedResource : expected) {
            assert userDbMeta.hasResource(expectedResource.getId());
        }
    }

}