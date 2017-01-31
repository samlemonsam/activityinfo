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
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerConstants;
import org.activityinfo.ui.client.component.formdesigner.drag.IsDraggable;

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

    public FieldPalette() {
        this.panel = new AbsolutePanel();

        List<Template> templates = Templates.list();
        for (int i = 0; i != templates.size(); ++i) {
            int row = (i / NUM_COLUMNS);
            int column = (i % NUM_COLUMNS);

            DnDLabel draggableLabel = new DnDLabel(templates.get(i));
            panel.add(draggableLabel, calculateLeft(column), calculateTop(row));
        }

        int rowCount = Math.round((float) templates.size() / NUM_COLUMNS);

        panel.setHeight(calculateTop(rowCount) + "px");
    }

    public void makeDraggable(PickupDragController dragController) {
        for (int i = 0; i < panel.getWidgetCount(); i++) {
            Widget widget = panel.getWidget(i);
            if (widget instanceof IsDraggable) {
                dragController.makeDraggable(widget);
            }
        }
    }

    @Override
    public Widget asWidget() {
        return panel;
    }

    public void setWidth(String width) {
        this.panel.setWidth(width);
    }

    private int calculateTop(int row) {
        return FormDesignerConstants.SOURCE_CONTROL_INITIAL_TOP +
                (FormDesignerConstants.SOURCE_CONTROL_HEIGHT_PX * row);
    }

    private int calculateLeft(int column) {
        return FormDesignerConstants.SOURCE_CONTROL_INITIAL_LEFT +
                (FormDesignerConstants.SOURCE_CONTROL_WIDTH_PX * column) +
                (FormDesignerConstants.SOURCE_CONTROL_MARGIN_RIGHT * column);
    }
}
