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
import org.activityinfo.store.query.impl.ColumnSetBuilder;
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
    public static ColumnSetBuilder executor;
    public ColumnSet columnSet;
    public static MySqlCatalog catalog;
    
    private Closeable objectify;

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
    
    public static void resetDatabase(String resourceName) throws Throwable {

        System.out.println("Running setup...");
        dbunit = new DbUnit();
        dbunit.openDatabase();
        dbunit.dropAllRows();
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, resourceName));
        catalog = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
        executor = new ColumnSetBuilder(catalog);
        CountryStructure.clearCache();
    }


    protected final void query(ResourceId formClassId, String... fields) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectResourceId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        execute(queryModel);
    }

    protected final void queryWhere(ResourceId formClassId, List<String> fields, String filter) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectResourceId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        queryModel.setFilter(filter);

        execute(queryModel);
    }


    private void execute(QueryModel queryModel) {
        columnSet = executor.build(queryModel);

        for(String field : columnSet.getColumns().keySet()) {
            System.out.println(field + ": " + column(field));
        }
    }


    protected final ColumnView column(String column) {
        return columnSet.getColumnView(column);
    }
    
}
