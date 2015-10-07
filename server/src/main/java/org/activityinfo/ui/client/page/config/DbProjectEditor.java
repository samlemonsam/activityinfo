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
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.client.state.StateProvider;
import org.activityinfo.legacy.shared.command.AddProject;
import org.activityinfo.legacy.shared.command.RequestChange;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.exception.DuplicatePartnerException;
import org.activityinfo.legacy.shared.model.ProjectDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.AppEvents;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.dialog.FormDialogCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogImpl;
import org.activityinfo.ui.client.page.common.dialog.FormDialogTether;
import org.activityinfo.ui.client.page.common.grid.AbstractGridPresenter;
import org.activityinfo.ui.client.page.common.grid.GridView;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.page.config.form.ProjectForm;

import java.util.ArrayList;

/*
 * Displays a grid where users can add, remove and change projects
 */
public class DbProjectEditor extends AbstractGridPresenter<ProjectDTO> implements DbPage {

    public static final PageId PAGE_ID = new PageId("projects");

    @ImplementedBy(DbProjectGrid.class)
    public interface View extends GridView<DbProjectEditor, ProjectDTO> {
        public void init(DbProjectEditor editor, UserDatabaseDTO db, ListStore<ProjectDTO> store);

        public FormDialogTether showAddDialog(ProjectDTO partner, FormDialogCallback callback);
    }

    private final Dispatcher service;
    private final EventBus eventBus;
    private final View view;

    private UserDatabaseDTO db;
    private ListStore<ProjectDTO> store;

    @Inject
    public DbProjectEditor(EventBus eventBus, Dispatcher service, StateProvider stateMgr, View view) {
        super(eventBus, stateMgr, view);
        this.service = service;
        this.eventBus = eventBus;
        this.view = view;
    }

    @Override
    public void go(UserDatabaseDTO db) {
        this.db = db;

        store = new ListStore<ProjectDTO>();
        store.setSortField("name");
        store.setSortDir(Style.SortDir.ASC);
        store.add(new ArrayList<ProjectDTO>(db.getProjects()));

        view.init(this, db, store);
        view.setActionEnabled(UIActions.DELETE, false);
        view.setActionEnabled(UIActions.EDIT, false);
    }


    @Override
    protected void onEdit(final ProjectDTO model) {

        final FormDialogImpl<ProjectForm> dialog = new FormDialogImpl<ProjectForm>(new ProjectForm());
        dialog.setWidth(450);
        dialog.setHeight(300);
        dialog.getForm().getBinding().bind(model);
        dialog.show(new FormDialogCallback() {

            @Override
            public void onValidated() {
                service.execute(RequestChange.update(model, "name", "description"),
                        dialog,
                        new AsyncCallback<VoidResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                // handled by monitor
                            }

                            @Override
                            public void onSuccess(VoidResult result) {
                                dialog.hide();
                                eventBus.fireEvent(AppEvents.SCHEMA_CHANGED);
                                view.refresh();
                            }
                        });
            }
        });

    }

    @Override
    protected void onDeleteConfirmed(final ProjectDTO project) {
        service.execute(RequestChange.delete(project), view.getDeletingMonitor(), new AsyncCallback<VoidResult>() {
            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert(I18N.CONSTANTS.error(), I18N.CONSTANTS.errorOnServer(), null);
            }

            @Override
            public void onSuccess(VoidResult result) {
                store.remove(project);
            }
        });
    }

    @Override
    protected void onAdd() {
        final ProjectDTO newProject = new ProjectDTO();
        this.view.showAddDialog(newProject, new FormDialogCallback() {

            @Override
            public void onValidated(final FormDialogTether dlg) {

                service.execute(new AddProject(db.getId(), newProject), dlg, new AsyncCallback<CreateResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof DuplicatePartnerException) {
                            MessageBox.alert(I18N.CONSTANTS.error(), I18N.CONSTANTS.errorOnServer(), null);
                        } else {
                            MessageBox.alert(I18N.CONSTANTS.error(), I18N.CONSTANTS.errorOnServer(), null);
                        }
                    }

                    @Override
                    public void onSuccess(CreateResult result) {
                        newProject.setId(result.getNewId());
                        store.add(newProject);
                        db.getProjects().add(newProject);
                        eventBus.fireEvent(AppEvents.SCHEMA_CHANGED);
                        dlg.hide();
                    }
                });
            }
        });
    }

    @Override
    public PageId getPageId() {
        return PAGE_ID;
    }

    @Override
    public Object getWidget() {
        return view;
    }

    @Override
    public boolean navigate(PageState place) {
        return false;
    }

    @Override
    public void shutdown() {
    }

    @Override
    protected String getStateId() {
        return "ProjectsGrid";
    }

    @Override
    public void onSelectionChanged(ModelData selectedItem) {
        view.setActionEnabled(UIActions.DELETE, selectedItem != null);
        view.setActionEnabled(UIActions.EDIT, selectedItem != null);
    }
}
