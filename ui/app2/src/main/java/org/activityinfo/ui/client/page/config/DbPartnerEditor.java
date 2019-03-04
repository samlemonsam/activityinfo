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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.legacy.shared.command.RemovePartner;
import org.activityinfo.legacy.shared.command.UpdatePartner;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.command.result.DuplicateCreateResult;
import org.activityinfo.legacy.shared.command.result.RemoveFailedResult;
import org.activityinfo.legacy.shared.command.result.RemoveResult;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.ui.client.AppEvents;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.monitor.MaskingAsyncMonitor;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.dialog.FormDialogCallback;
import org.activityinfo.ui.client.page.common.dialog.FormDialogImpl;
import org.activityinfo.ui.client.page.common.dialog.FormDialogTether;
import org.activityinfo.ui.client.page.common.toolbar.ActionListener;
import org.activityinfo.ui.client.page.common.toolbar.ActionToolBar;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.page.config.form.PartnerForm;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Alex Bertram
 */
public class DbPartnerEditor implements IsWidget, ActionListener, DbPage {

    public static final PageId PAGE_ID = new PageId("partners");

    private static final Logger LOGGER = Logger.getLogger(DbPartnerEditor.class.getName());

    private final EventBus eventBus;
    private final Dispatcher dispatcher;

    private final ContentPanel contentPanel;
    private final ActionToolBar toolBar;
    private final ListStore<PartnerDTO> store;
    private final Grid<PartnerDTO> grid;

    private UserDatabaseDTO db;

    @Inject
    public DbPartnerEditor(EventBus eventBus, Dispatcher dispatcher) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;

        toolBar = new ActionToolBar(this);
        toolBar.addButton(UIActions.ADD, I18N.CONSTANTS.addPartner(), IconImageBundle.ICONS.add());
        toolBar.addButton(UIActions.EDIT, I18N.CONSTANTS.edit(), IconImageBundle.ICONS.edit());
        toolBar.addButton(UIActions.DELETE, I18N.CONSTANTS.delete(), IconImageBundle.ICONS.delete());
        toolBar.setDirty(false);

        store = new ListStore<>();
        store.setSortField("name");
        store.setSortDir(Style.SortDir.ASC);
        store.setModelComparer((a, b) -> a.getId() == b.getId());

