package org.activityinfo.ui.client.component.form.subform;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.FormModel;

import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 01/22/2016.
 */
public class SubFormInstanceLoader {

    private final FormModel model;

    // keep copy of persisted instances, we need to track persisted instance to remove them on save if they were deleted by user.
    private final Set<FormInstance> persisted = Sets.newHashSet();

    public SubFormInstanceLoader(FormModel model) {
        this.model = model;
    }

    public Promise<List<FormInstance>> loadCollectionInstances(final FormClass subForm) {
        ParentCriteria criteria = ParentCriteria.isChildOf(
                ClassType.REPEATING.getResourceId(), model.getWorkingRootInstance().getId(), subForm.getId());
        return model.getLocator().queryInstances(criteria)
                .then(new Function<List<FormInstance>, List<FormInstance>>() {
                    @Override
                    public List<FormInstance> apply(List<FormInstance> instanceList) {
                        for (FormInstance instance : instanceList) {
                             model.getSubFormInstances().put(new FormModel.SubformValueKey(subForm, instance), instance);
                             persisted.add(instance);
                        }

                        return instanceList;
                    }
                });
    }

    public Promise<Void> loadKeyedSubformInstances(final FormClass subForm) {
        final Promise<Void> result = new Promise<>();

        ResourceId parentId = model.getWorkingRootInstance().getId();
        ParentCriteria.Parent parent = new ParentCriteria.Parent(parentId, parentId);
        parent.setClassId(subForm.getId());
        
        ParentCriteria criteria = new ParentCriteria(parent);

        model.getLocator().queryInstances(criteria).then(new Function<List<FormInstance>, Void>() {
            @Override
            public Void apply(final List<FormInstance> instanceList) {

                if (instanceList.isEmpty()) {
                    result.onSuccess(null);
                    return null;
                }

                final List<Integer> counter = Lists.newArrayList();

                for (final FormInstance valueInstance : instanceList) {
                    if (valueInstance.getKeyId().isPresent()) {
                        model.getLocator().getFormInstance(null, valueInstance.getKeyId().get()).then(new AsyncCallback<FormInstance>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                result.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(FormInstance key) {
                                model.getSubFormInstances().put(new FormModel.SubformValueKey(subForm, key), valueInstance);
                                persisted.add(valueInstance);
                                counter.add(1);
                                if (counter.size() == instanceList.size()) {
                                    result.onSuccess(null);
                                }
                            }
                        });
                    } else {
                        Log.error("Key is not found for instance: " + valueInstance.getId());
                    }
                }
                return null;
            }
        });
        return result;
    }

    public boolean isPersisted(FormInstance instance) {
        return persisted.contains(instance);
    }
}
