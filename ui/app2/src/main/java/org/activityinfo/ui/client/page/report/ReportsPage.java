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
package org.activityinfo.ui.client.page.report;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;

/**
 * Page which presents the list of reports visible to the user
 *
 * @author Alex Bertram
 */
public class ReportsPage extends LayoutContainer implements Page {
    public static final PageId PAGE_ID = new PageId("reports");

    @Inject
    public ReportsPage(EventBus eventBus, Dispatcher dispatcher, ResourceLocator locator) {

        setLayout(new BorderLayout());

        ContentPanel betaLinkPanel = new ContentPanel();
        betaLinkPanel.setBodyStyle("font-family: sans-serif;");
        betaLinkPanel.setHeaderVisible(false);
        betaLinkPanel.setLayout(new CenterLayout());
        Anchor betaLink = new Anchor(I18N.CONSTANTS.tryNewReportingInterface());
        betaLink.addClickHandler(clickEvent -> Window.open("/app?ui=3#analysis", "_blank", null));
        betaLinkPanel.add(betaLink);
        add(betaLinkPanel, new BorderLayoutData(LayoutRegion.NORTH, 32));

        BorderLayoutData newLayout = new BorderLayoutData(LayoutRegion.EAST);
        newLayout.setSize(0.40f);
        newLayout.setMargins(new Margins(0, 5, 0, 0));
        newLayout.setSplit(true);
        newLayout.setCollapsible(true);
        add(new NewReportPanel(eventBus, dispatcher, locator), newLayout);

        add(new ReportGridPanel(eventBus, dispatcher), new BorderLayoutData(LayoutRegion.CENTER));

    }

    @Override
    public void shutdown() {
        //
    }

    @Override
    public PageId getPageId() {
        return PAGE_ID;
    }

    @Override
    public Object getWidget() {
        return this;
    }

    @Override
    public void requestToNavigateAway(PageState place, NavigationCallback callback) {
        callback.onDecided(true);
    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    @Override
    public boolean navigate(PageState place) {
        return true;
    }

}
