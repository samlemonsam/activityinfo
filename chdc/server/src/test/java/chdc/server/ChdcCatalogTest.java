package chdc.server;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.store.query.server.ColumnSetBuilder;
import org.activityinfo.store.query.shared.NullFormSupervisor;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.TypedRecordUpdate;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;

public class ChdcCatalogTest {

    @Test
    @Ignore("WIP")
    public void test() throws SQLException {

        ChdcCatalog catalog = new ChdcCatalog();
        FormStorage storage = catalog.getForm(ResourceId.valueOf("country")).get();

//        assertThat(storage.cacheVersion(), equalTo(2L));

        TypedRecordUpdate update = new TypedRecordUpdate();
        update.setFormId(storage.getFormClass().getId());
        update.setRecordId(ResourceId.valueOf(ResourceId.generateCuid()));
        update.set(ResourceId.valueOf("name"), TextValue.valueOf("Hello world"));

        storage.add(update);

        QueryModel queryModel = new QueryModel(ResourceId.valueOf("country"));
        queryModel.selectExpr("name").as("name");

        ColumnSetBuilder builder = new ColumnSetBuilder(catalog, new NullFormSupervisor());
        ColumnSet columnSet = builder.build(queryModel);

        ColumnView name = columnSet.getColumnView("name");

        System.out.println(name);

    }

}