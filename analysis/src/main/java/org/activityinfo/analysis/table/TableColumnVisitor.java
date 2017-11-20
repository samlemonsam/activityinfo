package org.activityinfo.analysis.table;

public interface TableColumnVisitor<T> {

    T visitTextColumn(EffectiveTableColumn columnModel, TextFormat textFormat);

    T visitNumberColumn(EffectiveTableColumn columnModel, NumberFormat numberFormat);

    T visitErrorColumn(EffectiveTableColumn columnModel, ErrorFormat errorFormat);

    T visitGeoPointColumn(EffectiveTableColumn columnModel, GeoPointFormat geoPointFormat);

    T visitMultiEnumColumn(EffectiveTableColumn columnModel, MultiEnumFormat multiEnumFormat);

    T visitBooleanColumn(EffectiveTableColumn columnModel, BooleanFormat booleanFormat);

    T visitDateColumn(EffectiveTableColumn columnModel, DateFormat dateFormat);

    T visitSingleEnumColumn(EffectiveTableColumn columnModel, SingleEnumFormat singleEnumFormat);

}
