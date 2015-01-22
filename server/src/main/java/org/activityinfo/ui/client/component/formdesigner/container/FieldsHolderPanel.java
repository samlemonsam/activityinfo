package org.activityinfo.ui.client.component.formdesigner.container;
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

import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerStyles;

/**
 * @author yuriyz on 12/30/2014.
 */
public class FieldsHolderPanel implements WidgetContainer {

    private final FormDesigner formDesigner;
    private final ResourceId parentId;
    private final FieldPanel panel;

    public FieldsHolderPanel(FormDesigner formDesigner, ResourceId parentId) {
        this.formDesigner = formDesigner;
        this.parentId = parentId;
        this.panel = new FieldPanel(formDesigner) {
            @Override
            public String getSelectedClassName() {
                return FormDesignerStyles.INSTANCE.sectionWidgetContainerSelected();
            }
        };
        this.panel.getLabel().addStyleName(FormDesignerStyles.INSTANCE.sectionLabel());
    }

    @Override
    public ResourceId getParentId() {
        return parentId;
    }

    @Override
    public void syncWithModel() {
        // do nothing
    }

    public FieldPanel getPanel() {
        return panel;
    }

    @Override
    public Widget asWidget() {
        return panel.asWidget();
    }

    @Override
    public Widget getDragHandle() {
        return panel.getDragHandle();
    }

    @Override
    public FormDesigner getFormDesigner() {
        return formDesigner;
    }
}
