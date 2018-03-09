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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.FieldUpdater;
import org.activityinfo.ui.client.component.form.field.FormFieldWidget;
import org.activityinfo.ui.client.component.form.field.FormFieldWidgetFactory;
import org.activityinfo.ui.client.component.form.subform.PeriodSubFormPanel;
import org.activityinfo.ui.client.component.form.subform.RepeatingSubFormPanel;
import org.activityinfo.ui.client.component.form.subform.SubFormPanel;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yuriyz on 02/17/2015.
 */
public class FormWidgetCreator {

    public interface FieldUpdated {
        void onFieldUpdated(FormField field, FieldValue newValue);
    }

    private final FieldContainerFactory containerFactory;
    private final FormFieldWidgetFactory widgetFactory;

    private final Map<ResourceId, FieldContainer> containers = Maps.newHashMap();
    private final Map<FormClass, SubFormPanel> subformPanels = Maps.newHashMap();

    private final FormModel model;

    public FormWidgetCreator(FormModel model, FieldContainerFactory containerFactory, FormFieldWidgetFactory widgetFactory) {
        this.model = model;
        this.containerFactory = containerFactory;
        this.widgetFactory = widgetFactory;

    }


    public SubFormPanel createSubformPanel(FormClass subForm, int depth, RelevanceHandler relevanceHandler, PanelFiller filler) {
        final SubFormPanel panel;
        if (subForm.getSubFormKind() == SubFormKind.REPEATING) {
            panel = new RepeatingSubFormPanel(subForm, model);
        } else {
            panel = new PeriodSubFormPanel(model, subForm, relevanceHandler, filler, depth);
        }
        subformPanels.put(subForm, panel);
        return panel;
    }

    public SubFormPanel getSubformPanel(FormClass subForm) {
        return subformPanels.get(subForm);
    }

    public Set<RepeatingSubFormPanel> getRepeatingSubformPanels() {
        Set<RepeatingSubFormPanel> set = Sets.newHashSet();
        for (Map.Entry<FormClass, SubFormPanel> entry : subformPanels.entrySet()) {
            if (entry.getKey().isSubForm() && entry.getKey().getSubFormKind() == SubFormKind.REPEATING) {
                set.add((RepeatingSubFormPanel) entry.getValue());
            }
        }
        return set;
    }

    public Promise<Void> createWidgets(final FormClass formClass, final FieldUpdated fieldUpdated) {
        List<Promise<Void>> promises = Lists.newArrayList();
        for (final FormField field : formClass.getFields()) {
            if (field.getType() instanceof SubFormReferenceType) {
                FormClass subForm = model.getSubFormByOwnerFieldId(field.getId());
                if (subForm.getSubFormKind() != SubFormKind.REPEATING) { // for repeating we create it internally in sub SimpleFormPanel
                    Promise<Void> subFormWidgetsPromise = createWidgets(subForm, fieldUpdated);
                    promises.add(subFormWidgetsPromise);
                }
            } else {
                Promise<Void> promise = widgetFactory.createWidget(formClass, field, new FieldUpdater<FieldValue>() {
                    @Override
                    public void onInvalid(String errorMessage) {
                        containers.get(field.getId()).setInvalid(errorMessage);
                    }

                    @Override
                    public void update(FieldValue value) {
                        containers.get(field.getId()).setValid();
                        fieldUpdated.onFieldUpdated(field, value);
                    }
                }).then(new Function<FormFieldWidget, Void>() {
                    @Override
                    public Void apply(FormFieldWidget widget) {
                        FieldContainer fieldContainer = containerFactory.createContainer(field, widget, 4);
                        containers.put(field.getId(), fieldContainer);

                        model.addContainerOfClass(formClass.getId(), fieldContainer);

                        return null;
                    }
                });
                promises.add(promise);
            }
        }
        return Promise.waitAll(promises);
    }

    public FieldContainer get(ResourceId fieldId) {
        return containers.get(fieldId);
    }

    public Map<ResourceId, FieldContainer> getContainers() {
        return containers;
    }

}
