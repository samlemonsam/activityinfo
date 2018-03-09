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
import org.activityinfo.ui.client.dispatch.AsyncMonitor;

/*
 * The view always has a primary domain object to display. The view receives calls 
 * from the Presenter (the Presenter having an instance of the View), and the View 
 * throws events the Presenter subscribes to. The View does not have an instance of
 * the Presenter. The View only has 'dumb' methods: the Presenter acts as a proxy 
 * between the model and the view.
 */
@Deprecated
public interface View<M> extends TakesValue<M>, IsWidget {
    /*
     * Presenters have an async process of fetching data. Only after the data
     * has been fetched successfully and the data set on the View, the Presenter
     * calls this method. This is foremost important for data which is listed,
     * e.g. the choices of a combobox. The constructor can construct the UI,
     * when the presenter has this data fetched, it can be set on the view.
     * After this, the initialize method can be called.
     */
    public void initialize();

    /*
     * The UI displaying loading, network status (retry/error/complete) Usually
     * some standard Async monitor UI view, such as NullAsyncMonitor,
     * MaskingAsyncMonitor etc
     */
    public AsyncMonitor getLoadingMonitor();

}
