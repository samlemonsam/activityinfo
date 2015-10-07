package org.activityinfo.core.shared.importing.strategy;

import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.model.formTree.FormTree;

import java.util.List;
import java.util.Map;

/**
 * Manages the import of data to a single field on a FormClass
 *
 */
public interface FieldImportStrategy {

    /**
     * Returns true if this field importer can handle the given
     * {@code fieldNode}
     */
    boolean accept(FormTree.Node fieldNode);

    /**
     * Returns a list of potential "sites" to which imported columns
     * can be bound.
     */
    List<ImportTarget> getImportSites(FormTree.Node node);


    FieldImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> mappings, ImportModel model);

}
