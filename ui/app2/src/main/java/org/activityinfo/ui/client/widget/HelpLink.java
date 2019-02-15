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
package org.activityinfo.ui.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * Link to the ActivityInfo user manual
 */
public class HelpLink implements IsWidget {

    interface Templates extends SafeHtmlTemplates {
        @Template("<a href=\"http://help.activityinfo.org/m/75865/l/{0}\" target=\"_blank\">{1}</a>")
        SafeHtml link(int helpId, String label);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private HTML html;

    /**
     * @param label the label of thel ink
     * @param helpId the ScreenSteps id
     */
    @UiConstructor
    public HelpLink(String label, int helpId) {
        this.html = new HTML(TEMPLATES.link(helpId, label));
    }

    @Override
    public Widget asWidget() {
        return html;
    }
}
