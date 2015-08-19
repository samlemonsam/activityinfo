package org.activityinfo.ui.client.page.dashboard;

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

import com.extjs.gxt.ui.client.widget.custom.Portal;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.callback.SuccessCallback;
import org.activityinfo.legacy.shared.command.GetReports;
import org.activityinfo.legacy.shared.command.result.ReportsResult;
import org.activityinfo.legacy.shared.model.ReportMetadataDTO;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.dashboard.portlets.ChooseReportsPortlet;
import org.activityinfo.ui.client.page.dashboard.portlets.GoogleEarthPortlet;
import org.activityinfo.ui.client.page.dashboard.portlets.NewsPortlet;
import org.activityinfo.ui.client.page.dashboard.portlets.ReportPortlet;

import java.util.List;

public class DashboardPage extends Portal implements Page {

    public static final PageId PAGE_ID = new PageId("dashboard");
    private Dispatcher dispatcher;
    private EventBus eventBus;

    @Inject
    public DashboardPage(Dispatcher dispatcher, EventBus eventBus) {
        super(2);
        this.dispatcher = dispatcher;
        this.eventBus = eventBus;

        setBorders(true);
        setStyleAttribute("backgroundColor", "white");
        setColumnWidth(0, .63);
        setColumnWidth(1, .33);

        add(new NewsPortlet(), 1);
        add(new GoogleEarthPortlet(), 1);

        loadDashboard();

    }

    private void loadDashboard() {
        dispatcher.execute(new GetReports(), new SuccessCallback<ReportsResult>() {
            @Override
            public void onSuccess(ReportsResult result) {
                List<ReportMetadataDTO> dashboardReports = Lists.newArrayList();
                for (ReportMetadataDTO report : result.getData()) {
                    if(report.isDashboard()) {
                        dashboardReports.add(report);
                    }
                }
                
                if (dashboardReports.isEmpty()) {
                    add(new ChooseReportsPortlet(), 0);
                } else {
                    for (ReportMetadataDTO report  : dashboardReports) {
                        add(new ReportPortlet(dispatcher, eventBus, report), 0);
                    }
                }
                layout();
            }
        });
    }

    @Override
    public PageId getPageId() {
        return PAGE_ID;
    }

    @Override
    public Object getWidget() {
        return this;
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        callback.onDecided(true);
    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean navigate(PageState place) {
        return true;
    }
}