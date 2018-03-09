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
package org.activityinfo.ui.icons;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;

public interface IconClientBundle extends ClientBundle {
    
    public static IconClientBundle INSTANCE = GWT.create(IconClientBundle.class);
    
    
    @Source("icons.ttf")
    @DataResource.MimeType("application/x-font-ttf")
    DataResource trueTypeFont();

    @Source("icons.eot")
    @DataResource.MimeType("application/vnd.ms-fontobject")
    DataResource embeddedOpenTypeFont();

    @Source("icons.woff")
    @DataResource.MimeType("application/font-woff")
    DataResource openWebFont();

    @Source("icons.css")
    @CssResource.NotStrict
    CssResource iconStyle();
    
}