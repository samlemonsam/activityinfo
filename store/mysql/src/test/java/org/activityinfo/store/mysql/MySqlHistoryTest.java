package org.activityinfo.store.mysql;


import org.activityinfo.json.Json;
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.form.RecordHistoryEntry;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.RecordRef;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

public class MySqlHistoryTest extends AbstractMySqlTest {


    @Before
    public void setupDatabase() throws Throwable {
        resetDatabase("history.db.xml");
    }

    @Test
    public void locationChange() throws SQLException {
        MySqlRecordHistoryBuilder builder = new MySqlRecordHistoryBuilder(catalog);
        RecordHistory array = builder.build(new RecordRef(
                CuidAdapter.activityFormClass(33),
                CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, 968196924)));

        System.out.println(Json.stringify(Json.toJson(array), 2));
    }

}
