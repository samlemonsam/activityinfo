package org.activityinfo.ui.client.widget;
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

import com.google.gwt.uibinder.client.UiConstructor;
import org.activityinfo.ui.client.style.ElementStyle;

/**
 * Subclass of {@link com.google.gwt.user.client.ui.Button} that applies our application styles
 *
 * @author yuriyz on 3/14/14.
 */
public class ButtonWithSize extends com.google.gwt.user.client.ui.Button {

    // bootstrap:  .btn-lg, .btn-sm, or .btn-xs
    public static enum Size {

        LARGE("lg"),
        SMALL("sm"),
        EXTRA_SMALL("xs");

        private final String className;

        Size(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }
    }

    @UiConstructor
    public ButtonWithSize(ElementStyle style, Size size) {
        String styleName = "btn btn-" + style.name().toLowerCase();
        if (size != null) {
            styleName = styleName + " btn-" + size.getClassName();
        }
        setStyleName(styleName);
    }
}