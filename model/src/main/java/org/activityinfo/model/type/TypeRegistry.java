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
package org.activityinfo.model.type;

import com.google.common.collect.Maps;
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

import java.util.Map;

/**
 * Global registry of {@code FieldTypeClass}es.
 */
public class TypeRegistry {

    private static TypeRegistry INSTANCE;

    public static TypeRegistry get() {
        if (INSTANCE == null) {
            INSTANCE = new TypeRegistry();
        }
        return INSTANCE;
    }

    private Map<String, FieldTypeClass> typeMap = Maps.newHashMap();

    private TypeRegistry() {
        register(EnumType.TYPE_CLASS);
        register(ReferenceType.TYPE_CLASS);
        register(TextType.TYPE_CLASS);
        register(QuantityType.TYPE_CLASS);
        register(NarrativeType.TYPE_CLASS);
        register(CalculatedFieldType.TYPE_CLASS);
        register(LocalDateType.TYPE_CLASS);
        register(LocalDateIntervalType.TYPE_CLASS);
        register(MonthType.TYPE_CLASS);
        register(EpiWeekType.TYPE_CLASS);
        register(FortnightType.TYPE_CLASS);
        register(GeoPointType.TYPE_CLASS);
        register(GeoAreaType.TYPE_CLASS);
        register(BooleanType.TYPE_CLASS);
        register(BarcodeType.TYPE_CLASS);
        register(AttachmentType.TYPE_CLASS);
        register(SubFormReferenceType.TYPE_CLASS);
        register(SerialNumberType.TYPE_CLASS);
    }

    private void register(FieldTypeClass typeClass) {
        if (typeMap.containsKey(typeClass.getId())) {
            throw new RuntimeException("Type already registered: " + typeClass);
        }
        typeMap.put(typeClass.getId().toUpperCase(), typeClass);
    }

    public FieldTypeClass getTypeClass(String typeId) {

        // Handle deprecated ids:
        switch (typeId) {
            case "LOCAL_DATE":
                return LocalDateType.TYPE_CLASS;
            case "GEOGRAPHIC_POINT":
                return LocalDateType.TYPE_CLASS;
        }


        FieldTypeClass typeClass = typeMap.get(typeId.toUpperCase());
        if (typeClass == null) {
            throw new RuntimeException("Unknown type: " + typeId);
        }
        return typeClass;
    }

    public Iterable<FieldTypeClass> getTypeClasses() {
        return typeMap.values();
    }


}
