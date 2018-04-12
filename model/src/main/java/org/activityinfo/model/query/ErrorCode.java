package org.activityinfo.model.query;

public enum ErrorCode {

    MISSING(-1, "#VALUE!"),
    BAD_REF(-2, "#REF!");

    private int code;
    private String value;

    ErrorCode(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return "\0" + value;
    }

}
