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

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.legacy.shared.reports.model.Report;
import org.activityinfo.ui.client.dispatch.Dispatcher;

public abstract class ReportTemplate extends BaseModelData {

    protected final Dispatcher dispatcher;

    @Inject
    public ReportTemplate(Dispatcher dispatcher) {
        super();
        this.dispatcher = dispatcher;
    }

    public void setName(String name) {
        set("name", name);
    }

    public void setDescription(String description) {
        set("description", description);
    }

    public void setImagePath(String path) {
        set("path", path);
    }

    public abstract void createReport(AsyncCallback<Report> report);

}
