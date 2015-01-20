package org.activityinfo.store.mysql;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import net.lightoze.gwt.i18n.server.LocaleProxy;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.service.store.CollectionCatalog;
import org.activityinfo.store.query.impl.ColumnCache;
import org.activityinfo.store.query.impl.ColumnSetBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.activityinfo.model.legacy.CuidAdapter.activityFormClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by alex on 1/20/15.
 */
public class MySqlCatalogIntegrationTest {

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
        dbunit.loadDatset(Resources.getResource(MySqlCatalogTest.class, "rdc.db.xml"));
        catalogProvider = new MySqlCatalogProvider().openCatalog(dbunit.getExecutor());
        executor = new ColumnSetBuilder(catalogProvider, ColumnCache.NULL);
    }


    @Test
    public void test() {
        
        QueryModel model = new QueryModel(activityFormClass(33));
        model.selectField("date1");
        model.selectField("date2");
        
        FormClass formClass = catalogProvider.getCollection(activityFormClass(33)).getFormClass();
        for(FormField field : formClass.getFields()) {
            System.out.println(
                    field.getId() + " " + 
                    Strings.padEnd(field.getType().getClass().getSimpleName(), 25, ' ') +
                    field.getLabel());
            
            if(field.getType() instanceof QuantityType) {
                model.selectField(field.getId()).as("I" + CuidAdapter.getLegacyIdFromCuid(field.getId()));
            }
        }

        Stopwatch stopwatch = Stopwatch.createStarted();
        ColumnSet columnSet = executor.build(model);
        System.out.println("Query executed in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
        
        assertThat(columnSet.getNumRows(), equalTo(759));

        
    }
}
