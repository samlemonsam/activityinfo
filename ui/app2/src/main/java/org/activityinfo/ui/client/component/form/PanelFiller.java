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
package org.activityinfo.ui.client.component.form;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.*;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.ui.client.widget.form.FormGroup;

/**
 * @author yuriyz on 01/18/2016.
 */
public class PanelFiller {

    private final FlowPanel panel;
    private final FormModel model;
    private final FormWidgetCreator widgetCreator;
    private final RelevanceHandler relevanceHandler;

    private boolean headingVisible = false;

    public PanelFiller(FlowPanel panel, FormModel model, FormWidgetCreator widgetCreator, RelevanceHandler relevanceHandler) {
        this.panel = panel;
        this.model = model;
        this.widgetCreator = widgetCreator;
        this.relevanceHandler = relevanceHandler;
    }

    public void add(FormElementContainer container, int depth) {
        add(container, depth, panel);
    }

    public void add(FormElementContainer container, final int depth, final FlowPanel panel) {

        if (headingVisible) {
            FormHeading heading = new FormHeading();
            heading.setFormClass(model.getRootFormClass());
            panel.add(heading);
        }

        for (FormElement element : container.getElements()) {
            if (element instanceof FormLabel) {
                FormLabel formLabel = (FormLabel) element;
                panel.add(new FormGroup().label(formLabel.getLabel()));
            } else if (element instanceof FormSection) {
                panel.add(createHeader(depth, element.getLabel()));
                add((FormElementContainer) element, depth + 1);
            } else if (element instanceof FormField) {
                FormField formField = (FormField) element;
                if (formField.isVisible()) {
                    if (formField.getType() instanceof SubFormReferenceType) {

                        final FormClass subForm = model.getSubFormByOwnerFieldId(formField.getId());
                       
                        final FlowPanel subFormFieldPanel = new FlowPanel();
                        subFormFieldPanel.addStyleName(FormPanelStyles.INSTANCE.subformPanel());
                        subFormFieldPanel.add(createHeader(depth + 1, subForm.getLabel()));
                        subFormFieldPanel.add(widgetCreator.createSubformPanel(subForm, depth + 1, this).getLoadingPanel());

                        panel.add(subFormFieldPanel);
                    } else {
                        panel.add(widgetCreator.get(formField.getId()));
                    }
                }
            }
        }
    }

    public static Widget createHeader(int depth, String header) {
        String hn = "h" + (3 + depth);
        return new HTML("<" + hn + ">" + SafeHtmlUtils.htmlEscape(header) + "</" + hn + ">");
    }

    public void setHeadingVisible(boolean headingVisible) {
        this.headingVisible = headingVisible;
    }
}
