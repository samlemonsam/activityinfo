package org.activityinfo.server.endpoint.export;

public class ColumnSizeException extends RuntimeException {

    private String formLabel;
    private int colLength;
    private int colLimit;
    private String fileType;

    public ColumnSizeException() {
    }

    public ColumnSizeException(String formLabel, int colLength, int colLimit, String fileType) {
        this.formLabel = formLabel;
        this.colLength = colLength;
        this.colLimit = colLimit;
        this.fileType = fileType;
    }

    public String getFormLabel() {
        return formLabel;
    }

    public int getColLength() {
        return colLength;
    }

    public int getColLimit() {
        return colLimit;
    }

    public String getFileType() {
        return fileType;
    }
}
