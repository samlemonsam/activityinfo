package org.activityinfo.ui.client.page.config;

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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.TargetDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.page.common.columns.TimePeriodColumn;
import org.activityinfo.ui.client.page.common.dialog.FormDialogCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogImpl;
import org.activityinfo.ui.client.page.common.dialog.FormDialogTether;
import org.activityinfo.ui.client.page.common.grid.AbstractGridView;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.page.config.form.TargetForm;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;

public class DbTargetGrid extends AbstractGridView<TargetDTO, DbTargetEditor> implements DbTargetEditor.View {

    private final UiConstants messages;
    private final IconImageBundle icons;

    private Grid<TargetDTO> grid;
    private ListStore<TargetDTO> store;
    private ContentPanel targetValueContainer;
    private final AsyncMonitor loadingMonitor = new MaskingAsyncMonitor(this, I18N.CONSTANTS.loading());
    private UserDatabaseDTO db;

    @Inject
    public DbTargetGrid(UiConstants messages, IconImageBundle icons) {
        this.messages = messages;
        this.icons = icons;
    }

    @Override
    protected Grid<TargetDTO> createGridAndAddToContainer(Store store) {
        this.store = (ListStore<TargetDTO>) store;

        grid = new Grid<TargetDTO>((ListStore) store, createColumnModel());
        grid.setAutoExpandColumn("name");
        grid.setLoadMask(true);

        setLayout(new BorderLayout());
        add(grid, new BorderLayoutData(Style.LayoutRegion.CENTER));

        return grid;
    }

    protected ColumnModel createColumnModel() {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        ColumnConfig projectColumn = new ColumnConfig("projectId", messages.project(), 150);
        projectColumn.setRenderer(new GridCellRenderer() {
            @Override
            public SafeHtml render(ModelData modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore, Grid grid) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                Integer id = modelData.get(s);
                if (id != null) {
                    ProjectDTO project = db.getProjectById(id);
                    if(project != null) {
                        sb.appendEscaped(project.getName());
                    }
                }
                return sb.toSafeHtml();
            }
        });

        ColumnConfig partnerColumn = new ColumnConfig("partnerId", messages.partner(), 150);
        partnerColumn.setRenderer(new GridCellRenderer() {
            @Override
            public SafeHtml render(ModelData modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore, Grid grid) {
                SafeHtmlBuilder sb = new SafeHtmlBuilder();
                Integer id = modelData.get(s);
                if (id != null) {
                    PartnerDTO partner = db.getPartnerById(id);
                    if(partner != null) {
                        sb.appendEscaped(partner.getName());
                    }
                }
                return sb.toSafeHtml();
            }
        });

        columns.add(new ColumnConfig("name", messages.name(), 150));
        columns.add(projectColumn);
        columns.add(partnerColumn);
        columns.add(new TimePeriodColumn("timePeriod", messages.timePeriod(), 300));

        return new ColumnModel(columns);
    }

    @Override
    protected void initToolBar() {
        toolBar.addButton(UIActions.ADD, I18N.CONSTANTS.add(), icons.add());
        toolBar.addButton(UIActions.DELETE, messages.delete(), icons.delete());
        toolBar.addButton(UIActions.EDIT, messages.edit(), icons.edit());
    }

    @Override
    public void init(DbTargetEditor editor, UserDatabaseDTO db, ListStore<TargetDTO> store) {
        super.init(editor, store);
        this.setHeadingText(I18N.MESSAGES.targetsForDatabase(db.getName()));
        this.db = db;
    }

    @Override
    public FormDialogTether showAddDialog(TargetDTO target, UserDatabaseDTO db, boolean editDialog, FormDialogCallback callback) {
        TargetForm form = new TargetForm(db);
        form.getBinding().setStore(store);
        form.getBinding().bind(store.getRecord(target).getModel());

        FormDialogImpl<TargetForm> dlg = new FormDialogImpl<TargetForm>(form);
        dlg.setWidth(450);
        dlg.setHeight(300);
        if(editDialog) {
            dlg.setHeadingText(messages.editTarget());
        }
        else {
            dlg.setHeadingText(messages.createTarget());
        }

        dlg.show(callback);

        return dlg;
    }

    @Override
    public void createTargetValueContainer(Widget w) {
        targetValueContainer = new ContentPanel();
        targetValueContainer.setHeaderVisible(false);
        targetValueContainer.setBorders(false);
        targetValueContainer.setFrame(false);
        targetValueContainer.setLayout(new FitLayout());

        BorderLayoutData layout = new BorderLayoutData(Style.LayoutRegion.SOUTH);
        layout.setSplit(true);
        layout.setCollapsible(true);
        layout.setSize(250);
        layout.setMargins(new Margins(5, 0, 0, 0));

        targetValueContainer.add(w);

        add(targetValueContainer, layout);

    }

    @Override
    public AsyncMonitor getLoadingMonitor() {
        return loadingMonitor;
    }

}
