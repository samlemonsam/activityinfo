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
package org.activityinfo.store.mysql.cursor;

import org.activityinfo.model.formula.eval.FieldReader;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.mysql.mapping.FieldValueConverter;

import java.sql.ResultSet;
import java.sql.SQLException;


class ResultSetFieldReader implements FieldReader<ResultSet> {

    private int columnIndex;
    private FieldValueConverter extractor;
    private FieldType type;


    public ResultSetFieldReader(int columnIndex, FieldValueConverter extractor, FieldType type) {
        this.columnIndex = columnIndex;
        this.extractor = extractor;
        this.type = type;
    }

    @Override
    public FieldValue readField(ResultSet rs) {
        try {
            return extractor.toFieldValue(rs, columnIndex);
        } catch (SQLException e) {
            throw new RuntimeException("Exception reading column " + columnIndex + " using " + extractor, e);
        }
    }

    @Override
    public FieldType getType() {
        return type;
    }
}
