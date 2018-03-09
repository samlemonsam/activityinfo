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

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.i18n.shared.UiConstants;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.page.common.dialog.FormDialogCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogImpl;
import org.activityinfo.ui.client.page.common.dialog.FormDialogTether;
import org.activityinfo.ui.client.page.common.grid.AbstractGridView;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.page.config.design.BlankValidator;
import org.activityinfo.ui.client.page.config.design.CompositeValidator;
import org.activityinfo.ui.client.page.config.design.UniqueNameValidator;
import org.activityinfo.ui.client.page.config.form.ProjectForm;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;

public class DbProjectGrid extends AbstractGridView<ProjectDTO, DbProjectEditor> implements DbProjectEditor.View {
    private final UiConstants messages;
    private final IconImageBundle icons;

    private Grid<ProjectDTO> grid;

    @Inject
    public DbProjectGrid(UiConstants messages, IconImageBundle icons) {
        this.messages = messages;
        this.icons = icons;
    }

    @Override
    protected Grid<ProjectDTO> createGridAndAddToContainer(Store store) {
        grid = new Grid<ProjectDTO>((ListStore) store, createColumnModel());
        grid.setAutoExpandColumn("description");
        grid.setLoadMask(true);

        setLayout(new FitLayout());
        add(grid);

        return grid;
    }

    protected ColumnModel createColumnModel() {
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        columns.add(new ColumnConfig("name", messages.name(), 150));
        columns.add(new ColumnConfig("description", messages.description(), 300));

        return new ColumnModel(columns);
    }

    @Override
    protected void initToolBar() {
        toolBar.addButton(UIActions.ADD, I18N.CONSTANTS.addProject(), icons.add());
        toolBar.addButton(UIActions.EDIT, messages.edit(), icons.edit());
        toolBar.addButton(UIActions.DELETE, messages.delete(), icons.delete());
    }

    @Override
    public void init(DbProjectEditor editor, UserDatabaseDTO db, ListStore<ProjectDTO> store) {
        super.init(editor, store);
        this.setHeadingText(I18N.MESSAGES.projectsForDatabase(db.getName()));
    }

    @Override
    public FormDialogTether showAddDialog(ProjectDTO project, FormDialogCallback callback) {

        ProjectForm form = new ProjectForm();
        form.getBinding().bind(project);
        form.getNameField().setValidator(
                new CompositeValidator(new BlankValidator(), new UniqueNameValidator(new UniqueNameValidator.UniqueNamesFunction(grid.getStore().getModels()))));
        FormDialogImpl<ProjectForm> dlg = new FormDialogImpl<ProjectForm>(form);
        dlg.setWidth(450);
        dlg.setHeight(300);
        dlg.setHeadingText(messages.createProject());

        dlg.show(callback);

        return dlg;
    }

}
