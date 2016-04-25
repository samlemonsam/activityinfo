package org.activityinfo.legacy.shared.adapter;

import com.google.common.collect.Maps;
import com.google.gwt.http.client.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.Record;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.promise.Promise;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by yuriyz on 4/25/2016.
 */
public class HttpQueryExecutor {

    private static final Logger LOGGER = Logger.getLogger(HttpQueryExecutor.class.getName());

    private String baseUrl = "";

    public HttpQueryExecutor() {
    }

    public HttpQueryExecutor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Promise<ColumnSet> query(QueryModel model) {
        return queryColumnsRemotely(model);
    }

    private Promise<ColumnSet> queryColumnsRemotely(QueryModel queryModel) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, baseUrl + "/query/columns");
        builder.setHeader("Content-Type", "application/json");

        final Promise<ColumnSet> promise = new Promise<>();
        try {
            builder.sendRequest(Resources.toJson(queryModel.asRecord()), new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    LOGGER.log(Level.SEVERE, "Failed to perform HTTP request. " + exception.getMessage(), exception);
                    promise.reject(exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        promise.resolve(fromJson(response.getText()));
                    } else {
                        LOGGER.log(Level.SEVERE, "Failed to perform HTTP request. Status: " + response.getStatusCode()
                                + ", text: " + response.getText());
                        promise.reject(new RuntimeException("Failed to perform HTTP request. " + response.getText()));
                    }
                }
            });
        } catch (RequestException e) {
            LOGGER.log(Level.SEVERE, "Unable to perform HTTP request. " + e.getMessage(), e);
            promise.reject(e);
        }
        return promise;
    }

    public static ColumnSet fromJson(String json) {
        Record record = Resources.recordFromJson(json);

        // TODO
//        int numRows = object.getAsJsonPrimitive("rows").getAsInt();
//
//        Map<String, ColumnView> columnMap = new HashMap<>();
//        for (Map.Entry<String, JsonElement> column : object.getAsJsonObject("columns").entrySet()) {
//            JsonObject columnValue = column.getValue().getAsJsonObject();
//            String storage = columnValue.getAsJsonPrimitive("storage").getAsString();
//            switch (storage) {
//                case "array":
//                    columnMap.put(column.getKey(), new ColumnViewWrapper(numRows, columnValue.getAsJsonArray("values")));
//                    break;
//                case "coordinates":
//                    columnMap.put(column.getKey(), parseCoordinates(columnValue.getAsJsonArray("coordinates")));
//                    break;
//                case "empty":
//                    columnMap.put(column.getKey(), parseEmpty(numRows, columnValue));
//                    break;
//                default:
//                    throw new UnsupportedOperationException(storage);
//            }
//        }
        HashMap<String, ColumnView> map = Maps.newHashMap();
        ColumnSet set = new ColumnSet(0, map);
        return set;
    }

}
