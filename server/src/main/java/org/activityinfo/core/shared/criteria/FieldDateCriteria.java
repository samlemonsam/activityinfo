package org.activityinfo.core.shared.criteria;
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

import org.activityinfo.core.shared.Projection;
import org.activityinfo.model.date.LocalDateRange;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.time.LocalDate;

import javax.annotation.Nonnull;

/**
 * @author yuriyz on 07/03/2015.
 */
public class FieldDateCriteria implements Criteria {

    private FieldPath fieldPath;
    private LocalDateRange range;

    public FieldDateCriteria(@Nonnull ResourceId fieldId, @Nonnull LocalDateRange range) {
        this.fieldPath = new FieldPath(fieldId);
        this.range = range;
    }

    public FieldDateCriteria(@Nonnull FieldPath fieldPath, @Nonnull LocalDateRange range) {
        this.fieldPath = fieldPath;
        this.range = range;
    }

    @Override
    public void accept(CriteriaVisitor visitor) {
        visitor.visitFieldCriteria(this);
    }

    @Override
    public boolean apply(@Nonnull FormInstance input) {
        return true;
    }

    @Override
    public boolean apply(@Nonnull Projection input) {
        return match(input.getValue(fieldPath));
    }

    private boolean match(FieldValue value) {
        if (range != null) {
            return value instanceof LocalDate && range.isIn((LocalDate) value);
        }
        return true;
    }

    public ResourceId getFieldId() {
        return fieldPath.getRoot();
    }

    public FieldPath getFieldPath() {
        return fieldPath;
    }

    public LocalDateRange getRange() {
        return range;
    }
}
