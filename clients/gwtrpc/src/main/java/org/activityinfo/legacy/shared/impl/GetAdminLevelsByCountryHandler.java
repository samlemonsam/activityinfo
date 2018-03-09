/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
                .appendColumn("level.adminlevelId", "id")
                .appendColumn("level.name", "name")
                .appendColumn("level.polygons", "polygons")
                .appendColumn("level.countryId", "countryId")
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
                            level.setCountryId(row.getInt("countryId"));
                            levels.add(level);
                        }

                callback.onSuccess(new AdminLevelResult(levels));
            }
        });
    }
}
