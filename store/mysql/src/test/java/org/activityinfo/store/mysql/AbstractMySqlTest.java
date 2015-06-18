package org.activityinfo.store.mysql;


import com.google.common.io.Resources;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.junit.BeforeClass;

public abstract class AbstractMySqlTest {

    public static final int ADVOCACY = 4000;


    public static DbUnit dbunit;
    public static ColumnSetBuilder executor;
    public ColumnSet columnSet;
    public static CollectionCatalog catalogProvider;

    @BeforeClass
    public static void initLocale() throws Throwable {
        System.out.println("Initializing Locale...");
        LocaleProxy.initialize();
    }
    
    public static void resetDatabase() throws Throwable {

        System.out.println("Running setup...");
        dbunit = new DbUnit();
        dbunit.openDatabase();
        dbunit.dropAllRows();
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, "sites-simple1.db.xml"));
        catalogProvider = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
        executor = new ColumnSetBuilder(catalogProvider, ColumnCache.NULL);
    }


    protected final void query(ResourceId formClassId, String... fields) {
        QueryModel queryModel = new QueryModel(formClassId);
        queryModel.selectResourceId().as("_id");
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        columnSet = executor.build(queryModel);

        for(String field : fields) {
            System.out.println(field + ": " + column(field));
        }
    }


    protected final ColumnView column(String column) {
        return columnSet.getColumnView(column);
    }
    
}
