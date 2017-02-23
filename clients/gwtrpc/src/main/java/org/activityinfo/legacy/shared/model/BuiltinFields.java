package org.activityinfo.legacy.shared.model;
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

import com.google.common.base.Predicate;
import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;

import java.util.Date;

/**
 * @author yuriyz on 09/30/2015.
 */
public class BuiltinFields {

    private BuiltinFields() {
    }

    public static DateRange getDateRange(FormInstance instance, FormClass formClass) {
        Date startDate = null;
        Date endDate = null;
        for (FormField field : formClass.getFields()) {
            if (isStartDate(field.getId())) {
                LocalDate localDate = instance.getDate(field.getId());
                if (localDate != null) {
                    startDate = localDate.atMidnightInMyTimezone();
                }
            }
            if (isEndDate(field.getId())) {
                LocalDate localDate = instance.getDate(field.getId());
                if (localDate != null) {
                    endDate = localDate.atMidnightInMyTimezone();
                }
            }
        }
        return new DateRange(startDate, endDate);
    }

    public static FormField findField(FormClass formClass, Predicate<FormField> predicate) {
        for (FormField field : formClass.getFields()) {
            if (predicate.apply(field)) {
                return field;
            }
        }
        return null;
    }


    public static FormField getStartDateField(FormClass formClass) {
        return findField(formClass, new Predicate<FormField>() {
            @Override
            public boolean apply(FormField input) {
                return isStartDate(input.getId());
            }
        });
    }

    public static FormField getEndDateField(FormClass formClass) {
        return findField(formClass, new Predicate<FormField>() {
            @Override
            public boolean apply(FormField input) {
                return isEndDate(input.getId());
            }
        });
    }

    public static FormField getProjectField(FormClass formClass) {
        return findField(formClass, new Predicate<FormField>() {
            @Override
            public boolean apply(FormField input) {
                return isProjectField(input.getId());
            }
        });
    }

    public static FieldValue getProjectValue(FormInstance instance, FormClass formClass) {
        return instance.get(getProjectField(formClass).getId());
    }

    public static boolean isBuiltInDate(ResourceId fieldId) {
        return isStartDate(fieldId) || isEndDate(fieldId);
    }

    public static boolean isStartDate(ResourceId fieldId) {
        return CuidAdapter.getBlockSilently(fieldId, 1) == CuidAdapter.START_DATE_FIELD;
    }

    public static boolean isEndDate(ResourceId fieldId) {
        return CuidAdapter.getBlockSilently(fieldId, 1) == CuidAdapter.END_DATE_FIELD;
    }

    public static boolean isProjectField(ResourceId fieldId) {
        return CuidAdapter.getBlockSilently(fieldId, 1) == CuidAdapter.PROJECT_FIELD;
    }
}
