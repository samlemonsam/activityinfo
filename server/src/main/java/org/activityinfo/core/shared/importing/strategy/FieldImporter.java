package org.activityinfo.core.shared.importing.strategy;

import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.validation.ValidationResult;
import org.activityinfo.promise.Promise;

import java.util.List;

/**
 * FieldImporters operate on
 */
public interface FieldImporter {

    Promise<Void> prepare(ResourceLocator locator, List<? extends SourceRow> batch);

    void validateInstance(SourceRow row, List<ValidationResult> results);

    boolean updateInstance(SourceRow row, FormInstance instance);

    List<FieldImporterColumn> getColumns();

}
