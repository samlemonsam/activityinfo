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
package org.activityinfo.model.formTree;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestBatchFormClassProvider implements BatchFormClassProvider {

    private final Map<ResourceId, FormClass> map = new HashMap<>();

    @Override
    public FormClass getFormClass(ResourceId formId) {
        FormClass formClass = map.get(formId);
        if(formClass == null) {
            throw new IllegalArgumentException("No such form: " + formId);
        }
        return formClass;
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        Map<ResourceId, FormClass> resultMap = new HashMap<>();
        for (ResourceId formId : formIds) {
            FormClass formClass = map.get(formId);
            if(formClass != null) {
                resultMap.put(formId, formClass);
            }
        }
        return resultMap;
    }

    public void add(FormClass formClass) {
        map.put(formClass.getId(), formClass);
    }

    public void addAll(List<FormClass> formClasses) {
        for (FormClass formClass : formClasses) {
            add(formClass);
        }
    }
}
