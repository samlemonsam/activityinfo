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
package org.activityinfo.ui.client.component.report.view;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.GenerateElement;
import org.activityinfo.legacy.shared.reports.content.Content;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.report.HasReportElement;
import org.activityinfo.ui.client.page.report.ReportChangeHandler;
import org.activityinfo.ui.client.page.report.ReportEventBus;

/**
 * Keeps a report view in sync with a ReportElement being edited.
 */
public class ReportViewBinder<C extends Content, R extends ReportElement<C>> implements HasReportElement<R> {

    private static final int UPDATE_DELAY = 100;

    private final ReportEventBus reportEventBus;
    private final Dispatcher dispatcher;
    private final ReportView<R> view;

    private Timer updateTimer;

    private R elementModel;

    private int latestRequest;

    public static <C extends Content, R extends ReportElement<C>> ReportViewBinder<C, R> create(EventBus eventBus,
                                                                                                Dispatcher dispatcher,
                                                                                                ReportView<R> view) {
        return new ReportViewBinder<C, R>(eventBus, dispatcher, view);
    }

    public ReportViewBinder(EventBus eventBus, Dispatcher dispatcher, ReportView<R> view) {
        this.reportEventBus = new ReportEventBus(eventBus, this);
        this.dispatcher = dispatcher;
        this.view = view;

        reportEventBus.listen(new ReportChangeHandler() {

            @Override
            public void onChanged() {
                updateTimer.schedule(UPDATE_DELAY);
            }
        });

        updateTimer = new Timer() {

            @Override
            public void run() {
                load();
            }
        };
    }

    @Override
    public void bind(R model) {
        this.elementModel = model;
        load();
    }

    @Override
    public R getModel() {
        return elementModel;
    }

    public Component getComponent() {
        return (Component) view;
    }

    private void load() {
        
        if (!elementModel.getFilter().getRestrictions(DimensionType.Indicator).isEmpty()) {

            final int thisRequest = latestRequest+1;
            latestRequest = thisRequest;

            view.loading();

            executeLoad(new AsyncCallback<C>() {

                @Override
                public void onFailure(Throwable caught) {
                    Log.error(caught.getMessage(), caught);
                    view.onFailure(caught, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            load();
                        }
                    });
                }

                @Override
                public void onSuccess(C result) {
                    if (thisRequest == latestRequest) {
                        elementModel.setContent(result);
                        view.show(elementModel);
                    }
                }
            });
        }
    }

    protected void executeLoad(AsyncCallback<C> callback) {
        GenerateElement<C> request = new GenerateElement<C>(elementModel);
        dispatcher.execute(request, callback);
    }

    @Override
    public void disconnect() {
        reportEventBus.disconnect();
    }
}
