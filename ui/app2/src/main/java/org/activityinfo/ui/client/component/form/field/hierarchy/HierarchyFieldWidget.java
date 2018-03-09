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
package org.activityinfo.ui.client.component.form.field.hierarchy;

import com.google.common.base.Function;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.ReferenceValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.field.ReferenceFieldWidget;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Widget for Reference fields which presents multi-level combo boxes
 *
 */
public class HierarchyFieldWidget implements ReferenceFieldWidget {

    private final FlowPanel panel;
    private final Map<ResourceId, LevelView> widgets = new HashMap<>();
    private final Presenter presenter;

    private boolean readOnly;

    public HierarchyFieldWidget(ResourceLocator locator, Hierarchy tree,
                                ValueUpdater valueUpdater) {

        this.panel = new FlowPanel();
        for(Level level : tree.getLevels()) {
            LevelWidget widget = new LevelWidget(level.getLabel());
            widgets.put(level.getFormId(), widget);
            this.panel.add(widget);
        }

        this.presenter = new Presenter(locator, tree, widgets, valueUpdater);
    }

    @Override
    public void fireValueChanged() {
        this.presenter.fireValueChanged();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;

        for(LevelView widget : widgets.values()) {
            widget.setReadOnly(readOnly);
        }
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public Promise<Void> setValue(ReferenceValue value) {
        return presenter.setInitialSelection(value);
    }

    @Override
    public void clearValue() {
        presenter.setInitialSelection(ReferenceValue.EMPTY);
    }

    @Override
    public void setType(FieldType type) {

    }

    @Override
    public Widget asWidget() {
        return panel;
    }


    public static Promise<HierarchyFieldWidget> create(final ResourceLocator locator,
                                                 final ReferenceType type,
                                                 final ValueUpdater valueUpdater) {

        return Promise.map(type.getRange(), new Function<ResourceId, Promise<FormClass>>() {
            @Nullable
            @Override
            public Promise<FormClass> apply(@Nullable ResourceId input) {
                return locator.getFormClass(input);
            }
        }).then(new Function<List<FormClass>, HierarchyFieldWidget>() {
            @Nullable
            @Override
            public HierarchyFieldWidget apply(@Nullable List<FormClass> input) {
                return new HierarchyFieldWidget(locator, new Hierarchy(input), valueUpdater);
            }
        });
    }

}

