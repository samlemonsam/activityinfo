package org.activityinfo.ui.client.page.dashboard.portlets;

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

import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.custom.Portlet;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import org.activityinfo.i18n.shared.I18N;

public class NewsPortlet extends Portlet {


    public interface Templates extends SafeHtmlTemplates {

        @Template("<h2>{0}</h2><p>{1}</p><p><a href=\"{2}\" target=\"_blank\">Read more...</a></p>")
        SafeHtml newsItem(String title, String excerpt, SafeUri uri);
    }

    public static final Templates TEMPLATES = GWT.create(Templates.class);


    private Html contents;

    public NewsPortlet() {

        setHeadingText(I18N.CONSTANTS.activityInfoNews());

        contents = new Html();
        add(contents);

        ModelType type = new ModelType();
        type.setRoot("posts");
        type.addField("title");
        type.addField("id");
        type.addField("date");
        type.addField("excerpt");
        type.addField("url");

        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, "/about/news.json");
        HttpProxy<String> proxy = new HttpProxy<>(requestBuilder);

        // need a loader, proxy, and reader
        JsonLoadResultReader<ListLoadResult<ModelData>> reader = new JsonLoadResultReader<ListLoadResult<ModelData>>(type);

        final BaseListLoader<ListLoadResult<ModelData>> loader = new BaseListLoader<ListLoadResult<ModelData>>(proxy, reader);

        loader.addLoadListener(new LoadListener() {

            @Override
            public void loaderLoad(LoadEvent le) {
                SafeHtmlBuilder html = new SafeHtmlBuilder();
                ListLoadResult<ModelData> result = le.getData();
                for (ModelData item : result.getData()) {
                    String title = item.get("title");
                    String excerpt = item.get("excerpt");
                    SafeUri uri = UriUtils.fromString((String)item.get("url"));

                    html.append(TEMPLATES.newsItem(title, excerpt, uri));
                }

                contents.setHtml(html.toSafeHtml());
            }

            @Override
            public void loaderBeforeLoad(LoadEvent le) {
                contents.setText(I18N.CONSTANTS.loading());
            }

            @Override
            public void loaderLoadException(LoadEvent le) {
                contents.setText(I18N.CONSTANTS.connectionProblem());
            }

        });

        loader.load();
    }

}
