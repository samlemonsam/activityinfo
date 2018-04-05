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
package org.activityinfo.ui.client.table.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.XTemplates;
import org.activityinfo.analysis.table.ApiViewModel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.observable.Observable;

public class ApiPane implements IsWidget {

    private HTML panel;

    interface Templates extends XTemplates {

        @XTemplate(source = "ApiPane.html")
        SafeHtml panel(TableBundle.Style style, ApiViewModel model);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    public ApiPane(TableViewModel viewModel) {

        this.panel = new HTML("");
        this.panel.addStyleName(TableBundle.INSTANCE.style().detailPane());

        viewModel.getApiViewModel().subscribe(this::render);
    }

    private void renderLoading() {
        panel.setHTML(SafeHtmlUtils.htmlEscape(I18N.CONSTANTS.loading()));
    }

    private void render(Observable<ApiViewModel> observable) {
        if(observable.isLoaded()) {
            render(observable.get());
        } else {
            panel.setHTML(SafeHtmlUtils.htmlEscape(I18N.CONSTANTS.loading()));
        }
    }

    private void render(ApiViewModel apiViewModel) {
        panel.setHTML(TEMPLATES.panel(TableBundle.INSTANCE.style(), apiViewModel));
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
