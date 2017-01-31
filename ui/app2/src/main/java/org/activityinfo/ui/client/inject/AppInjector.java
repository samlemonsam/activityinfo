/**
 * Support classes for the Dependency Injection Framework, grace a Gin
 */
package org.activityinfo.ui.client.inject;

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

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.UsageTracker;
import org.activityinfo.ui.client.component.report.editor.map.MapModule;
import org.activityinfo.ui.client.dispatch.remote.cache.AdminEntityCache;
import org.activityinfo.ui.client.dispatch.remote.cache.SchemaCache;
import org.activityinfo.ui.client.local.LocalController;
import org.activityinfo.ui.client.local.LocalModule;
import org.activityinfo.ui.client.page.HistoryManager;
import org.activityinfo.ui.client.page.app.AppLoader;
import org.activityinfo.ui.client.page.config.ConfigLoader;
import org.activityinfo.ui.client.page.config.ConfigModule;
import org.activityinfo.ui.client.page.dashboard.DashboardLoader;
import org.activityinfo.ui.client.page.entry.DataEntryLoader;
import org.activityinfo.ui.client.page.entry.EntryModule;
import org.activityinfo.ui.client.page.print.PrintFormPanel;
import org.activityinfo.ui.client.page.report.ReportLoader;
import org.activityinfo.ui.client.page.report.ReportModule;

/**
 * GIN injector.
 * <p/>                                     ap
 * TODO: having this number of explicit entries is probably not ideal, try to
 * make better use of injection and injecting Provider<>s
 */
@GinModules({AppModule.class,
        ReportModule.class,
        EntryModule.class,
        MapModule.class,
        ConfigModule.class,
        LocalModule.class})
public interface AppInjector extends Ginjector {
    EventBus getEventBus();

    HistoryManager getHistoryManager();

    DataEntryLoader createDataEntryLoader();

    ReportLoader createReportLoader();

    ConfigLoader createConfigLoader();

    LocalController createOfflineController();

    UsageTracker getUsageTracker();

    DashboardLoader createDashboardLoader();

    SchemaCache createSchemaCache();

    AdminEntityCache createAdminCache();

    AppLoader createAppLoader();
    
    PrintFormPanel createPrintFormPanel();
}
