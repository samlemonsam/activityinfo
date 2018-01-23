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

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.GetUsers;
import org.activityinfo.legacy.shared.command.result.UserResult;
import org.activityinfo.legacy.shared.model.PartnerDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.legacy.shared.model.UserPermissionDTO;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.dispatch.AsyncMonitor;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.page.NavigationCallback;
import org.activityinfo.ui.client.page.PageId;
import org.activityinfo.ui.client.page.PageState;
import org.activityinfo.ui.client.page.common.dialog.SaveChangesCallback;
import org.activityinfo.ui.client.page.common.dialog.SavePromptMessageBox;
import org.activityinfo.ui.client.page.common.toolbar.ActionListener;
import org.activityinfo.ui.client.page.common.toolbar.ActionToolBar;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.ArrayList;
import java.util.List;

public class DbUserEditor extends ContentPanel implements DbPage, ActionListener {


    public static final PageId PAGE_ID = new PageId("dbusers");

    private static final SafeHtml ALL_CATEGORIES = new SafeHtmlBuilder()
            .appendHtmlConstant("<i>").appendEscaped(I18N.CONSTANTS.all()).appendHtmlConstant("</i>").toSafeHtml();

    private final EventBus eventBus;
    private final Dispatcher dispatcher;

    private PagingLoader<UserResult> loader;
    private ListStore<UserPermissionDTO> store;
    private Grid<UserPermissionDTO> grid;

    private UserDatabaseDTO db;

    private ActionToolBar toolBar;
    private DbUserEditorActions actions;

    private boolean modified = true;


    @Inject
    public DbUserEditor(EventBus eventBus, Dispatcher service, StateProvider stateMgr) {
        this.eventBus = eventBus;
        this.dispatcher = service;

        setHeadingText(I18N.CONSTANTS.users());
        setLayout(new FitLayout());

        createToolBar();
        createGrid();
        createPagingToolBar();
    }

    @Override
    public void go(UserDatabaseDTO db) {
        this.db = db;
        store.removeAll();

        actions = new DbUserEditorActions(this, dispatcher, loader, store, grid, db);

        toolBar.setActionEnabled(UIActions.SAVE, false);
        toolBar.setActionEnabled(UIActions.ADD, db.isManageUsersAllowed());
        toolBar.setActionEnabled(UIActions.DELETE, false);

        grid.getColumnModel().getColumnById("allowViewAll").setHidden(!db.isManageUsersAllowed());
        grid.getColumnModel().getColumnById("allowEditAll").setHidden(!db.isManageUsersAllowed());
        grid.getColumnModel().getColumnById("allowManageUsers").setHidden(!db.isManageAllUsersAllowed());
        grid.getColumnModel().getColumnById("allowManageAllUsers").setHidden(!db.isManageAllUsersAllowed());
        grid.getColumnModel().getColumnById("allowDesign").setHidden(!db.isDesignAllowed());

        loader.load();
        modified = false;
    }

    private void createToolBar() {
        toolBar = new ActionToolBar(this);
        toolBar.addSaveButton();
        toolBar.addButton(UIActions.ADD, I18N.CONSTANTS.addUser(), IconImageBundle.ICONS.addUser());
        toolBar.addButton(UIActions.DELETE, I18N.CONSTANTS.delete(), IconImageBundle.ICONS.deleteUser());
        toolBar.addButton(UIActions.EXPORT, I18N.CONSTANTS.export(), IconImageBundle.ICONS.excel());
        toolBar.addButton(UIActions.MAILING_LIST,
                I18N.CONSTANTS.CopyAddressToClipBoard(),
                IconImageBundle.ICONS.dataEntry());
        setTopComponent(toolBar);
    }

