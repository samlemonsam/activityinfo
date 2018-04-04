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

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.dispatch.monitor.NullAsyncMonitor;
import org.activityinfo.ui.client.page.common.columns.EditableLocalDateColumn;
import org.activityinfo.ui.client.page.common.columns.ReadTextColumn;
import org.activityinfo.ui.client.page.common.dialog.FormDialogCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogImpl;
import org.activityinfo.ui.client.page.common.dialog.FormDialogTether;
import org.activityinfo.ui.client.page.common.toolbar.ActionToolBar;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.page.config.LockedPeriodsPresenter.LockedPeriodListEditor;
import org.activityinfo.ui.client.page.config.mvp.CanCreate;
import org.activityinfo.ui.client.page.config.mvp.CanUpdate;

import java.util.*;

public class LockedPeriodGrid extends ContentPanel implements LockedPeriodListEditor {

    private EventBus eventBus = new SimpleEventBus();

    // UI stuff
    private ListStore<LockedPeriodDTO> lockedPeriodStore;
    private EditorGrid<LockedPeriodDTO> lockedPeriodDTOGrid;
    private AsyncMonitor deletingMonitor = new NullAsyncMonitor();
    private AsyncMonitor loadingMonitor = new NullAsyncMonitor();
    private AsyncMonitor updatingMonitor = new NullAsyncMonitor();
    private EditCheckColumnConfig columnEnabled;

    // Data
    private UserDatabaseDTO database;
    private LockedPeriodDTO lockedPeriod;
    private ActivityFormDTO activityFilter = null;

    // Nested views
    private AddLockedPeriodDialog addLockedPeriod;
    private ActionToolBar actionToolbar;
    private FormDialogImpl<AddLockedPeriodDialog> form;

    public class EditCheckColumnConfig extends CheckColumnConfig {

        public EditCheckColumnConfig(String id, String name, int width) {
            super(id, name, width);
            setToolTip(name);
            setEditor(new CellEditor(new CheckBox()));
        }

        public void checkForSelection(ComponentEvent be) {
            El el = be.getTargetEl();
            if (el != null && el.hasStyleName("x-grid3-cc-" + getId()) && !el.hasStyleName("x-grid3-check-col-disabled")) {
                Record r = lockedPeriodStore.getRecord(lockedPeriod);
                Boolean b = lockedPeriod.get(this.getDataIndex());
                r.set(this.getDataIndex(), b == null ? true : !b);
            }
        }
    }

    private boolean canEditLock(LockedPeriodDTO lock) {
        if (lock.getParent() instanceof ActivityDTO) {
            return database.isVisible((ActivityDTO) lock.getParent());
        } else if (lock.getParent() instanceof FolderDTO) {
            return database.isVisible((FolderDTO) lock.getParent());
        } else {
            // User can only edit if they have no folder limitations
            return !database.hasFolderLimitation();
        }
    }

    public LockedPeriodGrid() {
        super();

        initializeComponent();

        createListStore();
        createActionToolbar();
        createAddLockedPeriodDialog();
        createGrid();
    }

    private void createGrid() {
        List<ColumnConfig> configs = new ArrayList<>();

        columnEnabled = new EditCheckColumnConfig("enabled", I18N.CONSTANTS.enabledColumn(), 55);
        columnEnabled.setSortable(false);

        ColumnConfig columnPeriodType = new ColumnConfig();
        columnPeriodType.setHeaderText(I18N.CONSTANTS.type());
        columnPeriodType.setToolTip(I18N.CONSTANTS.type());
        columnPeriodType.setWidth(48);
        columnPeriodType.setRowHeader(true);
        columnPeriodType.setRenderer(new LockTypeIconCellRenderer());

        configs.add(columnEnabled);
        configs.add(columnPeriodType);
        configs.add(new ReadTextColumn("parentName", I18N.CONSTANTS.parentName(), 150));
        configs.add(new ReadTextColumn("name", I18N.CONSTANTS.name(), 100));
        configs.add(new EditableLocalDateColumn("fromDate", I18N.CONSTANTS.fromDate(), 100));
        configs.add(new EditableLocalDateColumn("toDate", I18N.CONSTANTS.toDate(), 100));

        lockedPeriodDTOGrid = new EditorGrid<>(lockedPeriodStore, new ColumnModel(configs));
        lockedPeriodDTOGrid.addListener(Events.BeforeEdit, baseEvent -> {
            GridEvent gridEvent = (GridEvent) baseEvent;
            LockedPeriodDTO model = (LockedPeriodDTO) gridEvent.getModel();
            baseEvent.setCancelled(!canEditLock(model));
        });
        lockedPeriodDTOGrid.addListener(Events.ValidateEdit, baseEvent -> {
            GridEvent gridEvent = (GridEvent) baseEvent;
            LockedPeriodDTO model = (LockedPeriodDTO) gridEvent.getModel();

            LocalDate fromDate = model.getFromDate();
            LocalDate toDate = model.getToDate();

            if ("fromDate".equals(gridEvent.getProperty())) {
                fromDate = (LocalDate) gridEvent.getValue();
            }
            if ("toDate".equals(gridEvent.getProperty())) {
                toDate = (LocalDate) gridEvent.getValue();
            }

            if (!fromDate.before(toDate) && !fromDate.equals(toDate)) {
                MessageBox.alert(I18N.CONSTANTS.alert(), I18N.CONSTANTS.fromDateIsBeforeToDate(), null);
                lockedPeriodDTOGrid.getActiveEditor().cancelEdit();
            }
        });

        lockedPeriodDTOGrid.addListener(Events.OnClick, (Listener<ComponentEvent>) this::updateState);
        add(lockedPeriodDTOGrid);
    }

