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
package org.activityinfo.ui.client.page.entry.form.field;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

/**
 *  Wraps an existing SafeHtmlRenderer in span that allows line wrapping
 */
public class MultilineRenderer<T> implements SafeHtmlRenderer<T> {

    private SafeHtmlRenderer<T> renderer;

    public MultilineRenderer(SafeHtmlRenderer<T> renderer) {
        this.renderer = renderer;
    }

    @Override
    public SafeHtml render(T item) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        render(item, builder);
        return builder.toSafeHtml();
    }

    @Override
    public void render(T item, SafeHtmlBuilder builder) {
        builder.appendHtmlConstant("<span style='white-space: pre-wrap; white-space:-moz-pre-wrap; white-space:-pre-wrap;white-space:-o-pre-wrap;white-space:break-word'>");
        renderer.render(item, builder);
        builder.appendHtmlConstant("</span>");
    }
}
