/**
 * Sencha GXT 4.0.0 - Sencha for GWT
 * Copyright (c) 2006-2015, Sencha Inc.
 *
 * licensing@sencha.com
 * http://www.sencha.com/products/gxt/license/
 *
 * ================================================================================
 * Evaluation/Trial License
 * ================================================================================
 * This version of Sencha GXT is licensed commercially for a limited period for
 * evaluation purposes only. Production use or use beyond the applicable evaluation
 * period is prohibited under this license.
 *
 * Please see the Sencha GXT Licensing page at:
 * http://www.sencha.com/products/gxt/license/
 *
 * For clarification or additional options, please contact:
 * licensing@sencha.com
 * ================================================================================
 *
 *
 *
 *
 *
 *
 *
 * ================================================================================
 * Disclaimer
 * ================================================================================
 * THIS SOFTWARE IS DISTRIBUTED "AS-IS" WITHOUT ANY WARRANTIES, CONDITIONS AND
 * REPRESENTATIONS WHETHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION THE
 * IMPLIED WARRANTIES AND CONDITIONS OF MERCHANTABILITY, MERCHANTABLE QUALITY,
 * FITNESS FOR A PARTICULAR PURPOSE, DURABILITY, NON-INFRINGEMENT, PERFORMANCE AND
 * THOSE ARISING BY STATUTE OR FROM CUSTOM OR USAGE OF TRADE OR COURSE OF DEALING.
 * ================================================================================
 */
package com.sencha.gxt.edash.shared.mock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class RESTClient {

  protected <D> void fetchData(String filename, final AsyncCallback<D> async) {
    String path = GWT.getModuleBaseURL() + "data/" + filename;
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, path);

    try {
      builder.sendRequest("", new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          String text = response.getText();

          JSONValue value = JSONParser.parseLenient(text);

          D returnValue = null;
          if (value.isObject() != null) {
            returnValue = (D) value.isObject().getJavaScriptObject();
          } else if (value.isArray() != null) {
            // generic type needs to be a List for this to work
            List list = new ArrayList();
            for (int i = 0; i < value.isArray().size(); i++) {
              JSONValue v = value.isArray().get(i);
              list.add(v.isObject().getJavaScriptObject());
            }
            returnValue = (D) list;
          }
          async.onSuccess(returnValue);
        }

        @Override
        public void onError(Request request, Throwable exception) {
          async.onFailure(exception);
        }
      });
    } catch (Exception e) {
      async.onFailure(e);
    }

  }
}
