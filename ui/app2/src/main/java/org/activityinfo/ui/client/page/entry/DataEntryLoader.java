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
package org.activityinfo.ui.client.page.entry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.ui.client.page.*;
import org.activityinfo.ui.client.page.entry.place.DataEntryPlaceParser;

public class DataEntryLoader implements PageLoader {
    private final Provider<DataEntryPage> dataEntryPageProvider;

    @Inject
    public DataEntryLoader(NavigationHandler pageManager,
                           PageStateSerializer placeSerializer,
                           Provider<DataEntryPage> dataEntryPageProvider) {

        this.dataEntryPageProvider = dataEntryPageProvider;

        pageManager.registerPageLoader(DataEntryPage.PAGE_ID, this);
        placeSerializer.registerParser(DataEntryPage.PAGE_ID, new DataEntryPlaceParser());
    }

    @Override
    public void load(final PageId pageId, final PageState pageState, final AsyncCallback<Page> callback) {

        DataEntryPage dataEntryPage = dataEntryPageProvider.get();
        dataEntryPage.navigate(pageState);
        callback.onSuccess(dataEntryPage);

    }

}