    private void updateState(ComponentEvent be) {
        lockedPeriod = lockedPeriodDTOGrid.getSelectionModel().getSelectedItem();
        if (lockedPeriod != null && canEditLock(lockedPeriod)) {
            setDeleteEnabled(true);
            columnEnabled.checkForSelection(be);
        } else {
            setDeleteEnabled(false);
        }
    }

    private void updateStateLater() {
        Scheduler.get().scheduleDeferred(() -> updateState(null));
    }

    private void initializeComponent() {
        setHeadingText(I18N.CONSTANTS.lockPanelTitle());
        setLayout(new FitLayout());
    }

    private void createListStore() {
        lockedPeriodStore = new ListStore<>();
        lockedPeriodStore.addListener(Store.DataChanged, event -> {
            actionToolbar.setUpdateEnabled(true);
            updateStateLater();
        });
        lockedPeriodStore.addListener(Store.Remove, event -> updateStateLater());
    }

    private void createActionToolbar() {
        actionToolbar = new ActionToolBar(actionId -> {
            switch (actionId) {
                case UIActions.ADD:
                    eventBus.fireEvent(new StartCreateEvent());
                    break;
                case UIActions.DELETE:
                    eventBus.fireEvent(new RequestDeleteEvent());
                    break;
                case UIActions.SAVE:
                    eventBus.fireEvent(new UpdateEvent());
                    break;
                case UIActions.DISCARD_CHANGES:
                default:
                    eventBus.fireEvent(new CancelUpdateEvent());
                    break;
            }
        });
        actionToolbar.addDeleteButton();
        actionToolbar.addCreateButton();
        actionToolbar.addSaveSplitButton();
        actionToolbar.setDeleteEnabled(false);
        actionToolbar.setUpdateEnabled(false);
        this.setTopComponent(actionToolbar);
    }

    private void createAddLockedPeriodDialog() {
        addLockedPeriod = new AddLockedPeriodDialog();
        addLockedPeriod.addCreateHandler(createEvent -> {
            lockedPeriod = addLockedPeriod.getValue();
            eventBus.fireEvent(new CreateEvent());
        });
        addLockedPeriod.addCancelCreateHandler(createEvent -> eventBus.fireEvent(new CancelCreateEvent()));
    }

    @Override
    public void create(LockedPeriodDTO item) {
        lockedPeriodStore.add(item);
        addLockedPeriod.cancelCreate();
        if (form != null) {
            form.hide();
        }
    }

    @Override
    public void update(LockedPeriodDTO item) {
        lockedPeriodStore.commitChanges();
    }

    @Override
    public void delete(LockedPeriodDTO item) {
        lockedPeriodStore.remove(item);
    }

    @Override
    public void setCreateEnabled(boolean createEnabled) {
        actionToolbar.setAddEnabled(createEnabled);
    }

    @Override
    public void setUpdateEnabled(boolean updateEnabled) {
        actionToolbar.setUpdateEnabled(updateEnabled);
    }

    @Override
    public final void setDeleteEnabled(boolean deleteEnabled) {
        actionToolbar.setDeleteEnabled(deleteEnabled);
    }

    @Override
    public LockedPeriodDTO getValue() {
        return lockedPeriod;
    }

