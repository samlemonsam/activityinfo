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
package org.activityinfo.ui.client.page.config.design;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

class DbEditorMenu {
    private MenuItem newIndicator;
    private MenuItem newAttributeGroup;
    private MenuItem newAttribute;
    private MenuItem newFolder;
    private MenuItem newActivity;
    private MenuItem newForm;
    private MenuItem newLocationType;
    private Menu menu;


    public DbEditorMenu(SelectionListener<MenuEvent> listener) {
        newFolder = new MenuItem(I18N.CONSTANTS.newFolder(), IconImageBundle.ICONS.folder(), listener);
        newFolder.setItemId("Folder");
        newFolder.setEnabled(false);

        newActivity = new MenuItem(I18N.CONSTANTS.newClassicActivity(), IconImageBundle.ICONS.addActivity(), listener);
        newActivity.setItemId("Activity");
        newActivity.setEnabled(false);

        newForm = new MenuItem(I18N.CONSTANTS.newForm(), IconImageBundle.ICONS.form(), listener);
        newForm.setItemId("Form");
        newForm.setEnabled(false);

        newLocationType = new MenuItem(
                I18N.CONSTANTS.newLocationType(),
                IconImageBundle.ICONS.marker(), listener);
        newLocationType.setItemId("LocationType");
        newLocationType.setEnabled(false);

        newAttributeGroup = newMenuItem("AttributeGroup",
                I18N.CONSTANTS.newAttributeGroup(),
                IconImageBundle.ICONS.attribute(),
                listener);
        newAttributeGroup.setEnabled(false);

        newAttribute = newMenuItem("Attribute",
                I18N.CONSTANTS.newAttribute(),
                IconImageBundle.ICONS.attribute(),
                listener);
        newAttribute.setEnabled(false);

        newIndicator = new MenuItem(I18N.CONSTANTS.newIndicator(),
                IconImageBundle.ICONS.indicator(),
                listener);
        newIndicator.setItemId("Indicator");
        newIndicator.setEnabled(false);

        menu = new Menu();
        menu.add(newFolder);
        menu.add(newActivity);
        menu.add(newForm);
        menu.add(newLocationType);
        menu.add(newAttributeGroup);
        menu.add(newAttribute);
        menu.add(newIndicator);
    }


    private MenuItem newMenuItem(String itemId,
                                 String label,
                                 AbstractImagePrototype icon,
                                 SelectionListener<MenuEvent> listener) {
        final MenuItem newMenuItem = new MenuItem(label, icon, listener);
        newMenuItem.setItemId(itemId);
        return newMenuItem;
    }


    public Menu asMenu() {
        return menu;
    }

    public void setNewFolderEnabled(boolean enabled) {
        newFolder.setEnabled(enabled);
    }

    public void setNewActivityEnabled(boolean enabled) {
        newActivity.setEnabled(enabled);
    }

    public void setNewFormEnabled(boolean enabled) {
        newForm.setEnabled(enabled);
    }

    public void setNewLocationTypeEnabled(boolean enabled) {
        newLocationType.setEnabled(enabled);
    }

    public void setNewAttributeGroupEnabled(boolean enabled) {
        newAttributeGroup.setEnabled(enabled);
    }

    public void setNewIndicatorEnabled(boolean enabled) {
        newIndicator.setEnabled(enabled);
    }

    public void setNewAttributeEnabled(boolean enabled) {
        newAttribute.setEnabled(enabled);
    }
}
