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
/**
 * Support classes for the Dependency Injection Framework, grace a Gin
 */
package org.activityinfo.ui.client.inject;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.UsageTracker;
import org.activityinfo.ui.client.component.report.editor.map.MapModule;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.remote.cache.AdminEntityCache;
import org.activityinfo.ui.client.dispatch.remote.cache.SchemaCache;
import org.activityinfo.ui.client.offline.OfflineController;
import org.activityinfo.ui.client.offline.OfflineModule;
import org.activityinfo.ui.client.page.FormPageLoader;
import org.activityinfo.ui.client.page.HistoryManager;
import org.activityinfo.ui.client.page.app.AppLoader;
import org.activityinfo.ui.client.page.config.ConfigLoader;
import org.activityinfo.ui.client.page.dashboard.DashboardLoader;
import org.activityinfo.ui.client.page.entry.DataEntryLoader;
import org.activityinfo.ui.client.page.entry.EntryModule;
import org.activityinfo.ui.client.page.print.PrintFormPanel;
import org.activityinfo.ui.client.page.report.ReportLoader;
import org.activityinfo.ui.client.page.report.ReportModule;

/**
 * GIN injector.
 */
@GinModules({AppModule.class,
        ReportModule.class,
        EntryModule.class,
        MapModule.class,
        OfflineModule.class})
public interface AppInjector extends Ginjector {
    EventBus getEventBus();

    HistoryManager getHistoryManager();

    DataEntryLoader createDataEntryLoader();

    ReportLoader createReportLoader();

    ConfigLoader createConfigLoader();

    OfflineController createOfflineController();

    UsageTracker getUsageTracker();

    DashboardLoader createDashboardLoader();

    SchemaCache createSchemaCache();

    AdminEntityCache createAdminCache();

    AppLoader createAppLoader();
    
    PrintFormPanel createPrintFormPanel();

    FormPageLoader createFormLoader();

    ResourceLocator getResourceLocator();

}
