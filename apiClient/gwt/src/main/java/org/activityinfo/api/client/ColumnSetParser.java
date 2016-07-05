package org.activityinfo.api.client;

import com.google.common.collect.Maps;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
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
                        return new ConstantColumnView(rowsCount, jsonValue.isString().stringValue());
                    case NUMBER:
                        return new ConstantColumnView(rowsCount, jsonValue.isNumber().doubleValue());
                    case BOOLEAN:
                        return new ConstantColumnView(rowsCount, jsonValue.isBoolean().booleanValue());
                }
        }
        throw new UnsupportedOperationException("ColumnSetParser: storage is not supported, storage: " + storage);
    }
}
