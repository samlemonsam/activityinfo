package org.activityinfo.model.type.subform;
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

import com.google.common.collect.Maps;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;

import java.util.Map;

/**
 * Represents type of form classes : locationType classes or activity classes.
 * Mainly used to allow user define subform kind.
 *
 * @author yuriyz on 03/12/2015.
 */
public enum ClassType {

    LOCATION_TYPE("Location type"),
    PARTNER("Partner"),
    PROJECT("Project");

    private static final Map<String, ClassType> resourceIdLookup = Maps.newHashMap();

    static {
        for (ClassType type : ClassType.values()) {
            resourceIdLookup.put(type.getResourceId().asString(), type);
        }
    }

    private final String label;

    ClassType(String label) {
        this.label = label;
    }

    public ResourceId getResourceId() {
        return ResourceId.valueOf("_classType_" + this.name().toLowerCase());
    }

    public FormClass getDefinition() {
        FormClass formClass = new FormClass(getResourceId());
        formClass.setLabel("Select " + label);

        return formClass;
    }

    public SubFormKind createSubformKind() {
        return new SubFormKind() {
            @Override
            public FormClass getDefinition() {
                return ClassType.this.getDefinition();
            }
        };
    }

    public static ClassType byId(ResourceId resourceId) {
        return resourceIdLookup.get(resourceId.asString());
    }

    public static boolean isClassType(ResourceId resourceId) {
        return byId(resourceId) !=null;
    }
}
