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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface ApplicationBundle extends ClientBundle {

    public static final ApplicationBundle INSTANCE = GWT.create(ApplicationBundle.class);

    @Source("Application.css")
    Styles styles();

    public interface Styles extends CssResource {

        String over();

        @ClassName("loading-indicator")
        String loadingIndicator();

        String gallery();

        String indicatorTable();

        String indicatorHeading();

        String comments();

        String appTitle();

        String unmapped();

        @ClassName("cell-hover")
        String cellHover();

        String groupName();

        String mapped();

        String details();

        String indicatorQuantity();

        @ClassName("loading-placeholder")
        String loadingPlaceholder();

        String indicatorGroupHeading();

        String indicatorGroupChild();
    }
}
