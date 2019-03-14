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
package org.activityinfo.ui.client.component.report.editor.map;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.ui.client.component.filter.IndicatorTreePanel;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.widget.wizard.WizardPage;

import java.util.Collection;
import java.util.List;

public class IndicatorPage extends WizardPage {
    // List of all indicators
    private IndicatorTreePanel treePanel;

    public IndicatorPage(Dispatcher service, ResourceLocator locator) {

        VBoxLayout pageLayout = new VBoxLayout();
        pageLayout.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
        pageLayout.setPadding(new Padding(15));
        setLayout(pageLayout);

        Text header = new Text(I18N.CONSTANTS.chooseIndicatorsToMap());
        header.setTagName("h2");
        add(header);

        VBoxLayoutData indicatorLayout = new VBoxLayoutData();
        indicatorLayout.setFlex(1);

        treePanel = new IndicatorTreePanel(service, locator);
        treePanel.setHeaderVisible(false);
        treePanel.setLeafCheckableOnly();
        treePanel.addCheckChangedListener(new Listener<TreePanelEvent>() {
            @Override
            public void handleEvent(TreePanelEvent be) {
                IndicatorPage.this.fireEvent(Events.SelectionChange, new BaseEvent(Events.SelectionChange));
            }
        });

        add(treePanel, indicatorLayout);
    }

    public Collection<IndicatorDTO> getSelection() {
        return treePanel.getSelection();
    }

    public List<Integer> getSelectedIds() {
        return treePanel.getSelectedIds();
    }

    @Override
    public boolean isNextEnabled() {
        return !treePanel.getSelection().isEmpty();
    }
}
