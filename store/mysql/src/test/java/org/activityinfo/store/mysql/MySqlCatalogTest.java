package org.activityinfo.store.mysql;

import com.google.common.io.Resources;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.collections.CountryCollection;
import org.activityinfo.store.mysql.collections.DatabaseCollection;
import org.activityinfo.store.mysql.collections.LocationCollectionProvider;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.activityinfo.store.mysql.ColumnSetMatchers.hasValues;
import static org.junit.Assert.assertThat;


public class MySqlCatalogTest {

    private static DbUnit dbunit;
    private static ColumnSetBuilder executor;
    private ColumnSet columnSet;

    @BeforeClass
    public static void setup() throws Throwable {
        System.out.println("Initializing Locale...");
        LocaleProxy.initialize();

        System.out.println("Running setup...");
        dbunit = new DbUnit();
        dbunit.openDatabase();
        dbunit.dropAllRows();
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, "sites-simple1.db.xml"));
        CollectionCatalog catalogProvider = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
        executor = new ColumnSetBuilder(catalogProvider, ColumnCache.NULL);
    }

    @Test
    public void testCountry() {
        query(CountryCollection.FORM_CLASS_ID, "label", "code");
        assertThat(column("label"), hasValues("Rdc"));
        assertThat(column("code"), hasValues("CD"));
    }

    @Test
    public void testDatabase() {
        query(DatabaseCollection.FORM_CLASS_ID, "label", "description", "country", "country.label", "country.code");
        assertThat(column("label"), hasValues("PEAR", "PEAR Plus", "Alpha", "Public"));
        assertThat(column("description"), hasValues(null, null, null, null));
        assertThat(column("country"), hasValues("c0000000001", "c0000000001", "c0000000001", "c0000000001"));
        assertThat(column("country.label"), hasValues("Rdc", "Rdc", "Rdc", "Rdc"));
        assertThat(column("country.code"), hasValues("CD", "CD", "CD", "CD"));
    }

    @Test
    public void testLocation() {
        query(CuidAdapter.locationFormClass(1), "label", "axe");
        assertThat(column("label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga", "Boga"));

        assertThat(column("axe"), hasValues(null, "Bunia-Wakombe", null, null));
    }

    @Test
    public void testSiteSimple() {
        query(CuidAdapter.activityFormClass(1), "date1", "date2", "partner", 
                "partner.label", "location.label", "BENE");
        
    }


    private void query(ResourceId formClassId, String... fields) {
        QueryModel queryModel = new QueryModel(formClassId);
        for(String field : fields) {
            queryModel.selectExpr(field).setId(field);
        }
        columnSet = executor.build(queryModel);

        for(String field : fields) {
            System.out.println(field + ": " + column(field));
        }
    }

    private ColumnView column(String column) {
        return columnSet.getColumnView(column);
    }

}
