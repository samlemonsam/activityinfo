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
            case "coordinates":
                JSONArray jsonArray = jsonColumn.get("coordinates").isArray();
                double[] coordinates = new double[jsonArray.size()];
                for (int i = 0 ; i < jsonArray.size(); i++) {
                    JSONNumber number = jsonArray.get(i).isNumber();
                    coordinates[i] = number != null ? number.doubleValue() : Double.NaN;
                }
                switch (type) {
                    case GEOGRAPHIC_POINT:
                        return new GeoPointColumnView(coordinates);
                    case GEOGRAPHIC_AREA:
                        return new GeoAreaColumnView(coordinates);
                }
        }
        throw new UnsupportedOperationException("ColumnSetParser: storage is not supported, storage: " + storage);
    }
}
