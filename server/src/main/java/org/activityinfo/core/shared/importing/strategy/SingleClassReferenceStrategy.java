package org.activityinfo.core.shared.importing.strategy;

import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.model.formTree.FormTree;

import java.util.List;
import java.util.Map;

/**
 * Strategy for importing reference fields whose ranges include a single class. In this
 * case, we also know the class of the value that will be imported, but we still need to
 * deal with transient references that might include more complex reference fields, such
 * as point-in-hierarchies.
 *
 * <p>In the example below, both the Partner and Localite fields would be accepted by this
 * strategy, but we consider each unique field as a single target site, even if it is
 * referenced by multiple fields in the tree.
 *
 * <p><We should actually be distinguishing between fields of equivalent ranges but different
 * semantic meaning, but we don't yet have a use case that poses this problem.
 *
 * <pre>
 * ..Partner = <Partner>
 * ..............Name
 * ..............Full Name
 * ..Localité = [Localité]
 * ...............Name
 * ...............Alternate Name
 * ...............Geographic coordinates
 * ...............Administrative Unit = [Zone de Santé  | Territoire | District | Province]
 * ....................................................Name
 * .....................................[Province]
 * .......................................Name
 * .....................................[Zone de Santé]
 * .......................................Name
 *.......................................Province = [Province]
 * ....................................................Name
 * .....................................[District]
 * .......................................Name
 * .......................................Province = [Province]
 * .....................................[Territoire]
 * .......................................Name
 * .......................................District = [District]
 * ....................................................Name
 * ....................................................Province = [Province]
 * .................................................................Name
 * </pre>
 *
 * In the example above, we allow the user to match columns to "Partner Name" and.or "Partner Full Name"
 * which are used to match the Partner entity.
 *
 */
public class SingleClassReferenceStrategy implements FieldImportStrategy {


    @Override
    public boolean accept(FormTree.Node fieldNode) {
        return fieldNode.isReference() && fieldNode.getRange().size() == 1;
    }

    @Override
    public List<ImportTarget> getImportSites(FormTree.Node node) {
        return new SingleClassTargetBuilder(node).getTargets();
    }

    @Override
    public SingleClassImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> mappings, ImportModel model) {
        return new SingleClassTargetBuilder(node).newImporter(mappings);
    }
}
