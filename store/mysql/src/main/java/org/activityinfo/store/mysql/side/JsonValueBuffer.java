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
package org.activityinfo.store.mysql.side;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.activityinfo.json.JsonParser;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.store.spi.CursorObserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yuriyz on 5/18/2016.
 */
public class JsonValueBuffer implements ValueBuffer {

    private FieldValue value = null;
    private final List<CursorObserver<FieldValue>> observers = Lists.newArrayList();

    private final FieldType type;
    private final JsonParser parser = new JsonParser();

    public JsonValueBuffer(FieldType type) {
        this.type = type;
    }

    @Override
    public void add(CursorObserver<FieldValue> observer) {
        observers.add(observer);
    }

    @Override
    public void set(ResultSet rs) throws SQLException {
        String json = rs.getString(STRING_VALUE_COLUMN);
        if (Strings.isNullOrEmpty(json)) {
            value = null;
        } else {
            value = type.parseJsonValue(parser.parse(json));
        }
    }

    @Override
    public void next() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.onNext(value);
        }
        value = null;
    }

    @Override
    public void done() {
        for (CursorObserver<FieldValue> observer : observers) {
            observer.done();
        }
    }
}
