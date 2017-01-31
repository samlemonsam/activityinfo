package org.activityinfo.ui.client.page.entry.location;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.result.LocationResult;
import org.activityinfo.ui.client.page.entry.form.resources.SiteFormResources;

public class SearchStatusView extends Html {

    public SearchStatusView(LocationSearchPresenter presenter) {
        addStyleName(SiteFormResources.INSTANCE.style().locationDialogHelp());
        presenter.getStore().getLoader().addLoadListener(new LoadListener() {

            @Override
            public void loaderBeforeLoad(LoadEvent le) {
                setHtml(SafeHtmlUtils.EMPTY_SAFE_HTML);
                addStyleName(SiteFormResources.INSTANCE.style().locationDialogHelp());
            }

            @Override
            public void loaderLoadException(LoadEvent le) {
                Log.error("Search load exception", le.exception);
                setText(I18N.CONSTANTS.connectionProblem());
            }

            @Override
            public void loaderLoad(LoadEvent le) {
                LocationResult data = le.getData();
                if (data.getTotalLength() == 0) {
                    setText(I18N.CONSTANTS.locationSearchNoResults());
                } else if (data.getTotalLength() > data.getData().size()) {
                    SafeHtmlBuilder html = new SafeHtmlBuilder();
                    html.appendHtmlConstant(I18N.MESSAGES.matchingLocations(data.getTotalLength()));
                    html.appendHtmlConstant("<br>");
                    html.appendHtmlConstant(I18N.CONSTANTS.tooManyLocationsToDisplay());
                    setHtml(html.toSafeHtml());
                }
            }
        });
    }

}
