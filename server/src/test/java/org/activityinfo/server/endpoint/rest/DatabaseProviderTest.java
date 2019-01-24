package org.activityinfo.server.endpoint.rest;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.inject.Inject;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.DatabaseModule;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.store.spi.DatabaseProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules({
        TestHibernateModule.class,
        DatabaseModule.class
})
@OnDataSet("/dbunit/schema4.db.xml")
public class DatabaseProviderTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    private static final int IRAQ_DB = 1;
    private static final int SYRIA_DB = 2;
    private static final int LEBANON_DB = 3;
    private static final int DELETED_DB = 4;
    private static final int NON_EXISTENT_DB = 999;

    private static final int PROVINCE = 1;
    private static final ResourceId PROVINCE_CODE = CuidAdapter.adminLevelFormClass(PROVINCE);

    private static final int ALEX = 1;
    private static final int BAVON = 2;
    private static final int JOHN = 3;
    private static final int JACOB = 4;

    @Inject
    private DatabaseProvider databaseProvider;

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void visibleDatabasesTest() {
        // Alex owns two databases
        List<UserDatabaseMeta> visibleDatabases = databaseProvider.getVisibleDatabases(ALEX);
        assertThat(visibleDatabases.size(), equalTo(2));

        // Bavon owns 1 database
        visibleDatabases = databaseProvider.getVisibleDatabases(BAVON);
        assertThat(visibleDatabases.size(), equalTo(1));

        // John is assigned to 2 databases
        visibleDatabases = databaseProvider.getVisibleDatabases(JOHN);
        assertThat(visibleDatabases.size(), equalTo(2));

        // Jacob has no assigned or owned databases
        visibleDatabases = databaseProvider.getVisibleDatabases(JACOB);
        assertThat(visibleDatabases.size(), equalTo(0));
    }

    @Test
    public void geodb() {
        Optional<UserDatabaseMeta> geodb = databaseProvider.getDatabaseMetadata(GeoDatabaseProvider.GEODB_ID, ALEX);
        assertTrue(geodb.isPresent());
        assertThat(geodb.get().getDatabaseId(), equalTo(GeoDatabaseProvider.GEODB_ID));
        assertTrue(geodb.get().isPublished());
    }

    @Test
    public void getDatabaseByResource() {
        // Admin level form
        Optional<UserDatabaseMeta> geodb = databaseProvider.getDatabaseMetadataByResource(PROVINCE_CODE, ALEX);
        assertTrue(geodb.isPresent());
        assertThat(geodb.get().getDatabaseId(), equalTo(GeoDatabaseProvider.GEODB_ID));

        // Form
        Optional<UserDatabaseMeta> db = databaseProvider.getDatabaseMetadataByResource(CuidAdapter.activityFormClass(1), ALEX);
        assertTrue(db.isPresent());
        assertThat(db.get().getDatabaseId(), equalTo(CuidAdapter.databaseId(1)));

        // Folder
        db = databaseProvider.getDatabaseMetadataByResource(CuidAdapter.folderId(1), ALEX);
        assertTrue(db.isPresent());
        assertThat(db.get().getDatabaseId(), equalTo(CuidAdapter.databaseId(1)));
    }

    @Test
    public void missingDatabase() {
        Optional<UserDatabaseMeta> db = databaseProvider.getDatabaseMetadata(NON_EXISTENT_DB, ALEX);
        // Missing database should not be present
        assertFalse(db.isPresent());
    }

    @Test
    public void deletedDatabase() {
        Optional<UserDatabaseMeta> db = databaseProvider.getDatabaseMetadata(DELETED_DB, ALEX);
        // Deleted database should be present but marked as deleted with no data
        assertTrue(db.isPresent());
        assertTrue(db.get().isDeleted());
        assertTrue(db.get().getGrants().isEmpty());
        assertTrue(db.get().getResources().isEmpty());
        assertTrue(db.get().getLocks().isEmpty());
        assertThat(db.get().getLabel(), equalTo(""));
        assertThat(db.get().getDescription(), equalTo(""));
    }

    @Test
    public void database_alex() {
        Optional<UserDatabaseMeta> iraqDb = databaseProvider.getDatabaseMetadata(IRAQ_DB, ALEX);
        Optional<UserDatabaseMeta> syriaDb = databaseProvider.getDatabaseMetadata(SYRIA_DB, ALEX);

        // IRAQ: Should have a returned UserDatabaseMeta which is entirely visible. Owner has no grants.
        // Database has 7 resources in total
        // Partner Form is also visible, bringing total visible resources to 8
        assertTrue(iraqDb.isPresent());
        assertTrue(iraqDb.get().isVisible());
        assertTrue(iraqDb.get().isOwner());
        assertThat(iraqDb.get().getResources().size(), equalTo(8));
        assertTrue(iraqDb.get().getGrants().isEmpty());

        // SYRIA: Should have a returned UserDatabaseMeta which is entirely visible. Owner has no grants.
        // Database has 7 resources in total
        // Partner Form is also visible, bringing total visible resources to 8
        assertTrue(syriaDb.isPresent());
        assertTrue(syriaDb.get().isVisible());
        assertTrue(syriaDb.get().isOwner());
        assertThat(syriaDb.get().getResources().size(), equalTo(8));
        assertTrue(syriaDb.get().getGrants().isEmpty());
    }

    @Test
    public void database_bavon() {
        Optional<UserDatabaseMeta> iraqDb = databaseProvider.getDatabaseMetadata(IRAQ_DB, BAVON);
        Optional<UserDatabaseMeta> syriaDb = databaseProvider.getDatabaseMetadata(SYRIA_DB, BAVON);
        Optional<UserDatabaseMeta> lebanonDb = databaseProvider.getDatabaseMetadata(LEBANON_DB, BAVON);

        // IRAQ: Should have a returned UserDatabaseMeta which is invisible as user has no grants and no public resources
        assertTrue(iraqDb.isPresent());
        assertFalse(iraqDb.get().isVisible());
        assertTrue(iraqDb.get().getResources().isEmpty());
        assertTrue(iraqDb.get().getGrants().isEmpty());

        // SYRIA: Should have a returned UserDatabaseMeta which is invisible as user has no grants and no public resources
        assertTrue(syriaDb.isPresent());
        assertFalse(syriaDb.get().isVisible());
        assertTrue(syriaDb.get().getResources().isEmpty());
        assertTrue(syriaDb.get().getGrants().isEmpty());

        // LEBANON: Should have a returned UserDatabaseMeta which is entirely visible. Owner has no grants.
        // Database has 7 resources in total
        // Partner Form is also visible, bringing total visible resources to 8
        assertTrue(lebanonDb.isPresent());
        assertTrue(lebanonDb.get().isVisible());
        assertTrue(lebanonDb.get().isOwner());
        assertThat(lebanonDb.get().getResources().size(), equalTo(8));
        assertTrue(lebanonDb.get().getGrants().isEmpty());
    }

    @Test
    public void database_john() {
        Optional<UserDatabaseMeta> iraqDb = databaseProvider.getDatabaseMetadata(IRAQ_DB, JOHN);
        Optional<UserDatabaseMeta> syriaDb = databaseProvider.getDatabaseMetadata(SYRIA_DB, JOHN);
        Optional<UserDatabaseMeta> lebanonDb = databaseProvider.getDatabaseMetadata(LEBANON_DB, JOHN);

        // IRAQ: Should have a returned UserDatabaseMeta which is invisible as user has no grants and no public resources
        assertTrue(iraqDb.isPresent());
        assertFalse(iraqDb.get().isVisible());
        assertTrue(iraqDb.get().getResources().isEmpty());
        assertTrue(iraqDb.get().getGrants().isEmpty());

        // SYRIA: Should have a returned UserDatabaseMeta which is visible. User has grant on Folder 4, with associated resources visible (3).
        // Partner Form is also visible, bringing total visible resources to 4
        assertTrue(syriaDb.isPresent());
        assertTrue(syriaDb.get().isVisible());
        assertThat(syriaDb.get().getGrants().size(), equalTo(2));
        assertTrue(syriaDb.get().hasGrant(CuidAdapter.folderId(4)));
        assertThat(syriaDb.get().getResources().size(), equalTo(4));

        // LEBANON: Should have a returned UserDatabaseMeta which is visible. User has grant on Folder 7, with associated resources visible (3).
        // Public INTAKE FORM and SUBFORM resources are also visible, as well as Partner Form Resource, bringing total visible resources to 6.
        assertTrue(lebanonDb.isPresent());
        assertTrue(lebanonDb.get().isVisible());
        assertThat(lebanonDb.get().getGrants().size(), equalTo(2));
        assertTrue(lebanonDb.get().hasGrant(CuidAdapter.folderId(7)));
        assertThat(lebanonDb.get().getResources().size(), equalTo(6));
    }

    @Test
    public void database_jacob() {
        Optional<UserDatabaseMeta> iraqDb = databaseProvider.getDatabaseMetadata(IRAQ_DB, JACOB);
        Optional<UserDatabaseMeta> syriaDb = databaseProvider.getDatabaseMetadata(SYRIA_DB, JACOB);
        Optional<UserDatabaseMeta> lebanonDb = databaseProvider.getDatabaseMetadata(LEBANON_DB, JACOB);

        // IRAQ: Should have a returned UserDatabaseMeta which is invisible as user has no grants and no public resources
        assertTrue(iraqDb.isPresent());
        assertFalse(iraqDb.get().isVisible());
        assertTrue(iraqDb.get().getResources().isEmpty());
        assertTrue(iraqDb.get().getGrants().isEmpty());

        // SYRIA: Should have a returned UserDatabaseMeta which is invisible as user has no grants and no public resources
        assertTrue(syriaDb.isPresent());
        assertFalse(syriaDb.get().isVisible());
        assertTrue(syriaDb.get().getResources().isEmpty());
        assertTrue(syriaDb.get().getGrants().isEmpty());

        // LEBANON: Should have a returned UserDatabaseMeta which is visible as there are public resources
        // Should be 3 resources visible: Intake Form and its Sub Form, and the In-Built Partner Form
        assertTrue(lebanonDb.isPresent());
        assertTrue(lebanonDb.get().isVisible());
        assertThat(lebanonDb.get().getResources().size(), equalTo(3));
        assertTrue(lebanonDb.get().getGrants().isEmpty());
    }

}