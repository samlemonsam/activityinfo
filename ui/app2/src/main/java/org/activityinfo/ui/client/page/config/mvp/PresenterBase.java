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

import org.activityinfo.legacy.shared.model.DTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;

/*
 * Base class to reduce code in presenter classes and to provide a template
 */
@Deprecated
public class PresenterBase<V extends View<M>, M extends DTO> implements Presenter<V, M> {

    protected final Dispatcher service;
    protected final V view;

    public PresenterBase(Dispatcher service, V view) {
        this.service = service;
        this.view = view;

        addListeners();
    }

    /*
     * Adds all the relevant listeners from the view to the presenter
     */
    protected void addListeners() {

    }
}
