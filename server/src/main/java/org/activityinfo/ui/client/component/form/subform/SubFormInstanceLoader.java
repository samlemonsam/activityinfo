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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.activityinfo.core.shared.criteria.ParentCriteria;
import org.activityinfo.legacy.shared.Log;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.subform.ClassType;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.form.FormModel;

import java.util.List;
import java.util.Map;
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
        return model.getLocator().queryInstances(ParentCriteria.isChildOf(ClassType.COLLECTION.getResourceId(), model.getWorkingRootInstance().getId()))
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

    public Promise<List<FormInstance>> loadKeyedSubformInstances(final FormClass subForm) {

        final Promise<List<FormInstance>> promise = model.getLocator().queryInstances(ParentCriteria.isChildOf(ClassType.COLLECTION.getResourceId(), model.getWorkingRootInstance().getId()));
        final Promise<List<FormInstance>> keys = promise.then(new Function<List<FormInstance>, List<FormInstance>>() {
            @Override
            public List<FormInstance> apply(List<FormInstance> instanceList) {
                final List<Promise<FormInstance>> keyPromises = Lists.newArrayList();

                for (FormInstance instance : instanceList) {
                    if (instance.getKeyId().isPresent()) {
                        keyPromises.add(model.getLocator().getFormInstance(instance.getKeyId().get()));
                    } else {
                        Log.error("Key is not found for instance: " + instance.getId());
                    }
                }

                return Promise.waitAll(keyPromises).then(new Function<Void, List<FormInstance>>() {
                    @Override
                    public List<FormInstance> apply(Void input) {
                        List<FormInstance> list = Lists.newArrayList();
                        for (Promise<FormInstance> key : keyPromises) {
                            list.add(key.get());
                        }
                        return list;
                    }
                }).get();
            }
        });
        return Promise.waitAll(promise, keys).then(new Function<Void, List<FormInstance>>() {
            @Override
            public List<FormInstance> apply(Void input) {

                Map<ResourceId, FormInstance> keyMap = asMap(keys.get());

                for (FormInstance instance : promise.get()) {

                    if (instance.getKeyId().isPresent()) {
                        FormInstance keyInstance = keyMap.get(instance.getKeyId().get());
                        if (keyInstance != null) {
                            model.getSubFormInstances().put(new FormModel.SubformValueKey(subForm, keyInstance), instance);
                            persisted.add(instance);
                        } else {
                            Log.error("Key instance is not found for keyed instance: " + instance.getId() + ", keyId: " + instance.getKeyId().get());
                        }
                    } else {
                        Log.error("Key is not found for keyed instance: " + instance.getId());
                    }
                }
                return promise.get();
            }
        });
    }

    private static Map<ResourceId, FormInstance> asMap(List<FormInstance> list) {
        Map<ResourceId, FormInstance> map = Maps.newHashMap();
        for (FormInstance instance : list) {
            map.put(instance.getId(), instance);
        }
        return map;
    }

    public boolean isPersisted(FormInstance instance) {
        return persisted.contains(instance);
    }
}
