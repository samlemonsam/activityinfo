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
package org.activityinfo.ui.client.component.importDialog.model.strategy;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.importDialog.model.match.ColumnTypeGuesser;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import java.util.List;
import java.util.Set;

/**
 * Imports enum fields.
 *
 * @author yuriyz on 09/01/2015.
 */
public class EnumFieldImporter implements FieldImporter {

    private final List<ColumnAccessor> sources;
    private final List<ImportTarget> targets;
    private final EnumType enumType;

    public EnumFieldImporter(List<ColumnAccessor> sources, List<ImportTarget> targets, EnumType enumType) {
        this.sources = sources;
        this.targets = targets;
        this.enumType = enumType;
    }

    @Override
    public Promise<Void> prepare(ResourceLocator locator, List<? extends SourceRow> batch) {
        return Promise.done();
    }

    @Override
    public void validateInstance(SourceRow row, List<ValidationResult> results) {
        if (enumType.getCardinality() == Cardinality.SINGLE) {
            results.add(validateSingleValuedEnum(row));
        } else {
            results.addAll(validateMultiValuedEnum(row));
        }
    }

    private List<ValidationResult> validateMultiValuedEnum(SourceRow row) {
        List<ValidationResult> result = Lists.newArrayList();
        for (ColumnAccessor source : sources) {
            String value = source.getValue(row);
            if (!Strings.isNullOrEmpty(value) && !ColumnTypeGuesser.isBoolean(value)) {
                result.add(ValidationResult.error(I18N.MESSAGES.unknownMultiEnumValue(value)));
            } else {
                result.add(ValidationResult.OK);
            }
        }
        return result;
    }

    private ValidationResult validateSingleValuedEnum(SourceRow row) {
        ColumnAccessor source = sources.get(0);
        if (source.isMissing(row)) {
            if (targets.get(0).getFormField().isRequired()) {
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
    public boolean updateInstance(SourceRow row, TypedFormRecord instance) {
        if (enumType.getCardinality() == Cardinality.SINGLE) {
            return persistSingleValuedEnum(row, instance);
        } else {
            return persistMultiValuedEnum(row, instance);
        }
    }

    private boolean persistMultiValuedEnum(SourceRow row, TypedFormRecord instance) {
        if (ValidationResult.isPersistable(validateMultiValuedEnum(row))) {
            final Set<ResourceId> result = Sets.newHashSet();
            for (int i = 0; i< sources.size(); i++) {
                for (EnumItem enumItem : enumType.getValues()) {
                    if (enumItem.getId().asString().equals(targets.get(i).getSite().asString()) && Boolean.valueOf(sources.get(i).getValue(row))) {
                        result.add(enumItem.getId());
                        break;
                    }
                }
            }

            if (!result.isEmpty()) {
                instance.set(targets.get(0).getFormField().getId(), new EnumValue(result));
                return true;
            }
        }
        return false;
    }

    private boolean persistSingleValuedEnum(SourceRow row, TypedFormRecord instance) {
        if (validateSingleValuedEnum(row).isPersistable()) {
            final Set<ResourceId> result = Sets.newHashSet();
            String value = sources.get(0).getValue(row);
            for (EnumItem enumItem : enumType.getValues()) {
                if (enumItem.getLabel().equalsIgnoreCase(value)) {
                    result.add(enumItem.getId());
                    break;
                }
            }

            if (!result.isEmpty()) {
                instance.set(targets.get(0).getFormField().getId(), new EnumValue(result));
                return true;
            }
        }
        return false;
    }

    @Override
    public List<FieldImporterColumn> getColumns() {
        List<FieldImporterColumn> columns = Lists.newArrayList();
        for (int i = 0; i < targets.size(); i++) {
            columns.add(new FieldImporterColumn(targets.get(i), sources.get(i)));
        }
        return columns;
    }
}
