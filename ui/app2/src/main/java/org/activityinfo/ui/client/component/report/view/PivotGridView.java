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
package org.activityinfo.ui.client.component.report.view;

import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.page.common.ApplicationBundle;

import java.util.Objects;

/**
 * Overrides the GridView to provide hovering on cells and
 */
public class PivotGridView extends GridView {

    private static final int TOOLTIP_WIDTH = 150;

    private Element overCell = null;
    private final ToolTip toolTip;

    public PivotGridView() {
        ToolTipConfig config = new ToolTipConfig();
        config.setTitle(I18N.CONSTANTS.drillDownTipHeading());
        config.setText(I18N.CONSTANTS.drillDownTip());
        config.setAnchor(null);
        config.setAnchorToTarget(false);
        config.setMinWidth(TOOLTIP_WIDTH);
        config.setMaxWidth(TOOLTIP_WIDTH);

        toolTip = new ToolTip();
        toolTip.update(config);
    }

    @Override
    protected void handleComponentEvent(GridEvent ge) {
        Element cell;
        switch (ge.getEventTypeInt()) {
            case Event.ONMOUSEMOVE:
                cell = getCell(ge.getRowIndex(), ge.getColIndex());
                if (!Objects.equals(cell, overCell)) {
                    if (overCell != null) {
                        onCellOut(overCell);
                    }
                    if (cell != null) {
                        onCellOver(cell);
                    }
                }
                break;

            case Event.ONMOUSEOVER:
                EventTarget from = ge.getEvent().getRelatedEventTarget();
                if (from == null || (Element.is(from) && !grid.getElement().isOrHasChild(Element.as(from)))) {
                    cell = getCell(ge.getRowIndex(), ge.getColIndex());
                    if (cell != null) {
                        onCellOver(cell);
                    }
                }
                break;
            case Event.ONMOUSEOUT:
                EventTarget to = ge.getEvent().getRelatedEventTarget();
                if (to == null || (Element.is(to) && !grid.getElement().isOrHasChild(Element.as(to)))) {
                    if (overCell != null) {
                        onCellOut(overCell);
                    }
                }
                break;
            case Event.ONMOUSEDOWN:
            case Event.ONSCROLL:
                super.handleComponentEvent(ge);
                break;
        }
    }

    private void onCellOver(Element cell) {
        if ("value".equals(cell.getAttribute("data-pivot"))) {
            fly(cell).addStyleName(ApplicationBundle.INSTANCE.styles().cellHover());
            overCell = cell;
            toolTip.showAt((Window.getClientWidth() - TOOLTIP_WIDTH) / 2, 0);
        }
    }

    private void onCellOut(Element cell) {
        fly(cell).removeStyleName(ApplicationBundle.INSTANCE.styles().cellHover());
        overCell = null;
        toolTip.hide();
    }

    @Override
    protected void doDetach() {
        super.doDetach();
        toolTip.hide();
    }
}
