package org.activityinfo.io.csv;

import org.activityinfo.analysis.table.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.query.ColumnSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CsvColumnFactory implements TableColumnVisitor<List<CsvColumn>> {

    private ColumnSet columnSet;

    public CsvColumnFactory(ColumnSet columnSet) {
        this.columnSet = columnSet;
    }

    @Override
    public List<CsvColumn> visitTextColumn(EffectiveTableColumn columnModel, TextFormat textFormat) {
        return singleColumn(columnModel, with(textFormat.createRenderer()));
    }

    @Override
    public List<CsvColumn> visitNumberColumn(EffectiveTableColumn columnModel, NumberFormat numberFormat) {
        return singleColumn(columnModel, with(numberFormat.createRenderer()));
    }

    @Override
    public List<CsvColumn> visitErrorColumn(EffectiveTableColumn columnModel, ErrorFormat errorFormat) {
        return Collections.emptyList();
    }

    @Override
    public List<CsvColumn> visitGeoPointColumn(EffectiveTableColumn columnModel, GeoPointFormat geoPointFormat) {
        ColumnRenderer<Double> latRenderer = geoPointFormat.createLatitudeRenderer();
        ColumnRenderer<Double> longRenderer = geoPointFormat.createLongitudeRenderer();

        String latHeading = columnModel.getLabel() + " - " + I18N.CONSTANTS.latitude();
        String longHeading = columnModel.getLabel() + " - " + I18N.CONSTANTS.longitude();

        CsvColumn latitudeColumn = new CsvColumn(latHeading, with(latRenderer));
        CsvColumn longitudeColumn = new CsvColumn(longHeading, with(longRenderer));

        return Arrays.asList(latitudeColumn, longitudeColumn);
    }

    @Override
    public List<CsvColumn> visitMultiEnumColumn(EffectiveTableColumn columnModel, MultiEnumFormat multiEnumFormat) {
        return singleColumn(columnModel, with(multiEnumFormat.createRenderer()));
    }

    @Override
    public List<CsvColumn> visitBooleanColumn(EffectiveTableColumn columnModel, BooleanFormat booleanFormat) {
        return singleColumn(columnModel, with(booleanFormat.createRenderer()));
    }

    @Override
    public List<CsvColumn> visitDateColumn(EffectiveTableColumn columnModel, DateFormat dateFormat) {
        return singleColumn(columnModel, with(dateFormat.createLocalDateRenderer()));
    }

    @Override
    public List<CsvColumn> visitSingleEnumColumn(EffectiveTableColumn columnModel, SingleEnumFormat singleEnumFormat) {
        return singleColumn(columnModel, with(singleEnumFormat.createRenderer()));
    }

    private List<CsvColumn> singleColumn(EffectiveTableColumn columnModel, ColumnRenderer renderer) {
        CsvColumn column = new CsvColumn(columnModel.getLabel(), renderer);
        return Collections.singletonList(column);
    }

    private <T> ColumnRenderer<T> with(ColumnRenderer<T> columnRenderer) {
        columnRenderer.updateColumnSet(columnSet);
        return columnRenderer;
    }

}
