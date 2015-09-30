package org.activityinfo.core.shared.importing.strategy;

import com.google.common.base.Preconditions;
import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.validation.ValidationResult;
import org.activityinfo.core.shared.type.converter.Converter;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.date.DateRange;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.BuiltinFields;
import org.activityinfo.promise.Promise;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Imports simple data fields using the supplied converter.
 */
public class DataFieldImporter implements FieldImporter {

    private ColumnAccessor source;
    private ImportTarget target;
    private Converter converter;
    private FormTree.Node node;
    private ImportModel model;

    public DataFieldImporter(ColumnAccessor source, ImportTarget target, Converter converter, FormTree.Node node, ImportModel model) {
        this.source = source;
        this.target = target;
        this.converter = converter;
        this.node = node;
        this.model = model;
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
        try {
            converter.convert(source.getValue(row));

            if (BuiltinFields.isBuiltInDate(target.getFormField().getId())) {
                DateRange rangeDate = getRangeDate(row, converter);
                if (!rangeDate.isValid()) {
                    return ValidationResult.error(I18N.CONSTANTS.inconsistentDateRangeWarning());
                }
            }

            return ValidationResult.OK;
        } catch(Exception e) {
            return ValidationResult.error(e.getMessage());
        }
    }

    private DateRange getRangeDate(SourceRow row, Converter converter) {
        ColumnAccessor startDateAccessor = null;
        ColumnAccessor endDateAccessor = null;
        for (FormField field : node.getDefiningFormClass().getFields()) {
            if (BuiltinFields.isStartDate(field.getId())) {
                startDateAccessor = model.getMappedColumns(field.getId()).get(DataFieldImportStrategy.VALUE);
            }
            if (BuiltinFields.isEndDate(field.getId())) {
                endDateAccessor = model.getMappedColumns(field.getId()).get(DataFieldImportStrategy.VALUE);
            }
        }
        Preconditions.checkNotNull(startDateAccessor, "It must not be null because start date is built-in required field.");
        Preconditions.checkNotNull(endDateAccessor, "It must not be null because end date is built-in required field.");
        return new DateRange((Date) converter.convert(startDateAccessor.getValue(row)), (Date) converter.convert(endDateAccessor.getValue(row)));
    }

    @Override
    public boolean updateInstance(SourceRow row, FormInstance instance) {
        final ValidationResult validateResult = validate(row);
        if (validateResult.isPersistable()) {
            instance.set(target.getFormField().getId(), converter.convert(source.getValue(row)));
            return true;
        }
        return false;
    }

    @Override
    public List<FieldImporterColumn> getColumns() {
        return Collections.singletonList(new FieldImporterColumn(target, source));
    }
}

