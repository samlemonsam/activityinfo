package org.activityinfo.ui.client.page.config.link;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DatabaseGridPanel extends ContentPanel {

    interface Templates extends SafeHtmlTemplates {

        @Template("<span style=\"color: gray\">{0}</span>")
        SafeHtml grayedOut(String databaseName);

    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    private final Dispatcher dispatcher;
    private Grid<UserDatabaseDTO> grid;
    private Set<Integer> linked = Collections.emptySet();

    public DatabaseGridPanel(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;

        ListLoader<ListLoadResult<UserDatabaseDTO>> loader = new BaseListLoader<ListLoadResult<UserDatabaseDTO>>(new
                DatabaseProxy());
        ListStore<UserDatabaseDTO> store = new ListStore<UserDatabaseDTO>(loader);
        grid = new Grid(store, createColumnModel());
        grid.setView(new HighlightingGridView());
        grid.setAutoExpandColumn("name");
        grid.setHideHeaders(true);

        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        setLayout(new FitLayout());
        add(grid);

        loader.load();

        IndicatorLinkResources.INSTANCE.style().ensureInjected();

    }

    public void addMouseOverListener(Listener<GridEvent<UserDatabaseDTO>> listener) {
        grid.addListener(HighlightingGridView.ROW_MOUSE_OVER, listener);
    }

    public void addSelectionChangeListener(SelectionChangedListener<UserDatabaseDTO> listener) {
        grid.getSelectionModel().addSelectionChangedListener(listener);
    }

    public HighlightingGridView getGridView() {
        return (HighlightingGridView) grid.getView();
    }

    public void setLinked(Set<Integer> linked) {
        this.linked = linked;
        if (grid.isRendered()) {
            getGridView().refreshAllRows();
        }
    }

    public UserDatabaseDTO getSelectedItem() {
        return grid.getSelectionModel().getSelectedItem();
    }

    private ColumnModel createColumnModel() {
        ColumnConfig icon = new ColumnConfig("icon", 28);
        icon.setSortable(false);
        icon.setMenuDisabled(true);
        icon.setRenderer(new GridCellRenderer<UserDatabaseDTO>() {

            @Override
            public SafeHtml render(UserDatabaseDTO model,
                                   String property,
                                   ColumnData config,
                                   int rowIndex,
                                   int colIndex,
                                   ListStore<UserDatabaseDTO> store,
                                   Grid<UserDatabaseDTO> grid) {

                return linked.contains(model.getId()) ?
                        IconImageBundle.ICONS.link().getSafeHtml() :
                        SafeHtmlUtils.EMPTY_SAFE_HTML;
            }
        });

        ColumnConfig name = new ColumnConfig("name", I18N.CONSTANTS.database(), 150);
        name.setSortable(false);
        name.setMenuDisabled(true);
        name.setRenderer(new GridCellRenderer<UserDatabaseDTO>() {

            @Override
            public SafeHtml render(UserDatabaseDTO model,
                                 String property,
                                 ColumnData config,
                                 int rowIndex,
                                 int colIndex,
                                 ListStore<UserDatabaseDTO> store,
                                 Grid<UserDatabaseDTO> grid) {

                if (hasPermission(model)) {
                    return SafeHtmlUtils.fromString(model.getName());
                } else {
                    return TEMPLATES.grayedOut(model.getName());
                }
            }
        });

        return new ColumnModel(Arrays.asList(icon, name));

    }

    protected boolean hasPermission(UserDatabaseDTO model) {
        return true;
    }

    private class DatabaseProxy extends RpcProxy<List<UserDatabaseDTO>> {

        @Override
        protected void load(Object loadConfig, final AsyncCallback<List<UserDatabaseDTO>> callback) {

            dispatcher.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(SchemaDTO result) {
                    callback.onSuccess(result.getDatabases());
                }
            });

        }

    }

}
