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
package org.activityinfo.ui.client.local;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.impl.*;
import org.activityinfo.ui.client.local.command.HandlerRegistry;
import org.activityinfo.ui.client.local.command.UpdateMonthlyReportsAsync;

public class HandlerRegistryProvider implements Provider<HandlerRegistry> {

    private final HandlerRegistry registry;

    @Inject
    public HandlerRegistryProvider(GetSchemaHandlerAsync schemaHandler,
                                   OldGetSitesHandler sitesHandler,
                                   GetMonthlyReportsHandlerAsync getMonthlyReportsHandler,
                                   GetAdminEntitiesHandler adminHandler,
                                   GetPartnersDimensionHandler partnersDimensionHandler,
                                   CreateSiteHandlerAsync createSiteHandler,
                                   UpdateSiteHandlerAsync updateSiteHandlerAsync,
                                   UpdateMonthlyReportsAsync updateMonthly,
                                   CreateLocationHandlerAsync createLocationHandler,
                                   SearchLocationsHandler searchLocationsHandler,
                                   // SearchHandler searchHandler,
                                   OldPivotSitesHandler pivotSitesHandler,
                                   GetLocationsHandler getLocationsHandler,
                                   DeleteSiteHandlerAsync deleteSiteHandler,
                                   GetSiteAttachmentsHandler getSiteAttachmentsHandler,
                                   GetActivityFormHandler getActivityHandler,
                                   BatchCommandHandlerAsync batchCommandHandler) {

        registry = new HandlerRegistry();
        registry.registerHandler(GetSchema.class, schemaHandler);
        registry.registerHandler(GetSites.class, sitesHandler);
        registry.registerHandler(GetAdminEntities.class, adminHandler);
        registry.registerHandler(GetPartnersDimension.class, partnersDimensionHandler);
        registry.registerHandler(CreateSite.class, createSiteHandler);
        registry.registerHandler(UpdateSite.class, updateSiteHandlerAsync);
        registry.registerHandler(CreateLocation.class, createLocationHandler);
        registry.registerHandler(UpdateMonthlyReports.class, updateMonthly);
        // registry.registerHandler(Search.class, searchHandler);
        registry.registerHandler(SearchLocations.class, searchLocationsHandler);
        registry.registerHandler(PivotSites.class, pivotSitesHandler);
        registry.registerHandler(GetLocations.class, getLocationsHandler);
        registry.registerHandler(DeleteSite.class, deleteSiteHandler);
        registry.registerHandler(GetSiteAttachments.class, getSiteAttachmentsHandler);
        registry.registerHandler(GetMonthlyReports.class, getMonthlyReportsHandler);

        // new
        registry.registerHandler(GetActivityForm.class, getActivityHandler);
        registry.registerHandler(BatchCommand.class, batchCommandHandler);
    }

    @Override
    public HandlerRegistry get() {
        return registry;
    }
}
