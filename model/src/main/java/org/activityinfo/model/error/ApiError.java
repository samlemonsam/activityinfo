package org.activityinfo.model.error;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonSerializable;
import org.activityinfo.json.JsonValue;

/**
 * <p>
 * Defines an error which occurs during a call to the ActivityInfo API
 * from the normal ActivityInfo client, or from any other client or API call.
 * <ul>
 *     <li>The {@link ApiError#type} of error which has occurred is defined in the {@link ApiErrorType} enum class. </li>
 *     <li>For errors which can be handled programmatically, the {@link ApiError#code} is
 *     defined in the {@link ApiErrorCode} enum class.</li>
 *     <li>For errors which have an appropriate error message which can be displayed to the
 *     user, in their locale, the {@link ApiError#message} can be included as a string. </li>
 * </ul></p>
 */
public class ApiError implements JsonSerializable {

    /**
     * The {@link ApiErrorType} of the current error
     */
    private ApiErrorType type;

    /**
     * (Optional) {@link ApiErrorCode} for errors which can be handled programmatically
     */
    private ApiErrorCode code;

    /**
     * (Optional) Human-readable, localized error message which can be presented to user
     */
    private String message;

    private ApiError() {
    }

    public ApiError(ApiErrorType type) {
        this.type = type;
    }

    public ApiError(ApiErrorType type, ApiErrorCode code) {
        this.type = type;
        this.code = code;
    }

    public ApiError(ApiErrorType type, ApiErrorCode code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }

    public ApiErrorType getType() {
        return type;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public void setCode(ApiErrorCode code) {
        this.code = code;
    }

    public String getMessage() {
        assert hasMessage();
        return message;
    }

    public boolean hasMessage() {
        return message != null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static ApiError fromJson(JsonValue object) {
        ApiError apiError = new ApiError();
        apiError.type = ApiErrorType.valueOf(object.get("type").asString());
        if (object.hasKey("code")) {
            apiError.code = ApiErrorCode.valueOf(object.get("code").asString());
        }
        if (object.hasKey("message")) {
            apiError.message = object.get("message").asString();
        }
        return apiError;
    }

    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("type", type.name());
        if (code != null) {
            object.put("code", code.name());
        }
        if (message != null) {
            object.put("message", message);
        }
        return object;
    }

    public static ApiError serverError() {
        return serverError(null);
    }

    public static ApiError serverError(ApiErrorCode errorCode) {
        return new ApiError(ApiErrorType.SERVER_ERROR, errorCode, null);
    }

}