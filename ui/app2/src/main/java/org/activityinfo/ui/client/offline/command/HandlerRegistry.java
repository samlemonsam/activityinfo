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
package org.activityinfo.ui.client.offline.command;

import com.bedatadriven.rebar.sql.client.query.SqliteDialect;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.impl.*;

import java.util.HashMap;
import java.util.Map;

@Singleton
public final class HandlerRegistry {
    private static final SqliteDialect SQL_DIALECT = new SqliteDialect();

    private final Map<Class, CommandHandlerAsync> map = new HashMap<Class, CommandHandlerAsync>();
    
    public HandlerRegistry() {
        registerHandler(GetSchema.class, new GetSchemaHandler());
        registerHandler(GetSites.class, new OldGetSitesHandler(SQL_DIALECT));
        registerHandler(GetAdminEntities.class, new GetAdminEntitiesHandler());
        registerHandler(GetPartnersDimension.class, new GetPartnersDimensionHandler());
        registerHandler(CreateSite.class, new CreateSiteHandlerAsync());
        registerHandler(UpdateSite.class, new UpdateSiteHandlerAsync());
        registerHandler(CreateLocation.class, new CreateLocationHandlerAsync());
        registerHandler(UpdateMonthlyReports.class, new UpdateMonthlyReportsAsync());
        registerHandler(SearchLocations.class, new SearchLocationsHandler(SQL_DIALECT));
        registerHandler(PivotSites.class, new OldPivotSitesHandler(SQL_DIALECT));
        registerHandler(GetLocations.class, new GetLocationsHandler());
        registerHandler(DeleteSite.class, new DeleteSiteHandlerAsync());
        registerHandler(GetSiteAttachments.class, new GetSiteAttachmentsHandler());
        registerHandler(GetMonthlyReports.class, new GetMonthlyReportsHandlerAsync());
        registerHandler(GetActivityForm.class, new GetActivityFormHandler());
        registerHandler(BatchCommand.class, new BatchCommandHandlerAsync());
    }

    public <C extends Command<R>, R extends CommandResult> void registerHandler(Class<C> commandClass,
                                                                                CommandHandlerAsync<C, R> handler) {
        map.put(commandClass, handler);
    }

    public <C extends Command<R>, R extends CommandResult> CommandHandlerAsync<C, R> getHandler(C c) {
        CommandHandlerAsync<C, R> handler = map.get(c.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("No handler class for " + c.toString());
        }

        return handler;
    }

    public boolean hasHandler(Command<?> command) {
        return map.containsKey(command.getClass());
    }

}
