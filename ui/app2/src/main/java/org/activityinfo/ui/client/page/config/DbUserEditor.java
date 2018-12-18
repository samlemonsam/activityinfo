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
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.command.GetUsers;
import org.activityinfo.legacy.shared.command.result.UserResult;
import org.activityinfo.legacy.shared.model.*;
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

    private static final int VIEW_COL_INDEX = 4;
    private static final int CREATE_COL_INDEX = 6;
    private static final int EDIT_COL_INDEX = 8;
    private static final int DELETE_COL_INDEX = 10;
    private static final int MANAGE_USERS_COL_INDEX = 12;

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

    public void setModified(boolean modified) {
        this.modified = modified;
        toolBar.setDirty(modified);
    }

    @Override
    public void go(UserDatabaseDTO db) {
        this.db = db;
        store.removeAll();

        actions = new DbUserEditorActions(this, dispatcher, loader, store, grid, db);

        toolBar.setActionEnabled(UIActions.SAVE, false);
        toolBar.setActionEnabled(UIActions.ADD, db.isManageUsersAllowed());
        toolBar.setActionEnabled(UIActions.DELETE, false);

        grid.getColumnModel().getColumnById(PermissionType.VIEW_ALL.name()).setHidden(!db.isManageUsersAllowed());
        grid.getColumnModel().getColumnById(PermissionType.EDIT_ALL.name()).setHidden(!db.isManageUsersAllowed());
        grid.getColumnModel().getColumnById(PermissionType.MANAGE_USERS.name()).setHidden(!db.isManageAllUsersAllowed());
        grid.getColumnModel().getColumnById(PermissionType.MANAGE_ALL_USERS.name()).setHidden(!db.isManageAllUsersAllowed());
        grid.getColumnModel().getColumnById(PermissionType.DESIGN.name()).setHidden(!db.isDesignAllowed());

        loader.load();
        setModified(false);
    }

    private void createToolBar() {
        toolBar = new ActionToolBar(this);
        toolBar.addSaveButton();
        toolBar.addButton(UIActions.ADD, I18N.CONSTANTS.addUser(), IconImageBundle.ICONS.addUser());
        toolBar.addButton(UIActions.EDIT, I18N.CONSTANTS.edit(), IconImageBundle.ICONS.edit());
        toolBar.addButton(UIActions.DELETE, I18N.CONSTANTS.delete(), IconImageBundle.ICONS.deleteUser());
        toolBar.addButton(UIActions.EXPORT, I18N.CONSTANTS.export(), IconImageBundle.ICONS.excel());
        toolBar.addButton(UIActions.MAILING_LIST,
                I18N.CONSTANTS.CopyAddressToClipBoard(),
                IconImageBundle.ICONS.dataEntry());
        setTopComponent(toolBar);
    }

    private void createGrid() {

        loader = new BasePagingLoader<>(new UserProxy());
        loader.setRemoteSort(true);
        
        store = new ListStore<>(loader);
        store.setKeyProvider(model -> model.getEmail());
        store.addListener(Store.Update, event -> {
            setModified(!store.getModifiedRecords().isEmpty());
        });

        final List<ColumnConfig> columns = new ArrayList<ColumnConfig>();

        columns.add(new ColumnConfig("name", I18N.CONSTANTS.name(), 100));
        columns.add(new ColumnConfig("email", I18N.CONSTANTS.email(), 150));
        columns.add(new ColumnConfig("partner.name", I18N.CONSTANTS.partner(), 150));

        ColumnConfig folderColumn = new ColumnConfig("category", I18N.CONSTANTS.folders(), 150);
        folderColumn.setSortable(false);
        folderColumn.setRenderer(new GridCellRenderer() {
            @Override
            public SafeHtml render(ModelData modelData, String s, ColumnData columnData, int i, int i1, ListStore listStore, Grid grid) {
                if (modelData instanceof UserPermissionDTO) {
                    UserPermissionDTO permission = (UserPermissionDTO) modelData;
                    if (permission.hasFolderLimitation()) {
                        SafeHtmlBuilder html = new SafeHtmlBuilder();
                        boolean needsComma = false;
                        for (FolderDTO folder : permission.getFolders()) {
                            if (needsComma) {
                                html.appendHtmlConstant(", ");
                            }
                            html.appendEscaped(folder.getName());
                            needsComma = true;
                        }
                        return html.toSafeHtml();
                    }
                }
                return ALL_CATEGORIES;
            }
        });
        columns.add(folderColumn);

        PermissionCheckConfig allowView = buildColumn(PermissionType.VIEW, I18N.CONSTANTS.forThisPartner(), I18N.CONSTANTS.allowViewLong());
        PermissionCheckConfig allowViewAll = buildColumn(PermissionType.VIEW_ALL, I18N.CONSTANTS.forAllPartners(), I18N.CONSTANTS.allowViewAllLong());

        PermissionCheckConfig allowCreate = buildColumn(PermissionType.CREATE, I18N.CONSTANTS.forThisPartner(), I18N.CONSTANTS.allowCreateLong());
        PermissionCheckConfig allowCreateAll = buildColumn(PermissionType.CREATE_ALL, I18N.CONSTANTS.forAllPartners(), I18N.CONSTANTS.allowCreateAllLong());

        PermissionCheckConfig allowEdit = buildColumn(PermissionType.EDIT, I18N.CONSTANTS.forThisPartner(), I18N.CONSTANTS.allowEditLong());
        PermissionCheckConfig allowEditAll = buildColumn(PermissionType.EDIT_ALL, I18N.CONSTANTS.forAllPartners(), I18N.CONSTANTS.allowEditAllLong());

        PermissionCheckConfig allowDelete = buildColumn(PermissionType.DELETE, I18N.CONSTANTS.forThisPartner(), I18N.CONSTANTS.allowDeleteLong());
        PermissionCheckConfig allowDeleteAll = buildColumn(PermissionType.DELETE_ALL, I18N.CONSTANTS.forAllPartners(), I18N.CONSTANTS.allowDeleteAllLong());

        PermissionCheckConfig allowManageUsers = buildColumn(PermissionType.MANAGE_USERS, I18N.CONSTANTS.forThisPartner(), "");
        PermissionCheckConfig allowManageAllUsers = buildColumn(PermissionType.MANAGE_ALL_USERS, I18N.CONSTANTS.forAllPartners(), "");

        PermissionCheckConfig allowExport = buildColumn(PermissionType.EXPORT_RECORDS, I18N.CONSTANTS.allowExport(), I18N.CONSTANTS.allowExportLong());

        PermissionCheckConfig allowDesign = buildColumn(PermissionType.DESIGN, I18N.CONSTANTS.allowDesign(), I18N.CONSTANTS.allowDesignLong());

        columns.add(allowView);
        columns.add(allowViewAll);
        columns.add(allowCreate);
        columns.add(allowCreateAll);
        columns.add(allowEdit);
        columns.add(allowEditAll);
        columns.add(allowDelete);
        columns.add(allowDeleteAll);
        columns.add(allowManageUsers);
        columns.add(allowManageAllUsers);
        columns.add(allowExport);
        columns.add(allowDesign);

        ColumnModel columnModel = new ColumnModel(columns);
        addHeader(I18N.CONSTANTS.allowView(), VIEW_COL_INDEX, columnModel);
        addHeader(I18N.CONSTANTS.allowCreate(), CREATE_COL_INDEX, columnModel);
        addHeader(I18N.CONSTANTS.allowEdit(), EDIT_COL_INDEX, columnModel);
        addHeader(I18N.CONSTANTS.allowDelete(), DELETE_COL_INDEX, columnModel);
        addHeader(I18N.CONSTANTS.allowManageUsers(), MANAGE_USERS_COL_INDEX, columnModel);

        grid = new Grid<>(store, columnModel);
        grid.setLoadMask(true);
        grid.setSelectionModel(new GridSelectionModel<>());
        grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<UserPermissionDTO>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<UserPermissionDTO> se) {
                onSelectionChanged(se.getSelectedItem());
            }
        });
        grid.addListener(Events.DoubleClick, new Listener<GridEvent<UserPermissionDTO>>() {

            @Override
            public void handleEvent(GridEvent<UserPermissionDTO> event) {
                actions.edit(event.getModel());
            }
        });
        grid.addPlugin(allowViewAll);
        grid.addPlugin(allowCreate);
        grid.addPlugin(allowCreateAll);
        grid.addPlugin(allowEdit);
        grid.addPlugin(allowEditAll);
        grid.addPlugin(allowDelete);
        grid.addPlugin(allowDeleteAll);
        grid.addPlugin(allowManageUsers);
        grid.addPlugin(allowManageAllUsers);
        grid.addPlugin(allowExport);
        grid.addPlugin(allowDesign);
        add(grid);
    }

    private void addHeader(String header, int colIndex, ColumnModel columnModel) {
        HeaderGroupConfig headerConfig = new HeaderGroupConfig(SafeHtmlUtils.fromTrustedString(header),1,2);
        columnModel.addHeaderGroup(0, colIndex, headerConfig);
    }

    private PermissionCheckConfig buildColumn(PermissionType type, String label, String tooltip) {
        PermissionCheckConfig column = new PermissionCheckConfig(type.name(),
                label,
                75);
        column.setDataIndex(type.getDtoPropertyName());
        column.setToolTip(tooltip);
        column.setAlignment(Style.HorizontalAlignment.CENTER);
        return column;
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

    private boolean validateChange(UserPermissionDTO user, PermissionType permissionType) {

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

        // check if database user has a greater permission set than user - if not, then we cnanot change permissions
        if (!db.hasGreaterPermissions(user)) {
            return false;
        }

        // do not allow users to set rights they themselves do not have
        return db.canGivePermission(permissionType, user);
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

    private void onRowEdit(PermissionType permissionType, boolean value, Record record) {
        record.beginEdit();
        if (!value) {
            // Cascade remove permissions
            if (permissionType == PermissionType.VIEW_ALL) {
                record.set(PermissionType.CREATE_ALL.getDtoPropertyName(), false);
                record.set(PermissionType.EDIT_ALL.getDtoPropertyName(), false);
                record.set(PermissionType.DELETE_ALL.getDtoPropertyName(), false);
            }
            if (permissionType == PermissionType.CREATE) {
                record.set(PermissionType.CREATE_ALL.getDtoPropertyName(), false);
            }
            if (permissionType == PermissionType.EDIT) {
                record.set(PermissionType.EDIT_ALL.getDtoPropertyName(), false);
            }
            if (permissionType == PermissionType.DELETE) {
                record.set(PermissionType.DELETE_ALL.getDtoPropertyName(), false);
            }
            if (permissionType == PermissionType.MANAGE_USERS) {
                record.set(PermissionType.MANAGE_ALL_USERS.getDtoPropertyName(), false);
            }
        } else {
            // cascade add permissions
            if (permissionType == PermissionType.CREATE_ALL) {
                record.set(PermissionType.CREATE.getDtoPropertyName(), true);
                record.set(PermissionType.VIEW_ALL.getDtoPropertyName(), true);
            } else if (permissionType == PermissionType.EDIT_ALL) {
                record.set(PermissionType.EDIT.getDtoPropertyName(), true);
                record.set(PermissionType.VIEW_ALL.getDtoPropertyName(), true);
            } else if (permissionType == PermissionType.DELETE_ALL) {
                record.set(PermissionType.DELETE.getDtoPropertyName(), true);
                record.set(PermissionType.VIEW_ALL.getDtoPropertyName(), true);
            } else if (permissionType == PermissionType.MANAGE_ALL_USERS) {
                record.set(PermissionType.MANAGE_USERS.getDtoPropertyName(), true);
            }
        }

        record.endEdit();
        setModified(!store.getModifiedRecords().isEmpty());
    }

    private void onSelectionChanged(UserPermissionDTO selectedItem) {

        if (selectedItem != null) {
            PartnerDTO selectedPartner = selectedItem.getPartner();

            toolBar.setActionEnabled(UIActions.DELETE,
                    db.isManageAllUsersAllowed() ||
                    (db.isManageUsersAllowed() && db.getMyPartnerId() == selectedPartner.getId()));
        }
        toolBar.setActionEnabled(UIActions.DELETE, selectedItem != null);
        toolBar.setActionEnabled(UIActions.DELETE, selectedItem != null);
    }


    private void edit(UserPermissionDTO model) {
        actions.edit(model);
    }

    @Override
    public void onUIAction(String actionId) {
        if (actionId.equals(UIActions.SAVE)) {
            actions.save();
        } else if (actionId.equals(UIActions.ADD)) {
            actions.add();
        } else if (actionId.equals(UIActions.EDIT)) {
            actions.edit(grid.getSelectionModel().getSelectedItem());
        } else if (actionId.equals(UIActions.DELETE)) {
            actions.delete();
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
                PermissionType permission = PermissionType.valueOf(grid.getColumnModel().getColumnId(ge.getColIndex()));
                Record r = store.getRecord(m);
                Boolean b = m.get(getDataIndex());
                if (validateChange(m, permission)) {
                    boolean newValue = b == null ? true : !b;
                    r.set(getDataIndex(), newValue);
                    onRowEdit(permission, newValue, r);
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
