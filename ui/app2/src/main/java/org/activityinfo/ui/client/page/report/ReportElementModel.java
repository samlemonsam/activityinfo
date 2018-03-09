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
package org.activityinfo.ui.client.page.report;

import com.extjs.gxt.ui.client.data.BaseModelData;
import org.activityinfo.legacy.shared.reports.model.ReportElement;

public class ReportElementModel extends BaseModelData {

    public ReportElementModel(ReportElement element) {
        setElementTitle(element.getTitle());
        setReportElement(element);
    }

    public void setId(int id) {
        set("id", id);
    }

    public int getId() {
        return (Integer) get("id");
    }

    public void setEdited(boolean e) {
        set("edited", e);
    }

    public boolean getEdited() {
        return (Boolean) get("edited");
    }

    public void setElementTitle(String elementTitle) {
        set("elementTitle", elementTitle);
    }

    public String getElementTitle() {
        return get("elementTitle");
    }

    public void setReportElement(ReportElement reportElement) {
        set("reportElement", reportElement);
    }

    public ReportElement getReportElement() {
        return (ReportElement) get("reportElement");
    }

}