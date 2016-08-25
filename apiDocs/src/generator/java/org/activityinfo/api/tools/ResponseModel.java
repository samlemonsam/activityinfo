package org.activityinfo.api.tools;

import io.swagger.models.Response;

public class ResponseModel {
    private int statusCode;
    private Response response;

    public ResponseModel(int statusCode, Response response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }
    
    public String getDescription() {
        return response.getDescription();
    }
}
