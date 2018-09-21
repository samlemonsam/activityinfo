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
package org.activityinfo.ui.client.page.common;

import com.extjs.gxt.ui.client.data.ModelData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;

import java.util.List;

/**
 * Renders gallery of icons with descriptions
 */
public class GalleryRenderer<T extends ModelData> implements SafeHtmlRenderer<List<T>> {


    private String iconPath;

    public GalleryRenderer(String iconPath) {
        this.iconPath = iconPath;
    }

    interface Templates extends SafeHtmlTemplates {

        @Template("<dd><img src=\"{0}\" title=\"{1}\"><div><h4>{1}</h4><p>{2}</p></div></dd>")
        SafeHtml item(SafeUri iconUri, String name, String description);
    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);


    @Override
    public SafeHtml render(List<T> models) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        render(models, builder);
        return builder.toSafeHtml();
    }

    @Override
    public void render(List<T> models, SafeHtmlBuilder html) {
        html.appendHtmlConstant("<dl>");

        for (T model : models) {
            SafeUri iconPath = UriUtils.fromTrustedString(GWT.getModuleBaseForStaticFiles() + this.iconPath + model.get("path"));
            String name = model.get("name");
            String description = model.get("description");

            html.append(TEMPLATES.item(iconPath, name, description));
        }

        html.appendHtmlConstant("<div style=\"clear:left;\"></div>");
        html.appendHtmlConstant("</dl>");
    }
}
