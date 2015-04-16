package org.activityinfo.store.mysql;

import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.formTree.FormTreeBuilder;
import org.activityinfo.model.formTree.FormTreePrettyPrinter;
import org.activityinfo.model.formTree.JsonFormTreeBuilder;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.mysql.collections.CountryCollection;
import org.activityinfo.store.mysql.collections.DatabaseCollection;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.activityinfo.model.legacy.CuidAdapter.adminLevelFormClass;
import static org.activityinfo.model.legacy.CuidAdapter.partnerInstanceId;
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasAllNullValuesWithLengthOf;
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasValues;
import static org.junit.Assert.assertThat;


public class MySqlCatalogTest {

    private static DbUnit dbunit;
    private static ColumnSetBuilder executor;
    private ColumnSet columnSet;
    private static CollectionCatalog catalogProvider;

    @BeforeClass
    public static void setup() throws Throwable {
        System.out.println("Initializing Locale...");
        LocaleProxy.initialize();

        System.out.println("Running setup...");
        dbunit = new DbUnit();
        dbunit.openDatabase();
        dbunit.dropAllRows();
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, "sites-simple1.db.xml"));
        catalogProvider = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
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
        assertThat(column("description"), hasAllNullValuesWithLengthOf(3));
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
    public void testAdmin() {
        query(CuidAdapter.adminLevelFormClass(2), "name", "province.name", "code");
        assertThat(column("province.name"), hasValues("Ituri", "Sud Kivu", "Sud Kivu", "Sud Kivu", "Ituri"));
        assertThat(column("name"), hasValues("Bukavu", "Walungu", "Shabunda", "Kalehe", "Irumu"));
        assertThat(column("code"), hasValues( "203", "201", "202", "203", "203"));
    }
    
    @Test
    public void testAdminTree() {
        FormTreeBuilder builder = new FormTreeBuilder(catalogProvider);
        FormTree formTree = builder.queryTree(adminLevelFormClass(2));
        JsonObject formTreeObject = JsonFormTreeBuilder.toJson(formTree);
        formTree = JsonFormTreeBuilder.fromJson(formTreeObject);

        FormTreePrettyPrinter.print(formTree);
    }
    

    @Test
    public void testSiteSimple() {
        query(CuidAdapter.activityFormClass(1), "date1", "date2", "partner", 
                "partner.label", "location.label", "BENE", "cause");
        
        assertThat(column("partner"), hasValues(partnerInstanceId(1), partnerInstanceId(1), partnerInstanceId(2)));
        assertThat(column("partner.label"), hasValues("NRC", "NRC", "Solidarites"));
        assertThat(column("location.label"), hasValues("Penekusu Kivu", "Ngshwe", "Boga"));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000));
        assertThat(column("cause"), hasValues(null, "Deplacement", "Catastrophe Naturelle"));
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
