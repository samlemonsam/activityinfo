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
            fly(cell).addStyleName("cell-hover");
            overCell = cell;
            toolTip.showAt((Window.getClientWidth() - TOOLTIP_WIDTH) / 2, 0);
        }
    }

    private void onCellOut(Element cell) {
        fly(cell).removeStyleName("cell-hover");
        overCell = null;
        toolTip.hide();
    }

    @Override
    protected void doDetach() {
        super.doDetach();
        toolTip.hide();
    }
}
