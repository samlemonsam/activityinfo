package org.activityinfo.ui.client.component.form;
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

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.*;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.model.type.subform.SubFormType;
import org.activityinfo.ui.client.component.form.subform.SubFormCollectionManipulator;
import org.activityinfo.ui.client.component.form.subform.SubFormTabsManipulator;

/**
 * @author yuriyz on 01/18/2016.
 */
public class PanelFiller {

    private final FlowPanel panel;
    private final FormModel model;
    private final FormWidgetCreator widgetCreator;

    public PanelFiller(FlowPanel panel, FormModel model, FormWidgetCreator widgetCreator) {
        this.panel = panel;
        this.model = model;
        this.widgetCreator = widgetCreator;
    }

    public void add(FormElementContainer container, int depth) {
        for (FormElement element : container.getElements()) {
            if (element instanceof FormSection) {
                panel.add(createHeader(depth, element.getLabel()));
                add((FormElementContainer) element, depth + 1);
            } else if (element instanceof FormField) {
                FormField formField = (FormField) element;
                if (formField.isVisible()) {
                    if (formField.getType() instanceof SubFormType) {
                        FormClass subForm = model.getSubFormByOwnerFieldId(formField.getId());

                        panel.add(createHeader(depth, subForm.getLabel()));

                        if (ClassType.isCollection(subForm)) { // unkeyed subforms -> simple collection
                            new SubFormCollectionManipulator(subForm, model, panel).show();
                        } else { // keyed subforms
                            final SubFormTabsManipulator subFormTabsManipulator = new SubFormTabsManipulator(model.getLocator());

                            panel.add(subFormTabsManipulator.getPresenter().getView());

                            subFormTabsManipulator.show(subForm, model);
                            add(subForm, depth + 1);
                        }
                    } else {
                        panel.add(widgetCreator.get(formField.getId()));
                    }
                }
            }
        }
    }

    private static Widget createHeader(int depth, String header) {
        String hn = "h" + (3 + depth);
        return new HTML("<" + hn + ">" + SafeHtmlUtils.htmlEscape(header) + "</" + hn + ">");
    }
}
