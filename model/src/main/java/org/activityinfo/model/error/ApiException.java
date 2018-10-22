package org.activityinfo.model.error;

public class ApiException extends RuntimeException {

    private String apiError;

    public ApiException() {
    }

    public ApiException(String apiError) {
        this.apiError = apiError;
    }

    public String getApiError() {
        return apiError;
    }

}
