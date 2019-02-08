package org.activityinfo.server.database.hibernate;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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
        Optional<DatabaseMeta> databaseMeta = databaseMetaProvider.getDatabaseMeta(databaseId(1));
        assertTrue(databaseMeta.isPresent());
        match(databaseMeta.get(), database);
    }

    @Test
    public void getMissingDatabase() {
        Database database = entityManager.get().find(Database.class, 66666);
        Optional<DatabaseMeta> databaseMeta = databaseMetaProvider.getDatabaseMeta(databaseId(66666));
        assertNull(database);
        assertFalse(databaseMeta.isPresent());
    }

    private void match(DatabaseMeta databaseMeta, Database database) {
        assertThat(databaseMeta.getDatabaseId(), equalTo(databaseId(database.getId())));
        assertThat(databaseMeta.getOwnerId(), equalTo(database.getOwner().getId()));
        assertThat(databaseMeta.getLabel(), equalTo(database.getName()));
        assertThat(databaseMeta.getDescription(), equalTo(database.getFullName()));
        assertThat(databaseMeta.getVersion(), equalTo(database.getMetaVersion()));
    }

    @Test
    public void getMultipleDatabases() {
        Set<ResourceId> databaseIds = Sets.newHashSet(CuidAdapter.databaseId(1),
                CuidAdapter.databaseId(2),
                CuidAdapter.databaseId(4));

        Map<ResourceId,DatabaseMeta> databaseMeta = databaseMetaProvider.getDatabaseMeta(databaseIds);
        assertFalse(databaseMeta.isEmpty());
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
        Optional<DatabaseMeta> databaseMeta = databaseMetaProvider.getDatabaseMetaForResource(formId);
        assertTrue(databaseMeta.isPresent());
        match(databaseMeta.get(), database);
    }

    @Test
    public void caching() {
        // Clear memcache
        MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
        memcacheService.clearAll();
        assert memcacheService.getStatistics().getItemCount() == 0;
        assert memcacheService.getStatistics().getHitCount() == 0;

        // Fetch a database - should be cached in memcache once retrieved
        Optional<DatabaseMeta> dbMeta = databaseMetaProvider.getDatabaseMeta(databaseId(1));
        assert dbMeta.isPresent();
        assert memcacheService.getStatistics().getItemCount() > 0;
        assert memcacheService.getStatistics().getHitCount() == 0;

        // Fetch the same database again - should be retrieved from session cache but present in memcache
        Optional<DatabaseMeta> cachedDbMeta = databaseMetaProvider.getDatabaseMeta(databaseId(1));
        assert cachedDbMeta.isPresent();
        assert memcacheService.getStatistics().getItemCount() > 0;
        assert memcacheService.getStatistics().getHitCount() == 0;
        assert memcacheService.contains(HibernateDatabaseMetaCache.memcacheKey(cachedDbMeta.get().getDatabaseId(), cachedDbMeta.get().getVersion()));
    }

}