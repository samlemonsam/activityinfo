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

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.DimensionType;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.callback.SuccessCallback;
import org.activityinfo.ui.client.page.entry.column.ColumnModelProvider;
import org.activityinfo.ui.client.page.entry.column.DefaultColumnModelProvider;
import org.activityinfo.ui.client.page.entry.grouping.GroupingModel;
import org.activityinfo.ui.client.page.entry.grouping.NullGroupingModel;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;
import org.activityinfo.ui.client.widget.legacy.LoadingPlaceHolder;

/**
 * The SiteGridPanel contains the main toolbar for the Site Grid display, and
 * switches between the {@link FlatSiteGridPanel} and the tree grids.
 * <p/>
 * <p/>
 * Note that this class is FINAL. It should not be subclassed. The grid can be
 * customized by providing a different {@code columnModelProvider}, and
 * containers can install their own toolbar by calling
 * {@code setTopComponent() }
 */
public final class SiteGridPanel extends ContentPanel {

    private final Dispatcher dispatcher;
    private final ColumnModelProvider columnModelProvider;

    private SiteGridPanelView grid = null;

    public SiteGridPanel(Dispatcher dispatcher, ColumnModelProvider columnModelProvider) {
        this.dispatcher = dispatcher;
        this.columnModelProvider = columnModelProvider;

        setHeadingText(I18N.CONSTANTS.sitesHeader());
        setIcon(IconImageBundle.ICONS.table());
        setLayout(new FitLayout());
    }

    public SiteGridPanel(Dispatcher dispatcher) {
        this(dispatcher, new DefaultColumnModelProvider(dispatcher));
    }

    public void load(final GroupingModel grouping, final Filter filter) {

        removeAll();
        add(new LoadingPlaceHolder());
        layout();
        columnModelProvider.fetchColumnModels(filter, grouping, new SuccessCallback<ColumnModel>() {
            @Override
            public void onSuccess(ColumnModel columnModel) {
                createGrid(grouping, filter, columnModel);
            }
        });
        updateHeading(filter);
    }

    private void updateHeading(final Filter filter) {
        setHeadingText(I18N.CONSTANTS.sitesHeader());

        dispatcher.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                //
            }

            @Override
            public void onSuccess(SchemaDTO result) {
                if (filter.isDimensionRestrictedToSingleCategory(DimensionType.Activity)) {
                    int activityId = filter.getRestrictedCategory(DimensionType.Activity);
                    ActivityDTO activity = result.getActivityById(activityId);
                    setHeadingText(activity.getDatabaseName() + " - " + activity.getName());
                } else if (filter.isDimensionRestrictedToSingleCategory(DimensionType.Database)) {
                    int databaseId = filter.getRestrictedCategory(DimensionType.Database);
                    UserDatabaseDTO db = result.getDatabaseById(databaseId);
                    setHeadingText(db.getName());
                }
            }
        });

    }

    public void refresh() {
        if (grid != null) {
            grid.refresh();
        }
    }

    protected void createGrid(GroupingModel grouping, Filter filter, ColumnModel columnModel) {

        if (grouping != NullGroupingModel.INSTANCE) {
            installTreeGrid(grouping, filter, columnModel);
            return;
        }

        if (!filter.isDimensionRestrictedToSingleCategory(DimensionType.Activity)) {
            installFlatSiteGridPanel(filter, columnModel);
            return;
        }

        int activityId = filter.getRestrictedCategory(DimensionType.Activity);
        dispatcher.execute(new GetActivityForm(activityId), new AsyncCallback<ActivityFormDTO>() {

            @Override
            public void onFailure(Throwable caught) {
                installFlatSiteGridPanel(filter, columnModel);
            }

            @Override
            public void onSuccess(ActivityFormDTO result) {
                if (result.getClassicView()) {
                    installFlatSiteGridPanel(filter, columnModel);
                } else {
                    installTableViewLinkPanel(result);
                }
            }
        });
    }

    private void installTreeGrid(GroupingModel grouping, Filter filter, ColumnModel columnModel) {
        SiteTreeGrid treeGrid = new SiteTreeGrid(dispatcher, grouping, filter, columnModel);
        installGrid(treeGrid);
    }

    private void installFlatSiteGridPanel(Filter filter, ColumnModel columnModel) {
        FlatSiteGridPanel panel = new FlatSiteGridPanel(dispatcher);
        panel.initGrid(filter, columnModel);
        installGrid(panel);
    }

    private void installTableViewLinkPanel(ActivityFormDTO form) {
        TableViewLinkPanel panel = new TableViewLinkPanel(form);
        installGrid(panel);
    }

    public void addSelectionChangedListener(SelectionChangedListener<SiteDTO> listener) {
        addListener(Events.SelectionChange, listener);
    }
    public void addRowDoubleClickListener(SelectionChangedListener<SiteDTO> listener) {
        addListener(Events.RowDoubleClick, listener);
    }
    
    private void installGrid(SiteGridPanelView grid) {
        this.grid = grid;

        removeAll();
        add(grid.asComponent());
        layout();

        grid.addSelectionChangeListener(new SelectionChangedListener<SiteDTO>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<SiteDTO> se) {
                fireEvent(Events.SelectionChange, se);
            }
        });
        grid.addDoubleClickListener(new SelectionChangedListener<SiteDTO>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<SiteDTO> se) {
                fireEvent(Events.RowDoubleClick, se);
            }
        });
    }

    public SiteDTO getSelection() {
        if (grid == null) {
            return null;
        } else {
            return grid.getSelection();
        }
    }
}
