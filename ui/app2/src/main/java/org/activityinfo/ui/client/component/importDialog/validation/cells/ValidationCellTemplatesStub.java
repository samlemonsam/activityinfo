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
package org.activityinfo.ui.client.component.importDialog.validation.cells;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
* Created by alex on 2/19/14.
*/
public class ValidationCellTemplatesStub implements ValidationCellTemplates {

    @Override
    public SafeHtml deleted(String text) {
        return SafeHtmlUtils.fromTrustedString("<s>" + text + "</s>");
    }

    @Override
    public SafeHtml inserted(String text) {
        return SafeHtmlUtils.fromTrustedString("<i>" + text + "</i>");
    }

    @Override
    public SafeHtml invalid(String text) {
        return SafeHtmlUtils.fromTrustedString("<s>" + text + "</s>");
    }
}
