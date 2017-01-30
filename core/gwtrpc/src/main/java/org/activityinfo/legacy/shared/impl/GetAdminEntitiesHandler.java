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
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetAdminEntities;
import org.activityinfo.legacy.shared.command.result.AdminEntityResult;
import org.activityinfo.legacy.shared.model.AdminEntityDTO;
import org.activityinfo.model.type.geo.Extents;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GetAdminEntitiesHandler implements CommandHandlerAsync<GetAdminEntities, AdminEntityResult> {

    @Override
    public void execute(GetAdminEntities cmd,
                        ExecutionContext context,
                        final AsyncCallback<AdminEntityResult> callback) {

        SqlQuery query = SqlQuery.select("AdminEntity.adminEntityId",
                "AdminEntity.name",
                "AdminEntity.adminLevelId",
                "AdminEntity.adminEntityParentId",
                "x1",
                "y1",
                "x2",
                "y2").from(Tables.ADMIN_ENTITY, "AdminEntity").whereTrue("not AdminEntity.deleted");


        query.orderBy("AdminEntity.name");

        if (cmd.getLevelId() != null) {
            query.where("AdminEntity.AdminLevelId").equalTo(cmd.getLevelId());
        } else {
            query.leftJoin(Tables.ADMIN_LEVEL, "level")
                 .on("AdminEntity.AdminLevelID=level.AdminLevelId");
            query.appendColumn("level.name", "levelName");
        }

        if (cmd.getEntityIds() != null && !cmd.getEntityIds().isEmpty()) {
            query.where("AdminEntity.AdminEntityId").in(cmd.getEntityIds());
        }

        if (cmd.getParentId() != null) {
            if(cmd.getParentId() == GetAdminEntities.ROOT) {
                query.where("AdminEntity.AdminEntityParentId IS NULL");
            } else {
                query.where("AdminEntity.AdminEntityParentId").equalTo(cmd.getParentId());
            }
        }

        if (cmd.getFilter() != null) {
            Filter filter = cmd.getFilter();
            if(filter.isRestricted(DimensionType.Activity) ||
               filter.isRestricted(DimensionType.Database) ||
               filter.isRestricted(DimensionType.Indicator)) {

                SqlQuery subQuery = SqlQuery
                        .select("link.AdminEntityId")
                        .from(Tables.SITE, "site")
                        .leftJoin(Tables.LOCATION, "Location")
                           .on("Location.LocationId = site.LocationId")
                        .leftJoin(Tables.LOCATION_ADMIN_LINK, "link")
                           .on("link.LocationId = Location.LocationId");

                if(filter.isRestricted(DimensionType.Activity)) {
                    subQuery.where("site.ActivityId")
                            .in(filter.getRestrictions(DimensionType.Activity));
                }
                if(filter.isRestricted(DimensionType.Database)) {
                    subQuery.leftJoin(Tables.ACTIVITY, "activity")
                            .on("site.ActivityId=activity.ActivityId")
                            .where("activity.DatabaseId")
                            .in(filter.getRestrictions(DimensionType.Database));
                }
                if(filter.isRestricted(DimensionType.Indicator)) {
                    subQuery.leftJoin(Tables.REPORTING_PERIOD, "rp").on("site.siteId=rp.SiteId")
                            .leftJoin(Tables.INDICATOR_VALUE, "iv").on("iv.reportingPeriodId=rp.reportingPeriodId")
                            .where("iv.indicatorId").in(filter.getRestrictions(DimensionType.Indicator));

                }
                query.where("AdminEntity.AdminEntityId").in(subQuery);
            }
        }

        if (cmd.getFilter() != null && cmd.getFilter().isRestricted(DimensionType.AdminLevel)) {
            if (cmd.getLevelId() == null) {
                query.where("AdminEntityId").in(cmd.getFilter().getRestrictions(DimensionType.AdminLevel));
            } else {
                SqlQuery subQuery = SqlQuery.select("adminEntityId")
                                            .from(Tables.ADMIN_ENTITY, "AdminEntity")
                                            .where("AdminLevelId")
                                            .equalTo(cmd.getLevelId())
                                            .where("AdminEntityId")
                                            .in(cmd.getFilter().getRestrictions(DimensionType.AdminLevel));
                query.where("AdminEntity.AdminEntityId").in(subQuery);
            }
        }
        query.execute(context.getTransaction(), new SqlResultCallback() {

            @Override
            public void onSuccess(SqlTransaction tx, SqlResultSet results) {
                final List<AdminEntityDTO> entities = new ArrayList<AdminEntityDTO>();
                Set<String> names = Sets.newHashSet();
                Set<String> duplicates = Sets.newHashSet();
                for (SqlResultSetRow row : results.getRows()) {
                    AdminEntityDTO entity = toEntity(row);
                    if(!names.add(entity.getName())) {
                        duplicates.add(entity.getName());
                    }
                    entities.add(entity);
                }
                for(int i=0;i!=entities.size();++i) {
                    if(duplicates.contains(entities.get(i).getName())) {
                        String levelName = results.getRow(i).getString("levelName");
                        entities.get(i).setName(entities.get(i).getName() +
                                                " [" + levelName + "]");
                    }
                }
                callback.onSuccess(new AdminEntityResult(entities));
            }
        });
    }

    public static AdminEntityDTO toEntity(SqlResultSetRow row) {

        AdminEntityDTO entity = new AdminEntityDTO();
        entity.setId(row.getInt("adminEntityId"));
        entity.setName(row.getString("name"));
        entity.setLevelId(row.getInt("adminLevelId"));
        entity.setLevelName(row.getString("levelName"));
        if (!row.isNull("adminEntityParentId")) {
            entity.setParentId(row.getInt("adminEntityParentId"));
        }
        Extents bounds = Extents.empty();
        if (!row.isNull("x1")) {
            bounds.setMinLon(row.getDouble("x1"));
            bounds.setMinLat(row.getDouble("y1"));
            bounds.setMaxLon(row.getDouble("x2"));
            bounds.setMaxLat(row.getDouble("y2"));
            entity.setBounds(bounds);
        }
        return entity;
    }
}
