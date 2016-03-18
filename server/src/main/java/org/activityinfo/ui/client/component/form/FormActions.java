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

import com.google.common.collect.BiMap;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.IsResource;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.event.BeforeSaveEvent;
import org.activityinfo.ui.client.component.form.event.SaveFailedEvent;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 02/18/2015.
 */
public class FormActions {

    private final ResourceLocator locator;
    private final FormModel model;

    public FormActions(ResourceLocator locator, FormModel model) {
        this.locator = locator;
        this.model = model;
    }

    public Promise<Void> save() {

        model.getEventBus().fireEvent(new BeforeSaveEvent());

        BiMap<FormModel.SubformValueKey, FormInstance> subformInstances = model.getSubFormInstances();

        List<IsResource> toPersist = Lists.newArrayList();
        toPersist.add(model.getWorkingRootInstance()); // root instance

        for (Map.Entry<FormModel.SubformValueKey, FormInstance> entry : subformInstances.entrySet()) { // sub form instances
            if (!ClassType.isRepeating(entry.getKey().getSubForm())) {
                toPersist.add(entry.getKey().getTabInstance()); // keyes
            }
            toPersist.add(entry.getValue()); // values

        }

        Promise<Void> persist = locator.persist(toPersist);
        Promise<Void> remove = Promise.done();

        if (!model.getPersistedInstanceToRemoveByLocator().isEmpty()) {
            remove = locator.remove(model.getPersistedInstanceToRemoveByLocator());
        }

        Promise<Void> waitAll = Promise.waitAll(persist, remove);

        waitAll.then(new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                model.getEventBus().fireEvent(new SaveFailedEvent(caught));
            }

            @Override
            public void onSuccess(Void result) {
                model.getPersistedInstanceToRemoveByLocator().clear();
            }
        });

        return waitAll;
    }


}
