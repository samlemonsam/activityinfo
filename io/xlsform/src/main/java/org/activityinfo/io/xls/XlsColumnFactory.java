package org.activityinfo.io.xls;

import org.activityinfo.analysis.table.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.query.ColumnSet;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class XlsColumnFactory implements TableColumnVisitor<List<XlsColumn>> {

    private XlsColumnStyleFactory styles;
    private ColumnSet columnSet;

    public XlsColumnFactory(XlsColumnStyleFactory styles, ColumnSet columnSet) {
        this.styles = styles;
        this.columnSet = columnSet;
    }

    @Override
    public List<XlsColumn> visitTextColumn(EffectiveTableColumn columnModel, TextFormat textFormat) {
        return singleColumn(columnModel, styles.getTextStyle(), new XlsTextRenderer(with(textFormat.createRenderer())));
    }

    @Override
    public List<XlsColumn> visitNumberColumn(EffectiveTableColumn columnModel, NumberFormat numberFormat) {
        return singleColumn(columnModel, styles.getTextStyle(), new XlsNumberRenderer(with(numberFormat.createRenderer())));
    }

    @Override
    public List<XlsColumn> visitErrorColumn(EffectiveTableColumn columnModel, ErrorFormat errorFormat) {
        return Collections.emptyList();
    }

    @Override
    public List<XlsColumn> visitGeoPointColumn(EffectiveTableColumn columnModel, GeoPointFormat geoPointFormat) {

        ColumnRenderer<Double> latRenderer = with(geoPointFormat.createLatitudeRenderer());
        ColumnRenderer<Double> lngRenderer = with(geoPointFormat.createLongitudeRenderer());

        String latHeader = columnModel.getLabel() + " - " + I18N.CONSTANTS.latitude();
        String lngHeader = columnModel.getLabel() + " - " + I18N.CONSTANTS.longitude();

        XlsColumn latColumn = new XlsColumn(latHeader, styles.getCoordStyle(), new XlsNumberRenderer(latRenderer));
        XlsColumn lngColumn = new XlsColumn(lngHeader, styles.getCoordStyle(), new XlsNumberRenderer(lngRenderer));

        return Arrays.asList(latColumn, lngColumn);
    }

    @Override
    public List<XlsColumn> visitMultiEnumColumn(EffectiveTableColumn columnModel, MultiEnumFormat multiEnumFormat) {
        return singleColumn(columnModel, styles.getTextStyle(), new XlsTextRenderer(with(multiEnumFormat.createRenderer())));
    }

    @Override
    public List<XlsColumn> visitBooleanColumn(EffectiveTableColumn columnModel, BooleanFormat booleanFormat) {
        return singleColumn(columnModel, styles.getTextStyle(), new XlsBooleanRenderer(with(booleanFormat.createRenderer())));
    }

    @Override
    public List<XlsColumn> visitDateColumn(EffectiveTableColumn columnModel, DateFormat dateFormat) {
        return singleColumn(columnModel, styles.getDateStyle(), new XlsDateRenderer(with(dateFormat.createRenderer())));
    }

    @Override
    public List<XlsColumn> visitSingleEnumColumn(EffectiveTableColumn columnModel, SingleEnumFormat singleEnumFormat) {
        return singleTextColumn(columnModel, singleEnumFormat);
    }

    private List<XlsColumn> singleTextColumn(EffectiveTableColumn columnModel, SimpleColumnFormat<String> format) {
        return singleColumn(columnModel, styles.getTextStyle(), new XlsTextRenderer(with(format.createRenderer())));
    }

    private List<XlsColumn> singleColumn(EffectiveTableColumn columnModel, CellStyle style, XlsColumnRenderer renderer) {
        XlsColumn column = new XlsColumn(columnModel.getLabel(), style, renderer);

        return Collections.singletonList(column);
    }

    private <T> ColumnRenderer<T> with(ColumnRenderer<T> renderer) {
        renderer.updateColumnSet(columnSet);
        return renderer;
    }
}
