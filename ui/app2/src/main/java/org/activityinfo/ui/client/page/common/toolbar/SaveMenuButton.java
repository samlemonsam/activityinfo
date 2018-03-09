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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.page.report.ReportDesignPage;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

import java.util.Arrays;
import java.util.List;

/**
 * SplitButton that provides users with a choice to "Save" or "Save As"
 */
public class SaveMenuButton extends SplitButton {

    private Menu menu;
    private SelectionListener<MenuEvent> menuListener;
    private ReportDesignPage.SaveCallback callback;
    private List<SaveMethod> methods;

    public SaveMenuButton() {
        this.setText(I18N.CONSTANTS.save());

        this.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                if (callback != null) {
                    callback.save(methods.get(0));
                }
            }
        });

        menuListener = new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent menuEvent) {
                callback.save((SaveMethod) menuEvent.getItem().getData("method"));
            }
        };

        menu = new Menu();
        setMenu(menu);

        setMethods(Arrays.asList(SaveMethod.values()));
    }

    public void setMethods(List<SaveMethod> methods) {
        this.methods = methods;
        setIcon(IconImageBundle.ICONS.save());
        menu.removeAll();
        for (SaveMethod method : methods) {
            MenuItem word = new MenuItem(formatLabel(method), IconImageBundle.ICONS.save(), menuListener);
            word.setData("method", method);
            menu.add(word);
        }
    }

    public SaveMenuButton setCallback(ReportDesignPage.SaveCallback callback) {
        this.callback = callback;
        return this;
    }

    private String formatLabel(SaveMethod method) {
        switch (method) {
            case SAVE:
                return I18N.CONSTANTS.save();
            case SAVEAS:
                return I18N.CONSTANTS.saveAs();
        }
        throw new IllegalArgumentException("Undefined save method [" + method.name() + "]");
    }


}
