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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.ui.client.component.importDialog.model.ImportModel;
import org.activityinfo.ui.client.component.importDialog.model.type.converter.FieldParserFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeographicPointImportStrategy implements FieldImportStrategy {

    static final TargetSiteId LATITUDE = new TargetSiteId("latitude");
    static final TargetSiteId LONGITUDE = new TargetSiteId("longitude");
    private final FieldParserFactory converterFactory;

    public GeographicPointImportStrategy(FieldParserFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    @Override
    public boolean accept(FormTree.Node fieldNode) {
        return fieldNode.getTypeClass() == FieldTypeClass.GEOGRAPHIC_POINT;
    }

    @Override
    public List<ImportTarget> getImportSites(FormTree.Node node) {
        return Arrays.asList(
                latitudeTarget(node),
                longitudeTarget(node));
    }

    private ImportTarget longitudeTarget(FormTree.Node node) {
        return new ImportTarget(node.getField(), LONGITUDE, I18N.CONSTANTS.longitude(), node.getDefiningFormClass().getId());
    }

    private ImportTarget latitudeTarget(FormTree.Node node) {
        return new ImportTarget(node.getField(), LATITUDE, I18N.CONSTANTS.latitude(), node.getDefiningFormClass().getId());
    }

    @Override
    public FieldImporter createImporter(FormTree.Node node, Map<TargetSiteId, ColumnAccessor> mappings, ImportModel model) {
        ColumnAccessor sourceColumns[] = new ColumnAccessor[] {
                MoreObjects.firstNonNull(mappings.get(LATITUDE), MissingColumn.INSTANCE),
                MoreObjects.firstNonNull(mappings.get(LONGITUDE), MissingColumn.INSTANCE) };

        ImportTarget targets[] = new ImportTarget[] {
                latitudeTarget(node),
                longitudeTarget(node) };

        return new GeographicPointImporter(node.getFieldId(), sourceColumns, targets,
                converterFactory.getCoordinateNumberFormatter());
    }
}
