package org.activityinfo.core.shared.importing.strategy;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author yuriyz on 09/01/2015.
 */
public class EnumImportStrategy implements FieldImportStrategy {

    public static final TargetSiteId VALUE = new TargetSiteId("value");

    @Override
    public boolean accept(FormTree.Node fieldNode) {
        return fieldNode.isEnum();
    }

    @Override
    public List<ImportTarget> getImportSites(FormTree.Node node) {
        return Collections.singletonList(target(node));
    }

    @Override
    public FieldImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> mappings) {
        return new EnumFieldImporter(mappings.get(VALUE), target(node), (EnumType) node.getType());
    }

    private ImportTarget target(FormTree.Node node) {
        return new ImportTarget(node.getField(), VALUE, node.getField().getLabel(), node.getDefiningFormClass().getId());
    }
}
