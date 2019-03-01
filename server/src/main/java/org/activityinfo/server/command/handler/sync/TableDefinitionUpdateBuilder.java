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
package org.activityinfo.server.command.handler.sync;

import com.bedatadriven.rebar.sync.server.JpaUpdateBuilder;
import org.activityinfo.legacy.shared.command.GetSyncRegionUpdates;
import org.activityinfo.legacy.shared.command.result.SyncRegionUpdate;
import org.activityinfo.server.database.hibernate.entity.*;
import org.json.JSONException;

public class TableDefinitionUpdateBuilder implements UpdateBuilder {

    public static final String REGION_TYPE = "site-tables";
    
    public static final String CURRENT_VERSION = "4";
    
    private final JpaUpdateBuilder builder = new JpaUpdateBuilder();

    private static final Class[] tablesToSync = new Class[]{
            Country.class,
            AdminLevel.class,
            AdminEntity.class,
            LocationType.class,
            Database.class,
            Folder.class,
            Partner.class,
            Activity.class,
            Indicator.class,
            AttributeGroup.class,
            Attribute.class,
            User.class,
            UserPermission.class,
            LockedPeriod.class,
            Project.class};
    
    @Override
    public SyncRegionUpdate build(User user, GetSyncRegionUpdates request) throws JSONException {
        SyncRegionUpdate update = new SyncRegionUpdate();
        update.setComplete(true);
        update.setVersion(CURRENT_VERSION);

        if (!CURRENT_VERSION.equals(request.getLocalVersion())) {

            for (Class schemaClass : tablesToSync) {
                builder.createTableIfNotExists(schemaClass);
            }

            builder.createTableIfNotExists(Site.class);
            builder.createTableIfNotExists(ReportingPeriod.class);

            builder.executeStatement(
                    "create table if not exists AttributeGroupInActivity (ActivityId integer, AttributeGroupId integer)");
            builder.executeStatement("create table if not exists PartnerInDatabase (DatabaseId integer, PartnerId int)");
            builder.executeStatement("create table if not exists IndicatorLink (SourceIndicatorId integer, DestinationIndicatorId int)");

            builder.executeStatement("create index if not exists site_activity on site (ActivityId)");

            builder.executeStatement(
                    "create table if not exists AttributeValue (SiteId integer, AttributeId integer, Value integer)");
            builder.executeStatement(
                    "create table if not exists IndicatorValue (ReportingPeriodId integer, IndicatorId integer, " +
                    "Value real, TextValue text, DateValue text, BooleanValue bit)");

            builder.executeStatement(
                    "create table if not exists sitehistory (id integer, siteid integer, userid integer, " +
                    "timecreated real, initial integer, json text)");

            builder.createTableIfNotExists(Location.class);
            builder.executeStatement(
                    "create table if not exists LocationAdminLink (LocationId integer, AdminEntityId integer)");

            builder.executeStatement(
                    "CREATE TABLE IF NOT EXISTS  target (targetId int, name text, date1 text, date2 text, projectId int, " +
                            "partnerId int, adminEntityId int, databaseId int)");
            builder.executeStatement("CREATE TABLE IF NOT EXISTS  targetvalue (targetId int, IndicatorId int, value real)");

            builder.executeStatement("CREATE TABLE IF NOT EXISTS groupassignment (UserPermissionId int, PartnerId int)");
            
            update.setSql(builder.asJson());
        }

        return update;
    }
}
