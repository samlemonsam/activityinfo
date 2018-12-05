package org.activityinfo.server.database.hibernate;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.database.DatabaseGrant;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.GrantModel;
import org.activityinfo.model.permission.Operation;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.UserPermission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class
})
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class HibernateDatabaseGrantProviderTest {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Inject
    HibernateDatabaseGrantProvider databaseGrantProvider;

    @Inject
    Provider<EntityManager> entityManager;

    private static ResourceId databaseId(int id) {
        return CuidAdapter.databaseId(id);
    }

    @Test
    public void databaseGrant() {
        int userId = 3;
        int dbId = 1;
        UserPermission userPermission = queryUserPermission(userId,dbId);
        Optional<DatabaseGrant> databaseGrant = databaseGrantProvider.getDatabaseGrant(userId, databaseId(dbId));
        assertNotNull(userPermission);
        assertTrue(databaseGrant.isPresent());
        match(userPermission, databaseGrant.get());
    }

    private static void match(UserPermission userPermission, DatabaseGrant databaseGrant) {
        assertThat(databaseGrant.getDatabaseId(), equalTo(databaseId(userPermission.getDatabase().getId())));
        assertThat(databaseGrant.getUserId(), equalTo(userPermission.getUser().getId()));
        assertThat(databaseGrant.getVersion(), equalTo(userPermission.getVersion()));

        List<GrantModel> expectedGrants = HibernateDatabaseGrantProvider.buildGrants(userPermission);
        assertThat(databaseGrant.getGrants().size(), equalTo(expectedGrants.size()));
        for (GrantModel expectedGrant : expectedGrants) {
            assertTrue(databaseGrant.getGrants().containsKey(expectedGrant.getResourceId()));
            matchGrants(databaseGrant.getGrants().get(expectedGrant.getResourceId()), expectedGrant);
        }
    }

    private static void matchGrants(GrantModel testGrant, GrantModel expectedGrant) {
        assertThat(testGrant.getOperations().size(), equalTo(expectedGrant.getOperations().size()));
        for (Operation operation : expectedGrant.getOperations()) {
            assertTrue(testGrant.hasOperation(operation));
            assertThat(testGrant.getFilter(operation), equalTo(expectedGrant.getFilter(operation)));
        }
    }

    @Test
    public void missingDatabaseGrant() {
        int userId = 66666;
        int dbId = 1;
        UserPermission userPermission = queryUserPermission(userId,dbId);
        Optional<DatabaseGrant> databaseGrant = databaseGrantProvider.getDatabaseGrant(userId, databaseId(dbId));

        // Database exists but users permission should not
        assertNull(userPermission);
        assertFalse(databaseGrant.isPresent());
    }

    @Test
    public void removedDatabaseGrant() {
        int userId = 5;
        int dbId = 1;
        UserPermission userPermission = queryUserPermission(userId,dbId);
        Optional<DatabaseGrant> databaseGrant = databaseGrantProvider.getDatabaseGrant(userId, databaseId(dbId));

        // User has been removed, but the UserPermission record still exists.
        // However, the record should have no rights, and should be reflected in our DatabaseGrant
        assertNotNull(userPermission);
        assertTrue(databaseGrant.isPresent());
        match(userPermission, databaseGrant.get());
    }

    @Test
    public void databaseGrantForOwner() {
        UserPermission userPermission = queryUserPermission(1,1);
        Optional<DatabaseGrant> databaseGrant = databaseGrantProvider.getDatabaseGrant(1, databaseId(1));

        // Neither should exist
        assertNull(userPermission);
        assertFalse(databaseGrant.isPresent());
    }

    @Test
    public void allGrantsOnDatabase() {
        int dbId = 1;
        List<UserPermission> allDatabasePermissions = queryUserPermissionsByDb(dbId);
        List<DatabaseGrant> allDatabaseGrants = databaseGrantProvider.getAllDatabaseGrantsForDatabase(databaseId(dbId));
        assertThat(allDatabaseGrants.size(), equalTo(allDatabasePermissions.size()));
        Map<Integer,DatabaseGrant> userGrantMap = allDatabaseGrants.stream().collect(Collectors.toMap(DatabaseGrant::getUserId, grant -> grant));
        for (UserPermission userPermission : allDatabasePermissions) {
            assertTrue(userGrantMap.containsKey(userPermission.getUser().getId()));
            match(userPermission, userGrantMap.get(userPermission.getUser().getId()));
        }
    }

    @Test
    public void allGrantsForUser() {
        int userId = 5;
        List<UserPermission> allUserPermissions = queryUserPermissionsByUser(userId);
        List<DatabaseGrant> allUserGrants = databaseGrantProvider.getAllDatabaseGrantsForUser(userId);
        assertThat(allUserGrants.size(), equalTo(allUserPermissions.size()));
        Map<ResourceId,DatabaseGrant> databaseGrantMap = allUserGrants.stream().collect(Collectors.toMap(DatabaseGrant::getDatabaseId, grant -> grant));
        for (UserPermission userPermission : allUserPermissions) {
            assertTrue(databaseGrantMap.containsKey(databaseId(userPermission.getDatabase().getId())));
            match(userPermission, databaseGrantMap.get(databaseId(userPermission.getDatabase().getId())));
        }
    }

    private UserPermission queryUserPermission(int userId, int databaseId) {
        try {
            return entityManager.get().createQuery("SELECT up " +
                    "FROM UserPermission up " +
                    "WHERE up.user.id=:userId " +
                    "AND up.database.id=:databaseId", UserPermission.class)
                    .setParameter("userId", userId)
                    .setParameter("databaseId", databaseId)
                    .getSingleResult();
        } catch (NoResultException noGrant) {
            return null;
        }
    }

    private List<UserPermission> queryUserPermissionsByDb(int databaseId) {
        return entityManager.get().createQuery("SELECT up " +
                "FROM UserPermission up " +
                "WHERE up.database.id=:databaseId " +
                "AND up.allowView = TRUE", UserPermission.class)
                .setParameter("databaseId", databaseId)
                .getResultList();
    }

    private List<UserPermission> queryUserPermissionsByUser(int userId) {
        return entityManager.get().createQuery("SELECT up " +
                "FROM UserPermission up " +
                "WHERE up.user.id=:userId " +
                "AND up.allowView = TRUE", UserPermission.class)
                .setParameter("userId", userId)
                .getResultList();
    }

}