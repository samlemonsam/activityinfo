package org.activityinfo.core.shared.importing.strategy;

import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.type.converter.Converter;
import org.activityinfo.core.shared.type.converter.ConverterFactory;
import org.activityinfo.model.formTree.FormTree;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Imports a simple data field
 */
public class DataFieldImportStrategy implements FieldImportStrategy {

    public static final TargetSiteId VALUE = new TargetSiteId("value");

    private final ConverterFactory converterFactory;

    public DataFieldImportStrategy(ConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    @Override
    public boolean accept(FormTree.Node fieldNode) {
        return !fieldNode.isReference() && !fieldNode.isEnum();
    }

    @Override
    public List<ImportTarget> getImportSites(FormTree.Node node) {
        return Collections.singletonList(target(node));
    }

    @Override
    public FieldImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> bindings, ImportModel model) {

        ImportTarget requiredTarget = target(node);
        ColumnAccessor column = bindings.get(VALUE);
        if(column == null) {
            column = MissingColumn.INSTANCE;
        }

        Converter converter = converterFactory.createStringConverter(node.getTypeClass());

        return new DataFieldImporter(column, requiredTarget, converter, node, model);
    }

    private ImportTarget target(FormTree.Node node) {
        return new ImportTarget(node.getField(), VALUE, node.getField().getLabel(), node.getDefiningFormClass().getId());
    }
}
