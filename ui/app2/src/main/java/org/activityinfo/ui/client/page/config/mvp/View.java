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
package org.activityinfo.ui.client.page.config.mvp;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.IsWidget;

/*
 * The view always has a primary domain object to display. The view receives calls 
 * from the Presenter (the Presenter having an instance of the View), and the View 
 * throws events the Presenter subscribes to. The View does not have an instance of
 * the Presenter. The View only has 'dumb' methods: the Presenter acts as a proxy 
 * between the model and the view.
 */
@Deprecated
public interface View<M> extends TakesValue<M>, IsWidget {

}
