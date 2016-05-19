package org.activityinfo.legacy.shared.adapter;

import com.google.gwt.http.client.*;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnSetParser;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.promise.Promise;

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
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, baseUrl + "/resources/query/columns");
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
                        promise.resolve(ColumnSetParser.fromJsonColumnFormat(response.getText()));
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
}
