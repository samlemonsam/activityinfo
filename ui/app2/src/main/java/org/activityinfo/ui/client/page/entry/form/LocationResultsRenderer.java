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
package org.activityinfo.ui.client.page.entry.form;

import com.extjs.gxt.ui.client.widget.ListRenderer;
import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.activityinfo.legacy.shared.model.LocationDTO;

/**
 * Renders location search results
 */
public class LocationResultsRenderer extends ListRenderer<LocationDTO> {

    @Override
    protected void renderItem(LocationDTO locationDTO, SafeHtmlBuilder html) {
        html.appendHtmlConstant("<div class=locSerResult>");
        html.appendHtmlConstant("<div class=locSerMarker>");
        html.appendEscaped(Strings.nullToEmpty(locationDTO.getMarker()));
        html.appendHtmlConstant("</div>");

        html.appendHtmlConstant("<div class=locSerWrap>");
        html.appendHtmlConstant("<div class=locSerName>");
        html.appendEscaped(locationDTO.getName());
        html.appendHtmlConstant("</div>");

        if(locationDTO.hasAxe()) {
            html.appendHtmlConstant("<div class=locSerAxe>");
            html.appendEscaped(locationDTO.getAxe());
            html.appendHtmlConstant("</div>");
        }
        html.appendHtmlConstant("</div>");
        html.appendHtmlConstant("</div>");
    }
}
