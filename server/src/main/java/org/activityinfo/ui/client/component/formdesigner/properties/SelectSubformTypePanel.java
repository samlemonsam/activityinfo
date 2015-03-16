package org.activityinfo.ui.client.component.formdesigner.properties;
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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.form.FormInstanceLabeler;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.widget.DisplayWidget;
import org.activityinfo.ui.client.widget.ListBox;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 03/16/2015.
 */
public class SelectSubformTypePanel extends Composite implements DisplayWidget<FormInstance> {

    private static OurUiBinder uiBinder = GWT.create(OurUiBinder.class);

    interface OurUiBinder extends UiBinder<Widget, SelectSubformTypePanel> {
    }

    @UiField
    HTMLPanel rootPanel;
    @UiField
    ListBox classList;

    private final ResourceId parentId;
    private final FormDesigner formDesigner;

    private final Map<String, FormInstance> idToInstance = Maps.newHashMap();
    private ResourceId selectedClassId = null;

    public SelectSubformTypePanel(ResourceId parentId, FormDesigner formDesigner) {
        this.parentId = parentId;
        this.formDesigner = formDesigner;

        uiBinder.createAndBindUi(this);

        classList.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = classList.getSelectedIndex();
                if (selectedIndex != -1) {
                    FormInstance selectedInstance = idToInstance.get(classList.getValue(selectedIndex));
                    selectedClassId = selectedInstance.getClassId();
                } else {
                    selectedClassId = null;
                }
                stateChanged();
            }
        });
    }

    public void stateChanged() {
    }

    @Override
    public Promise<Void> show(FormInstance value) {
        ParentCriteria criteria = ParentCriteria.isChildOf(parentId, formDesigner.getModel().getRootFormClass().getOwnerId());
        return formDesigner.getResourceLocator().queryInstances(criteria).then(new Function<List<FormInstance>, Void>() {
            @Nullable
            @Override
            public Void apply(List<FormInstance> instances) {
                for (FormInstance instance : instances) {
                    idToInstance.put(instance.getId().asString(), instance);
                    classList.addItem(getInstanceLabel(instance), instance.getId().asString());
                }

                return null;
            }
        });
    }

    private String getInstanceLabel(FormInstance instance) {
        if (ClassType.isClassType(parentId)) {
            return instance.getString(CuidAdapter.field(instance.getClassId(), CuidAdapter.NAME_FIELD));
        }

        String fallbackLabel = FormInstanceLabeler.getLabel(instance);
        if (Strings.isNullOrEmpty(fallbackLabel)) {
            return "no label";
        }
        return fallbackLabel;
    }

    public boolean isValid() {
        return selectedClassId != null;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    public ResourceId getSelectedClassId() {
        return selectedClassId;
    }
}
