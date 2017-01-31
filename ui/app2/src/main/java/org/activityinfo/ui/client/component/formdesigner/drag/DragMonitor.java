package org.activityinfo.ui.client.component.formdesigner.drag;
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

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.drop.DropControllerExtended;
import org.activityinfo.ui.client.component.formdesigner.event.PanelUpdatedEvent;
import org.activityinfo.ui.client.component.formdesigner.palette.DnDLabel;

/**
 * Used to check whether widget was dropped after Drag gesture. If it was not dropped then
 * simulate "click" and drop widget at the end of the form.
 *
 * @author yuriyz on 11/13/2014.
 */
public class DragMonitor {

    private final FormDesigner formDesigner;

    private boolean widgetAdded = false;
    private DragContext dragContext = null;

    public DragMonitor(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        this.formDesigner.getEventBus().addHandler(PanelUpdatedEvent.TYPE, new PanelUpdatedEvent.Handler() {
            @Override
            public void handle(PanelUpdatedEvent event) {
                if (event.getType() == PanelUpdatedEvent.EventType.ADDED) {
                    widgetAdded = true;
                }
            }
        });
    }

    public void start(DragContext dragContext) {
        reset();
        this.dragContext = dragContext;
    }

    private void reset() {
        widgetAdded = false;
        dragContext = null;
    }

    public void dragEnd() {
        try {
            if (!widgetAdded && dragContext != null && dragContext.draggable instanceof DnDLabel) {
                DropControllerExtended dropController = formDesigner.getDropControllerRegistry().getRootDropController();
                dropController.onEnter(dragContext); // force to create positioner
                dropController.setPositionerToEnd(); // set it to end
                dropController.onPreviewDrop(dragContext); // drop
            }
        } catch (VetoDragException e) {
            // do nothing
        } finally {
            reset();
        }
    }
}
