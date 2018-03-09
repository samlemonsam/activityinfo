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

import java.util.HashMap;
import java.util.Map;


public class FormClassProviders {

    public static FormClassProvider fromMap(final Map<ResourceId, FormClass> map) {
        return new FormClassProvider() {
            @Override
            public FormClass getFormClass(ResourceId formId) {
                return map.get(formId);
            }
        };
    }
    
    public static FormClassProvider of(FormClass... formClasses) {
        Map<ResourceId, FormClass> map = new HashMap<>();
        for (FormClass formClass : formClasses) {
            map.put(formClass.getId(), formClass);
        }
        return fromMap(map);
    }
}
