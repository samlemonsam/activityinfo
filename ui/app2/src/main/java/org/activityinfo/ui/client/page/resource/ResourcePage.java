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
package org.activityinfo.ui.client.page.resource;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.component.formdesigner.FormSavedGuard;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.pageView.formClass.DesignTab;
import org.activityinfo.ui.client.widget.LoadingPanel;
import org.activityinfo.ui.client.widget.loading.PageLoadingPanel;
import org.activityinfo.ui.icons.Icons;

/**
 * Adapter that hosts a view of a given instance.
 */
public class ResourcePage implements Page {

    public static final PageId DESIGN_PAGE_ID = new PageId("idesign");
    public static final PageId TABLE_PAGE_ID = new PageId("itable");

    // scrollpanel.bs > div.container > loadingPanel
    private final ScrollPanel scrollPanel;
    private final SimplePanel container;
    private final LoadingPanel<ResourceId> loadingPanel;

    private final PageId pageId;
    private EventBus eventBus;
    private final ResourceLocator locator;
    private final StateProvider stateProvider;

    public ResourcePage(EventBus eventBus, ResourceLocator resourceLocator, PageId pageId, StateProvider stateProvider) {
        this.eventBus = eventBus;
        this.locator = resourceLocator;
        this.pageId = pageId;
        this.stateProvider = stateProvider;

        Icons.INSTANCE.ensureInjected();

        this.loadingPanel = new LoadingPanel<>(new PageLoadingPanel());

        this.container = new SimplePanel(loadingPanel.asWidget());
        this.container.addStyleName("container");

        this.scrollPanel = new ScrollPanel(container);
        this.scrollPanel.addStyleName("bs");
    }

    @Override
    public PageId getPageId() {
        return pageId;
    }

    @Override
    public Object getWidget() {
        return scrollPanel;
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        if (!FormSavedGuard.callNavigationCallback(scrollPanel, callback)) {
            callback.onDecided(true);
        }
    }

    @Override
    public String beforeWindowCloses() {
        FormSavedGuard guard = FormSavedGuard.getGuard(scrollPanel);
        if (guard == null || guard.isSaved()) {
            return null;
        } else {
            return I18N.CONSTANTS.unsavedChangesWarning();
        }
    }

    @Override
    public boolean navigate(PageState place) {
        final ResourcePlace resourcePlace = (ResourcePlace) place;

        if (resourcePlace.getPageId() == ResourcePage.DESIGN_PAGE_ID) {
            loadingPanel.setDisplayWidget(new DesignTab(locator, stateProvider));
        } else {
            throw new UnsupportedOperationException("Unknown page id:" + resourcePlace.getPageId());
        }
        this.loadingPanel.show(new Provider<Promise<ResourceId>>() {
            @Override
            public Promise<ResourceId> get() {
                return Promise.resolved(resourcePlace.getInstanceId());
            }
        });
        return true;
    }

    @Override
    public void shutdown() {
    }
}
