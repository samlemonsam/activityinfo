package org.activityinfo.core.shared.importing.strategy;

import org.activityinfo.core.client.ResourceLocator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.validation.ValidationResult;
import org.activityinfo.model.type.geo.AiLatLng;
import org.activityinfo.core.shared.type.converter.CoordinateAxis;
import org.activityinfo.core.shared.type.converter.CoordinateFormatException;
import org.activityinfo.core.shared.type.converter.CoordinateParser;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.promise.Promise;

import java.util.Arrays;
import java.util.List;

/**
 * Importer for Lat/Lng
 */
public class GeographicPointImporter implements FieldImporter {

    private final ResourceId fieldId;
    private final ColumnAccessor[] sourceColumns;
    private final CoordinateParser[] coordinateParsers;
    private final List<FieldImporterColumn> fieldImporterColumns;

    public GeographicPointImporter(ResourceId fieldId, ColumnAccessor[] sourceColumns, ImportTarget[] targetSites,
                                   CoordinateParser.NumberFormatter coordinateNumberFormatter) {
        this.fieldId = fieldId;
        this.sourceColumns = sourceColumns;
        CoordinateParser latitudeParser = new CoordinateParser(CoordinateAxis.LATITUDE, coordinateNumberFormatter);
        CoordinateParser longitudeParser = new CoordinateParser(CoordinateAxis.LONGITUDE, coordinateNumberFormatter);

        latitudeParser.setRequireSign(false);
        longitudeParser.setRequireSign(false);

        this.coordinateParsers = new CoordinateParser[]{latitudeParser, longitudeParser };
        this.fieldImporterColumns = Arrays.asList(
                new FieldImporterColumn(targetSites[0], sourceColumns[0]),
                new FieldImporterColumn(targetSites[1], sourceColumns[1]));
    }

    @Override
    public Promise<Void> prepare(ResourceLocator locator, List<? extends SourceRow> batch) {
        return Promise.done();
    }

    @Override
    public List<FieldImporterColumn> getColumns() {
        return fieldImporterColumns;
    }

    @Override
    public void validateInstance(SourceRow row, List<ValidationResult> results) {
        boolean latitudeMissing = sourceColumns[0].isMissing(row);
        boolean longitudeMissing = sourceColumns[1].isMissing(row);

        if (latitudeMissing && longitudeMissing) {
            results.add(ValidationResult.MISSING);
            results.add(ValidationResult.MISSING);
        } else {
            results.add(validateCoordinate(row, 0));
            results.add(validateCoordinate(row, 1));
        }
    }

    private ValidationResult validateCoordinate(SourceRow row, int i) {

        if (sourceColumns[i].isMissing(row)) {
            return ValidationResult.error("Both latitude and longitude are required");
        }

        try {
            double coordinate = parseCoordinate(row, i);
            // we reformat the coordinate make clear the conversion
            return ValidationResult.converted(coordinateParsers[i].format(coordinate), 1);
        } catch (Exception e) {
            return ValidationResult.error(e.getMessage());
        }
    }

    private double parseCoordinate(SourceRow row, int i) throws CoordinateFormatException {
        String string = sourceColumns[i].getValue(row);
        return coordinateParsers[i].parse(string);
    }

    @Override
    public boolean updateInstance(SourceRow row, FormInstance instance) {
        final boolean isLatOk = validateCoordinate(row, 0).shouldPersist();
        final boolean isLonOk = validateCoordinate(row, 1).shouldPersist();
        if (isLatOk && isLonOk) {
            double latitude = parseCoordinate(row, 0);
            double longitude = parseCoordinate(row, 1);
            instance.set(fieldId, new GeoPoint(latitude, longitude));
            return true;
        }
        return false;
    }
}
