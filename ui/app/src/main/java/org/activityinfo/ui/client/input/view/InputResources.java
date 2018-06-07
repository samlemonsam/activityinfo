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
package org.activityinfo.ui.client.input.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.sencha.gxt.themebuilder.base.client.config.ThemeDetails;

/**
 * Styles and resources for form input.
 */
public interface InputResources extends ClientBundle {

    InputResources INSTANCE = GWT.create(InputResources.class);

    @Source("Input.gss")
    Style style();

    ThemeDetails theme();

    interface Style extends CssResource {

        String field();

        String fieldInvalid();

        String fieldLabel();

        String fieldDescription();

        String form();

        String subform();

        String periodToolBar();

        String fieldUnits();

        String lockIcon();

        String validationMessage();

    }
}
