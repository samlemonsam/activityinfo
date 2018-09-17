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

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.*;
import org.activityinfo.ui.client.page.common.GalleryPage;
import org.activityinfo.ui.client.page.config.design.DbEditor;
import org.activityinfo.ui.client.page.config.link.IndicatorLinkPage;
import org.activityinfo.ui.client.page.config.link.IndicatorLinkPlace;

import java.util.Map;

public class ConfigLoader implements PageLoader {

    private final EventBus eventBus;
    private final Dispatcher dispatch;
    private Map<PageId, Provider<? extends Page>> pageProviders = Maps.newHashMap();
    private NavigationHandler navigationHandler;
    private StateProvider stateMgr;
    private ResourceLocator resourceLocator;

    @Inject
    public ConfigLoader(EventBus eventBus,
                        Dispatcher dispatcher,
                        NavigationHandler navigationHandler,
                        StateProvider stateMgr,
                        PageStateSerializer placeSerializer,
                        ResourceLocator resourceLocator) {
        this.eventBus = eventBus;
        this.dispatch = dispatcher;
        this.navigationHandler = navigationHandler;
        this.stateMgr = stateMgr;
        this.resourceLocator = resourceLocator;

        register(ConfigFrameSet.PAGE_ID);
        register(DbConfigPresenter.PAGE_ID);
        register(DbListPresenter.PAGE_ID);
        register(DbUserEditor.PAGE_ID);
        register(DbPartnerEditor.PAGE_ID);
        register(DbProjectEditor.PAGE_ID);
        register(LockedPeriodsPresenter.PAGE_ID);
        register(DbEditor.PAGE_ID);
        register(DbTargetEditor.PAGE_ID);
        register(IndicatorLinkPage.PAGE_ID);

        placeSerializer.registerStatelessPlace(DbListPresenter.PAGE_ID, new DbListPageState());
        placeSerializer.registerParser(DbConfigPresenter.PAGE_ID, new DbPageState.Parser(DbConfigPresenter.PAGE_ID));
        placeSerializer.registerParser(DbUserEditor.PAGE_ID, new DbPageState.Parser(DbUserEditor.PAGE_ID));
        placeSerializer.registerParser(DbPartnerEditor.PAGE_ID, new DbPageState.Parser(DbPartnerEditor.PAGE_ID));
        placeSerializer.registerParser(DbProjectEditor.PAGE_ID, new DbPageState.Parser(DbProjectEditor.PAGE_ID));
        placeSerializer.registerParser(LockedPeriodsPresenter.PAGE_ID,
                new DbPageState.Parser(LockedPeriodsPresenter.PAGE_ID));
        placeSerializer.registerParser(DbEditor.PAGE_ID, new DbPageState.Parser(DbEditor.PAGE_ID));
        placeSerializer.registerParser(DbTargetEditor.PAGE_ID, new DbPageState.Parser(DbTargetEditor.PAGE_ID));
        placeSerializer.registerStatelessPlace(IndicatorLinkPage.PAGE_ID, new IndicatorLinkPlace());
    }

    private void register(PageId pageId) {
        navigationHandler.registerPageLoader(pageId, this);
    }

    @Override
    public void load(final PageId pageId, final PageState place, final AsyncCallback<Page> callback) {
        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess() {

                final Page page = createPage(pageId);

                if (page == null) {
                    callback.onFailure(new Exception("ConfigLoader didn't know how to handle " + place.toString()));
                } else if (page instanceof DbPage) {
                    dispatch.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            callback.onFailure(caught);
                        }

                        @Override
                        public void onSuccess(SchemaDTO result) {
                            DbPageState dbPlace = (DbPageState) place;
                            ((DbPage) page).go(result.getDatabaseById(dbPlace.getDatabaseId()));
                            callback.onSuccess(page);
                        }

                    });
                } else {
                    page.navigate(place);
                    callback.onSuccess(page);
                }
            }
        });

    }

    private Page createPage(PageId pageId) {
        if(pageId.equals(ConfigFrameSet.PAGE_ID)) {
            return new ConfigFrameSet(eventBus, dispatch);

        } else if(pageId.equals(DbConfigPresenter.PAGE_ID)) {
            return new DbConfigPresenter(new GalleryPage(eventBus), dispatch);

        } else if(pageId.equals(DbListPresenter.PAGE_ID)) {
            return new DbListPage(eventBus, dispatch, stateMgr);

        } else if(pageId.equals(DbUserEditor.PAGE_ID)) {
            return new DbUserEditor(eventBus, dispatch, stateMgr);

        } else if(pageId.equals(DbPartnerEditor.PAGE_ID)) {
            return new DbPartnerEditor(eventBus, dispatch);

        } else if(pageId.equals(DbProjectEditor.PAGE_ID)) {
            return new DbProjectEditor(eventBus, dispatch, stateMgr, new DbProjectGrid());

        } else if(pageId.equals(DbEditor.PAGE_ID)) {
            return new DbEditor(eventBus, dispatch, resourceLocator, stateMgr);

        } else if(pageId.equals(DbTargetEditor.PAGE_ID)) {
            return new DbTargetEditor(eventBus, dispatch, stateMgr, new DbTargetGrid());

        } else if(pageId.equals(LockedPeriodsPresenter.PAGE_ID)) {
            return new LockedPeriodsPresenter(dispatch, eventBus, new LockedPeriodGrid());

        } else if(pageId.equals(IndicatorLinkPage.PAGE_ID)) {
            return new IndicatorLinkPage(dispatch);
        } else {
            return null;
        }
    }
}
