package org.activityinfo.core.shared.importing.strategy;

import com.google.common.base.Objects;
import org.activityinfo.core.shared.importing.model.ImportModel;
import org.activityinfo.core.shared.type.converter.FieldParserFactory;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.type.FieldTypeClass;

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
                Objects.firstNonNull(mappings.get(LATITUDE), MissingColumn.INSTANCE),
                Objects.firstNonNull(mappings.get(LONGITUDE), MissingColumn.INSTANCE) };

        ImportTarget targets[] = new ImportTarget[] {
                latitudeTarget(node),
                longitudeTarget(node) };

        return new GeographicPointImporter(node.getFieldId(), sourceColumns, targets,
                converterFactory.getCoordinateNumberFormatter());
    }
}