    private void createGrid() {

        loader = new BasePagingLoader<UserResult>(new UserProxy());
        loader.setRemoteSort(true);
        
        store = new ListStore<UserPermissionDTO>(loader);
        store.setKeyProvider(new ModelKeyProvider<UserPermissionDTO>() {
            @Override
            public String getKey(UserPermissionDTO model) {
                return model.getEmail();
            }
        });
        store.addListener(Store.Update, new Listener<StoreEvent<UserPermissionDTO>>() {

            @Override
            public void handleEvent(StoreEvent<UserPermissionDTO> event) {
                modified = !store.getModifiedRecords().isEmpty();
                toolBar.setDirty(modified);
            }
        });

        final List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        columns.add(new ColumnConfig("name", I18N.CONSTANTS.name(), 100));
        columns.add(new ColumnConfig("email", I18N.CONSTANTS.email(), 150));
        columns.add(new ColumnConfig("partner", I18N.CONSTANTS.partner(), 150));

        ColumnConfig categoryColumn = new ColumnConfig("category", I18N.CONSTANTS.category(), 150);
        categoryColumn.setRenderer(new GridCellRenderer() {
            @Override
            public SafeHtml render(ModelData modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore, Grid grid) {
                if (modelData instanceof UserPermissionDTO) {
                    UserPermissionDTO permission = new UserPermissionDTO();
                    if (permission.hasCategoryRestriction()) {
                        SafeHtmlBuilder html = new SafeHtmlBuilder();
                        boolean needsComma = false;
                        for (String category : permission.getActivityCategories()) {
                            if (needsComma) {
                                html.appendHtmlConstant(", ");
                            }
                            html.appendEscaped(category);
                            needsComma = true;
                        }
                        return html.toSafeHtml();
                    }
                }
                return ALL_CATEGORIES;
            }
        });
        columns.add(categoryColumn);

        PermissionCheckConfig allowView = new PermissionCheckConfig("allowViewSimple", I18N.CONSTANTS.allowView(), 75);
        allowView.setDataIndex("allowView");
        allowView.setToolTip(I18N.CONSTANTS.allowViewLong());
        columns.add(allowView);

        PermissionCheckConfig allowEdit = new PermissionCheckConfig("allowEditSimple", I18N.CONSTANTS.allowEdit(), 75);
        allowEdit.setDataIndex("allowEdit");
        allowEdit.setToolTip(I18N.CONSTANTS.allowEditLong());
        columns.add(allowEdit);

        PermissionCheckConfig allowViewAll = new PermissionCheckConfig("allowViewAll",
                I18N.CONSTANTS.allowViewAll(),
                75);
        allowViewAll.setToolTip(I18N.CONSTANTS.allowViewAllLong());
        columns.add(allowViewAll);

        PermissionCheckConfig allowEditAll = new PermissionCheckConfig("allowEditAll",
                I18N.CONSTANTS.allowEditAll(),
                75);
        allowEditAll.setToolTip(I18N.CONSTANTS.allowEditAllLong());
        columns.add(allowEditAll);

        PermissionCheckConfig allowManageUsers = null;
        allowManageUsers = new PermissionCheckConfig("allowManageUsers", I18N.CONSTANTS.allowManageUsers(), 150);
        columns.add(allowManageUsers);

        PermissionCheckConfig allowManageAllUsers = new PermissionCheckConfig("allowManageAllUsers",
                I18N.CONSTANTS.manageAllUsers(),
                150);
        columns.add(allowManageAllUsers);

        // only users with the right to design them selves can change the design
        // attribute
        PermissionCheckConfig allowDesign = new PermissionCheckConfig("allowDesign", I18N.CONSTANTS.allowDesign(), 75);
        allowDesign.setToolTip(I18N.CONSTANTS.allowDesignLong());
        columns.add(allowDesign);

        grid = new Grid<>(store, new ColumnModel(columns));
        grid.setLoadMask(true);
        grid.setSelectionModel(new GridSelectionModel<UserPermissionDTO>());
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<UserPermissionDTO>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<UserPermissionDTO> se) {
                onSelectionChanged(se.getSelectedItem());
            }
        });
        grid.addPlugin(allowEdit);
        grid.addPlugin(allowViewAll);
        grid.addPlugin(allowEditAll);
        grid.addPlugin(allowManageUsers);
        grid.addPlugin(allowManageAllUsers);
        grid.addPlugin(allowDesign);
        add(grid);
    }

    private void createPagingToolBar() {
        PagingToolBar pagingToolBar = new PagingToolBar(100);
        pagingToolBar.bind(loader);
        setBottomComponent(pagingToolBar);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean navigate(PageState place) {
        return false;
    }

    private boolean validateChange(UserPermissionDTO user, String property) {

        // If the user doesn't have the manageUser permission, then it's
        // definitely a no.
        if (!db.isManageUsersAllowed()) {
            return false;
        }

        // if the user is only allowed to manager their own partners, then make
        // sure they're changing someone from their own organisation
        if (!db.isManageAllUsersAllowed() && db.getMyPartner().getId() != user.getPartner().getId()) {
            return false;
        }

        // do not allow users to set rights they themselves do not have
        if ("allowViewAll".equals(property) && !db.isViewAllAllowed()) {
            return false;
        }
        if ("allowEdit".equals(property) && !db.isEditAllowed()) {
            return false;
        }
        if ("allowEditAll".equals(property) && !db.isEditAllAllowed()) {
            return false;
        }
        if ("allowDesign".equals(property) && !db.isDesignAllowed()) {
            return false;
        }
        if ("allowManageUsers".equals(property) && !db.isManageUsersAllowed()) {
            return false;
        }
        if ("allowManageAllUsers".equals(property) && !db.isManageAllUsersAllowed()) {
            return false;
        }

        return true;
    }

    @Override
    public PageId getPageId() {
        return PAGE_ID;
    }

    @Override
    public Object getWidget() {
        return this;
    }

    @Override
    public String beforeWindowCloses() {
        return null;
    }

    private void onRowEdit(String property, boolean value, Record record) {
        record.beginEdit();
        if (!value) {
            // Cascade remove permissions
            if ("allowViewAll".equals(property)) {
                record.set("allowEditAll", false);
            }
        } else {
            // cascade add permissions
            if ("allowEditAll".equals(property)) {
                record.set("allowEdit", true);
                record.set("allowViewAll", true);
            } else if ("allowManageAllUsers".equals(property)) {
                record.set("allowManageUsers", true);
            }
        }

        record.endEdit();
        modified = store.getModifiedRecords().size() != 0;
        toolBar.setDirty(modified);
    }

    private void onSelectionChanged(UserPermissionDTO selectedItem) {

        if (selectedItem != null) {
            PartnerDTO selectedPartner = selectedItem.getPartner();

            toolBar.setActionEnabled(UIActions.DELETE,
                    db.isManageAllUsersAllowed() ||
                    (db.isManageUsersAllowed() && db.getMyPartnerId() == selectedPartner.getId()));
        }
        toolBar.setActionEnabled(UIActions.DELETE, selectedItem != null);
    }

    @Override
    public void onUIAction(String actionId) {
        if (actionId.equals(UIActions.SAVE)) {
            actions.save();
            modified = false;
        } else if (actionId.equals(UIActions.ADD)) {
            actions.add();
            modified = true;
        } else if (actionId.equals(UIActions.DELETE)) {
            actions.delete();
            modified = true;
        } else if (actionId.equals(UIActions.EXPORT)) {
            actions.export();
        } else if (UIActions.MAILING_LIST.equals(actionId)) {
            createMailingListPopup();
        }
    }

    private void createMailingListPopup() {
        new MailingListDialog(dispatcher, db.getId());
    }

    @Override
    public void requestToNavigateAway(PageState place, final NavigationCallback callback) {
        if (modified) {
            final SavePromptMessageBox savePrompt = new SavePromptMessageBox();
            savePrompt.show(new SaveChangesCallback() {
                @Override
                public void save(AsyncMonitor monitor) {
                    savePrompt.hide();
                    actions.save(callback);
                }

                @Override
                public void cancel() {
                    savePrompt.hide();
                    callback.onDecided(false);
                }

                @Override
                public void discard() {
                    savePrompt.hide();
                    callback.onDecided(true);
                }
            });
        } else {
            callback.onDecided(true);
        }
    }

    private class PermissionCheckConfig extends CheckColumnConfig {

        public PermissionCheckConfig(String id, String name, int width) {
            super(id, name, width);
        }

        @Override
        protected void onMouseDown(GridEvent<ModelData> ge) {
            El el = ge.getTargetEl();
            if (el != null && el.hasStyleName("x-grid3-cc-" + getId()) &&
                !el.hasStyleName("x-grid3-check-col-disabled")) {
                ge.stopEvent();
                UserPermissionDTO m = (UserPermissionDTO) ge.getModel();
                String property = grid.getColumnModel().getColumnId(ge.getColIndex());
                Record r = store.getRecord(m);
                Boolean b = (Boolean) m.get(getDataIndex());
                if (validateChange(m, property)) {
                    boolean newValue = b == null ? true : !b;
                    r.set(getDataIndex(), newValue);
                    onRowEdit(property, newValue, r);
                }
            }
        }
    }

    private class UserProxy extends RpcProxy<UserResult> {

        @Override
        protected void load(Object loadConfig, final AsyncCallback<UserResult> callback) {

            PagingLoadConfig config = (PagingLoadConfig) loadConfig;
            GetUsers command = new GetUsers(db.getId());
            command.setOffset(config.getOffset());
            command.setLimit(config.getLimit());
            command.setSortInfo(config.getSortInfo());
            dispatcher.execute(command, new AsyncCallback<UserResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }

                @Override
                public void onSuccess(UserResult result) {
                    callback.onSuccess(result);
                }
            });
        }
    }
}
