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
package org.activityinfo.ui.client.table.view;

import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.TabItemConfig;
import com.sencha.gxt.widget.core.client.TabPanel;
import org.activityinfo.analysis.table.TableViewModel;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.store.FormStore;

/**
 * Sidebar panel containing details, history, etc.
 */
public class SidePanel extends ContentPanel {

    public SidePanel(FormStore formStore, TableViewModel viewModel) {
        setHeading("Side Panel");
        setHeaderVisible(false);
        TabPanel tabPanel = new TabPanel();
        tabPanel.add(new DetailsPane(viewModel), new TabItemConfig(I18N.CONSTANTS.details()));
        tabPanel.add(new HistoryPane(formStore, viewModel), new TabItemConfig(I18N.CONSTANTS.history()));
        tabPanel.add(new ApiPane(viewModel), new TabItemConfig("API"));
        add(tabPanel);
    }

}
