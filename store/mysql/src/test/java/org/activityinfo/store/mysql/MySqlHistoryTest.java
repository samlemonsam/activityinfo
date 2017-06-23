package org.activityinfo.store.mysql;


import com.google.gson.GsonBuilder;
import org.activityinfo.json.JsonArray;
import org.activityinfo.model.legacy.CuidAdapter;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public class MySqlHistoryTest extends AbstractMySqlTest {


    @Before
    public void setupDatabase() throws Throwable {
        resetDatabase("history.db.xml");
    }

    @Test
    public void locationChange() throws SQLException {
        RecordHistoryBuilder builder = new RecordHistoryBuilder(catalog);
        org.activityinfo.json.JsonArray array = builder.build(CuidAdapter.activityFormClass(33), CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, 968196924));

        prettyPrint(array);
    }

    private void prettyPrint(JsonArray array) {
        System.out.println(new GsonBuilder()
                .setPrettyPrinting()
                .create()
                .toJson(array));
    }

}
