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
package org.activityinfo.ui.client.component.filter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.ImageResource;

import java.util.Date;
import java.util.List;

public interface FilterResources extends ClientBundle {

    static final FilterResources INSTANCE = GWT.create(FilterResources.class);
    static final FilterMessages MESSAGES = GWT.create(FilterMessages.class);

    @Source("Filter.css") @NotStrict Style style();

    @Source("Filter.png") ImageResource filterIcon();

    @Source("CrossCircle.png") ImageResource crossIcon();

    interface Style extends CssResource {

    }

    interface FilterMessages extends Messages {

        @DefaultMessage("Before {0,date,medium}") String beforeDate(Date date);

        @DefaultMessage("After {0,date,medium}") String afterDate(Date date);

        @DefaultMessage("{0,date,medium} - {1,date,medium}") String betweenDates(Date date1, Date date2);

        @DefaultMessage("{0,list}") String filteredPartnerList(List<String> names);

        @DefaultMessage("{0,list}") String filteredAttributeList(List<String> names);
    }
}
