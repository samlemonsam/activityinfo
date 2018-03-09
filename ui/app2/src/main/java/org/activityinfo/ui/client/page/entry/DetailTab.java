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
package org.activityinfo.ui.client.page.entry;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.SiteRenderer;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.type.IndicatorNumberFormat;
import org.activityinfo.ui.client.page.common.ApplicationBundle;

public class DetailTab extends TabItem {

    private final Html content;
    private final Dispatcher dispatcher;

    public DetailTab(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        setText(I18N.CONSTANTS.details());

        this.setScrollMode(Scroll.AUTO);

        content = new Html();
        content.setStyleName(ApplicationBundle.INSTANCE.styles().details());
        add(content);

    }

    public void setSite(final SiteDTO site) {
        content.setText(I18N.CONSTANTS.loading());
        dispatcher.execute(new GetActivityForm(site.getActivityId()), new AsyncCallback<ActivityFormDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(ActivityFormDTO result) {
                render(result, site);

            }
        });
    }

    private void render(ActivityFormDTO form, SiteDTO site) {
        SiteRenderer renderer = new SiteRenderer(new IndicatorNumberFormat());
        content.setHtml(SafeHtmlUtils.fromTrustedString(renderer.renderSite(site, form, true)));
    }
}
