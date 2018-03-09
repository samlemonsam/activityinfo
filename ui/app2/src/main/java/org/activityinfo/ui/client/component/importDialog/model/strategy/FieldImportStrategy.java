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
