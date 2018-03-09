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

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;

import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 5/19/14.
 */
public class HierarchyReferenceStrategy implements FieldImportStrategy {

    @Override
    public boolean accept(FormTree.Node fieldNode) {
        return fieldNode.isReference() && fieldNode.getRange().size() > 1;
    }

    @Override
    public List<ImportTarget> getImportSites(FormTree.Node node) {
        return new HierarchyClassTargetBuilder(node).getTargets();
    }

    @Override
    public HierarchyClassImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> mappings, ImportModel model) {
        return new HierarchyClassTargetBuilder(node).newImporter(mappings);
    }
}
