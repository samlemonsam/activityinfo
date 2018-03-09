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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.resource.ResourcePage;
import org.activityinfo.ui.client.page.resource.ResourcePlace;
import org.activityinfo.ui.client.style.BaseStylesheet;

public class FormPageLoader implements PageLoader {

    private final NavigationHandler pageManager;
    private ResourceLocator resourceLocator;
    private final EventBus eventBus;
    private final StateProvider stateProvider;


    @Inject
    public FormPageLoader(NavigationHandler pageManager,
                          PageStateSerializer placeSerializer,
                          ResourceLocator resourceLocator,
                          EventBus eventBus, StateProvider stateProvider
    ) {

        this.resourceLocator = resourceLocator;
        this.pageManager = pageManager;
        this.eventBus = eventBus;
        this.stateProvider = stateProvider;

        pageManager.registerPageLoader(ResourcePage.DESIGN_PAGE_ID, this);
        placeSerializer.registerParser(ResourcePage.DESIGN_PAGE_ID, new ResourcePlace.Parser(ResourcePage.DESIGN_PAGE_ID));

        pageManager.registerPageLoader(ResourcePage.TABLE_PAGE_ID, this);
        placeSerializer.registerParser(ResourcePage.TABLE_PAGE_ID, new ResourcePlace.Parser(ResourcePage.TABLE_PAGE_ID));
    }

    @Override
    public void load(final PageId pageId, final PageState pageState, final AsyncCallback<Page> callback) {

        BaseStylesheet.INSTANCE.ensureInjected();

        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onSuccess() {
                if (pageState instanceof ResourcePlace) {
                    ResourcePlace resourcePlace = (ResourcePlace) pageState;
                    ResourcePage page = new ResourcePage(eventBus, resourceLocator, resourcePlace.getPageId(), stateProvider);
                    page.navigate(pageState);
                    callback.onSuccess(page);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }
}
