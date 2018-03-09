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
package org.activityinfo.ui.client.page.config;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.page.*;
import org.activityinfo.ui.client.page.common.nav.NavigationPanel;
import org.activityinfo.ui.client.widget.legacy.LoadingPlaceHolder;

/**
 * Standard 2-column split frame with a left-hand pane for navigation and the
 * center component for page navigation.
 */
public class VSplitFrameSet implements Frame {

    private final LayoutContainer container;
    private Page activePage;
    private Widget activeWidget;
    private NavigationPanel navPanel;
    private PageId pageId;

    public VSplitFrameSet(PageId pageId, NavigationPanel navPanel) {
        this.pageId = pageId;
        this.container = new LayoutContainer();
        this.navPanel = navPanel;
        container.setLayout(new BorderLayout());

        addNavigationPanel();
    }

    @Override
    public void shutdown() {
        getNavPanel().shutdown();
    }

    private void addNavigationPanel() {

        BorderLayoutData layoutData = new BorderLayoutData(Style.LayoutRegion.WEST);
        layoutData.setSplit(true);
        layoutData.setCollapsible(true);
        layoutData.setMargins(new Margins(0, 5, 0, 0));

        container.add(getNavPanel(), layoutData);
    }

    @Override
    public Widget getWidget() {
        return container;
    }

    @Override
    public Page getActivePage() {
        return activePage;
    }

    private void setWidget(Widget widget) {

        if (activeWidget != null) {
            container.remove(activeWidget);
        }

        container.add(widget, new BorderLayoutData(Style.LayoutRegion.CENTER));
        activeWidget = widget;

        if (container.isRendered()) {
            container.layout();
        }
    }

    @Override
    public AsyncMonitor showLoadingPlaceHolder(PageId page, PageState loadingPlace) {

        LoadingPlaceHolder placeHolder = new LoadingPlaceHolder();
        setWidget(placeHolder);
        activePage = null;
        return placeHolder;
    }

    @Override
    public void setActivePage(Page page) {

        setWidget((Widget) page.getWidget());
        activePage = page;
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        if (activePage == null) {
            callback.onDecided(true);
        } else {
            activePage.requestToNavigateAway(place, callback);
        }
    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    @Override
    public boolean navigate(PageState place) {
        return true;
    }

    @Override
    public PageId getPageId() {
        return pageId;
    }

    public void setNavPanel(NavigationPanel navPanel) {
        this.navPanel = navPanel;
    }

    public NavigationPanel getNavPanel() {
        return navPanel;
    }

}
