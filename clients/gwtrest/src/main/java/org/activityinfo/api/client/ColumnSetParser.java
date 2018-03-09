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
package org.activityinfo.api.client;

import com.google.common.collect.Maps;
import com.google.gwt.json.client.*;
import org.activityinfo.model.query.*;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


class ColumnSetParser {

    private static final Logger LOGGER = Logger.getLogger(ColumnSetParser.class.getName());

    static ColumnSet fromJson(String json) {
        try {
            JSONObject jsonObject = JSONParser.parseStrict(json).isObject();
            int rowsCount = (int) jsonObject.get("rows").isNumber().doubleValue();

            Map<String, ColumnView> views = Maps.newHashMap();

            JSONObject jsonColumns = jsonObject.get("columns").isObject();
            for (String columnId : jsonColumns.keySet()) {
                views.put(columnId, parseColumn(rowsCount, jsonColumns.get(columnId).isObject()));
            }
            return new ColumnSet(rowsCount, views);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to parse column set: " + e.getMessage() + "\n" + json, e);
            throw e;
        }
    }

    private static ColumnView parseColumn(int rowsCount, JSONObject jsonColumn) {
        String storage = jsonColumn.get("storage").isString().stringValue();
        String typeName = jsonColumn.get("type").isString().stringValue();
        ColumnType type = ColumnType.valueOf(typeName);

        switch (storage) {
            case "array":
                return new ColumnArrayView(type, jsonColumn.get("values").isArray().getJavaScriptObject());
            case "empty":
                return new EmptyColumnView(type, rowsCount);
            case "constant":
                JSONValue jsonValue = jsonColumn.get("value");
                switch (type) {
                    case STRING:
                        JSONString jsonString = jsonValue.isString();
                        if(jsonString == null) {
                            return new ConstantColumnView(rowsCount, null);
                        } else {
                            return new ConstantColumnView(rowsCount, jsonString.stringValue());
                        }
                    case NUMBER:
                        JSONNumber jsonNumber = jsonValue.isNumber();
                        if(jsonNumber == null) {
                            return new ConstantColumnView(rowsCount, Double.NaN);
                        } else {
                            return new ConstantColumnView(rowsCount, jsonNumber.doubleValue());
                        }
                    case BOOLEAN:
                        JSONBoolean jsonBoolean = jsonValue.isBoolean();
                        if(jsonBoolean == null) {
                            return ConstantColumnView.nullBoolean(rowsCount);
                        } else {
                            return new ConstantColumnView(rowsCount, jsonValue.isBoolean().booleanValue());
                        }
                }
        }
        throw new UnsupportedOperationException("ColumnSetParser: storage is not supported, storage: " + storage);
    }
}
