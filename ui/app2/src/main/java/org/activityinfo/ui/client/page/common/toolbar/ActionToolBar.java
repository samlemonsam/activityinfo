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
package org.activityinfo.ui.client.page.common.toolbar;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

/**
 * Convenience subclass for the GXT toolbar that directs all tool actions
 * through a common choke point implementing
 * {@link org.activityinfo.ui.client.page.common.toolbar.ActionListener}
 * <p/>
 * Also centralizes look&feel of common buttons like New, Edit, Refresh, Save,
 * etc.
 */
public class ActionToolBar extends ToolBar implements Listener<ButtonEvent> {

    private ActionListener listener;
    private SplitButton saveSplitButton;
    private Button saveButton;
    private Button addButton;
    private Button removeButton;
    private Button editButton;
    private Button uploadButton;

    public ActionToolBar() {
    }

    public ActionToolBar(ActionListener listener) {
        this.listener = listener;
        setEnabled(listener != null);
    }

    /**
     * @param actionId The id to be provided to the
     *                 {@link org.activityinfo.ui.client.page.common.toolbar.ActionListener}
     *                 if the button is selected
     * @param text     Text of the button
     * @param icon     Icon of the button. See
     *                 {@link org.activityinfo.ui.client.style.legacy.icon.IconImageBundle}
     */
    public Button addButton(String actionId, String text, AbstractImagePrototype icon) {
        Button button = new Button(text, icon);
        button.setItemId(actionId);
        button.addListener(Events.Select, this);
        add(button);

        return button;
    }

    public void add(Iterable<? extends Component> buttons) {
        for (Component component : buttons) {
            add(component);
        }
    }

    public void addCreateButton() {
        this.addButton = addButton(UIActions.ADD, I18N.CONSTANTS.addItem(), IconImageBundle.ICONS.add());

    }

    public void addPrintButton() {
        addButton(UIActions.PRINT, I18N.CONSTANTS.printForm(), IconImageBundle.ICONS.printer());
    }

    public void addUploadButton() {
        addButton(UIActions.UPLOAD, I18N.CONSTANTS.upload(), IconImageBundle.ICONS.up());

    }

    public void addEditButton(AbstractImagePrototype icon) {
        this.editButton = addButton(UIActions.EDIT, I18N.CONSTANTS.edit(), icon);
    }

    public void addDeleteButton() {
        this.removeButton = addButton(UIActions.DELETE, I18N.CONSTANTS.delete(), IconImageBundle.ICONS.delete());
    }

    public void addDeleteButton(String text) {
        this.removeButton = addButton(UIActions.DELETE, text, IconImageBundle.ICONS.delete());
    }

    public void addExcelExportButton() {
        addButton(UIActions.EXPORT, I18N.CONSTANTS.export(), IconImageBundle.ICONS.excel());
    }

    public void addSaveSplitButton() {
        saveSplitButton = new SplitButton(I18N.CONSTANTS.save());
        saveSplitButton.setIcon(IconImageBundle.ICONS.save());
        saveSplitButton.setItemId(UIActions.SAVE);
        saveSplitButton.addListener(Events.Select, this);

        Menu menu = new Menu();
        MenuItem saveItem = new MenuItem(I18N.CONSTANTS.save(),
                IconImageBundle.ICONS.save(),
                new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        if (listener != null) {
                            listener.onUIAction(UIActions.SAVE);
                        }
                    }
                });
        menu.add(saveItem);

        MenuItem discardItem = new MenuItem(I18N.CONSTANTS.discardChanges(),
                IconImageBundle.ICONS.cancel(),
                new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        listener.onUIAction(UIActions.DISCARD_CHANGES);
                    }
                });
        menu.add(discardItem);

        saveSplitButton.setMenu(menu);

        add(saveSplitButton);
    }

    public void addSaveButton() {
        this.saveButton = addButton(UIActions.SAVE, I18N.CONSTANTS.save(), IconImageBundle.ICONS.save());
    }

    public void setDirty(boolean dirty) {
        Button currentSaveButton = getSaveButton();

        if (currentSaveButton != null) {
            currentSaveButton.setEnabled(dirty);
            if (dirty) {
                currentSaveButton.setText(I18N.CONSTANTS.save());
                currentSaveButton.setIcon(IconImageBundle.ICONS.save());
            } else {
                currentSaveButton.setText(I18N.CONSTANTS.saved());
            }
        }
    }

    private Button getSaveButton() {
        return saveSplitButton == null ? saveButton : saveSplitButton;
    }

    @Override
    public void handleEvent(ButtonEvent be) {
        if (listener != null) {
            listener.onUIAction(be.getButton().getItemId());
        }
    }

    public void setUploadEnabled(boolean enabled) {
        if (uploadButton != null) {
            uploadButton.setEnabled(enabled);
        }
    }

    public void setActionEnabled(String actionId, boolean enabled) {
        Component c = getItemByItemId(actionId);

        if (c != null) {
            c.setEnabled(enabled);
        } else {
            Log.warn("ActionToolBar: setActionEnabled(" + actionId + ") was called, but button is not present");
        }
    }

    public void setDeleteEnabled(boolean enabled) {
        if (removeButton != null) {
            removeButton.setEnabled(enabled);
        }
    }

    public void setAddEnabled(boolean enabled) {
        if (addButton != null) {
            addButton.setEnabled(enabled);
        }
    }

    public void setUpdateEnabled(boolean enabled) {
        if (editButton != null) {
            editButton.setEnabled(enabled);
        }
    }

    public void setListener(ActionListener listener) {
        this.listener = listener;
        setEnabled(listener != null);
    }

    public void addImportButton() {
        addButton(UIActions.IMPORT, I18N.CONSTANTS.importText(), IconImageBundle.ICONS.importIcon());
    }

    public void addTransferButton() {
        addButton(UIActions.TRANSFER_DATABASE, I18N.CONSTANTS.transferDatabaseLabel(), IconImageBundle.ICONS.user());
    }

    public void addCancelTransferButton() {
        addButton(UIActions.CANCEL_TRANSFER, I18N.CONSTANTS.cancelTransfer(), IconImageBundle.ICONS.deleteUser());
    }

}
