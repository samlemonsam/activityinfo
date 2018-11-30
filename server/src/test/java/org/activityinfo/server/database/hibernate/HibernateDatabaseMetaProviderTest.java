package org.activityinfo.server.database.hibernate;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.database.DatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.server.database.hibernate.entity.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class
})
@OnDataSet("/dbunit/sites-simple1.db.xml")
public class HibernateDatabaseMetaProviderTest {

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
    HibernateDatabaseMetaProvider databaseMetaProvider;

    @Inject
    Provider<EntityManager> entityManager;

    private ResourceId databaseId(int id) {
        return CuidAdapter.databaseId(id);
    }

    @Test
    public void getDatabase() {
        Database database = entityManager.get().find(Database.class, 1);
        DatabaseMeta databaseMeta = databaseMetaProvider.getDatabaseMeta(databaseId(1));
        assertNotNull(databaseMeta);
        match(databaseMeta, database);
    }

    private void match(DatabaseMeta databaseMeta, Database database) {
        assertThat(databaseMeta.getDatabaseId(), equalTo(databaseId(database.getId())));
        assertThat(databaseMeta.getOwnerId(), equalTo(database.getOwner().getId()));
        assertThat(databaseMeta.getLabel(), equalTo(database.getName()));
        assertThat(databaseMeta.getDescription(), equalTo(database.getFullName()));
        assertThat(databaseMeta.getVersion(), equalTo(database.getVersion()));
    }

    @Test
    public void getMultipleDatabases() {
        Set<ResourceId> databaseIds = Sets.newHashSet(CuidAdapter.databaseId(1),
                CuidAdapter.databaseId(2),
                CuidAdapter.databaseId(4));

        Map<ResourceId,DatabaseMeta> databaseMeta = databaseMetaProvider.getDatabaseMeta(databaseIds);
        for (DatabaseMeta meta : databaseMeta.values()) {
            assertNotNull(meta);
            Database database = entityManager.get().find(Database.class, CuidAdapter.getLegacyIdFromCuid(meta.getDatabaseId()));
            match(meta, database);
        }
    }

    @Test
    public void getOwnedDatabases() {
        // User 1 owns 3 databases
        Map<ResourceId,DatabaseMeta> ownedDatabaseMeta = databaseMetaProvider.getOwnedDatabaseMeta(1);
        assertThat(ownedDatabaseMeta.size(), equalTo(3));
    }

    @Test
    public void getDatabaseByActivity() {
        // Activity 2 is in Database 1
        ResourceId formId = CuidAdapter.activityFormClass(2);
        Database database = entityManager.get().find(Database.class, 1);
        DatabaseMeta databaseMeta = databaseMetaProvider.getDatabaseMetaForResource(formId);
        assertNotNull(databaseMeta);
        match(databaseMeta, database);
    }

}