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
package org.activityinfo.ui.client.page.resource;

import com.google.common.base.Function;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.formdesigner.FormDesigner;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.dispatch.state.StateProvider;
import org.activityinfo.ui.client.widget.DisplayWidget;

import javax.annotation.Nullable;

/** *
 * Created by Mithun on 4/3/2014.
 */
public class DesignTab implements DisplayWidget<ResourceId> {

    private ResourceLocator resourceLocator;
    private StateProvider stateProvider;
    private FlowPanel panel;

    public DesignTab(ResourceLocator resourceLocator, StateProvider stateProvider) {
        this.resourceLocator = resourceLocator;
        this.stateProvider = stateProvider;
        this.panel = new FlowPanel();
    }

    @Override
    public Promise<Void> show(ResourceId resourceId) {
        return this.resourceLocator.getFormClass(resourceId)
                .then(new Function<FormClass, Void>() {
                    @Nullable
                    @Override
                    public Void apply(FormClass formClass) {
                        panel.add(new FormDesigner(resourceLocator, formClass, stateProvider).getFormDesignerPanel());
                        return null;
                    }
                });
    }

    @Override
    public Widget asWidget() {
        return panel;
    }
}
