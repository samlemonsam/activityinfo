package org.activityinfo.legacy.shared.impl;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.bedatadriven.rebar.sql.client.SqlResultCallback;
import com.bedatadriven.rebar.sql.client.SqlResultSet;
import com.bedatadriven.rebar.sql.client.SqlResultSetRow;
import com.bedatadriven.rebar.sql.client.SqlTransaction;
import com.bedatadriven.rebar.sql.client.query.SqlQuery;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.GetLocations;
import org.activityinfo.legacy.shared.command.result.LocationResult;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.legacy.shared.model.LocationDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetLocationsHandler implements CommandHandlerAsync<GetLocations, LocationResult> {

    @Override
    public void execute(final GetLocations command,
                        final ExecutionContext context,
                        final AsyncCallback<LocationResult> callback) {

        if (!command.hasLocationIds() && command.getLocationTypeId() == null) {
            callback.onSuccess(new LocationResult());
            return;
        }

        final Map<Integer, LocationDTO> dtos = new HashMap<>();

        SqlQuery query = SqlQuery.select("locationID", "name", "axe", "x", "y", "workflowStatusId", "LocationTypeId")
                                 .from(Tables.LOCATION, "Location");

        if (!command.getLocationIds().isEmpty()) {
            query.where("LocationId").in(command.getLocationIds());
        }
        if (command.getLocationTypeId() != null) {
            query.where("locationTypeId").equalTo(command.getLocationTypeId());
        }
        query.where("workflowstatusid").equalTo("validated");

        query.execute(context.getTransaction(), new SqlResultCallback() {
            @Override
            public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                for (SqlResultSetRow row : results.getRows()) {
                    final LocationDTO dto = new LocationDTO();
                    dto.setId(row.getInt("locationID"));
                    dto.setName(row.getString("name"));
                    dto.setAxe(row.getString("axe"));
                    dto.setWorkflowStatusId(row.getString("workflowStatusId"));
                    dto.setLocationTypeId(row.getInt("LocationTypeId"));
                    if (!row.isNull("x") && !row.isNull("y")) {
                        dto.setLatitude(row.getDouble("y"));
                        dto.setLongitude(row.getDouble("x"));
                    }
                    dtos.put(dto.getId(), dto);
                }

                SqlQuery query = SqlQuery.select()
                                         .appendColumn("AdminEntity.AdminEntityId", "adminEntityId")
                                         .appendColumn("AdminEntity.Name", "name")
                                         .appendColumn("AdminEntity.AdminLevelId", "levelId")
                                         .appendColumn("AdminEntity.AdminEntityParentId", "parentId")
                                         .appendColumn("link.LocationID", "locationId")
                                         .from(Tables.LOCATION_ADMIN_LINK, "link")
                                         .leftJoin(Tables.ADMIN_ENTITY, "AdminEntity")
                                         .on("link.AdminEntityId=AdminEntity.AdminEntityId")
                                         .whereTrue("AdminEntity.AdminEntityId is not null");

                if (!command.getLocationIds().isEmpty()) {
                    query.where("link.LocationId").in(command.getLocationIds());
                }

                if (command.getLocationTypeId() != null) {
                    query.leftJoin(Tables.LOCATION, "Location").on("link.LocationId=Location.LocationId");
                    query.where("Location.LocationTypeId").equalTo(command.getLocationTypeId());
                }


                query.execute(context.getTransaction(), new SqlResultCallback() {
                    @Override
                    public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                        for (SqlResultSetRow row : results.getRows()) {
                            AdminEntityDTO entity = new AdminEntityDTO();
                            entity.setId(row.getInt("adminEntityId"));
                            entity.setName(row.getString("name"));
                            entity.setLevelId(row.getInt("levelId"));
                            if (!row.isNull("parentId")) {
                                entity.setParentId(row.getInt("parentId"));
                            }
                            LocationDTO dto = dtos.get(row.getInt("locationId"));
                            if (dto != null) {
                                dto.setAdminEntity(entity.getLevelId(), entity);
                            }
                        }

                        List<LocationDTO> list = new ArrayList<>(dtos.values());
                        callback.onSuccess(new LocationResult(list));
                    }
                });
            }
        });
    }
}
