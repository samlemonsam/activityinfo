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

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerConstants;
import org.activityinfo.ui.client.component.formdesigner.drag.DragMonitor;

import java.util.Map;

/**
 * @author yuriyz on 12/16/2014.
 */
public class DropControllerRegistry {

    private final Map<ResourceId, DropControllerExtended> dropControllerMap = Maps.newHashMap();

    private final PickupDragController dragController;

    // Used to check whether widget was dropped after Drag gesture. If it was not dropped then
    // simulate "click" and drop widget at the end of the form.
    private final DragMonitor dragMonitor;

    private final FormDesigner formDesigner;

    public DropControllerRegistry(FormDesigner formDesigner) {
        this.formDesigner = formDesigner;
        this.dragMonitor = new DragMonitor(formDesigner);
        this.dragController = createDragController();
    }

    private PickupDragController createDragController() {
        PickupDragController dragController = new PickupDragController(RootPanel.get(), false) {
            @Override
            public void dragStart() {
                super.dragStart();
                dragMonitor.start(context);
            }

            @Override
            public void dragEnd() {
                dragMonitor.dragEnd(); // monitor must finish drag end first while context is still valid
                super.dragEnd();
            }
        };
        dragController.setBehaviorMultipleSelection(false);
        return dragController;
    }

    public DropControllerExtended register(ResourceId resourceId, FlowPanel dropTarget, FormDesigner formDesigner) {
        DropPanelDropController dropController = new DropPanelDropController(resourceId, dropTarget, formDesigner);
        register(resourceId, dropController);
        return dropController;
    }

    public DropControllerExtended register(ResourceId resourceId, DropControllerExtended controller) {
        Preconditions.checkNotNull(resourceId);
        Preconditions.checkNotNull(controller);

        if (dropControllerMap.containsKey(resourceId)) {
            throw new RuntimeException("Drop controller for this resource already registered, resourceId: " + resourceId);
        }

        dropControllerMap.put(resourceId, controller);
        dragController.registerDropController(controller);

        // store owner id with drop target (to be able reference dropController from dropTarget)
        controller.getDropTarget().getElement().setAttribute(FormDesignerConstants.OWNER_ID, resourceId.asString());

        return controller;
    }

    public DropControllerExtended getRootDropController() {
        return getDropController(formDesigner.getRootFormClass().getId());
    }

    public DropControllerExtended getDropController(Widget dropTarget) {
        String ownerId = dropTarget.getElement().getAttribute(FormDesignerConstants.OWNER_ID);
        return getDropController(ResourceId.valueOf(ownerId));
    }

    public DropControllerExtended getDropController(ResourceId resourceId) {
        Preconditions.checkNotNull(resourceId);

        DropControllerExtended dropController = dropControllerMap.get(resourceId);
        Preconditions.checkNotNull(resourceId);
        return dropController;
    }

    public PickupDragController getDragController() {
        return dragController;
    }

    public void unregister(ResourceId resourceId) {
        Preconditions.checkNotNull(resourceId);

        dragController.unregisterDropController(dropControllerMap.get(resourceId));
        dropControllerMap.remove(resourceId);

    }
}
