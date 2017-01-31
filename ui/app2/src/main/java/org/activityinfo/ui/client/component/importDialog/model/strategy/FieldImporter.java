package org.activityinfo.ui.client.component.importDialog.model.strategy;

import org.activityinfo.model.form.FormInstance;
import org.activityinfo.promise.Promise;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.validation.ValidationResult;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

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
