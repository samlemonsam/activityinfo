package org.activityinfo.model.query;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.activityinfo.model.resource.Resources;

import java.util.Map;

/**
 * Created by yuriyz on 4/25/2016.
 */
public class ColumnSetParser {

    public static ColumnSet fromJsonColumnFormat(String json) {
        return fromJsonColumnFormat(Resources.toJsonObject(json));
    }

    public static ColumnSet fromJsonColumnFormat(JsonObject jsonObject) {
        int rowsCount = jsonObject.getAsJsonPrimitive("rows").getAsInt();
        Map<String, ColumnView> views = Maps.newHashMap();

        for (Map.Entry<String, JsonElement> column : jsonObject.getAsJsonObject("columns").entrySet()) {
            JsonObject columnValue = column.getValue().getAsJsonObject();
            String storage = columnValue.getAsJsonPrimitive("storage").getAsString();
            switch (storage) {
                case "array":
                    views.put(column.getKey(), new ColumnViewWrapper(rowsCount, columnValue.getAsJsonArray("values")));
                    break;
                case "coordinates":
                    views.put(column.getKey(), parseCoordinates(columnValue.getAsJsonArray("coordinates")));
                    break;
                case "empty":
                    views.put(column.getKey(), parseEmpty(rowsCount, columnValue));
                    break;
                default:
                    throw new UnsupportedOperationException(storage);
            }
        }

        return new ColumnSet(rowsCount, views);
    }

    private static ColumnView parseEmpty(int numRows, JsonObject columnValue) {
        String typeName = columnValue.get("type").getAsString();
        ColumnType type = ColumnType.valueOf(typeName);
        return new EmptyColumnView(numRows, type);
    }

    private static ColumnView parseCoordinates(JsonArray coordinateArray) {
        double[] coordinates = new double[coordinateArray.size()];
        for (int i = 0; i < coordinateArray.size(); i++) {
            JsonElement coord = coordinateArray.get(i);
            if(coord.isJsonNull()) {
                coordinates[i] = Double.NaN;
            } else {
                coordinates[i] = coord.getAsDouble();
            }
        }
        return new GeoColumnView(coordinates);
    }
}
