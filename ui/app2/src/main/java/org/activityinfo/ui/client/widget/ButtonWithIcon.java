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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiConstructor;
import org.activityinfo.ui.client.style.ElementStyle;

/**
 * Button with an icon
 */
public class ButtonWithIcon extends Button {


    public interface Templates extends SafeHtmlTemplates {
        @Template("<span class=\"{0}\"></span> {1}")
        SafeHtml withIcon(String styleNames, String text);

        @Template("<span class=\"{0}\"></span>")
        SafeHtml withIcon(String styleNames);
    }

    public static Templates TEMPLATES = GWT.create(Templates.class);

    @UiConstructor
    public ButtonWithIcon(ElementStyle style, String iconStyle, String text) {
        super(style);
        setHTML(TEMPLATES.withIcon(iconStyle, text));
    }
}
