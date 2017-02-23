package org.activityinfo.model.blob;

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
