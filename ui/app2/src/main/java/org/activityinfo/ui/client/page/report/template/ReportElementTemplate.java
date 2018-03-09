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
package org.activityinfo.ui.client.page.report.template;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.ui.client.dispatch.Dispatcher;

public abstract class ReportElementTemplate extends ReportTemplate {

    public ReportElementTemplate(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void createReport(final AsyncCallback<Report> callback) {
        createElement(new AsyncCallback<ReportElement>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(ReportElement result) {
                Report report = new Report();
                report.addElement(result);
                callback.onSuccess(report);
            }
        });
    }

    public abstract void createElement(AsyncCallback<ReportElement> callback);

}
