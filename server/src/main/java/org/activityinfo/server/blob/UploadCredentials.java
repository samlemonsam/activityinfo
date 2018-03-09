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
package org.activityinfo.server.blob;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Credentials that can be used upload a blob from the client
 */
public class UploadCredentials {

    private String url;
    private String method;
    private Map<String, String> formFields = Maps.newHashMap();

    public UploadCredentials(String url, String method, Map<String, String> formFields) {
        this.url = url;
        this.method = method;
        this.formFields = formFields;
    }

    /**
     * @return the URL to which the upload should be submitted
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @returnt the method (POST or PUT) that the fiel should be submitted.
     */
    public String getMethod() {
        return method;
    }

    /**
     *
     * @return a set of metadata that should be included in the form submission.
     */
    public Map<String, String> getFormFields() {
        return formFields;
    }

    @Override
    public String toString() {
        StringBuilder form = new StringBuilder();
        form.append("<form action=\"" + url + "\" method=\"" + method + "\">\n");
        for(String fieldName : formFields.keySet()) {
            form.append("<input type=\"hidden\" name=\"" + fieldName + "\"" +
                        " value=\"" + formFields.get(fieldName) + "\">\n");
        }
        form.append("</form>");
        return form.toString();
    }

    public String asJson() {

        JsonObject fieldsObject = new JsonObject();
        for (Map.Entry<String, String> entry : formFields.entrySet()) {
            fieldsObject.addProperty(entry.getKey(), entry.getValue());
        }

        JsonObject object = new JsonObject();
        object.addProperty("url", url);
        object.addProperty("method", method);
        object.add("formFields", fieldsObject);
        return object.toString();
    }

}