        grid = new Grid<>(store, createColumnModel());
        grid.setAutoExpandColumn("fullName");
        grid.setLoadMask(true);
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<PartnerDTO>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<PartnerDTO> event) {
                onSelectionChanged(Optional.ofNullable(event.getSelectedItem()));
            }
        });

        this.contentPanel = new ContentPanel();
        this.contentPanel.setTopComponent(toolBar);
        this.contentPanel.setLayout(new FitLayout());
        this.contentPanel.add(grid);
    }

    private void onSelectionChanged(Optional<PartnerDTO> selection) {
        this.toolBar.setActionEnabled(UIActions.EDIT, selection.isPresent());
        this.toolBar.setActionEnabled(UIActions.DELETE, selection.isPresent());
    }

    @Override
    public void go(UserDatabaseDTO db) {
        this.db = db;
        this.contentPanel.setHeadingText(db.getName() + " - " + I18N.CONSTANTS.partners());
        this.store.removeAll();
        this.store.add(new ArrayList<>(db.getPartners()));
    }

    private ColumnModel createColumnModel() {
        List<ColumnConfig> columns = new ArrayList<>();
        columns.add(new ColumnConfig("name", I18N.CONSTANTS.name(), 150));
        columns.add(new ColumnConfig("fullName", I18N.CONSTANTS.description(), 300));

        return new ColumnModel(columns);
    }

    @Override
    public void onUIAction(String actionId) {
        switch (actionId) {
            case UIActions.ADD:
                showAddDialog();
                break;
            case UIActions.EDIT:
                showEditDialog(grid.getSelectionModel().getSelectedItem());
                break;
            case UIActions.DELETE:
                confirmDelete(grid.getSelectionModel().getSelectedItem());
                break;
        }
    }

    private void confirmDelete(PartnerDTO selectedItem) {
        MessageBox.confirm(
                I18N.CONSTANTS.removePartner(),
                I18N.MESSAGES.requestConfirmationToDeletePartner(selectedItem.getName()),
                event -> {
                    if(event.getButtonClicked().getItemId().equals(Dialog.YES)) {
                        delete(selectedItem);
                    }
                }
        );
    }

    private void delete(PartnerDTO selectedItem) {
        dispatcher.execute(new RemovePartner(db.getId(), selectedItem.getId()),
                new MaskingAsyncMonitor(grid, I18N.CONSTANTS.deletionInProgress()),
                new AsyncCallback<RemoveResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Log.debug("DbPartnerEditor caught exception while executing command RemovePartner: ", caught);
                    }

                    @Override
                    public void onSuccess(RemoveResult result) {
                        if (result instanceof RemoveFailedResult) {
                            Log.debug("DbPartnerEditor tried to remove partner '" + selectedItem.getName() +
                                    "' from database " + db.getId() + " but there's data associated with it");
                            MessageBox.alert(I18N.CONSTANTS.removePartner(),
                                    I18N.MESSAGES.partnerHasDataWarning(selectedItem.getName()),
                                    null);
                        } else {
                            Log.debug("DbPartnerEditor removed partner '" + selectedItem.getName() +
                                    "' from database " + db.getId());
                            store.remove(selectedItem);
                        }
                    }
                });
    }


    private Set<String> otherPartnerNames(PartnerDTO editingPartner) {
        List<PartnerDTO> models = grid.getStore().getModels();
        Set<String> names = Sets.newHashSet();
        for (PartnerDTO partner : models) {
                names.add(partner.getName() != null ? partner.getName().trim() : "");
        }
        if(editingPartner.getName() != null) {
            names.remove(editingPartner.getName());
        }
        return names;
    }

    private void showAddDialog() {

        PartnerDTO newPartner = new PartnerDTO();

        showEditDialog(newPartner);
    }

    private void showEditDialog(PartnerDTO partner) {
        PartnerForm form = new PartnerForm(otherPartnerNames(partner));
        form.getBinding().bind(partner);

        FormDialogImpl<PartnerForm> dlg = new FormDialogImpl<>(form);
        dlg.setWidth(450);
        dlg.setHeight(300);
        dlg.setHeadingText(I18N.CONSTANTS.newPartner());

        dlg.show(new FormDialogCallback() {
            @Override
            public void onValidated(FormDialogTether dlg) {
                dispatcher.execute(new UpdatePartner(db.getId(), partner), dlg, new AsyncCallback<CreateResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Log.debug("DbPartnerEditor caught exception while executing command AddPartner: ", caught);
                    }

                    @Override
                    public void onSuccess(CreateResult result) {
                        if (result instanceof DuplicateCreateResult) {
                            LOGGER.fine("DbPartnerEditor tried to add partner '" + partner.getName() +
                                    "' to database " + db.getId() + " but it already exists");
                            MessageBox.alert(I18N.CONSTANTS.newPartner(), I18N.CONSTANTS.duplicatePartner(), null);

                        } else {
                            LOGGER.fine("DbPartnerEditor added/updated new partner '" + partner.getName() +
                                    "' to database " + db.getId());
                            eventBus.fireEvent(AppEvents.SCHEMA_CHANGED);
                            dlg.hide();
                            updateStore(partner, result);
                        }
                    }


                });
            }
        });
    }

    private void updateStore(PartnerDTO partner, CreateResult createResult) {
        if(!partner.hasId()) {
            // Brand new partner, add directly to store
            partner.setId(createResult.getNewId());
            store.add(partner);

        } else if(partner.getId() == createResult.getNewId()){
            // Partner has been updated, update the store
            store.update(partner);
        } else {
            // Shared partner has been replaced
            store.remove(partner);

            // Update with new id
            partner.setId(createResult.getNewId());
            store.add(partner);
        }

    }

    @Override
    public Widget asWidget() {
        return contentPanel;
    }

    @Override
    public PageId getPageId() {
        return PAGE_ID;
    }

    @Override
    public Object getWidget() {
        return contentPanel;
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
        // No shutdown actions required
    }
}