    @Override
    public HandlerRegistration addCreateHandler(CreateHandler handler) {
        return eventBus.addHandler(CreateEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addUpdateHandler(UpdateHandler handler) {
        return eventBus.addHandler(UpdateEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addConfirmDeleteHandler(ConfirmDeleteHandler handler) {
        return eventBus.addHandler(ConfirmDeleteEvent.TYPE, handler);
    }

    @Override
    public void cancelUpdate(LockedPeriodDTO item) {
        lockedPeriodDTOGrid.stopEditing(true);
    }

    @Override
    public void askConfirmDelete(LockedPeriodDTO item) {
        MessageBox.confirm(I18N.CONSTANTS.deleteLockedPeriodTitle(),
                I18N.CONSTANTS.deleteLockedPeriodQuestion(),
                event -> {
                    if (event.getButtonClicked().getItemId().equals(Dialog.YES)) {
                        eventBus.fireEvent(new ConfirmDeleteEvent());
                    }
                });
    }

    @Override
    public void setParent(UserDatabaseDTO parent) {
        addLockedPeriod.setUserDatabase(parent);
    }

    @Override
    public void setItems(List<LockedPeriodDTO> items) {
        lockedPeriodStore.removeAll();
        lockedPeriodStore.add(filterLockedPeriodsByActivity(items));
    }

    private List<LockedPeriodDTO> filterLockedPeriodsByActivity(List<LockedPeriodDTO> items) {

        if (activityFilter != null) {
            // Remove LockedPeriods which have a different Activity then the
            // activiftyFilter
            List<LockedPeriodDTO> lockedPeriodsFilteredByActivity = new ArrayList<>();
            for (LockedPeriodDTO lock : items) {
                if (lock.getParent() != null && lock.getParent() instanceof IsActivityDTO) {
                    // Activity as parent, only add when activity equals filter
                    if (lock.getParent().getId() == activityFilter.getId()) {
                        lockedPeriodsFilteredByActivity.add(lock);
                    }
                } else {
                    // Database or Project, can be added
                    lockedPeriodsFilteredByActivity.add(lock);
                }
            }
            return lockedPeriodsFilteredByActivity;
        } else {
            // No filter, just return the items
            return items;
        }
    }

    @Override
    public HandlerRegistration addCancelUpdateHandler(CancelUpdateHandler handler) {
        return eventBus.addHandler(CancelUpdateEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addRequestDeleteHandler(RequestDeleteHandler handler) {
        return eventBus.addHandler(RequestDeleteEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addCancelCreateHandler(CanCreate.CancelCreateHandler handler) {
        return eventBus.addHandler(CancelCreateEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addStartCreateHandler(CanCreate.StartCreateHandler handler) {
        return eventBus.addHandler(StartCreateEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addRequestUpdateHandler(CanUpdate.RequestUpdateHandler handler) {
        return eventBus.addHandler(RequestUpdateEvent.TYPE, handler);
    }

    @Override
    public List<LockedPeriodDTO> getUnsavedItems() {
        List<LockedPeriodDTO> unsavedItems = new ArrayList<>();

        List<Record> modifiedRecords = lockedPeriodStore.getModifiedRecords();
        for (Record record : modifiedRecords) {
            unsavedItems.add((LockedPeriodDTO) record.getModel());
        }

        return unsavedItems;
    }

    @Override
    public boolean hasChangedItems() {
        return !getUnsavedItems().isEmpty();
    }

    @Override
    public boolean hasSingleChangedItem() {
        return getUnsavedItems().size() == 1;
    }

    @Override
    public Map<String, Object> getChanges(LockedPeriodDTO item) {
        for (Record record : lockedPeriodStore.getModifiedRecords()) {
            LockedPeriodDTO model = (LockedPeriodDTO) record.getModel();
            if (model.getId() == item.getId()) {
                Map<String, Object> changes = new HashMap<>();
                for (String property : record.getPropertyNames()) {
                    changes.put(property, model.get(property));
                }

                return changes;
            }
        }

        return null;
    }

    @Override
    public void cancelUpdateAll() {
        lockedPeriodStore.rejectChanges();
    }

    @Override
    public void startCreate() {
        addLockedPeriod.startCreate();
        form = new FormDialogImpl<>(addLockedPeriod);
        form.setHeadingText(I18N.CONSTANTS.addTimeLock());
        form.setWidth(400);
        form.setHeight(350);

        form.show(new FormDialogCallback() {

            @Override
            public void onValidated(FormDialogTether dlg) {
                if (addLockedPeriod.isValid()) {
                    LockedPeriodGrid.this.lockedPeriod = addLockedPeriod.getValue();
                    eventBus.fireEvent(new CreateEvent());
                }
            }

            @Override
            public void onCancelled() {
                eventBus.fireEvent(new CancelCreateEvent());
            }
        });
    }

    @Override
    public void cancelCreate() {
        addLockedPeriod.cancelCreate();
        if (form != null) {
            form.hide();
        }
    }

    @Override
    public AsyncMonitor getLoadingMonitor() {
        return loadingMonitor;
    }

    @Override
    public AsyncMonitor getCreatingMonitor() {
        return form;
    }

    @Override
    public AsyncMonitor getUpdatingMonitor() {
        return updatingMonitor;
    }

    @Override
    public AsyncMonitor getDeletingMonitor() {
        return deletingMonitor;
    }

    @Override
    public void setRefreshEnabled(boolean canRefresh) {
        //
    }

    @Override
    public void startUpdate() {
        //
    }

    @Override
    public void cancelDelete() {
        //
    }

    @Override
    public HandlerRegistration addRefreshHandler(RefreshHandler handler) {
        return eventBus.addHandler(RefreshEvent.TYPE, handler);
    }

    @Override
    public HandlerRegistration addFilterHandler(FilterHandler filter) {
        return null;
    }

    @Override
    public void initialize() {
        //
    }

    @Override
    public void setValue(LockedPeriodDTO value) {
        //
    }

    public void setActivityFilter(ActivityFormDTO activityFilter) {
        this.activityFilter = activityFilter;
    }

    public void setReadOnly(boolean isReadOnly) {
        if (isReadOnly) {
            remove(actionToolbar);
        }
    }

    @Override
    public void setTitle(String title) {
        setHeadingText(title);
    }

    @Override
    public void setUserDatabase(UserDatabaseDTO userDatabase) {
        this.database = userDatabase;
    }
}
