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

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormSection;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.component.formdesigner.FormDesignerStyles;
import org.activityinfo.ui.client.component.formdesigner.event.WidgetContainerSelectionEvent;

/**
 * @author yuriyz on 7/14/14.
 */
public class SectionWidgetContainer implements WidgetContainer {

    private FormDesigner formDesigner;
    private FormSection formSection;
    private final SectionPanel sectionPanel;
    private final ResourceId parentId;

    public SectionWidgetContainer(final FormDesigner formDesigner, final FormSection formSection, ResourceId parentId) {
        this.formDesigner = formDesigner;
        this.formSection = formSection;
        this.parentId = parentId;

        sectionPanel = new SectionPanel(formDesigner);
        sectionPanel.getPanel().getRemoveButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getFormClass().remove(formSection);
                formDesigner.getDropControllerRegistry().unregister(formSection.getId());
            }
        });
        sectionPanel.getPanel().setClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                formDesigner.getEventBus().fireEvent(new WidgetContainerSelectionEvent(SectionWidgetContainer.this));
            }
        });
        formDesigner.getEventBus().addHandler(WidgetContainerSelectionEvent.TYPE, new WidgetContainerSelectionEvent.Handler() {
            @Override
            public void handle(WidgetContainerSelectionEvent event) {
                WidgetContainer selectedItem = event.getSelectedItem();
                if (selectedItem instanceof SectionWidgetContainer) {
                    sectionPanel.getPanel().setSelected(selectedItem.asWidget().equals(sectionPanel.asWidget()));
                }
            }
        });

        sectionPanel.getPanel().getWidgetContainer().add(createDropPanel());
        syncWithModel();
    }

    public ResourceId getParentId() {
        return parentId;
    }

    private Widget createDropPanel() {
        FlowPanel dropPanel = new FlowPanel();
        dropPanel.addStyleName(FormDesignerStyles.INSTANCE.sectionWidgetContainer());

        formDesigner.getDropControllerRegistry().register(formSection.getId(), dropPanel, formDesigner);

        return dropPanel ;
    }

    public void syncWithModel() {
        sectionPanel.getPanel().getLabel().setHTML("<h3>" + SafeHtmlUtils.fromString(Strings.nullToEmpty(formSection.getLabel())).asString() + "</h3>");
    }

    public Widget asWidget() {
        return sectionPanel.asWidget();
    }

    @Override
    public Widget getDragHandle() {
        return sectionPanel.getDragHandle();
    }

    public FormDesigner getFormDesigner() {
        return formDesigner;
    }
}