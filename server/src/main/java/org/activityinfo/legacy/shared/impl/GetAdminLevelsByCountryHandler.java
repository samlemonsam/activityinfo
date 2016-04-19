package org.activityinfo.legacy.shared.impl;

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.GetAdminLevelsByCountry;
import org.activityinfo.legacy.shared.command.result.AdminLevelResult;
import org.activityinfo.legacy.shared.model.AdminLevelDTO;

import java.util.List;

/**
 * Created by yuriyz on 4/19/2016.
 */
public class GetAdminLevelsByCountryHandler implements CommandHandlerAsync<GetAdminLevelsByCountry, AdminLevelResult> {

    @Override
    public void execute(GetAdminLevelsByCountry command,
                        ExecutionContext context,
                        final AsyncCallback<AdminLevelResult> callback) {

        SqlQuery.select()
                .from(Tables.ADMIN_LEVEL, "level")
                .where("level.countryId")
                .equalTo(command.getCountryId())
                .execute(context.getTransaction(), new SqlResultCallback() {

            @Override
            public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                List<AdminLevelDTO> levels = Lists.newArrayList();
                for (SqlResultSetRow row : results.getRows()) {
                    AdminLevelDTO level = new AdminLevelDTO();
                    level.setId(row.getInt("id"));
                    level.setName(row.getString("name"));
                    level.setPolygons(row.getBoolean("polygons"));
                    levels.add(level);
                }

                callback.onSuccess(new AdminLevelResult(levels));
            }
        });
    }
}
