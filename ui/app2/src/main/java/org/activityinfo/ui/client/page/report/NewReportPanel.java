package org.activityinfo.ui.client.page.report;

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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.CreateReport;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.page.NavigationEvent;
import org.activityinfo.ui.client.page.NavigationHandler;
import org.activityinfo.ui.client.page.common.GalleryRenderer;
import org.activityinfo.ui.client.page.report.template.*;

public class NewReportPanel extends ContentPanel {

    private ListStore<ReportTemplate> store;
    private EventBus eventBus;
    private Dispatcher dispatcher;

    public NewReportPanel(EventBus eventBus, Dispatcher dispatcher, ResourceLocator locator) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;

        setHeadingText(I18N.CONSTANTS.createNewReport());
        setLayout(new FitLayout());

        store = new ListStore<>();
        store.add(new ChartTemplate(dispatcher));
        store.add(new PivotTableTemplate(dispatcher));
        store.add(new MapTemplate(dispatcher, locator));
        store.add(new CompositeTemplate(dispatcher));

        ListView<ReportTemplate> view = new ListView<ReportTemplate>();
        view.setStyleName("gallery");
        view.setRenderer(new GalleryRenderer<>("image/reports/"));
        view.setBorders(false);
        view.setStore(store);
        view.setItemSelector("dd");
        view.setOverStyle("over");
        view.setSelectStyle("over");

        view.addListener(Events.Select, new Listener<ListViewEvent<ReportTemplate>>() {

            @Override
            public void handleEvent(ListViewEvent<ReportTemplate> event) {
                createNew(event.getModel());
            }
        });
        add(view);
    }

    private void createNew(ReportTemplate model) {
        model.createReport(new AsyncCallback<Report>() {

            @Override
            public void onSuccess(Report report) {
                dispatcher.execute(new CreateReport(report), new AsyncCallback<CreateResult>() {

                    @Override
                    public void onFailure(Throwable caught) {

                    }

                    @Override
                    public void onSuccess(CreateResult created) {
                        eventBus.fireEvent(new NavigationEvent(NavigationHandler.NAVIGATION_REQUESTED,
                                new ReportDesignPageState(created.getNewId())));
                    }
                });
            }

            @Override
            public void onFailure(Throwable caught) {

            }
        });
    }

}
