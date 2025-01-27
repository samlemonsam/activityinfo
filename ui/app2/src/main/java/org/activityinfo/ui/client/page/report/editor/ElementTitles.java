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
package org.activityinfo.ui.client.page.report.editor;

import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.reports.model.MapReportElement;
import org.activityinfo.legacy.shared.reports.model.PivotChartReportElement;
import org.activityinfo.legacy.shared.reports.model.PivotTableReportElement;
import org.activityinfo.legacy.shared.reports.model.ReportElement;

public final class ElementTitles {

    private ElementTitles() {
    }

    public static String format(ReportElement model) {
        if (model.getTitle() != null) {
            return model.getTitle();
        } else if (model instanceof PivotChartReportElement) {
            return I18N.CONSTANTS.untitledChart();
        } else if (model instanceof PivotTableReportElement) {
            return I18N.CONSTANTS.untitledTable();
        } else if (model instanceof MapReportElement) {
            return I18N.CONSTANTS.untitledMap();
        } else {
            return "Untitled";
        }
    }

}
