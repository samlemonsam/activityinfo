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
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.reports.model.layers.MapLayer;
import org.activityinfo.legacy.shared.reports.model.layers.PolygonMapLayer;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.widget.wizard.Wizard;
import org.activityinfo.ui.client.widget.wizard.WizardPage;

/**
 * Displays a modal window enabling the user to add a layer by selecting one or
 * more indicators
 */
public final class NewLayerWizard extends Wizard {

    private IndicatorPage indicatorPage;
    private ResourceLocator locator;
    private LayerTypePage layerTypePage;
    private AdminLevelPage adminLevelPage;

    public NewLayerWizard(Dispatcher dispatcher, ResourceLocator locator) {
        indicatorPage = new IndicatorPage(dispatcher, locator);
        this.locator = locator;
        indicatorPage.addListener(Events.SelectionChange, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                onIndicatorsChanged();
            }
        });
        layerTypePage = new LayerTypePage();
        layerTypePage.addListener(Events.Change, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                onTypeChanged();
            }
        });
        adminLevelPage = new AdminLevelPage(dispatcher);
        adminLevelPage.addListener(Events.Change, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                onTypeChanged();
            }
        });

    }

    private void onIndicatorsChanged() {
        adminLevelPage.setIndicators(indicatorPage.getSelectedIds());
        fireEvent(Events.Change, new BaseEvent(Events.Change));
    }

    private void onTypeChanged() {
        fireEvent(Events.Change, new BaseEvent(Events.Change));
    }

    public MapLayer createLayer() {
        MapLayer layer = layerTypePage.newLayer();
        layer.setName(composeName());
        for (Integer indicatorId : indicatorPage.getSelectedIds()) {
            layer.addIndicatorId(indicatorId);
        }
        if (layer instanceof PolygonMapLayer) {
            ((PolygonMapLayer) layer).setAdminLevelId(adminLevelPage.getSelectedLevelId());
        }
        return layer;
    }

    private String composeName() {
        return indicatorPage.getSelection().iterator().next().getName();
    }

    @Override
    public WizardPage[] getPages() {
        return new WizardPage[]{indicatorPage, layerTypePage, adminLevelPage};
    }

    @Override
    public String getTitle() {
        return I18N.CONSTANTS.addLayer();
    }

    @Override
    public boolean isPageEnabled(WizardPage page) {
        if (page == adminLevelPage) {
            return layerTypePage.newLayer() instanceof PolygonMapLayer;
        } else {
            return true;
        }
    }

    @Override
    public boolean isFinishEnabled() {
        if (indicatorPage.getSelection().isEmpty()) {
            return false;
        }
        if (layerTypePage.newLayer() instanceof PolygonMapLayer && adminLevelPage.getSelectedLevelId() == null) {
            return false;
        }
        return true;
    }
}
