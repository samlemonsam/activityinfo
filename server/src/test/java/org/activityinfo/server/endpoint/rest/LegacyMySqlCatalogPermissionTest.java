package org.activityinfo.server.endpoint.rest;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.fixtures.InjectionSupport;
import org.activityinfo.fixtures.Modules;
import org.activityinfo.fixtures.TestHibernateModule;
import org.activityinfo.model.database.UserDatabaseMeta;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.permission.FormPermissions;
import org.activityinfo.model.permission.PermissionOracle;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.database.OnDataSet;
import org.activityinfo.store.mysql.MySqlStorageProvider;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.FormSupervisorAdapter;
import org.activityinfo.store.query.shared.FormScanCache;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.activityinfo.store.spi.DatabaseProvider;
import org.activityinfo.store.spi.FormStorage;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;

import static org.activityinfo.store.testing.ColumnSetMatchers.hasValues;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

@RunWith(InjectionSupport.class)
@Modules(TestHibernateModule.class)
@OnDataSet("/dbunit/catalog-test.db.xml")
public class LegacyMySqlCatalogPermissionTest {

    private int userId = 1;
    private int activityId = 1;

    @Inject
    Provider<EntityManager> em;

    @Inject
    MySqlStorageProvider catalog;

    private ColumnSet columnSet;
    private DatabaseProvider databaseProvider;
    private FormScanCache cache;

    @Before
    public void setUp() {
        this.databaseProvider = new DatabaseProviderImpl(em);
        this.cache = new NullFormScanCache();
    }

    @Test
    public void ownerPermissions() {
        setUserId(1);
        setActivityId(1);

        FormPermissions permissions = fetchFormPermissions();

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getViewFilter(), nullValue());
    }

    @Test
    public void noPermissions() {
        setUserId(21);
        setActivityId(1);

        FormPermissions permissions = fetchFormPermissions();

        assertThat(permissions.isVisible(), equalTo(false));
        assertThat(permissions.isEditAllowed(), equalTo(false));
        assertThat(permissions.getViewFilter(), nullValue());

    }

    @Test
    public void revokedPermissions() {
        // christian
        setUserId(5);
        setActivityId(1);

        FormPermissions permissions = fetchFormPermissions();

        assertThat(permissions.isVisible(), equalTo(false));
        assertThat(permissions.isEditAllowed(), equalTo(false));
        assertThat(permissions.getViewFilter(), nullValue());
    }

    @Test
    public void queryFormWithNoPermissions() {
        // Christian's view permissions have been revoked
        setUserId(5);
        setActivityId(1);

        query(CuidAdapter.activityFormClass(1), "_id", "partner.label");

        assertThat(column("_id"), hasValues(new String[0]));

    }

    @Test
    public void editPartnerPermissions() {
        setUserId(4);
        setActivityId(1);

        FormPermissions permissions = fetchFormPermissions();

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getViewFilter(), CoreMatchers.equalTo("a00000000010000000007 == \"p0000000002\""));
        assertThat(permissions.getUpdateFilter(), CoreMatchers.equalTo("a00000000010000000007 == \"p0000000002\""));
    }

    @Test
    public void recordsAreFiltered() {

        // User 4: Marlene (Solidarites)
        // Can only view records with the partner Solidarites (2) in database 1

        setUserId(4);
        setActivityId(1);

        // Database 1:
        // Activity 1: NFI (Once)

        // Site 1: Partner: (1)
        // Site 2: Partner: (1)
        // Site 3: Partner: Solidarites (2)

        query(getActivityId(), "_id", "partner.label");

        assertThat(column("_id"), hasValues("s0000000003"));
    }

    @Test
    public void recordCountsAreFiltered() {

        // User 4: Marlene (Solidarites)
        // Can only view records with the partner Solidarites (2) in database 1

        setUserId(4);
        setActivityId(1);

        // Database 1:
        // Activity 1: NFI (Once)

        // Site 1: Partner: (1)
        // Site 2: Partner: (1)
        // Site 3: Partner: Solidarites (2)


        query(getActivityId(), "_id", "FOOOOOO");

        assertThat(column("_id"), hasValues("s0000000003"));
    }


    @Test
    public void editAllPermissions() {
        setUserId(3);
        setActivityId(1);

        FormPermissions permissions = fetchFormPermissions();

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getViewFilter(), nullValue());
        assertThat(permissions.getUpdateFilter(),  nullValue());
    }

    @Test
    public void viewAllPermissions() {
        setUserId(2);
        setActivityId(1);

        FormPermissions permissions = fetchFormPermissions();

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.isEditAllowed(), equalTo(true));
        assertThat(permissions.getViewFilter(), nullValue());
    }

    @Test
    public void publicPermission() {
        // random user
        setUserId(999);
        // public form class
        setActivityId(41);

        FormPermissions permissions = fetchFormPermissions();

        assertThat(permissions.isVisible(), equalTo(true));
        assertThat(permissions.getViewFilter(), nullValue());
        assertThat(permissions.isEditAllowed(), equalTo(false));
    }


    private FormPermissions fetchFormPermissions() {
        FormStorage formStorage = catalog.getForm(getActivityId()).get();
        UserDatabaseMeta dbMeta = databaseProvider.getDatabaseMetadata(formStorage.getFormClass().getDatabaseId(), userId);
        return PermissionOracle.formPermissions(getActivityId(), dbMeta);
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public ResourceId getActivityId() {
        return CuidAdapter.activityFormClass(activityId);
    }

    protected final void query(ResourceId formClassId, String... fields) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectRecordId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        query(queryModel);
    }


    protected final void query(QueryModel queryModel) {

        ColumnSetBuilder builder = new ColumnSetBuilder(
                catalog,
                cache,
                new FormSupervisorAdapter(catalog, databaseProvider, userId));

        columnSet = builder.build(queryModel);

        for(String field : columnSet.getColumns().keySet()) {
            System.out.println(field + ": " + column(field));
        }
    }

    protected final ColumnView column(String column) {
        return columnSet.getColumnView(column);
    }

}