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
package org.activityinfo.analysis.table;

import org.activityinfo.analysis.ParsedFormula;
import org.activityinfo.model.formula.FormulaNode;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.*;

public class ColumnFormatFactory implements FieldTypeVisitor<ColumnFormat> {


    private String columnId;
    private FormulaNode formula;

    public ColumnFormatFactory(String columnId, FormulaNode formula) {
        this.columnId = columnId;
        this.formula = formula;
    }

    public static ColumnFormat create(String id, ParsedFormula formula) {
        if(!formula.isValid()) {
            return new ErrorFormat();
        }

        return formula.getResultType().accept(new ColumnFormatFactory(id, formula.getRootNode()));
    }


    @Override
    public ColumnFormat visitAttachment(AttachmentType attachmentType) {
        return new ErrorFormat();
    }

    @Override
    public ColumnFormat visitCalculated(CalculatedFieldType calculatedFieldType) {
        assert false : "calculated expression should have been resolved to concrete type";
        return new ErrorFormat();
    }

    @Override
    public ColumnFormat visitReference(ReferenceType referenceType) {
        return new TextFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitNarrative(NarrativeType narrativeType) {
        return new TextFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitBoolean(BooleanType booleanType) {
        return new BooleanFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitQuantity(QuantityType type) {
        return new NumberFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitGeoPoint(GeoPointType geoPointType) {
        return new GeoPointFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitGeoArea(GeoAreaType geoAreaType) {
        return new ErrorFormat();
    }

    @Override
    public ColumnFormat visitEnum(EnumType enumType) {
        if(enumType.getCardinality() == Cardinality.SINGLE) {
            return new SingleEnumFormat(columnId, formula, enumType);
        } else {
            return new MultiEnumFormat(columnId, formula, enumType);
        }
    }

    @Override
    public ColumnFormat visitBarcode(BarcodeType barcodeType) {
        return new TextFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitSubForm(SubFormReferenceType subFormReferenceType) {
        return new ErrorFormat();
    }

    @Override
    public ColumnFormat visitLocalDate(LocalDateType localDateType) {
        return new DateFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitWeek(EpiWeekType epiWeekType) {
        return new TextFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitFortnight(FortnightType fortnightType) {
        return new TextFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitMonth(MonthType monthType) {
        return new TextFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitYear(YearType yearType) {
        return new NumberFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
        return new ErrorFormat();
    }

    @Override
    public ColumnFormat visitText(TextType textType) {
        return new TextFormat(columnId, formula);
    }

    @Override
    public ColumnFormat visitSerialNumber(SerialNumberType serialNumberType) {
        return new TextFormat(columnId, formula);
    }

}
