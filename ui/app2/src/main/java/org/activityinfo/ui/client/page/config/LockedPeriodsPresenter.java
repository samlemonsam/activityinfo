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

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.ImplementedBy;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.*;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.command.result.VoidResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.config.LockedPeriodsPresenter.LockedPeriodListEditor;
import org.activityinfo.ui.client.page.config.mvp.AddCreateView;
import org.activityinfo.ui.client.page.config.mvp.CanCreate.CreateEvent;
import org.activityinfo.ui.client.page.config.mvp.CanDelete.ConfirmDeleteEvent;
import org.activityinfo.ui.client.page.config.mvp.CanDelete.RequestDeleteEvent;
import org.activityinfo.ui.client.page.config.mvp.CanFilter.FilterEvent;
import org.activityinfo.ui.client.page.config.mvp.CanRefresh.RefreshEvent;
import org.activityinfo.ui.client.page.config.mvp.CrudView;
import org.activityinfo.ui.client.page.config.mvp.ListPresenterBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LockedPeriodsPresenter extends ListPresenterBase<LockedPeriodDTO, List<LockedPeriodDTO>,
        UserDatabaseDTO, LockedPeriodListEditor> implements DbPage {

    @ImplementedBy(LockedPeriodGrid.class)
    public interface LockedPeriodListEditor extends CrudView<LockedPeriodDTO, UserDatabaseDTO> {

        void setTitle(String title);

        void setUserDatabase(UserDatabaseDTO userDatabase);
    }

    @ImplementedBy(AddLockedPeriodDialog.class)
    public interface AddLockedPeriodView extends AddCreateView<LockedPeriodDTO> {

        void setUserDatabase(UserDatabaseDTO userDatabase);
    }

    public static final PageId PAGE_ID = new PageId("lockedPeriod");

    @Inject
    public LockedPeriodsPresenter(Dispatcher service, EventBus eventBus, LockedPeriodListEditor view) {
        super(service, eventBus, view);

    }

    @Override
    public void onCreate(CreateEvent event) {
        view.getCreatingMonitor().beforeRequest();

        final LockedPeriodDTO lockedPeriod = view.getValue();
        CreateLockedPeriod lockUserDatabase = new CreateLockedPeriod(lockedPeriod);
        if (lockedPeriod.getParent() instanceof IsActivityDTO) {
            lockUserDatabase.setActivityId(lockedPeriod.getParent().getId());
        }
        if (lockedPeriod.getParent() instanceof ProjectDTO) {
            lockUserDatabase.setProjectId(lockedPeriod.getParent().getId());
        }
        if (lockedPeriod.getParent() instanceof UserDatabaseDTO) {
            lockUserDatabase.setDatabaseId(lockedPeriod.getParent().getId());
        }
        if (lockedPeriod.getParent() instanceof FolderDTO) {
            lockUserDatabase.setFolderId(lockedPeriod.getParent().getId());
        }

        service.execute(lockUserDatabase, new AsyncCallback<CreateResult>() {
            @Override
            public void onFailure(Throwable caught) {
                view.getCreatingMonitor().onServerError(caught);
                MessageBox.alert(I18N.CONSTANTS.error(),
                        I18N.CONSTANTS.errorOnServer() + "\n\n" + caught.getMessage(),
                        null);
            }

            @Override
            public void onSuccess(CreateResult result) {
                // Update the Id for the child instance
                lockedPeriod.setId(result.getNewId());

                // Tell the view there's a new kid on the block
                view.create(lockedPeriod);
                view.getCreatingMonitor().onCompleted();

                // Actually add the lock to it's parent
                lockedPeriod.getParent().getLockedPeriods().add(lockedPeriod);
            }
        });
    }

    @Override
    public void onUpdate() {
        if (view.hasChangedItems()) {
            // Tell the user we're about to persist his changes
            view.getUpdatingMonitor().beforeRequest();

            service.execute(createBatchCommand(), new AsyncCallback<BatchResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    // Tell the user an error occurred
                    view.getDeletingMonitor().onServerError(caught);
                    MessageBox.alert(I18N.CONSTANTS.error(),
                            I18N.CONSTANTS.errorOnServer() + "\n\n" + caught.getMessage(),
                            null);
                }

                @Override
                public void onSuccess(BatchResult result) {
                    // Tell the user we're done updating
                    view.getDeletingMonitor().onCompleted();

                    // Update the in-memory model
                    updateParent();

                    // Update the view
                    view.update();
                }

                /** Replace changed locks */
                private void updateParent() {
                    for (LockedPeriodDTO lockedPeriod : view.getUnsavedItems()) {
                        LockedPeriodDTO lockedPeriodToRemove = null;

                        // Cache the LockedPeriods candidate for removal
                        Set<LockedPeriodDTO> lockedPeriodsToUpdate = parentModel.getLockedPeriods();

                        // Find the LockedPeriod in the model
                        for (LockedPeriodDTO oldLockedPeriod : lockedPeriodsToUpdate) {
                            if (lockedPeriod.getId() == oldLockedPeriod.getId()) {
                                lockedPeriodToRemove = oldLockedPeriod;
                                break;
                            }
                        }

                        // Replace LockedPeriod when the same entity is
                        // found
                        if (lockedPeriodToRemove != null) {
                            // Remove from cache
                            lockedPeriodsToUpdate.remove(lockedPeriodToRemove);

                            // Replace old instance with new instance
                            parentModel.getLockedPeriods().remove(lockedPeriodToRemove);
                            parentModel.getLockedPeriods().add(lockedPeriod);
                        }
                    }
                }
            });
        }
    }

    private BatchCommand createBatchCommand() {
        BatchCommand batch = new BatchCommand();
        for (LockedPeriodDTO lockedPeriod : view.getUnsavedItems()) {
            batch.add(new UpdateEntity(lockedPeriod.getEntityName(),
                    lockedPeriod.getId(),
                    view.getChanges(lockedPeriod)));
        }
        return batch;
    }

    @Override
    public void onCancelUpdate() {
        //
    }

    @Override
    public void onConfirmDelete(ConfirmDeleteEvent deleteEvent) {
        final LockedPeriodDTO lockedPeriod = view.getValue();
        service.execute(new Delete(lockedPeriod), new AsyncCallback<VoidResult>() {

            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert(I18N.CONSTANTS.error(),
                        I18N.CONSTANTS.errorOnServer() + "\n\n" + caught.getMessage(),
                        null);
            }

            @Override
            public void onSuccess(VoidResult result) {
                view.delete(lockedPeriod);
                parentModel.getLockedPeriods().remove(lockedPeriod);
            }
        });
    }

    @Override
    public void onFilter(FilterEvent filterEvent) {
        //
    }

    @Override
    public void onRefresh(RefreshEvent refreshEvent) {
        service.execute(new GetSchema(), new AsyncCallback<SchemaDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                MessageBox.alert(I18N.CONSTANTS.error(),
                        I18N.CONSTANTS.errorOnServer() + "\n\n" + caught.getMessage(),
                        null);
            }

            @Override
            public void onSuccess(SchemaDTO result) {
                go(result.getDatabaseById(parentModel.getId()));
            }
        });
    }

    @Override
    public void onRequestDelete(RequestDeleteEvent deleteEvent) {
        view.askConfirmDelete(view.getValue());
    }

    @Override
    public void go(UserDatabaseDTO db) {
        parentModel = db;

        ArrayList<LockedPeriodDTO> items = new ArrayList<>(db.getLockedPeriods());
        for (ActivityDTO activity : db.getActivities()) {
            if (activity.getLockedPeriods() != null) {
                items.addAll(activity.getLockedPeriods());
            }
        }
        for (ProjectDTO project : db.getProjects()) {
            if (project.getLockedPeriods() != null) {
                items.addAll(project.getLockedPeriods());
            }
        }
        for (FolderDTO folder : db.getFolders()) {
            items.addAll(folder.getLockedPeriods());
        }

        view.setItems(items);
        if (!items.isEmpty()) {
            view.setValue(items.get(0));
        }
        view.setUserDatabase(db);
        view.setParent(parentModel);
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
        return view;
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

}