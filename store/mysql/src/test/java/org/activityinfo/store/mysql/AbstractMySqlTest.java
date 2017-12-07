package org.activityinfo.store.mysql;


import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.io.Resources;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.metadata.CountryStructure;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.server.FormSupervisorAdapter;
import org.activityinfo.store.query.server.ServerColumnFactory;
import org.activityinfo.store.query.shared.FormScanCache;
import org.activityinfo.store.query.shared.NullFormScanCache;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.List;

public abstract class AbstractMySqlTest {

    public static final int ADVOCACY = 4000;


    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalMemcacheServiceTestConfig(),
                    new LocalDatastoreServiceTestConfig()
                        .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));


    public static DbUnit dbunit;
    public ColumnSet columnSet;
    public static MySqlCatalog catalog;
    
    private Closeable objectify;

    private int userId = 1;

    protected FormScanCache cache = new NullFormScanCache();

    @BeforeClass
    public static void initLocale() throws Throwable {
        System.out.println("Initializing Locale...");
        LocaleProxy.initialize();
    }

    @Before
    public void setUp() {
        helper.setUp();
        objectify = ObjectifyService.begin();
    }

    @After
    public void tearDown() {
        objectify.close();
        helper.tearDown();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public static void resetDatabase(String resourceName) throws Throwable {

        System.out.println("Running setup...");
        dbunit = new DbUnit();
        dbunit.openDatabase();
        dbunit.dropAllRows();
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, resourceName));
        catalog = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
        CountryStructure.clearCache();
    }

    protected final void newRequest() {
        catalog = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
    }


    protected final void query(ResourceId formClassId, String... fields) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectResourceId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        query(queryModel);
    }

    protected final void queryWhere(ResourceId formClassId, List<String> fields, String filter) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectResourceId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        queryModel.setFilter(filter);

        query(queryModel);
    }


    protected final void query(QueryModel queryModel) {

        ColumnSetBuilder builder = new ColumnSetBuilder(
                catalog,
                cache,
                new FormSupervisorAdapter(catalog, userId));

        columnSet = builder.build(queryModel);

        for(String field : columnSet.getColumns().keySet()) {
            System.out.println(field + ": " + column(field));
        }
    }


    protected final ColumnView column(String column) {
        return columnSet.getColumnView(column);
    }
    
}
