package org.activityinfo.ui.client.component.formdesigner.drop;
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
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * DnD library doesn't support multiple drop controllers for the same drop panel. As workaround we forward calls.
 *
 * @author yuriyz on 7/8/14.
 */
public class ForwardDropController implements DropControllerExtended {

    private final List<DropControllerExtended> controllers = Lists.newArrayList();

    private final Widget dropTarget;

    public ForwardDropController(Widget dropTarget) {
        this.dropTarget = dropTarget;
    }

    @Override
    public Widget getDropTarget() {
        return dropTarget;
    }

    public void add(DropControllerExtended controller) {
        controllers.add(controller);
    }

    @Override
    public void onDrop(DragContext context) {
        for (DropController dropController : controllers) {
            dropController.onDrop(context);
        }
    }

    @Override
    public void onEnter(DragContext context) {
        for (DropController dropController : controllers) {
            dropController.onEnter(context);
        }
    }

    @Override
    public void onLeave(DragContext context) {
        for (DropController dropController : controllers) {
            dropController.onLeave(context);
        }
    }

    @Override
    public void onMove(DragContext context) {
        for (DropController dropController : controllers) {
            dropController.onMove(context);
        }
    }

    @Override
    public void onPreviewDrop(DragContext context) throws VetoDragException {
        for (DropController dropController : controllers) {
            dropController.onPreviewDrop(context);
        }
    }

    @Override
    public void setPositionerToEnd() {
        for (DropControllerExtended dropController : controllers) {
            dropController.setPositionerToEnd();
        }
    }
}
