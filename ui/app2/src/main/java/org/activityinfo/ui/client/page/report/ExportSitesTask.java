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
package org.activityinfo.ui.client.page.report;

import com.google.common.base.Strings;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.Filter;
import org.activityinfo.legacy.shared.command.FilterUrlSerializer;
import org.activityinfo.ui.client.dispatch.Dispatcher;

public class ExportSitesTask implements ExportDialog.AsyncTask {

    private Dispatcher dispatcher;
    private Filter filter;

    public ExportSitesTask(Dispatcher dispatcher, Filter filter) {
        this.dispatcher = dispatcher;
        this.filter = filter;
    }

    @Override
    public void start(final AsyncCallback<ExportDialog.AsyncTaskPoller> callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, "/ActivityInfo/export");
        requestBuilder.setHeader("Content-type", "application/x-www-form-urlencoded");
        requestBuilder.setRequestData("locale=" + LocaleInfo.getCurrentLocale().getLocaleName() +
                "&filter=" + FilterUrlSerializer.toUrlFragment(filter));
        requestBuilder.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                final String exportId = response.getText();
                if(Strings.isNullOrEmpty(exportId)) {
                    callback.onFailure(new RuntimeException());
                } else {
                    callback.onSuccess(new Poller(exportId));
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                callback.onFailure(exception);
            }
        });
        try {
            requestBuilder.send();
        } catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    private class Poller implements ExportDialog.AsyncTaskPoller {

        private String exportId;

        public Poller(String exportId) {
            this.exportId = exportId;
        }

        @Override
        public void poll(final ExportDialog.ProgressCallback callback) {
            RequestBuilder request = new RequestBuilder(RequestBuilder.GET, "/generated/status/" + exportId);
            request.setCallback(new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        JSONObject status = JSONParser.parseStrict(response.getText()).isObject();
                        double progress = status.get("progress").isNumber().doubleValue();
                        JSONValue downloadUri = status.get("downloadUri");

                        if(downloadUri != null) {
                            callback.onDownloadReady(downloadUri.isString().stringValue());
                        } else {
                            callback.onProgress(progress);
                        }
                    } else {
                        callback.onFailure(new RuntimeException());
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    callback.onFailure(exception);
                }
            });
            try {
                request.send();
            } catch (RequestException e) {
                callback.onFailure(e);
            }
        }
    }
}
