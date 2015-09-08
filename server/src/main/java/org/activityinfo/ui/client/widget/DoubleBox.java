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

import com.google.gwt.user.client.Window;

/**
 * @author yuriyz on 3/14/14.
 */
public class DoubleBox extends com.google.gwt.user.client.ui.DoubleBox {

    public DoubleBox() {
        setStyleName("form-control");

        // AI-1217 : use type=number for all browsers except Chrome. In Chrome all numbers (independent from locale)
        // are converted into number with dot as separator.
        //https://www.aeyoun.com/webdev/html5-input-number-localization.html
//        if (!isChrome()) {
//            getElement().setPropertyString("type", "number");
//        }
    }

    public boolean isChrome() {
        return Window.Navigator.getUserAgent().toLowerCase().contains("chrome");
    }
}
