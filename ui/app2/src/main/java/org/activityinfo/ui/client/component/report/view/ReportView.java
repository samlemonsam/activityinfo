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
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.legacy.shared.reports.model.ReportElement;

public interface ReportView<T extends ReportElement> {

    void loading();

    void show(T element);

    Component asComponent();

    void onFailure(Throwable caught, ClickHandler retryCallback);
}
