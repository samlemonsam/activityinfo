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

import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.number.QuantityType;

import java.util.Collections;
import java.util.Set;

/**
 * @author yuriyz on 02/06/2015.
 */
public class SubformConstants {

    /**
     * In subform case we need to keep type of FormClass. Since we want to keep FormClass to model SubForms
     * solution is to store type via FormField that keeps reference on FormClass that describes type (e.g. Period or Territory)
     */
    public static final ResourceId TYPE_FIELD_ID = ResourceId.valueOf("_subform_class_type");

    /**
     * Tabs count on subform.
     */
    public static final ResourceId TAB_COUNT_FIELD_ID = ResourceId.valueOf("_subform_tab_count");

    private static final Set<ResourceId> BUILT_IN_FIELDS = Sets.newHashSet();

    static {
        BUILT_IN_FIELDS.add(TYPE_FIELD_ID);
        BUILT_IN_FIELDS.add(TAB_COUNT_FIELD_ID);
    }

    public static final int DEFAULT_TAB_COUNT = 4;
    public static final int MIN_TAB_COUNT = 1;
    public static final int MAX_TAB_COUNT = 100;

    private SubformConstants() {
    }

    public static Set<ResourceId> subformBuiltInFieldIds() {
        return Collections.unmodifiableSet(BUILT_IN_FIELDS);
    }

    public static boolean isSubformBuiltInField(ResourceId resourceId) {
        return subformBuiltInFieldIds().contains(resourceId);
    }

    public static boolean isCollection(FormClass formClass) {
        return ClassType.byId(getSubformType(formClass)) == ClassType.COLLECTION;
    }

    public static ResourceId getSubformType(FormClass subForm) {
        ReferenceType typeClass = (ReferenceType) subForm.getField(SubformConstants.TYPE_FIELD_ID).getType();
        return typeClass.getRange().iterator().next();
    }

    public static int getTabCount(FormClass subForm) {
        QuantityType tabsCountType = (QuantityType) subForm.getField(SubformConstants.TAB_COUNT_FIELD_ID).getType();
        try {
            return (int) Double.parseDouble(tabsCountType.getUnits());
        } catch (Exception e) {
            return SubformConstants.DEFAULT_TAB_COUNT;
        }
    }

}
