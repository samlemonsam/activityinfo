package org.activityinfo.ui.client;

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

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.state.StateManager;
import com.extjs.gxt.ui.client.util.Theme;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.ui.client.dispatch.state.SafeStateProvider;
import org.activityinfo.ui.client.inject.AppInjector;
import org.activityinfo.ui.client.inject.ClientSideAuthProvider;
import org.activityinfo.ui.client.page.common.ApplicationBundle;
import org.activityinfo.ui.client.page.print.PrintFormPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ActivityInfoEntryPoint implements EntryPoint {

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {

        Log.info("Application: onModuleLoad starting");
        Log.info("Application Permutation: " + GWT.getPermutationStrongName());

        ApplicationBundle.INSTANCE.styles().ensureInjected();

        try {
            new ClientSideAuthProvider().get();
        } catch (Exception e) {
            Log.error("Exception getting client side authentication", e);
            SessionUtil.forceLogin();
        }

        if (Log.isErrorEnabled()) {
            GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
                @Override
                public void onUncaughtException(Throwable e) {
                    Log.error("Uncaught exception", e);
                }
            });
        }

        GXT.setDefaultTheme(Theme.BLUE, true);

        // avoid cookie overflow
        StateManager.get().setProvider(new SafeStateProvider());

        Log.trace("Application: GXT theme set");

        final AppInjector injector = GWT.create(AppInjector.class);

        if(Window.Location.getHash() != null && Window.Location.getHash().startsWith("#print/")) {
            // Show form only view appropriate for printing
            
            openPrintView(injector);
        
        } else {

            // Launch the normal application
            
            injector.createAppLoader();
            injector.createDashboardLoader();
            injector.createDataEntryLoader();
            injector.createReportLoader();
            injector.createConfigLoader();
            injector.createFormLoader();
            injector.getUsageTracker();

            injector.getHistoryManager();

            injector.createOfflineController();

            createCaches(injector);

            AppCacheMonitor.start();
        }

        Log.info("Application: everyone plugged, firing Init event");

        injector.getEventBus().fireEvent(AppEvents.INIT);

    }


    protected void createCaches(AppInjector injector) {
        injector.createSchemaCache();
        injector.createAdminCache();
    }


    private void openPrintView(AppInjector injector) {
        PrintFormPanel printFormPanel = injector.createPrintFormPanel();

        RootPanel.get().add(printFormPanel);
        
    }
}
