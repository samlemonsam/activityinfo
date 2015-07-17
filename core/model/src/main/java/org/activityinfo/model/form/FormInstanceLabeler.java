package org.activityinfo.model.form;
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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 2/11/14.
 */
public class FormInstanceLabeler {

    private FormInstanceLabeler() {
    }

    public static String getLabel(FormInstance instance) {
        return Strings.nullToEmpty(instance.getString(getFormInstanceLabelCuid(instance)));
    }

    public static void setLabel(FormInstance instance, String label) {
        instance.set(getFormInstanceLabelCuid(instance), label);
    }

    public static List<String> getLabels(List<FormInstance> list) {
        final List<String> labels = Lists.newArrayList();
        if (list != null && !list.isEmpty()) {
            for (FormInstance instance : list) {
                labels.add(getLabel(instance));
            }
        }
        return labels;
    }

    public static Set<String> getDuplicatedInstanceLabels(List<FormInstance> list) {
        final List<String> existingNames = FormInstanceLabeler.getLabels(list);
        final Set<String> duplicates = Sets.newHashSet();
        final Set<String> temp = Sets.newHashSet();
        for (String name : existingNames) {
            if (!temp.add(name)) {
                duplicates.add(name);
            }
        }
        return duplicates;
    }

    public static ResourceId getFormInstanceLabelCuid(FormInstance formInstance) {
        return CuidAdapter.field(formInstance.getClassId(), CuidAdapter.NAME_FIELD);
    }
}
