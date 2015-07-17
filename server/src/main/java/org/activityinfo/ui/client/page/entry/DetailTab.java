package org.activityinfo.ui.client.page.entry;

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

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.type.IndicatorNumberFormat;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.ActivityFormDTO;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.ui.client.page.entry.form.SiteRenderer;

public class DetailTab extends TabItem {

    private final Html content;
    private final Dispatcher dispatcher;

    public DetailTab(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        setText(I18N.CONSTANTS.details());

        this.setScrollMode(Scroll.AUTO);

        content = new Html();
        content.setStyleName("details");
        add(content);

    }

    public void setSite(final SiteDTO site) {
        content.setHtml(I18N.CONSTANTS.loading());
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
        content.setHtml(renderer.renderSite(site, form, true));
    }
}
