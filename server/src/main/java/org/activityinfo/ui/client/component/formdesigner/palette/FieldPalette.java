package org.activityinfo.ui.client.component.formdesigner.palette;
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

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.component.formdesigner.Metrics;
import org.activityinfo.ui.client.component.formdesigner.drop.ForwardDropController;

import java.util.List;

/**
 * A panel containing a list of FieldTemplates that the user
 * can drag on the FormPanel
 *
 * @author yuriyz on 07/07/2014.
 */
public class FieldPalette implements IsWidget {

    public static final int NUM_COLUMNS = 1;

    private final AbsolutePanel panel;
    private final PickupDragController dragController;

    // Used to check whether widget was dropped after Drag gesture. If it was not dropped then
    // simulate "click" and drop widget at the end of the form.
    private DragMonitor dragMonitor;

    public FieldPalette() {
        this.panel = new AbsolutePanel();
        dragController = new PickupDragController(RootPanel.get(), false) {
            @Override
            public void dragStart() {
                super.dragStart();
                dragMonitor.start(context);
            }

            @Override
            public void dragEnd() {
                dragMonitor.dragEnd(); // monitor must finished drag end first while context is still valid
                super.dragEnd();
            }
        };
        dragController.setBehaviorMultipleSelection(false);

        List<FieldTemplate> templates = FieldTemplates.list();
        for (int i = 0; i != templates.size(); ++i) {
            int row = (i / NUM_COLUMNS);
            int column = (i % NUM_COLUMNS);

            FieldLabel draggableLabel = new FieldLabel(templates.get(i));
            dragController.makeDraggable(draggableLabel);
            panel.add(draggableLabel, calculateLeft(column), calculateTop(row));
        }

        int rowCount = Math.round((float) templates.size() / NUM_COLUMNS);

        panel.setHeight(calculateTop(rowCount) + "px");
    }

    public void bind(EventBus eventBus, ForwardDropController dropController) {
        this.dragController.registerDropController(dropController);
        this.dragMonitor = new DragMonitor(eventBus, dropController);
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public void setWidth(String width) {
        this.panel.setWidth(width);
    }

    private int calculateTop(int row) {
        return Metrics.SOURCE_CONTROL_INITIAL_TOP +
                (Metrics.SOURCE_CONTROL_HEIGHT_PX * row);
    }

    private int calculateLeft(int column) {
        return Metrics.SOURCE_CONTROL_INITIAL_LEFT +
                (Metrics.SOURCE_CONTROL_WIDTH_PX * column) +
                (Metrics.SOURCE_CONTROL_MARGIN_RIGHT * column);
    }

    public PickupDragController getDragController() {
        return dragController;
    }

}
