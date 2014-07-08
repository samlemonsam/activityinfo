package org.activityinfo.ui.client.component.formdesigner;
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
import com.google.gwt.user.client.ui.Widget;

/**
 * @author yuriyz on 07/07/2014.
 */
public class FormDesigner {

    private final ControlBucketBuilder controlBucketBuilder;
    private final PickupDragController dragController;

    public FormDesigner(FormDesignerPanel formDesignerPanel) {
        dragController = new PickupDragController(formDesignerPanel.getContainerPanel(), false);
        dragController.setBehaviorMultipleSelection(false);

        controlBucketBuilder = new ControlBucketBuilder(formDesignerPanel.getControlBucket(), dragController);
        controlBucketBuilder.build();

        dragController.addDragHandler(new ControlDnDHandler());

        DropPanelDropController widgetDropController = new DropPanelDropController(formDesignerPanel.getDropPanel(), this);
        dragController.registerDropController(widgetDropController);
    }

    public ControlType getControlType(Widget widget) {
        return controlBucketBuilder.getControlMap().inverse().get(widget);
    }
}
