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

import com.google.common.collect.Lists;
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

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
        EnumType type = (EnumType) node.getType();
        List<ImportTarget> result = Lists.newArrayList();
        if (type.getCardinality() == Cardinality.SINGLE) {
            result.add(new ImportTarget(node.getField(), VALUE, node.getField().getLabel(), node.getDefiningFormClass().getId()));
        } else {
            for (EnumItem item : type.getValues()) {
                result.add(new ImportTarget(node.getField(), new TargetSiteId(item.getId().asString()), label(item.getLabel(), node.getField().getLabel()), node.getDefiningFormClass().getId()));
            }
        }
        return result;
    }

    public static String label(String itemLabel, String fieldLabel) {
        return itemLabel + " - " + fieldLabel;
    }

    @Override
    public FieldImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> mappings, ImportModel model) {

        EnumType type = (EnumType) node.getType();
        List<ColumnAccessor> sourceColumns = Lists.newArrayList();

        if (type.getCardinality() == Cardinality.SINGLE) {
            sourceColumns.add(mappings.get(VALUE));
        } else {
            for (EnumItem item : type.getValues()) {
                sourceColumns.add(mappings.get(new TargetSiteId(item.getId().asString())));
            }
        }

        return new EnumFieldImporter(sourceColumns, getImportSites(node), type);
    }

}
