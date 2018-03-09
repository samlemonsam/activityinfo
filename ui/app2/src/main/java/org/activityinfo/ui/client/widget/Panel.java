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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.HeaderPanel;

/**
 * Subclasses {@link com.google.gwt.user.client.ui.HeaderPanel} to add
 * our application styles
 *
 * @see <a href="http://getbootstrap.com/components/#panels">Bootstrap Panels</a>
 */
public class Panel extends HeaderPanel {

    public enum Style {
        PRIMARY,
        SUCCESS,
        INFO,
        WARNING,
        DANGER
    }

    @UiConstructor
    public Panel(Style style) {

        addStyleName("panel panel-" + style.name().toLowerCase());

        DivElement header = getElement().getChild(0).cast();
        header.setClassName("panel-heading");

        DivElement footer = getElement().getChild(1).cast();
        footer.setClassName("panel-footer");

        DivElement body = getElement().getChild(2).cast();
        body.setClassName("panel-body");
    }
}
