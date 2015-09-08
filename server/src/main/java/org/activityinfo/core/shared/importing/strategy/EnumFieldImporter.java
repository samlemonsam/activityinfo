package org.activityinfo.core.shared.importing.strategy;
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
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.validation.ValidationResult;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.promise.Promise;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Imports enum fields.
 *
 * @author yuriyz on 09/01/2015.
 */
public class EnumFieldImporter implements FieldImporter {

    private final ColumnAccessor source;
    private final ImportTarget target;
    private final EnumType enumType;

    public EnumFieldImporter(ColumnAccessor source, ImportTarget target, EnumType enumType) {
        this.source = source;
        this.target = target;
        this.enumType = enumType;
    }

    @Override
    public Promise<Void> prepare(ResourceLocator locator, List<? extends SourceRow> batch) {
        return Promise.done();
    }

    @Override
    public void validateInstance(SourceRow row, List<ValidationResult> results) {
        results.add(validate(row));
    }

    private ValidationResult validate(SourceRow row) {
        if (source.isMissing(row)) {
            if (target.getFormField().isRequired()) {
                return ValidationResult.error("Required value is missing");
            } else {
                return ValidationResult.MISSING;
            }
        }

        String value = source.getValue(row);
        for (EnumItem enumItem : enumType.getValues()) {
            if (enumItem.getLabel().equalsIgnoreCase(value)) {
                return ValidationResult.OK;
            }
        }

        return ValidationResult.error("Unknown value: " + value);
    }

    @Override
    public boolean updateInstance(SourceRow row, FormInstance instance) {
        final ValidationResult validateResult = validate(row);
        if (validateResult.isPersistable()) {
            String value = source.getValue(row);
            final Set<ResourceId> result = Sets.newHashSet();
            for (EnumItem enumItem : enumType.getValues()) {
                if (enumItem.getLabel().equalsIgnoreCase(value)) {
                    result.add(enumItem.getId());
                }
            }
            if (!result.isEmpty()) {
                instance.set(target.getFormField().getId(), new EnumValue(result));
                return true;
            }
        }
        return false;
    }

    @Override
    public List<FieldImporterColumn> getColumns() {
        return Collections.singletonList(new FieldImporterColumn(target, source));
    }
}
