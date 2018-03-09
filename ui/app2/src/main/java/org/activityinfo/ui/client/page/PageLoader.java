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
package org.activityinfo.ui.client.page;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Component responsible for
 */
public interface PageLoader {

    /**
     * Loads the Page for the given pageId, potentially asynchronously.
     *
     * @param pageId    The ID of the page for which to load the presenter
     * @param pageState A PageState object describing the requested state of the Page
     *                  at load
     * @param callback  Note: PageLoaders are difficult to test so minimize the logic
     *                  within this class.
     */
    public void load(PageId pageId, PageState pageState,
                     AsyncCallback<Page> callback);

}
