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

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.Page;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.toolbar.ActionToolBar;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;

public class DbListPage extends ContentPanel implements DbListPresenter.View, Page {

    private Grid<UserDatabaseDTO> grid;
    private DbListPresenter presenter;
    private ActionToolBar toolBar;

    @Inject
    public DbListPage(EventBus eventBus, Dispatcher dispatcher, StateProvider stateMgr) {
        presenter = new DbListPresenter(eventBus, dispatcher, this);

        setLayout(new FitLayout());
        setHeadingText(I18N.CONSTANTS.databases());
        setIcon(IconImageBundle.ICONS.database());

        createGrid();
        createToolBar();

        presenter.onSelectionChanged(null);
    }

    private void createToolBar() {
        toolBar = new ActionToolBar();
        toolBar.addButton(UIActions.ADD, I18N.CONSTANTS.newDatabase(), IconImageBundle.ICONS.addDatabase());
        toolBar.addEditButton(IconImageBundle.ICONS.editDatabase());
        toolBar.addButton(UIActions.RENAME, I18N.CONSTANTS.renameDatabase(), IconImageBundle.ICONS.database());
        toolBar.addDeleteButton();
        toolBar.addTransferButton();
        toolBar.addCancelTransferButton();
        toolBar.setListener(presenter);
        this.setTopComponent(toolBar);
    }

    private void createGrid() {
        grid = new Grid<UserDatabaseDTO>(presenter.getStore(), createColumnModel());
        grid.setAutoExpandColumn("fullName");
        grid.setLoadMask(true);

        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
            @Override
            public void handleEvent(GridEvent be) {
                presenter.onUIAction(UIActions.EDIT);
            }
        });
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<UserDatabaseDTO>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<UserDatabaseDTO> se) {
                presenter.onSelectionChanged(se.getSelectedItem());
            }
        });

        add(grid);
    }

    private ColumnModel createColumnModel() {
        presenter.getStore().setStoreSorter(new StoreSorter<UserDatabaseDTO>() {
            @Override
            public int compare(Store<UserDatabaseDTO> store, UserDatabaseDTO m1, UserDatabaseDTO m2, String property) {
                if ("country".equals(property)) {
                    return comparator.compare(m1.getCountry().getName(), m2.getCountry().getName());
                }
                return super.compare(store, m1, m2, property);
            }
        });

        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
        columns.add(new ColumnConfig("name", I18N.CONSTANTS.name(), 100));
        columns.add(new ColumnConfig("fullName", I18N.CONSTANTS.fullName(), 150));
        columns.add(new ColumnConfig("ownerName", I18N.CONSTANTS.ownerName(), 150));

        columns.add(createCountryColumn());
        return new ColumnModel(columns);
    }

    private ColumnConfig createCountryColumn() {
        ColumnConfig countryColumn = new ColumnConfig("country", I18N.CONSTANTS.country(), 150);
        countryColumn.setRenderer(new GridCellRenderer<UserDatabaseDTO>() {
            @Override
            public SafeHtml render(UserDatabaseDTO model,
                                   String property,
                                   ColumnData config,
                                   int rowIndex,
                                   int colIndex,
                                   ListStore<UserDatabaseDTO> store,
                                   Grid<UserDatabaseDTO> grid) {
                return SafeHtmlUtils.fromString(model.getCountry().getName());
            }
        });
        return countryColumn;
    }

    @Override
    public void setActionEnabled(String id, boolean enabled) {
        toolBar.setActionEnabled(id, enabled);
    }

    @Override
    public AsyncMonitor getDeletingMonitor() {
        return new MaskingAsyncMonitor(this, I18N.CONSTANTS.deleting());
    }

    @Override
    public PageId getPageId() {
        return DbListPresenter.PAGE_ID;
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
        return false;
    }

    @Override
    public void shutdown() {

    }
}
