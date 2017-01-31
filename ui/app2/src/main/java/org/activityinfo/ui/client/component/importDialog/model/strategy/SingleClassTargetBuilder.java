package org.activityinfo.ui.client.component.importDialog.model.strategy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;

import java.util.List;
import java.util.Map;

public class SingleClassTargetBuilder {

    private final FormTree.Node rootField;
    private final TargetCollector targetCollector;

    public SingleClassTargetBuilder(FormTree.Node referenceField) {
        rootField = referenceField;
        targetCollector = new TargetCollector(referenceField);
    }

    public List<ImportTarget> getTargets() {
        return targetCollector.getTargets();
    }

    public SingleClassImporter newImporter(Map<TargetSiteId, ColumnAccessor> mappings) {
        List<ColumnAccessor> sourceColumns = Lists.newArrayList();
        Map<FieldPath, Integer> referenceValues = targetCollector.getPathMap(mappings, sourceColumns);
        List<FieldImporterColumn> fieldImporterColumns = targetCollector.fieldImporterColumns(mappings);

        ResourceId rangeClassId = Iterables.getOnlyElement(rootField.getRange());

        return new SingleClassImporter(rangeClassId, rootField.getField().isRequired(),
                sourceColumns, referenceValues, fieldImporterColumns, rootField.getFieldId());
    }
}
