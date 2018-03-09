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
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.type.converter.FieldParserFactory;
import org.activityinfo.ui.client.component.importDialog.model.type.converter.FieldValueParser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Imports a simple data field
 */
public class DataFieldImportStrategy implements FieldImportStrategy {

    public static final TargetSiteId VALUE = new TargetSiteId("value");

    private final FieldParserFactory converterFactory;

    public DataFieldImportStrategy(FieldParserFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    @Override
    public boolean accept(FormTree.Node fieldNode) {
        return !fieldNode.isReference() && !fieldNode.isEnum();
    }

    @Override
    public List<ImportTarget> getImportSites(FormTree.Node node) {
        if (node.getType() instanceof AttachmentType) { // we are not ready for attachment import yet
            return Collections.emptyList();
        }
        return Collections.singletonList(target(node));
    }

    @Override
    public FieldImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> bindings, ImportModel model) {

        ImportTarget requiredTarget = target(node);
        ColumnAccessor column = bindings.get(VALUE);
        if(column == null) {
            column = MissingColumn.INSTANCE;
        }

        FieldValueParser converter = converterFactory.createStringConverter(node.getType());

        return new DataFieldImporter(column, requiredTarget, converter, node, model);
    }

    private ImportTarget target(FormTree.Node node) {
        return new ImportTarget(node.getField(), VALUE, node.getField().getLabel(), node.getDefiningFormClass().getId());
    }
}
