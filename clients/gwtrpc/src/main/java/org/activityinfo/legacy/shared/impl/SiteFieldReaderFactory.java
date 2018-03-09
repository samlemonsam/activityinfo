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
package org.activityinfo.legacy.shared.impl;

import org.activityinfo.legacy.shared.model.IndicatorDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formula.eval.FieldReader;
import org.activityinfo.model.formula.eval.FieldReaderFactory;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

public class SiteFieldReaderFactory implements FieldReaderFactory<SiteDTO> {

    @Override
    public FieldReader<SiteDTO> create(FormField field) {
        if(field.getType() instanceof QuantityType) {
            return new QuantityReader(field);
        } else {
            return new NullReader();
        }
    }

    private static class NullReader implements FieldReader<SiteDTO> {

        @Override
        public FieldValue readField(SiteDTO record) {
            return NullFieldValue.INSTANCE;
        }

        @Override
        public FieldType getType() {
            return new QuantityType();
        }
    }

    private static class QuantityReader implements FieldReader<SiteDTO> {

        private final QuantityType type;
        private final String propertyName;


        public QuantityReader(FormField field) {
            type = (QuantityType) field.getType();
            propertyName = IndicatorDTO.getPropertyName(CuidAdapter.getLegacyIdFromCuid(field.getId()));
        }

        @Override
        public FieldValue readField(SiteDTO record) {
            Object value = record.get(propertyName);
            if(value instanceof Number) {
                return new Quantity(((Number) value).doubleValue());
            } else {
                return NullFieldValue.INSTANCE;
            }
        }

        @Override
        public FieldType getType() {
            return type;
        }
    }
}
