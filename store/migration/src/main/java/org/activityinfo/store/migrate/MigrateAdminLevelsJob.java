package org.activityinfo.store.migrate;

import com.google.appengine.tools.pipeline.ImmediateValue;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MigrateAdminLevelsJob extends Job0<List<Void>> {

    public static final String QUERY = "select a.adminlevelid, a.parentId, a.name, b.name from adminlevel a " +
            "left join adminlevel b on (a.parentid=b.adminlevelid) " +
            "where a.deleted = 0 " +
            "order by a.adminlevelid";

    @Override
    public Value<List<Void>> run() throws Exception {

        List<Value<Void>> migrations = new ArrayList<>();

        QueryExecutor executor = new MySqlQueryExecutor();
        try(ResultSet rs = executor.query(QUERY)) {
            while(rs.next()) {
                int id = rs.getInt(1);
                ImmediateValue<String> formId = immediate(CuidAdapter.adminLevelFormClass(id).asString());
                migrations.add(futureCall(new MigrateFormJob(), formId));
            }
        }

        return futureList(migrations);
    }
}
